package com.br.yat.gerenciador.model.dto;

public record ParametrosDTO(int loginMaxTentativas, int loginTempoBloqueio, int senhaMin, int forcarTrocaSenhaDias,
		int tempoSessaoMin, String senhaResetPadrao,

		int licencaAlertaExpiracaoDias, int licencaMaxDispositivos, boolean licencaAtivaPadrao,

		boolean logSistemaAtivo, String logNivelPadrao, int tempoRefreshDashboard, int logsDiasRetencao,

		boolean emailNotificacaoAtivo, String emailAlertaLicenca) {

}
