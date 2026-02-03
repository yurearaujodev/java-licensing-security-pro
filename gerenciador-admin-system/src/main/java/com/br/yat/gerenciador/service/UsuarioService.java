package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoMenuDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
//import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.validation.UsuarioValidationUtils;

public class UsuarioService {

	private static final MenuChave CHAVE_SALVAR = MenuChave.CADASTROS_USUARIO;
	//private final UsuarioPolicy usuarioPolicy = new UsuarioPolicy();

	public void salvarUsuario(Usuario usuario, List<MenuChave> chavesSelecionadas, Usuario executor) {
		validarAcesso(executor, CHAVE_SALVAR);
		validarDados(usuario, chavesSelecionadas);
		validarRestricoesMaster(usuario);

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);
			try {
				UsuarioDao usuarioDao = new UsuarioDao(conn);
				UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
				PermissaoDao permissaoDao = new PermissaoDao(conn);

				validarRegrasPersistencia(usuarioDao, usuario);

				Usuario estadoAnterior = null;
				boolean isNovo = (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0);
				if (!isNovo) {
					estadoAnterior = usuarioDao.searchById(usuario.getIdUsuario());
				}

				processarSenha(usuario, isNovo);
				salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, isNovo, conn);
				sincronizarPermissoes(upDao, permissaoDao, usuario, chavesSelecionadas, executor);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("SALVAR_USUARIO", e);
				throw e;
			} finally {
				SensitiveData.safeClear(usuario.getSenhaHash());
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void validarRestricoesMaster(Usuario usuario) {
		if (usuario.isMaster() && usuario.getStatus() != StatusUsuario.ATIVO) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"O STATUS DO MASTER NÃO PODE SER ALTERADO.");
		}
		if (usuario.getEmpresa() == null || usuario.getEmpresa().getIdEmpresa() == null) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A EMPRESA É OBRIGATÓRIA.");
		}
	}

	private void validarRegrasPersistencia(UsuarioDao dao, Usuario usuario) throws SQLException {
		if (usuario.isMaster()) {
			Usuario masterExistente = dao.buscarMasterUnico();
			if (masterExistente != null && !masterExistente.getIdUsuario().equals(usuario.getIdUsuario())) {
				throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
						"JÁ EXISTE UM USUÁRIO MASTER CADASTRADO.");
			}
		}
		validarDuplicidadeEmail(dao, usuario);
		if (usuario.getIdUsuario() != null && usuario.getIdUsuario() > 0) {
			Usuario base = dao.searchById(usuario.getIdUsuario());
			if (base != null && base.isMaster())
				usuario.setMaster(true);
		}
	}

	private void processarSenha(Usuario usuario, boolean isNovo) {
		char[] senhaPura = usuario.getSenhaHash();
		if (senhaPura != null && senhaPura.length > 0) {
			if (senhaPura.length < 4) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"A SENHA DEVE TER NO MÍNIMO 4 CARACTERES.");
			}
			usuario.setSenhaHashString(PasswordUtils.hashPassword(senhaPura));
		} else if (isNovo) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA É OBRIGATÓRIA.");
		}
	}

	private void salvarOuAtualizar(UsuarioDao dao, Usuario usuario, Usuario anterior, boolean isNovo, Connection conn)
			throws SQLException {
		LogSistemaDao logDao = new LogSistemaDao(conn);
		if (isNovo) {
			int id = dao.save(usuario);
			usuario.setIdUsuario(id);
			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "INSERIR_USUARIO", "usuario", id, null, usuario));
		} else {
			dao.update(usuario);
			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "ALTERAR_USUARIO", "usuario", usuario.getIdUsuario(),
					anterior, usuario));
			if (usuario.getSenhaHash() != null && usuario.getSenhaHash().length > 0) {
				dao.atualizarUltimoLogin(usuario.getIdUsuario());
			}
		}
	}

	private void registrarLogErro(String operacao, Exception e) {
		try (Connection connLog = ConnectionFactory.getConnection()) {
			new LogSistemaDao(connLog).save(AuditLogHelper.gerarLogErro("ERRO", operacao, "usuario", e.getMessage()));
		} catch (Exception ex) {
		}
	}

	public void excluirUsuario(int idUsuario, Usuario executor) {
		if (idUsuario <= 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ID DE USUÁRIO INVÁLIDO.");
		}

		if (executor != null && executor.getIdUsuario().equals(idUsuario)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"SEGURANÇA: VOCÊ NÃO PODE EXCLUIR SUA PRÓPRIA CONTA.");
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);
			Usuario anterior = dao.searchById(idUsuario);
			if (anterior == null) {
				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
						"O USUÁRIO QUE VOCÊ ESTÁ TENTANDO EXCLUIR NÃO EXISTE OU JÁ FOI REMOVIDO.");
			}
			if (anterior.isMaster()) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"O USUÁRIO MASTER NÃO PODE SER EXCLUÍDO.");
			}
			dao.softDeleteById(idUsuario);
			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "EXCLUIR_USUARIO", "usuario", idUsuario, anterior,
					null));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO ACESSAR BANCO", e);
		}
	}

	private void validarAcesso(Usuario executor, MenuChave chaveNecessaria) {
		if (executor == null)
			return;
		if (executor.isMaster())
			return;

		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
			List<MenuChave> permissoesAtivas = upDao.buscarChavesAtivasPorUsuario(executor.getIdUsuario());

			if (!permissoesAtivas.contains(chaveNecessaria)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"ACESSO NEGADO: O SEU UTILIZADOR NÃO TEM PERMISSÃO PARA ESTA OPERAÇÃO.");
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO VALIDAR PERMISSÕES", e);
		}
	}

	public List<Usuario> listarExcluidos(Usuario executor) {
		validarAcesso(executor, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES);

		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDao(conn).listarExcluidos();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR EXCLUÍDOS", e);
		}
	}

	public void restaurarUsuario(int idUsuario, Usuario executor) {
		validarAcesso(executor, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES);

		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			dao.restaurar(idUsuario);

			new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso("CADASTRO", "RESTAURAR_USUARIO", "usuario",
					idUsuario, "Status: EXCLUIDO", "Status: ATIVO"));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESTAURAR", e);
		}
	}

	public Usuario autenticar(String email, char[] senhaPura) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);
			Usuario user = dao.buscarPorEmail(email);

			if (user == null) {
				logDao.save(AuditLogHelper.gerarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario",
						"Usuário não existe: " + email));
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO NÃO ENCONTRADO.");
			}
			if (StatusUsuario.BLOQUEADO == user.getStatus()) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"CONTA BLOQUEADA POR EXCESSO DE TENTATIVAS. CONTATE O ADMIN.");

			}

			if (StatusUsuario.INATIVO == user.getStatus()) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ESTE USUÁRIO ESTÁ INATIVO.");
			}

			boolean senhaValida = PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString());
			if (!senhaValida) {
				logDao.save(AuditLogHelper.gerarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario",
						"Senha incorreta para: " + email));
				if (!user.isMaster()) {
					dao.incrementarTentativasFalhas(email);
					if (user.getTentativasFalhas() + 1 >= 5) {
						dao.bloquearUsuario(user.getIdUsuario());
						logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "BLOQUEIO_AUTOMATICO", "usuario",
								user.getIdUsuario(), "Status: ATIVO", "Status: BLOQUEADO"));
						throw new ValidationException(ValidationErrorType.INVALID_FIELD,
								"LIMITE ATINGIDO. SUA CONTA FOI BLOQUEADA POR SEGURANÇA.");
					}
				}
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"SENHA INCORRETA. TENTATIVA " + (user.getTentativasFalhas() + 1) + " DE 5.");
			}
			dao.atualizarUltimoLogin(user.getIdUsuario());
			logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "LOGIN_SUCESSO", "usuario", user.getIdUsuario(),
					null, "Sessão Iniciada"));
			return user;
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		} finally {
			SensitiveData.safeClear(senhaPura);
		}
	}

	public List<Usuario> listarUsuarios(String termo) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			return (termo == null || termo.trim().isEmpty()) ? dao.listAll() : dao.listarPorNomeOuEmail(termo);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR USUÁRIOS", e);
		}
	}

	public List<Usuario> listarUsuariosPorPermissao(MenuChave chave) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDao(conn).listarPorPermissao(chave.name());
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO FILTRAR PERMISSÃO", e);
		}
	}

	public List<MenuChave> carregarPermissoesAtivas(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioPermissaoDao(conn).buscarChavesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void validarDados(Usuario usuario, List<MenuChave> chaves) {
		UsuarioValidationUtils.validarUsuario(usuario);
		if (chaves == null || chaves.isEmpty()) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O USUÁRIO DEVE TER AO MENOS UMA PERMISSÃO.");
		}
	}

	private void validarDuplicidadeEmail(UsuarioDao dao, Usuario u) {
		Usuario existente = dao.buscarPorEmail(u.getEmail());
		if (existente != null && !existente.getIdUsuario().equals(u.getIdUsuario())) {
			throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
					"ESTE E-MAIL JÁ ESTÁ CADASTRADO NO SISTEMA.");
		}
	}

	private void sincronizarPermissoes(UsuarioPermissaoDao upDao, PermissaoDao pDao, Usuario usuario,
			List<MenuChave> chaves, Usuario executor) throws SQLException {

		List<MenuChave> chavesAnteriores = upDao.buscarChavesAtivasPorUsuario(usuario.getIdUsuario());

		if (usuario.isMaster())
			chaves = List.of(MenuChave.values());
		validarDados(usuario, chaves);
		validarHierarquiaPermissao(chaves, executor);

		upDao.disableAllFromUser(usuario.getIdUsuario());

		for (MenuChave chave : chaves) {
			int idPermissao = garantirInfraestruturaMenu(upDao.getConnection(), chave);

			UsuarioPermissao up = new UsuarioPermissao();
			up.setIdUsuario(usuario.getIdUsuario());
			up.setIdPermissoes(idPermissao);
			up.setAtiva(true);
			up.setUsuarioConcedeu(executor != null ? executor : usuario);

			upDao.saveOrUpdate(up);
		}

		registrarLogPermissoes(upDao.getConnection(), usuario, chavesAnteriores, chaves);
	}

	private void registrarLogPermissoes(Connection conn, Usuario usuario, List<MenuChave> anteriores,
			List<MenuChave> novas) throws SQLException {

		LogSistemaDao logDao = new LogSistemaDao(conn);
		String antes = anteriores != null ? anteriores.toString() : "NENHUMA";
		String depois = usuario.isMaster() ? "PACOTE_MASTER_COMPLETO" : (novas != null ? novas.toString() : "NENHUMA");

		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "SINCRONIZAR_PERMISSOES", "usuario_permissao",
				usuario.getIdUsuario(), antes, depois));
	}

	private int garantirInfraestruturaMenu(Connection conn, MenuChave chave) throws SQLException {
		PermissaoDao pDao = new PermissaoDao(conn);
		var permissaoBanco = pDao.findByChave(chave.name());

		if (permissaoBanco != null)
			return permissaoBanco.getIdPermissoes();

		permissaoBanco = new Permissao();
		permissaoBanco.setChave(chave.name());
		permissaoBanco.setTipo("MENU");
		String categoria = extrairCategoria(chave.name());
		permissaoBanco.setCategoria(categoria);

		int idPerm = pDao.save(permissaoBanco);

		MenuSistemaDao menuDao = new MenuSistemaDao(conn);
		int idMenu = menuDao.save(chave.name(), categoria);

		new PermissaoMenuDao(conn).vincular(idPerm, idMenu);

		return idPerm;
	}

	private void validarHierarquiaPermissao(List<MenuChave> chavesAlvo, Usuario executor) {
		if (executor == null || executor.isMaster())
			return;

		List<MenuChave> permissoesDoExecutor = carregarPermissoesAtivas(executor.getIdUsuario());
		for (MenuChave chave : chavesAlvo) {
			if (!permissoesDoExecutor.contains(chave)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"SEGURANÇA: VOCÊ NÃO PODE CONCEDER A PERMISSÃO [" + chave + "] POIS NÃO A POSSUI.");
			}
		}
	}

	private String extrairCategoria(String nomeEnum) {
		return nomeEnum.contains("_") ? nomeEnum.split("_")[0] : "GERAL";
	}

	public Empresa buscarEmpresaFornecedora() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new EmpresaDao(conn).buscarPorFornecedora();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO BUSCAR EMPRESA: " + e.getMessage(), e);
		}
	}
}