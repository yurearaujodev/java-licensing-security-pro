package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.Diferenca;

public class AuditLogService extends BaseService {

	private final DaoFactory daoFactory;

	public AuditLogService(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void registrarErro(String tipo, String acao, String entidade, Exception e) {
		try {
			execute(conn -> {
				String msgErro = (e.getMessage() != null) ? e.getMessage() : e.toString();
				LogSistema log = AuditLogHelper.gerarLogErro(tipo, acao, entidade, msgErro);
				LogSistemaDao dao = daoFactory.createLogSistemaDao(conn);
				dao.save(log);
				return null;
			});
		} catch (Exception ex) {
			logger.error("Falha crítica ao gravar log de erro", ex);
		}
	}

	public void registrarSucesso(Connection conn, String tipo, String acao, String entidade, Integer idRef,
			Object antes, Object depois) {
		try {
			LogSistema log = AuditLogHelper.gerarLogSucesso(tipo, acao, entidade, idRef, antes, depois);
			LogSistemaDao dao = daoFactory.createLogSistemaDao(conn);
			dao.save(log);
		} catch (Exception e) {
			logger.error("Falha ao registrar log de sucesso", e);
		}
	}

	public void registrarSucessoManual(Connection conn, Usuario executor, int total, int dias) {
		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("mensagem", "Limpeza de logs realizada");
		detalhes.put("registros_removidos", total);
		detalhes.put("politica_dias", dias);

		LogSistema log = AuditLogHelper.gerarLogSucesso("MANUTENCAO", "LIMPEZA_LOGS", "sistema", null, null, detalhes);

		log.setUsuario(
				(executor != null && executor.getIdUsuario() != null && executor.getIdUsuario() > 0) ? executor : null);

		try {
			LogSistemaDao dao = daoFactory.createLogSistemaDao(conn);
			dao.save(log);
		} catch (Exception e) {
			logger.error("Falha ao registrar log de sucesso manual", e);
		}
	}

	public void registrarAlteracaoPermissoesUsuario(Connection conn, Usuario usuario, Diferenca<String> diff) {

		if (diff == null || !diff.temAlteracao()) {
			return;
		}

		Map<String, Object> antes = Map.of("usuarioId", usuario.getIdUsuario(), "removidas", diff.getRemovidos());

		Map<String, Object> depois = Map.of("usuarioId", usuario.getIdUsuario(), "adicionadas", diff.getAdicionados());

		registrarSucesso(conn, "SEGURANCA", "ALTERAR_PERMISSOES_USUARIO", "usuario", usuario.getIdUsuario(), antes,
				depois);
	}

	public void registrarAlteracaoPermissoesPerfil(Connection conn, int idPerfil, Diferenca<String> diff) {

		if (diff == null || !diff.temAlteracao()) {
			return;
		}

		Map<String, Object> antes = Map.of("perfilId", idPerfil, "removidas", diff.getRemovidos());

		Map<String, Object> depois = Map.of("perfilId", idPerfil, "adicionadas", diff.getAdicionados());

		registrarSucesso(conn, "SEGURANCA", "ALTERAR_PERMISSOES_PERFIL", "perfil", idPerfil, antes, depois);
	}

}
