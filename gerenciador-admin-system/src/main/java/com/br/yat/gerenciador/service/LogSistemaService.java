package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.util.AuditLogHelper;

public class LogSistemaService extends BaseService {

	private static final MenuChave CHAVE_LIMPEZA = MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS;

	public List<LogSistema> filtrarLogs(String tipo, String acao, String usuario, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			// Apenas quem pode ver logs acessa aqui
			validarAcesso(conn, executor, MenuChave.AUDITORIA_LOG_DO_SISTEMA, TipoPermissao.READ);
			return new LogSistemaDao(conn).listarComFiltros(tipo, acao, usuario);
		} catch (SQLException e) {
			registrarLogErro("ERRO", "CONSULTAR_LOGS", "log_sistema", e);
			return List.of();
		}
	}

	/**
	 * Realiza a limpeza dos logs baseada no parâmetro configurado no sistema. Este
	 * método pode ser chamado pela tela de configuração ou automaticamente no login
	 * do Master.
	 */
	public void executarLimpezaAutomatica(Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			// 1. Double Validation: Só o Master ou quem tem permissão na chave de limpeza
			validarAcesso(conn, executor, CHAVE_LIMPEZA, TipoPermissao.DELETE);

			// 2. Busca quantos dias o Master configurou (usando sua
			// ParametroSistemaService)
			ParametroSistemaService params = new ParametroSistemaService();
			int dias = params.getInt(ParametroChave.LOGS_DIAS_RETENCAO, 90); // default 90 dias

			// 3. Executa a limpeza
			LogSistemaDao dao = new LogSistemaDao(conn);
			int removidos = dao.limparLogsAntigos(dias);

			if (removidos > 0) {
				// Registramos que houve uma limpeza (importante para auditoria!)
				registrarLogSucessoManual(conn, executor, removidos, dias);
			}

		} catch (Exception e) {
			registrarLogErro("ERRO", "LIMPEZA_AUTOMATICA", "sistema", e);
		}
	}

	private void registrarLogSucessoManual(Connection conn, Usuario executor, int total, int dias) throws SQLException {
		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("mensagem", "Limpeza de logs realizada");
		detalhes.put("registros_removidos", total);
		detalhes.put("politica_dias", dias);

		LogSistema log = AuditLogHelper.gerarLogSucesso("MANUTENCAO", "LIMPEZA_LOGS", "sistema", null, null, detalhes);

		if (executor != null && executor.getIdUsuario() != null && executor.getIdUsuario() > 0) {
			log.setUsuario(executor);
		} else {
			log.setUsuario(null);
		}

		new LogSistemaDao(conn).save(log);
	}
}