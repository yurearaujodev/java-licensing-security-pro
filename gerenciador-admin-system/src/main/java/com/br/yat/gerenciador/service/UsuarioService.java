package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.LoggerFactory;

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
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.RegraSenha;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.TimeUtils;
import com.br.yat.gerenciador.validation.UsuarioValidationUtils;

public class UsuarioService {

	private static final String ESPECIAIS = "!@#$%^&*(),.?\":{}|<>";
	private static final EnumSet<RegraSenha> REGRAS_OBRIGATORIAS = EnumSet.of(RegraSenha.MAIUSCULA, RegraSenha.NUMERO,
			RegraSenha.ESPECIAL);

	private static final MenuChave CHAVE_SALVAR = MenuChave.CADASTROS_USUARIO;

	public Usuario autenticar(String email, char[] senhaPura) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			UsuarioPermissaoDao permDao = new UsuarioPermissaoDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);
			ParametroSistemaService parametroService = new ParametroSistemaService();
			int maxTentativas = parametroService.getInt(ParametroChave.LOGIN_MAX_TENTATIVAS, 5);

			int minutosBloqueio = parametroService.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5);
			Usuario user = dao.buscarPorEmail(email);

			if (user == null) {
				logDao.save(
						AuditLogHelper.gerarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario", "Inexistente: " + email));
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO OU SENHA INVÁLIDOS.");
			}

			if (StatusUsuario.BLOQUEADO == user.getStatus()) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"ESTA CONTA ESTÁ BLOQUEADA PERMANENTEMENTE. PROCURE O ADMINISTRADOR.");
			}
			if (permDao.estaBloqueado(user.getIdUsuario())) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"ACESSO SUSPENSO TEMPORARIAMENTE. AGUARDE O PRAZO DE " + minutosBloqueio + " MINUTOS.");
			}

			if (StatusUsuario.INATIVO == user.getStatus()) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ESTE USUÁRIO ESTÁ INATIVO.");
			}

			boolean senhaValida = PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString());

			if (!senhaValida) {
				if (!user.isMaster()) {
					int tentativasAtuais = dao.incrementarERetornarTentativas(email);

					if (tentativasAtuais >= maxTentativas) {

						if (permDao.jaTeveBloqueioTemporario(user.getIdUsuario())) {
							dao.bloquearUsuario(user.getIdUsuario());
							logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "BLOQUEIO_PERMANENTE", "usuario",
									user.getIdUsuario(), "ATIVO", "BLOQUEADO"));
							throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
									"LIMITE ATINGIDO. SUA CONTA FOI BLOQUEADA POR SEGURANÇA.");
						} else {
							permDao.bloquearTemporariamente(user.getIdUsuario(), minutosBloqueio);

							dao.resetTentativasFalhas(user.getIdUsuario());

							logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "BLOQUEIO_TEMPORARIO", "usuario",
									user.getIdUsuario(), null, "" + minutosBloqueio + " min"));
							throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
									"LIMITE ATINGIDO. CONTA SUSPENSA POR " + minutosBloqueio + " MINUTOS.");
						}
					}

					throw new ValidationException(ValidationErrorType.INVALID_FIELD,
							"SENHA INCORRETA. TENTATIVA " + tentativasAtuais + " DE " + maxTentativas + ".");
				}
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "SENHA INCORRETA.");
			}

			dao.atualizarUltimoLogin(user.getIdUsuario());
			dao.resetTentativasFalhas(user.getIdUsuario());

			logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "LOGIN_SUCESSO", "usuario", user.getIdUsuario(),
					null, "Sessão Iniciada"));

			return user;

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "Erro de conexão com o banco de dados.",
					e);
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

	public List<Usuario> listarUsuariosUltimoLogin(String termo) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			List<Usuario> lista = (termo == null || termo.trim().isEmpty()) ? dao.listAll()
					: dao.listarPorNomeOuEmail(termo);

			for (Usuario u : lista) {
				if (u.getUltimoLogin() != null) {
					u.setTempoDesdeUltimoAcesso(TimeUtils.formatarTempoDecorrido(u.getUltimoLogin()));
				}
			}
			return lista;
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR", e);
		}
	}

	public List<Usuario> listarExcluidos(Usuario executor) {

		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES);
			return new UsuarioDao(conn).listarExcluidos();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR EXCLUÍDOS", e);
		}
	}

	public void salvarUsuario(Usuario usuario, List<MenuChave> chavesSelecionadas, Usuario executor) {
		validarDados(usuario, chavesSelecionadas);
		validarRestricoesMaster(usuario);

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);
			validarAcesso(conn, executor, CHAVE_SALVAR);
			try {
				UsuarioDao usuarioDao = new UsuarioDao(conn);
				UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
				PermissaoDao permissaoDao = new PermissaoDao(conn);

				validarRegrasPersistencia(usuarioDao, usuario);

				Usuario estadoAnterior = null;
				boolean isNovo = (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0);

				Usuario alvoExistente = null;
				if (!isNovo) {
					alvoExistente = usuarioDao.searchById(usuario.getIdUsuario());

					if (alvoExistente == null) {
						throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
								"USUÁRIO NÃO ENCONTRADO PARA ATUALIZAÇÃO.");
					}

					estadoAnterior = Usuario.snapshotParaValidacaoSenha(alvoExistente);
				}

				if (alvoExistente != null && !temMaisPoder(conn, executor, alvoExistente)) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"Você não tem permissão para alterar este usuário, pois ele possui privilégios superiores.");
				}
				ParametroSistemaService parametroService = new ParametroSistemaService();
				boolean senhaAlterada = processarSenha(parametroService, usuario, isNovo, executor, estadoAnterior);

				salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, isNovo, conn);

				if (senhaAlterada && !isNovo) {
					usuarioDao.atualizarUltimaAlteracaoSenha(usuario.getIdUsuario());
				}
				sincronizarPermissoes(conn, upDao, permissaoDao, usuario, chavesSelecionadas, executor);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("SALVAR_USUARIO", e);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void salvarOuAtualizar(UsuarioDao dao, Usuario usuario, Usuario anterior, boolean isNovo, Connection conn) {
		LogSistemaDao logDao = new LogSistemaDao(conn);

		if (isNovo) {
			int id = dao.save(usuario);
			usuario.setIdUsuario(id);
			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "INSERIR_USUARIO", "usuario", id, null, usuario));
		} else {
			if (anterior.getStatus() != StatusUsuario.ATIVO && usuario.getStatus() == StatusUsuario.ATIVO) {

				dao.resetTentativasFalhas(usuario.getIdUsuario());

				// new
				// UsuarioPermissaoDao(conn).removerBloqueioTemporario(usuario.getIdUsuario());

				logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "REATIVACAO_MANUAL", "usuario",
						usuario.getIdUsuario(), anterior.getStatus().name(), "ATIVO"));
			}

			dao.update(usuario);

			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "ALTERAR_USUARIO", "usuario", usuario.getIdUsuario(),
					anterior, usuario));
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

			if (!UsuarioPolicy.podeExcluir(executor, anterior)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"Você não tem permissão para excluir este usuário.");
			}
			if (!temMaisPoder(conn, executor, anterior)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"Você não tem permissão para excluir este usuário, pois ele possui privilégios superiores.");
			}

			dao.softDeleteById(idUsuario);
			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "EXCLUIR_USUARIO", "usuario", idUsuario, anterior,
					null));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO ACESSAR BANCO", e);
		}
	}

	public void restaurarUsuario(int idUsuario, Usuario executor) {

		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES);
			UsuarioDao dao = new UsuarioDao(conn);
			dao.restaurar(idUsuario);

			new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso("CADASTRO", "RESTAURAR_USUARIO", "usuario",
					idUsuario, "Status: EXCLUIDO", "Status: ATIVO"));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESTAURAR", e);
		}
	}

	private boolean processarSenha(ParametroSistemaService parametroService, Usuario usuario, boolean isNovo,
			Usuario executor, Usuario estadoAnterior) {
		char[] senhaNova = usuario.getSenhaHash();
		char[] senhaAntiga = usuario.getSenhaAntiga();
		char[] senhaConfirmar = usuario.getConfirmarSenha();

		try {
			if (isNovo && (senhaNova == null || senhaNova.length == 0)) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA É OBRIGATÓRIA.");
			}

			if (senhaNova == null || senhaNova.length == 0) {
				return false;
			}

			validarComplexidade(senhaNova, parametroService);

			if (senhaConfirmar == null || !Arrays.equals(senhaNova, senhaConfirmar)) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			}

			if (!isNovo && executor == null) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Usuário executor não identificado.");
			}
			boolean alterandoPropriaSenha = !isNovo && executor != null
					&& executor.getIdUsuario().equals(usuario.getIdUsuario());

			if (!isNovo && !alterandoPropriaSenha && !UsuarioPolicy.isPrivilegiado(executor)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"Você não tem permissão para alterar a senha deste usuário.");
			}

			if (alterandoPropriaSenha) {

				if (estadoAnterior == null || estadoAnterior.getSenhaHashString() == null) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"Não foi possível validar a senha anterior.");
				}

				if (senhaAntiga == null
						|| !PasswordUtils.verifyPassword(senhaAntiga, estadoAnterior.getSenhaHashString())) {

					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"A SENHA ANTIGA INFORMADA ESTÁ INCORRETA.");
				}
			}

			usuario.setSenhaHashString(PasswordUtils.hashPassword(senhaNova));
			return true;
		} finally {
			SensitiveData.safeClear(senhaNova);
			SensitiveData.safeClear(senhaAntiga);
			SensitiveData.safeClear(senhaConfirmar);
		}
	}

	private void validarComplexidade(char[] senha, ParametroSistemaService parametroService) {
		int minTamanho = parametroService.getInt(ParametroChave.SENHA_MIN_TAMANHO, 6);
		if (senha.length < minTamanho) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"A SENHA DEVE TER NO MÍNIMO " + minTamanho + " CARACTERES.");
		}

		EnumSet<RegraSenha> regras = EnumSet.noneOf(RegraSenha.class);

		for (char c : senha) {
			if (Character.isUpperCase(c))
				regras.add(RegraSenha.MAIUSCULA);
			if (Character.isDigit(c))
				regras.add(RegraSenha.NUMERO);
			if (ESPECIAIS.indexOf(c) >= 0)
				regras.add(RegraSenha.ESPECIAL);

			if (regras.containsAll(REGRAS_OBRIGATORIAS)) {
				return;
			}
		}

		if (!regras.contains(RegraSenha.MAIUSCULA))
			erro("UMA LETRA MAIÚSCULA");
		if (!regras.contains(RegraSenha.NUMERO))
			erro("UM NÚMERO");
		if (!regras.contains(RegraSenha.ESPECIAL))
			erro("UM CARACTERE ESPECIAL");
	}

	private void erro(String msg) {
		throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A SENHA DEVE CONTER PELO MENOS " + msg + ".");
	}

	private void sincronizarPermissoes(Connection conn, UsuarioPermissaoDao upDao, PermissaoDao pDao, Usuario usuario,
			List<MenuChave> chaves, Usuario executor) {

		List<MenuChave> chavesAnteriores = upDao.buscarChavesAtivasPorUsuario(usuario.getIdUsuario());

		if (UsuarioPolicy.isPrivilegiado(usuario)) {
			chaves = UsuarioPolicy.permissoesCompletas();
		}

		validarHierarquiaPermissao(conn, chaves, executor);

		List<UsuarioPermissao> novasEntidades = new ArrayList<>();
		for (MenuChave chave : chaves) {
			int idPermissao = garantirInfraestruturaMenu(conn, chave);

			UsuarioPermissao up = new UsuarioPermissao();
			up.setIdUsuario(usuario.getIdUsuario());
			up.setIdPermissoes(idPermissao);
			up.setAtiva(true);
			up.setUsuarioConcedeu(executor != null ? executor : usuario);

			novasEntidades.add(up);
		}

		upDao.syncByUsuario(usuario.getIdUsuario(), novasEntidades);

		registrarLogPermissoes(conn, usuario, chavesAnteriores, chaves);
	}

	private void registrarLogErro(String operacao, Exception e) {
		try (Connection connLog = ConnectionFactory.getConnection()) {
			new LogSistemaDao(connLog).save(AuditLogHelper.gerarLogErro("ERRO", operacao, "usuario", e.getMessage()));
		} catch (Exception ex) {
			LoggerFactory.getLogger(UsuarioService.class).error("Falha ao registrar log de erro", ex);
		}
	}

	private void validarAcesso(Connection conn, Usuario executor, MenuChave chaveNecessaria) {
		if (executor == null) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Usuário não autenticado.");
		}
		if (UsuarioPolicy.ignoraValidacaoPermissao(executor)) {
			return;
		}

		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
		List<MenuChave> permissoesAtivas = upDao.buscarChavesAtivasPorUsuario(executor.getIdUsuario());

		if (!permissoesAtivas.contains(chaveNecessaria)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"ACESSO NEGADO: O SEU UTILIZADOR NÃO TEM PERMISSÃO PARA ESTA OPERAÇÃO.");
		}

	}

	private void validarRegrasPersistencia(UsuarioDao dao, Usuario usuario) {
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

	private void validarRestricoesMaster(Usuario usuario) {
		if (usuario.isMaster() && usuario.getStatus() != StatusUsuario.ATIVO) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"O STATUS DO MASTER NÃO PODE SER ALTERADO.");
		}
		if (usuario.getEmpresa() == null || usuario.getEmpresa().getIdEmpresa() == null) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A EMPRESA É OBRIGATÓRIA.");
		}
	}

	private boolean temMaisPoder(Connection conn, Usuario executor, Usuario alvo) {
		if (UsuarioPolicy.isPrivilegiado(executor))
			return true;
		if (UsuarioPolicy.isPrivilegiado(alvo))
			return false;

		List<MenuChave> permissoesAlvo = carregarPermissoesAtivas(conn, alvo.getIdUsuario());
		return possuiTodasAsPermissoes(conn, executor, permissoesAlvo);
	}

	private List<MenuChave> carregarPermissoesAtivas(Connection conn, int idUsuario) {
		return new UsuarioPermissaoDao(conn).buscarChavesAtivasPorUsuario(idUsuario);
	}

	private boolean possuiTodasAsPermissoes(Connection conn, Usuario executor, List<MenuChave> chavesNecessarias) {
		if (executor == null || UsuarioPolicy.isPrivilegiado(executor))
			return true;

		List<MenuChave> permissoesExecutor = carregarPermissoesAtivas(conn, executor.getIdUsuario());
		for (MenuChave chave : chavesNecessarias) {
			if (!permissoesExecutor.contains(chave)) {
				return false;
			}
		}
		return true;
	}

	private void validarHierarquiaPermissao(Connection conn, List<MenuChave> chavesAlvo, Usuario executor) {
		if (!possuiTodasAsPermissoes(conn, executor, chavesAlvo)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"SEGURANÇA: VOCÊ NÃO PODE CONCEDER PERMISSÕES QUE NÃO POSSUI.");
		}
	}

	public List<MenuChave> carregarPermissoesAtivas(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioPermissaoDao(conn).buscarChavesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public boolean existeUsuarioMaster() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			return dao.buscarMasterUnico() != null;
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO VERIFICAR MASTER", e);
		}
	}

	private int garantirInfraestruturaMenu(Connection conn, MenuChave chave) {
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

	public boolean podeEditarPermissoes(Usuario u) {
		return UsuarioPolicy.podeEditarPermissoes(u);
	}

	public List<Usuario> listarUsuariosPorPermissao(MenuChave chave) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDao(conn).listarPorPermissao(chave.name());
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO FILTRAR PERMISSÃO", e);
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

	private void registrarLogPermissoes(Connection conn, Usuario usuario, List<MenuChave> anteriores,
			List<MenuChave> novas) {

		LogSistemaDao logDao = new LogSistemaDao(conn);
		String antes = anteriores != null ? anteriores.toString() : "NENHUMA";
		String depois = usuario.isMaster() ? "PACOTE_MASTER_COMPLETO" : (novas != null ? novas.toString() : "NENHUMA");

		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "SINCRONIZAR_PERMISSOES", "usuario_permissao",
				usuario.getIdUsuario(), antes, depois));
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