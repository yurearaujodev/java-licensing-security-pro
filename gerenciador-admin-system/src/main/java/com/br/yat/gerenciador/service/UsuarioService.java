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
import java.util.stream.Collectors;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
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
	private final AutenticacaoService authService = new AutenticacaoService();
	
	private final ParametroSistemaService parametroService = new ParametroSistemaService();
	private static final MenuChave CHAVE_SALVAR = MenuChave.CADASTROS_USUARIO;

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
			UsuarioDao usuarioDao = new UsuarioDao(conn);

			// Regra para destravar o Setup Inicial
			boolean isSetupInicial = (executor == null && !existeUsuarioMaster() && usuario.isMaster());

			ConnectionFactory.beginTransaction(conn);

			// Só valida acesso se não for o primeiro Master do sistema
			if (!isSetupInicial) {
				validarAcesso(conn, executor, CHAVE_SALVAR, "WRITE");
			}

			try {
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

				// Validações de Poder (Policy)
				if (!isSetupInicial && alvoExistente != null) {
					if (!UsuarioPolicy.isPrivilegiado(executor) && UsuarioPolicy.isPrivilegiado(alvoExistente)) {
						throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
								"APENAS UM MASTER PODE ALTERAR OUTRO MASTER.");
					}

					if (!temMaisPoder(conn, executor, alvoExistente)) {
						throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
								"Privilégios insuficientes para alterar este usuário.");
					}
				}

				boolean senhaAlterada = processarSenha(usuario, isNovo, executor, estadoAnterior);

				if (senhaAlterada) {
					int diasExpira = parametroService.getInt(ParametroChave.SENHA_EXPIRA_DIAS, 90);
				    usuario.setSenhaExpiraEm(LocalDateTime.now().plusDays(diasExpira));
				}
				// 1. Persistência do Usuário (Garante ID para as permissões)
				salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, isNovo, conn);

				if (senhaAlterada && !isNovo) {
					
					logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "SENHA_ALTERADA", "usuario",
							usuario.getIdUsuario(), "O executor alterou a senha deste usuário.", null));
				}

				// 2. Processamento de Permissões
				List<UsuarioPermissao> permissoesPerfil = carregarPermissoesDoPerfil(conn, usuario, executor);
				List<UsuarioPermissao> permissoesDiretas = carregarPermissoesGranulares(conn, usuario,
						permissoesGranulares, datasExpiracao);

				Map<Integer, UsuarioPermissao> mapa = new LinkedHashMap<>();
				permissoesPerfil.forEach(up -> mapa.put(up.getIdPermissoes(), up));
				permissoesDiretas.forEach(up -> mapa.put(up.getIdPermissoes(), up));

				List<UsuarioPermissao> listaFinalSincronismo = new ArrayList<>(mapa.values());
				listaFinalSincronismo.forEach(up -> up.setIdUsuario(usuario.getIdUsuario()));

				// 3. Double Validation: Hierarquia de Acesso (Policy + Nível)
				// Se o usuário alvo for Master, ele ignora a validação de hierarquia (tem tudo)
				if (!isSetupInicial && !UsuarioPolicy.isPrivilegiado(usuario)) {
					validarHierarquiaUsuarioPermissao(conn, executor, listaFinalSincronismo);
				}

				// 4. Sincronização
				upDao.syncByUsuario(usuario.getIdUsuario(), listaFinalSincronismo);
				registrarLogPermissoesFinal(conn, usuario, listaFinalSincronismo);

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
			return up;
		}).toList();
	}
	private boolean processarSenha(Usuario usuario, boolean isNovo, Usuario executor, Usuario estadoAnterior) {
		char[] senhaNova = usuario.getSenhaHash();
		char[] senhaAntiga = usuario.getSenhaAntiga();
		char[] senhaConfirmar = usuario.getConfirmarSenha();

		try {
			if (!isNovo && (senhaNova == null || senhaNova.length == 0)) {
	            return false;
	        }
			if (isNovo && (senhaNova == null || senhaNova.length == 0)) throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA É OBRIGATÓRIA.");
			if (senhaNova == null || senhaNova.length == 0) return false;

			if (senhaConfirmar == null || !Arrays.equals(senhaNova, senhaConfirmar)) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			}

			boolean alterandoPropriaSenha = !isNovo && executor != null && executor.getIdUsuario().equals(usuario.getIdUsuario());

			if (alterandoPropriaSenha) {
				if (estadoAnterior == null || !PasswordUtils.verifyPassword(senhaAntiga, estadoAnterior.getSenhaHashString())) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "A SENHA ANTIGA ESTÁ INCORRETA.");
				}
			} else if (!isNovo && !UsuarioPolicy.isPrivilegiado(executor)) {
                throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Sem permissão para alterar senha.");
            }

            // USO DA NOVA SERVICE: Centraliza a regra de complexidade e Hash
			usuario.setSenhaHashString(authService.gerarHashSeguro(senhaNova));
			return true;
		} finally {
			SensitiveData.safeClear(senhaNova);
			SensitiveData.safeClear(senhaAntiga);
			SensitiveData.safeClear(senhaConfirmar);
		}
	}

	private List<UsuarioPermissao> carregarPermissoesGranulares(Connection conn, Usuario usuario,
	        Map<MenuChave, List<String>> permissoesGranulares, Map<MenuChave, String> datasTexto) throws SQLException {
	    
	    List<UsuarioPermissao> novasEntidades = new ArrayList<>();
	    PermissaoDao pDao = new PermissaoDao(conn);

	    // SE FOR MASTER: Não tenta criar infraestrutura (isso o PerfilService já fez)
	    // Apenas busca todas as permissões existentes no banco e associa ao usuário.
	    if (usuario.isMaster()) {
	        List<Permissao> todasNoBanco = pDao.listAll(); 
	        for (Permissao p : todasNoBanco) {
	            novasEntidades.add(criarEntidadePermissao(usuario.getIdUsuario(), p.getIdPermissoes()));
	        }
	        return novasEntidades;
	    }

	    // SE NÃO FOR MASTER: Processa as permissões específicas marcadas na tela
	    if (permissoesGranulares != null) {
	        permissoesGranulares.forEach((chave, tipos) -> {
	            LocalDateTime dataExp = null;
	            if (datasTexto != null && datasTexto.containsKey(chave)) {
	                dataExp = TimeUtils.parseDataHora(datasTexto.get(chave));
	            }
	            
	            for (String tipo : tipos) {
	                Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
	                if (p != null) {
	                    UsuarioPermissao up = criarEntidadePermissao(usuario.getIdUsuario(), p.getIdPermissoes());
	                    up.setHerdada(false);
	                    up.setExpiraEm(dataExp);
	                    novasEntidades.add(up);
	                }
	            }
	        });
	    }

	    return novasEntidades;
	}

	private UsuarioPermissao criarEntidadePermissao(Integer idUsuario, Integer idPermissao) {
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

	// 1. ATUALIZAÇÃO DO temMaisPoder (Segurança contra nulos)
	private boolean temMaisPoder(Connection conn, Usuario executor, Usuario alvo) {
		if (UsuarioPolicy.isPrivilegiado(executor))
			return true;
		if (UsuarioPolicy.isPrivilegiado(alvo))
			return false;

		PermissaoDao pDao = new PermissaoDao(conn);

		// Coalesce para 0 caso o usuário não tenha permissões (evita
		// NullPointerException)
		Integer nivelMaxExecutor = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
		Integer nivelMaxAlvo = pDao.buscarMaiorNivelDoUsuario(alvo.getIdUsuario());

		int nExecutor = (nivelMaxExecutor != null ? nivelMaxExecutor : 0);
	    int nAlvo = (nivelMaxAlvo != null ? nivelMaxAlvo : 0);
		
	    return UsuarioPolicy.temHierarquiaParaAlterar(executor, nExecutor, nAlvo);
	}

	// 2. ATUALIZAÇÃO DA validarHierarquiaUsuarioPermissao (Inclusão da trava de
	// nível)
	private void validarHierarquiaUsuarioPermissao(Connection conn, Usuario executor,
			List<UsuarioPermissao> permissoesSendoAtribuidas) {

		if (executor == null || UsuarioPolicy.isPrivilegiado(executor))
			return;

		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
		PermissaoDao pDao = new PermissaoDao(conn);

		// Buscamos o teto de poder do executor
		Integer nivelTetoExecutor = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
		nivelTetoExecutor = (nivelTetoExecutor != null) ? nivelTetoExecutor : 0;

		List<UsuarioPermissao> permissoesDoExecutor = upDao.listarPorUsuario(executor.getIdUsuario());
		Map<Integer, java.time.LocalDateTime> mapaExecutor = permissoesDoExecutor.stream()
				.collect(Collectors.toMap(UsuarioPermissao::getIdPermissoes,
						up -> up.getExpiraEm() == null ? java.time.LocalDateTime.MAX : up.getExpiraEm(), (e, s) -> e));

		for (UsuarioPermissao upAlvo : permissoesSendoAtribuidas) {
			Permissao p = pDao.findById(upAlvo.getIdPermissoes());
			if (p == null)
				continue;

			// TRAVA 1: O executor possui essa permissão?
			if (!mapaExecutor.containsKey(upAlvo.getIdPermissoes())) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"VOCÊ NÃO POSSUI ACESSO A [" + p.getChave() + "].");
			}

			// TRAVA 2: O Nível da permissão é superior ao que o executor pode gerenciar?
			// (NOVO!)
			if (p.getNivel() > nivelTetoExecutor) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"NÍVEL INSUFICIENTE: A permissão [" + p.getChave() + "] exige nível " + p.getNivel()
								+ " e seu nível máximo é " + nivelTetoExecutor);
			}

			// TRAVA 3: Hierarquia de Tempo
			LocalDateTime expiraExecutor = mapaExecutor.get(upAlvo.getIdPermissoes());
			LocalDateTime expiraAlvo = upAlvo.getExpiraEm() == null ? java.time.LocalDateTime.MAX
					: upAlvo.getExpiraEm();

			if (expiraAlvo.isAfter(expiraExecutor)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"DATA INVÁLIDA: O prazo para [" + p.getChave() + "] excede o seu limite de expiração.");
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

	public Empresa buscarEmpresaFornecedora() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new EmpresaDao(conn).buscarPorFornecedora();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO BUSCAR EMPRESA: " + e.getMessage(), e);
		}
	}
}