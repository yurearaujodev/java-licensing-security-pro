package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PerfilDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.ValidationUtils;

public class PerfilService extends BaseService {

	private static final MenuChave CHAVE_ACESSO = MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES;

	// Double Validation: Verificação de campos na Service
	public void salvarPerfil(Perfil perfil, Map<MenuChave, List<String>> permissoes, Usuario executor) {
		validarCampos(perfil);

		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_ACESSO, "WRITE");

			ConnectionFactory.beginTransaction(conn);
			try {
				PerfilDao perfilDao = new PerfilDao(conn);
				PerfilPermissoesDao ppDao = new PerfilPermissoesDao(conn);
				PermissaoDao pDao = new PermissaoDao(conn);
				LogSistemaDao logDao = new LogSistemaDao(conn);

				boolean isNovo = (perfil.getIdPerfil() == null || perfil.getIdPerfil() == 0);

				// 1. Salva ou Atualiza o Perfil
				if (isNovo) {
					int id = perfilDao.save(perfil);
					perfil.setIdPerfil(id);
					logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "CRIAR_PERFIL", "perfil", id, null,
							perfil.getNome()));
				} else {
					perfilDao.update(perfil);
					logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "ALTERAR_PERFIL", "perfil",
							perfil.getIdPerfil(), "Update", perfil.getNome()));
				}
				if (!isNovo) {
					ppDao.desvincularTodasDoPerfil(perfil.getIdPerfil());
				}
				// 2. Sincroniza Permissões do Perfil (Double Check)
				sincronizarPermissoesPerfil(ppDao, pDao, perfil.getIdPerfil(), permissoes);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "SALVAR_PERFIL", "perfil", e);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void sincronizarPermissoesPerfil(PerfilPermissoesDao ppDao, PermissaoDao pDao, int idPerfil,
			Map<MenuChave, List<String>> permissoes) {
		// Para Perfis, as permissões são sempre "herdadas = true" por padrão para quem
		// as usa
		permissoes.forEach((chave, tipos) -> {
			for (String tipo : tipos) {
				Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
				if (p != null) {
					ppDao.vincularPermissaoAoPerfil(idPerfil, p.getIdPermissoes(), true);
				}
			}
		});
	}

	public Perfil buscarOuCriarPerfilMaster() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PerfilDao dao = new PerfilDao(conn);

			// 1. Primeiro tentamos buscar o que já existe
			return dao.buscarPorNome("MASTER").orElseGet(() -> {
				// 2. Se não existir, aí sim criamos um novo
				Perfil novo = new Perfil();
				novo.setNome("MASTER");
				novo.setDescricao("PERFIL ADMINISTRADOR MASTER (SETUP INICIAL)");

				int idGerado = dao.save(novo);
				novo.setIdPerfil(idGerado);
				return novo;
			});
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "FALHA AO GERENCIAR PERFIL MASTER", e);
		}
	}
	
	public void excluirPerfil(int idPerfil, Usuario executor) {
	    try (Connection conn = ConnectionFactory.getConnection()) {
	        validarAcesso(conn, executor, CHAVE_ACESSO, "DELETE");

	        PerfilDao perfilDao = new PerfilDao(conn);
	        LogSistemaDao logDao = new LogSistemaDao(conn);

	        // Ajustado para o seu searchById que retorna null se não encontrar
	        Perfil perfil = perfilDao.searchById(idPerfil);
	        
	        if (perfil == null) {
	            throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "PERFIL NÃO ENCONTRADO.");
	        }

	        // Double Validation: Bloqueia exclusão do MASTER
	        if ("MASTER".equalsIgnoreCase(perfil.getNome())) {
	            throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "O PERFIL MASTER NÃO PODE SER EXCLUÍDO OU DESATIVADO.");
	        }

	        ConnectionFactory.beginTransaction(conn);
	        try {
	            perfilDao.softDeleteById(idPerfil);

	            logDao.save(AuditLogHelper.gerarLogSucesso(
	                "SEGURANCA", "EXCLUIR_PERFIL", "perfil", idPerfil, 
	                perfil.getNome(), "Perfil movido para lixeira (Soft Delete)"
	            ));

	            ConnectionFactory.commitTransaction(conn);
	        } catch (Exception e) {
	            ConnectionFactory.rollbackTransaction(conn);
	            registrarLogErro("ERRO", "EXCLUIR_PERFIL", "perfil", e);
	            throw e;
	        }
	    } catch (SQLException e) {
	        throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO DE CONEXÃO AO EXCLUIR PERFIL", e);
	    }
	}
	
	public List<String> listarPermissoesAtivasPorMenu(int idUsuario, MenuChave menu) {
	    try (Connection conn = ConnectionFactory.getConnection()) {
	        // Passamos a conexão para o DAO
	        PermissaoDao pDao = new PermissaoDao(conn);
	        return pDao.buscarTiposAtivosPorUsuarioEMenu(idUsuario, menu.name());
	    } catch (SQLException e) {
	        throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "FALHA NA CONEXÃO", e);
	    }
	}

	private void validarCampos(Perfil p) {
		if (ValidationUtils.isEmpty(p.getNome())) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NOME DO PERFIL É OBRIGATÓRIO.");
		}
	}

	public List<Perfil> listarTodos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new PerfilDao(conn).listAll();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR PERFIS", e);
		}
	}

	public List<Permissao> listarPermissoesDoPerfil(int idPerfil) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new PerfilPermissoesDao(conn).listarPermissoesPorPerfil(idPerfil);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR PERMISSÕES", e);
		}
	}
}