package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.dto.UsuarioPermissaoDetatlheDTO;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.TimeUtils;
import com.br.yat.gerenciador.validation.UsuarioValidationUtils;

public class UsuarioService extends BaseService {
	private final AutenticacaoService authService;

	private final ParametroSistemaService parametroService;
	private final UsuarioPermissaoService permissaoService;

	public UsuarioService(AutenticacaoService authService, ParametroSistemaService parametroService,
			UsuarioPermissaoService permissaoService) {
		this.authService = authService;
		this.parametroService = parametroService;
		this.permissaoService = permissaoService;
	}

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
		if (datasExpiracao != null && !datasExpiracao.isEmpty()) {
	        datasExpiracao.forEach((menu, dataStr) -> {
	            if (dataStr != null && !dataStr.isBlank()) {
	                try {
	                    LocalDateTime dataDigitada = TimeUtils.parseDataHora(dataStr);
	                    
	                    // Validação 1: Bloqueia datas retroativas
	                    if (dataDigitada.isBefore(LocalDateTime.now().minusMinutes(1))) {
	                        throw new ValidationException(ValidationErrorType.INVALID_FIELD, 
	                            "A DATA DE EXPIRAÇÃO PARA [" + menu + "] NÃO PODE SER NO PASSADO.");
	                    }
	                    
	                    // Validação 2: Bloqueia erros de digitação absurdos (ex: ano 2099)
	                    if (dataDigitada.isAfter(LocalDateTime.now().plusYears(10))) {
	                        throw new ValidationException(ValidationErrorType.INVALID_FIELD, 
	                            "A DATA DE EXPIRAÇÃO PARA [" + menu + "] EXCEDEU O LIMITE (MÁX 10 ANOS).");
	                    }
	                } catch (Exception e) {
	                    if (e instanceof ValidationException) throw e;
	                    throw new ValidationException(ValidationErrorType.INVALID_FIELD, 
	                        "FORMATO DE DATA INVÁLIDO PARA O MENU: " + menu);
	                }
	            }
	        });
	    }
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao usuarioDao = new UsuarioDao(conn);
			boolean isSetupInicial = (executor == null && !existeUsuarioMaster() && usuario.isMaster());

			ConnectionFactory.beginTransaction(conn);

			if (!isSetupInicial) {
				validarAcesso(conn, executor, CHAVE_SALVAR, "WRITE");
			}

			try {
				UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
				validarRegrasPersistencia(usuarioDao, usuario);
				if (usuario.getPerfil() != null) {
				    PermissaoDao pDao = new PermissaoDao(conn);
				    List<Permissao> permissoesDoPerfil = pDao.listarPermissoesDoPerfil(usuario.getPerfil().getIdPerfil());

				    // 1. Criamos um novo mapa mutável para processamento
				    Map<MenuChave, List<String>> novasPermissoes = new HashMap<>();

				    permissoesGranulares.forEach((chave, tiposOriginais) -> {
				        // 2. Criamos uma cópia mutável da lista (Resolve o UnsupportedOperationException)
				        List<String> tiposMutaveis = new ArrayList<>(tiposOriginais);

				        // 3. Removemos apenas o que o Perfil já possui (desde que não tenha data de expiração)
				        tiposMutaveis.removeIf(tipo -> {
				            boolean jaExisteNoPerfil = permissoesDoPerfil.stream()
				                    .anyMatch(p -> p.getChave().equals(chave.name()) && p.getTipo().equalsIgnoreCase(tipo));
				            
				            boolean temDataEspecial = datasExpiracao.containsKey(chave);

				            return jaExisteNoPerfil && !temDataEspecial;
				        });

				        // 4. Se restou algo (é uma permissão extra que você marcou), guardamos no novo mapa
				        if (!tiposMutaveis.isEmpty()) {
				            novasPermissoes.put(chave, tiposMutaveis);
				        }
				    });

				    // 5. Substituímos a referência para o service seguir com os dados limpos
				    permissoesGranulares = novasPermissoes;
				}

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

				salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, isNovo, conn);

				if (senhaAlterada && !isNovo) {
					registrarLogSucesso(conn, "SEGURANCA", "SENHA_ALTERADA", "usuario", usuario.getIdUsuario(),
							"O executor alterou a senha deste usuário.", null);
				}

				List<UsuarioPermissao> listaFinalSincronismo = permissaoService.montarPermissoes(conn, usuario,
						executor, isSetupInicial, permissoesGranulares, datasExpiracao);

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
			registrarLogSucesso(conn, "CADASTRO", "INSERIR_USUARIO", "usuario", id, null, usuario);
		} else {
			if (anterior.getStatus() != StatusUsuario.ATIVO && usuario.getStatus() == StatusUsuario.ATIVO) {
				dao.resetTentativasFalhas(usuario.getIdUsuario());

				Map<String, String> alteracaoStatus = new HashMap<>();
				alteracaoStatus.put("de", anterior.getStatus().name());
				alteracaoStatus.put("para", "ATIVO");
				alteracaoStatus.put("motivo", "Reativação manual via service layer");

				registrarLogSucesso(conn, "SEGURANCA", "REATIVACAO_MANUAL", "usuario", usuario.getIdUsuario(), null,
						alteracaoStatus);
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
			registrarLogSucesso(conn, "CADASTRO", "EXCLUIR_USUARIO", "usuario", idUsuario, anterior, null);
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

			Map<String, String> detalhes = new HashMap<>();
			detalhes.put("status_anterior", "EXCLUIDO");
			detalhes.put("status_atual", "ATIVO");
			detalhes.put("acao", "Restauração de registro via lixeira");

			registrarLogSucesso(conn, "CADASTRO", "RESTAURAR_USUARIO", "usuario", idUsuario, null, detalhes);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESTAURAR", e);
		}
	}

	private boolean processarSenha(Usuario usuario, boolean isNovo, Usuario executor, Usuario estadoAnterior) {
		char[] senhaNova = usuario.getSenhaHash();
		char[] senhaAntiga = usuario.getSenhaAntiga();
		char[] senhaConfirmar = usuario.getConfirmarSenha();

		try {
			if (isNovo && (senhaNova == null || senhaNova.length == 0))
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA É OBRIGATÓRIA.");
			if (senhaNova == null || senhaNova.length == 0)
				return false;

			if (senhaConfirmar == null || senhaNova.length != senhaConfirmar.length) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			}

			int diff = 0;
			for (int i = 0; i < senhaNova.length; i++) {
				diff |= senhaNova[i] ^ senhaConfirmar[i];
			}

			if (diff != 0) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			}

			boolean alterandoPropriaSenha = !isNovo && executor != null && executor.getIdUsuario() != null
					&& executor.getIdUsuario().equals(usuario.getIdUsuario());

			if (alterandoPropriaSenha) {
				if (estadoAnterior == null
						|| !PasswordUtils.verifyPassword(senhaAntiga, estadoAnterior.getSenhaHashString())) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "A SENHA ANTIGA ESTÁ INCORRETA.");
				}
			} else if (!isNovo && !UsuarioPolicy.isPrivilegiado(executor)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Sem permissão para alterar senha.");
			}

			usuario.setSenhaHashString(authService.gerarHashSeguro(senhaNova));
			return true;
		} finally {
			SensitiveData.safeClear(senhaNova);
			SensitiveData.safeClear(senhaAntiga);
			SensitiveData.safeClear(senhaConfirmar);
		}
	}
//	
//	public List<Permissao> carregarPermissoesEspeciais(Integer idUsuario) {
//	    // 1ª Validação (Input): Evita ida ao banco com dados inválidos
//	    if (idUsuario == null || idUsuario <= 0) return new ArrayList<>();
//
//	    try (Connection conn = ConnectionFactory.getConnection()) {
//	        // 2ª Validação (State): A GenericDao já valida se a conexão está aberta no construtor
//	        return new PermissaoDao(conn).buscarPermissoesGranularesNaoHerdadas(idUsuario);
//	    } catch (SQLException e) {
//	        throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "FALHA NA CONEXÃO AO CARREGAR PERMISSÕES", e);
//	    }
//	}

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

		PermissaoDao pDao = new PermissaoDao(conn);

		Integer nivelMaxExecutor = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
		Integer nivelMaxAlvo = pDao.buscarMaiorNivelDoUsuario(alvo.getIdUsuario());

		int nExecutor = (nivelMaxExecutor != null ? nivelMaxExecutor : 0);
		int nAlvo = (nivelMaxAlvo != null ? nivelMaxAlvo : 0);

		return UsuarioPolicy.temHierarquiaParaAlterar(executor, nExecutor, nAlvo);
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
	
	public List<UsuarioPermissaoDetatlheDTO> carregarDadosPermissoesEdicao(Integer idUsuario) {
	    // 1ª Validação (Input): Double Validation
	    if (idUsuario == null || idUsuario <= 0) return new ArrayList<>();

	    try (Connection conn = ConnectionFactory.getConnection()) {
	        PermissaoDao pDao = new PermissaoDao(conn);
	        UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);

	        // 1. Pegamos as definições das permissões
	        List<Permissao> listaPermissoes = pDao.listarPermissoesAtivasPorUsuario(idUsuario);
	        
	        // 2. Pegamos os vínculos reais (onde mora a data)
	        List<UsuarioPermissao> listaVinculos = upDao.listarPorUsuario(idUsuario);

	        // Criamos um mapa para de-para rápido: ID_PERMISSAO -> VINCULO
	        Map<Integer, UsuarioPermissao> mapaVinculos = listaVinculos.stream()
	            .collect(Collectors.toMap(UsuarioPermissao::getIdPermissoes, v -> v, (a, b) -> a));

	        // 2ª Validação (State/Integridade): Double Validation
	        // Montamos o DTO que o Controller vai usar
	        return listaPermissoes.stream().map(p -> {
	            UsuarioPermissao vinculo = mapaVinculos.get(p.getIdPermissoes());
	            
	            // Aqui você poderia validar se a data vinda do banco é coerente
	            return new UsuarioPermissaoDetatlheDTO(p, vinculo);
	        }).toList();

	    } catch (SQLException e) {
	        throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO CARREGAR DADOS PARA EDICAO", e);
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

		if ((chaves == null || chaves.isEmpty()) && usuario.getPerfil() == null) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O USUÁRIO DEVE TER AO MENOS UMA PERMISSÃO.");
		}
	}

	private void validarDuplicidadeEmail(UsuarioDao dao, Usuario u) {
		Usuario existente = dao.buscarPorEmail(u.getEmail());
		if (existente != null && (u.getIdUsuario() == null || !existente.getIdUsuario().equals(u.getIdUsuario()))) {
			throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
					"ESTE E-MAIL JÁ ESTÁ CADASTRADO NO SISTEMA.");
		}
	}

	private void registrarLogPermissoesFinal(Connection conn, Usuario usuario, List<UsuarioPermissao> finais) {
		PermissaoDao pDao = new PermissaoDao(conn);

		List<Permissao> todasPermissoes = pDao.listAll();

		Map<Integer, Permissao> mapaRef = todasPermissoes.stream()
				.collect(Collectors.toMap(Permissao::getIdPermissoes, p -> p));

		String resumo = finais.stream().map(up -> {
			Permissao p = mapaRef.get(up.getIdPermissoes());

			String nome = (p != null) ? p.getChave() + " (" + p.getTipo() + ")" : "ID:" + up.getIdPermissoes();
			String origem = up.isHerdada() ? "[PERFIL]" : "[DIRETA]";

			String expira = (up.getExpiraEm() != null) ? " EXPIRA EM: " + TimeUtils.formatarDataHora(up.getExpiraEm())
					: " (PERMANENTE)";

			return nome + origem + expira;
		}).collect(Collectors.joining(" | "));

		registrarLogSucesso(conn, "SEGURANCA", "SINCRONIZAR_PERMISSOES", "usuario_permissao", usuario.getIdUsuario(),
				"Sincronização de acessos realizada com sucesso.", resumo);
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