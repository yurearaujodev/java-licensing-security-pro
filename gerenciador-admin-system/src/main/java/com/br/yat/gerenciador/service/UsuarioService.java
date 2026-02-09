package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
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

public class UsuarioService extends BaseService {

	private static final String ESPECIAIS = "!@#$%^&*(),.?\":{}|<>";
	private static final EnumSet<RegraSenha> REGRAS_OBRIGATORIAS = EnumSet.of(RegraSenha.MAIUSCULA, RegraSenha.NUMERO,
			RegraSenha.ESPECIAL);

	private static final MenuChave CHAVE_SALVAR = MenuChave.CADASTROS_USUARIO;

	public Usuario autenticar(String email, char[] senhaPura) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);
			ParametroSistemaService parametroService = new ParametroSistemaService();

			// 1. Configurações de segurança
			int maxTentativas = parametroService.getInt(ParametroChave.LOGIN_MAX_TENTATIVAS, 5);
			int minutosBloqueio = parametroService.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5);

			Usuario user = dao.buscarPorEmail(email);

			// 2. Validações Básicas
			if (user == null) {
				logDao.save(
						AuditLogHelper.gerarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario", "Inexistente: " + email));
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO OU SENHA INVÁLIDOS.");
			}

			if (StatusUsuario.BLOQUEADO == user.getStatus()) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"ESTA CONTA ESTÁ BLOQUEADA PERMANENTEMENTE.");
			}

			if (StatusUsuario.INATIVO == user.getStatus()) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ESTE USUÁRIO ESTÁ INATIVO.");
			}

			// 3. Verificação de Bloqueio Temporário (AQUI MUDOU)
			if (user.getBloqueadoAte() != null) {
				if (user.getBloqueadoAte().isAfter(java.time.LocalDateTime.now())) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"ACESSO SUSPENSO TEMPORARIAMENTE ATÉ "
									+ TimeUtils.formatarDataHora(user.getBloqueadoAte()));
				} else {
					// Se o tempo passou, limpamos o bloqueio na DAO
					dao.resetTentativasFalhas(user.getIdUsuario());
				}
			}

			// 4. Validação da Senha
			boolean senhaValida = PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString());

			if (!senhaValida) {
				if (!user.isMaster()) {
					int tentativasAtuais = dao.incrementarERetornarTentativas(email);

					if (tentativasAtuais >= maxTentativas) {
						LocalDateTime ate = LocalDateTime.now().plusMinutes(minutosBloqueio);
						dao.bloquearTemporariamente(user.getIdUsuario(), ate);

						// IMPORTANTE: Atualizar o objeto em memória para refletir o bloqueio imediato
						user.setBloqueadoAte(ate);

						logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "BLOQUEIO_TEMPORARIO", "usuario",
								user.getIdUsuario(), "Tentativas: " + tentativasAtuais, "Até: " + ate));

						throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
								"LIMITE ATINGIDO. CONTA SUSPENSA ATÉ " + TimeUtils.formatarDataHora(ate));
					}

					throw new ValidationException(ValidationErrorType.INVALID_FIELD,
							"SENHA INCORRETA. TENTATIVA " + tentativasAtuais + " DE " + maxTentativas + ".");
				}
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "SENHA INCORRETA.");
			}

			// 5. Sucesso
			dao.atualizarUltimoLogin(user.getIdUsuario());
			// Resetamos tentativas no sucesso para garantir
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

	public List<Usuario> listarUsuarios(String termo, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, MenuChave.CADASTROS_USUARIO, "READ");

			UsuarioDao dao = new UsuarioDao(conn);
			return (termo == null || termo.trim().isEmpty()) ? dao.listAll() : dao.listarPorNomeOuEmail(termo);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR USUÁRIOS", e);
		}
	}

	public List<Usuario> listarUsuariosUltimoLogin(String termo, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, MenuChave.CADASTROS_USUARIO, "READ");

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
			validarAcesso(conn, executor, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, "READ");

			return new UsuarioDao(conn).listarExcluidos();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR EXCLUÍDOS", e);
		}
	}

	public void salvarUsuario(Usuario usuario, Map<MenuChave, List<String>> permissoesGranulares,
			Map<MenuChave, String> datasExpiracao, Usuario executor) {
		validarDados(usuario, permissoesGranulares);
		validarRestricoesMaster(usuario);

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);
			validarAcesso(conn, executor, CHAVE_SALVAR, "WRITE");

			try {
				UsuarioDao usuarioDao = new UsuarioDao(conn);
				UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
				LogSistemaDao logDao = new LogSistemaDao(conn);

				validarRegrasPersistencia(usuarioDao, usuario);

				Usuario estadoAnterior = null;
				boolean isNovo = (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0);

				Usuario alvoExistente = null;
				if (!isNovo) {
					alvoExistente = usuarioDao.searchById(usuario.getIdUsuario());
					if (alvoExistente == null) {
						throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
								"USUÁRIO NÃO ENCONTRADO.");
					}
					estadoAnterior = Usuario.snapshotParaValidacaoSenha(alvoExistente);
				}

				// Validação de poder entre executor e alvo
				if (alvoExistente != null && !temMaisPoder(conn, executor, alvoExistente)) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"Privilégios insuficientes para alterar este usuário.");
				}

				ParametroSistemaService parametroService = new ParametroSistemaService();
				boolean senhaAlterada = processarSenha(parametroService, usuario, isNovo, executor, estadoAnterior);

				// 1. SALVAMOS O USUÁRIO PRIMEIRO (Para garantir o ID se for novo)
				salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, isNovo, conn);

				if (senhaAlterada && !isNovo) {
					logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "SENHA_ALTERADA", "usuario",
							usuario.getIdUsuario(), "O executor alterou a senha deste usuário.", null));
				}

				// 2. CARREGAMOS AS PERMISSÕES (Agora o usuario.getIdUsuario() nunca é nulo)
				List<UsuarioPermissao> permissoesPerfil = carregarPermissoesDoPerfil(conn, usuario, executor);
				List<UsuarioPermissao> permissoesDiretas = carregarPermissoesGranulares(conn, usuario,
						permissoesGranulares, datasExpiracao, executor);

				// 3. MERGE DAS PERMISSÕES (Evita duplicados)
				Map<Integer, UsuarioPermissao> mapa = new LinkedHashMap<>();
				permissoesPerfil.forEach(up -> mapa.put(up.getIdPermissoes(), up));
				permissoesDiretas.forEach(up -> mapa.put(up.getIdPermissoes(), up));

				List<UsuarioPermissao> todasAsPermissoesParaSincronizar = new ArrayList<>(mapa.values());

				// Garante o vínculo do ID do usuário em cada objeto de permissão
				todasAsPermissoesParaSincronizar.forEach(up -> up.setIdUsuario(usuario.getIdUsuario()));

				// 4. DOUBLE VALIDATION: Hierarquia de Acesso e de Tempo
				validarHierarquiaUsuarioPermissao(conn, executor, todasAsPermissoesParaSincronizar);

				// 5. SINCRONIZAÇÃO FINAL
				upDao.syncByUsuario(usuario.getIdUsuario(), todasAsPermissoesParaSincronizar);

				registrarLogPermissoesFinal(conn, usuario, todasAsPermissoesParaSincronizar);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "SALVAR_USUARIO", "usuario", e);
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
			// Se estiver reativando o usuário (Inativo -> Ativo) ou tirando de Bloqueado
			if (anterior.getStatus() != StatusUsuario.ATIVO && usuario.getStatus() == StatusUsuario.ATIVO) {
				dao.resetTentativasFalhas(usuario.getIdUsuario());
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
			validarAcesso(conn, executor, MenuChave.CADASTROS_USUARIO, "DELETE");

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
			registrarLogErro("ERRO", "EXCLUIR_USUARIO", "usuario", e);
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO ACESSAR BANCO", e);
		}
	}

	public void restaurarUsuario(int idUsuario, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, "DELETE");

			UsuarioDao dao = new UsuarioDao(conn);
			dao.restaurar(idUsuario);

			new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso("CADASTRO", "RESTAURAR_USUARIO", "usuario",
					idUsuario, "Status: EXCLUIDO", "Status: ATIVO"));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESTAURAR", e);
		}
	}

	private List<UsuarioPermissao> carregarPermissoesDoPerfil(Connection conn, Usuario usuario, Usuario executor) {
		if (usuario.getPerfil() == null || usuario.getPerfil().getIdPerfil() == null) {
			return List.of();
		}

		PerfilPermissoesDao ppDao = new PerfilPermissoesDao(conn);

		return ppDao.listarPermissoesPorPerfil(usuario.getPerfil().getIdPerfil()).stream().map(p -> {
			UsuarioPermissao up = new UsuarioPermissao();
			up.setIdUsuario(usuario.getIdUsuario());
			up.setIdPermissoes(p.getIdPermissoes());
			up.setAtiva(true);
			up.setHerdada(true);
			up.setUsuarioConcedeu(executor);
			return up;
		}).toList();
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

	private List<UsuarioPermissao> carregarPermissoesGranulares(Connection conn, Usuario usuario,
			Map<MenuChave, List<String>> permissoesGranulares, Map<MenuChave, String> datasTexto, Usuario executor) {
		List<UsuarioPermissao> novasEntidades = new ArrayList<>();
		PermissaoDao pDao = new PermissaoDao(conn);

		if (usuario.isMaster()) {
			for (MenuChave chave : MenuChave.values()) {
				List<Integer> ids = garantirInfraestruturaMenu(conn, chave);
				for (Integer id : ids) {
					UsuarioPermissao up = criarEntidadePermissao(usuario.getIdUsuario(), id, executor);
					up.setHerdada(false);
					novasEntidades.add(up);
				}
			}
			return novasEntidades;
		}

		permissoesGranulares.forEach((chave, tipos) -> {
			LocalDateTime dataExp = null;
			if (datasTexto != null && datasTexto.containsKey(chave)) {
				dataExp = TimeUtils.parseDataHora(datasTexto.get(chave));
			}
			for (String tipo : tipos) {
				Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
				if (p != null) {
					UsuarioPermissao up = criarEntidadePermissao(usuario.getIdUsuario(), p.getIdPermissoes(), executor);
					up.setHerdada(false);
					up.setExpiraEm(dataExp);
					novasEntidades.add(up);
				}
			}
		});

		return novasEntidades;
	}

	private UsuarioPermissao criarEntidadePermissao(Integer idUsuario, Integer idPermissao, Usuario executor) {
		UsuarioPermissao up = new UsuarioPermissao();
		up.setIdUsuario(idUsuario);
		up.setIdPermissoes(idPermissao);
		up.setAtiva(true);
		return up;
	}

	public Usuario buscarMasterUnico() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioDao(conn).buscarMasterUnico();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR MASTER", e);
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

	public List<String> listarPermissoesAtivasPorMenu(int idUsuario, MenuChave menu) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PermissaoDao pDao = new PermissaoDao(conn);
			return pDao.buscarTiposAtivosPorUsuarioEMenu(idUsuario, menu.name());
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR PERMISSÕES", e);
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

		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);

		for (MenuChave chave : chavesNecessarias) {
			if (!upDao.usuarioPossuiPermissaoEspecifica(executor.getIdUsuario(), chave.name(), "WRITE")) {
				return false;
			}
		}
		return true;
	}

	private void validarHierarquiaUsuarioPermissao(Connection conn, Usuario executor,
			List<UsuarioPermissao> permissoesSendoAtribuidas) {

		if (executor == null || UsuarioPolicy.isPrivilegiado(executor)) {
			return;
		}

		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
		PermissaoDao pDao = new PermissaoDao(conn);

		// 1. Buscamos as permissões atuais do EXECUTOR (incluindo as datas de expiração
		// dele)
		List<UsuarioPermissao> permissoesDoExecutor = upDao.listarPorUsuario(executor.getIdUsuario());

		// Criamos um mapa: ID_PERMISSAO -> DATA_EXPIRACAO para busca rápida
		Map<Integer, java.time.LocalDateTime> mapaExecutor = permissoesDoExecutor.stream()
				.collect(Collectors.toMap(UsuarioPermissao::getIdPermissoes,
						up -> up.getExpiraEm() == null ? java.time.LocalDateTime.MAX : up.getExpiraEm(),
						(existente, substituto) -> existente // Caso haja duplicata, mantém a primeira
				));

		for (UsuarioPermissao upAlvo : permissoesSendoAtribuidas) {
			// 2. Tranca 1: O executor possui essa permissão?
			if (!mapaExecutor.containsKey(upAlvo.getIdPermissoes())) {
				Permissao p = pDao.findById(upAlvo.getIdPermissoes());
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"VOCÊ NÃO PODE CONCEDER [" + (p != null ? p.getChave() : "ID " + upAlvo.getIdPermissoes())
								+ "]. VOCÊ NÃO POSSUI ESTE ACESSO.");
			}

			// 3. Tranca 2: Hierarquia de Tempo (Double Validation)
			java.time.LocalDateTime expiraExecutor = mapaExecutor.get(upAlvo.getIdPermissoes());
			java.time.LocalDateTime expiraAlvo = upAlvo.getExpiraEm() == null ? java.time.LocalDateTime.MAX
					: upAlvo.getExpiraEm();

			if (expiraAlvo.isAfter(expiraExecutor)) {
				Permissao p = pDao.findById(upAlvo.getIdPermissoes());
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"DATA INVÁLIDA: Sua permissão para [" + p.getChave() + "] expira em "
								+ (expiraExecutor.equals(java.time.LocalDateTime.MAX) ? "nunca"
										: TimeUtils.formatarDataHora(expiraExecutor))
								+ ". Você não pode conceder um prazo maior que o seu.");
			}
		}
	}

	public List<MenuChave> carregarPermissoesAtivas(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioPermissaoDao(conn).buscarChavesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public List<Permissao> carregarPermissoesDetalhadas(Integer idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PermissaoDao pDao = new PermissaoDao(conn);
			return pDao.listarPermissoesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO CARREGAR PERMISSÕES DETALHADAS", e);
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

	private List<Integer> garantirInfraestruturaMenu(Connection conn, MenuChave chave) {
		PermissaoDao pDao = new PermissaoDao(conn);
		MenuSistemaDao menuDao = new MenuSistemaDao(conn);
		PermissaoMenuDao pmDao = new PermissaoMenuDao(conn);

		String categoria = extrairCategoria(chave.name());

		// 1. Garante que o Menu existe na tabela menu_sistema
		int idMenu = menuDao.save(chave.name(), categoria);

		List<Integer> idsGerados = new ArrayList<>();
		List<String> tiposOperacao = List.of("READ", "WRITE", "DELETE");

		for (String tipo : tiposOperacao) {
			// Busca se já existe a combinação CHAVE + TIPO (ex: CADASTROS_USUARIO + READ)
			var permissaoBanco = pDao.findByChaveETipo(chave.name(), tipo);

			int idPerm;
			if (permissaoBanco == null) {
				// Se não existe, cria a nova permissão granular
				Permissao novaP = new Permissao();
				novaP.setChave(chave.name());
				novaP.setTipo(tipo);
				novaP.setCategoria(categoria);
				novaP.setDescricao("Permissão de " + tipo + " em " + chave.name());

				idPerm = pDao.save(novaP);
				pmDao.vincular(idPerm, idMenu);
			} else {
				idPerm = permissaoBanco.getIdPermissoes();
			}
			idsGerados.add(idPerm);
		}

		return idsGerados;
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

	private void validarDados(Usuario usuario, Map<MenuChave, List<String>> chaves) {
		UsuarioValidationUtils.validarUsuario(usuario);
		if (chaves == null || chaves.isEmpty() && (usuario.getPerfil() == null)) {
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

	private void registrarLogPermissoesFinal(Connection conn, Usuario usuario, List<UsuarioPermissao> finais) {
		LogSistemaDao logDao = new LogSistemaDao(conn);
		PermissaoDao pDao = new PermissaoDao(conn);

		// 1. PERFORMANCE: Busca todas as permissões ativas do sistema (1 única query)
		List<Permissao> todasPermissoes = pDao.listAll();

		// 2. Transforma em um mapa para busca O(1) em memória
		Map<Integer, Permissao> mapaRef = todasPermissoes.stream()
				.collect(Collectors.toMap(Permissao::getIdPermissoes, p -> p));

		// 3. Monta o resumo formatado
		String resumo = finais.stream().map(up -> {
			// Busca no mapa em vez de ir no banco
			Permissao p = mapaRef.get(up.getIdPermissoes());

			String nome = (p != null) ? p.getChave() + " (" + p.getTipo() + ")" : "ID:" + up.getIdPermissoes();
			String origem = up.isHerdada() ? "[PERFIL]" : "[DIRETA]";

			// Formata a expiração se existir (Double Validation visual no log)
			String expira = (up.getExpiraEm() != null) ? " EXPIRA EM: " + TimeUtils.formatarDataHora(up.getExpiraEm())
					: " (PERMANENTE)";

			return nome + origem + expira;
		}).collect(Collectors.joining(" | "));

		// 4. Grava o log de auditoria
		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "SINCRONIZAR_PERMISSOES", "usuario_permissao",
				usuario.getIdUsuario(), "Sincronização de acessos realizada com sucesso.", resumo));
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