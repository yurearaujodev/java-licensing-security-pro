package com.br.yat.gerenciador.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.ParametroSistema;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.dto.ParametrosDTO;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.ParametroSistemaView;

public class ParametroSistemaController extends BaseController {

	private final ParametroSistemaView view;
	private final ParametroSistemaService service;
	private final AutenticacaoService authService;

	public ParametroSistemaController(ParametroSistemaView view, ParametroSistemaService service,
			AutenticacaoService authService) {
		this.view = view;
		this.service = service;
		this.authService = authService;
		init();
	}

	private void init() {
		carregarParametros();
		configurarAcoes();
	}

	// -------------------- CARREGAR PARÂMETROS --------------------
	private void carregarParametros() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> {

			return new ParametrosDTO(
					// --- LOGIN ---
					service.getInt(ParametroChave.LOGIN_MAX_TENTATIVAS, 5),
					service.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5),
					service.getInt(ParametroChave.SENHA_MIN_TAMANHO, 6),
					service.getInt(ParametroChave.FORCAR_TROCA_SENHA_DIAS, 90),
					service.getInt(ParametroChave.TEMPO_SESSAO_MIN, 30),
					service.getString(ParametroChave.SENHA_RESET_PADRAO, "Mudar@123"),

					// --- LICENÇA ---
					service.getInt(ParametroChave.LICENCA_ALERTA_EXPIRACAO_DIAS, 7),
					service.getInt(ParametroChave.LICENCA_MAX_DISPOSITIVOS, 3),
					service.getBoolean(ParametroChave.LICENCA_ATIVA_PADRAO, true),

					// --- SISTEMA / LOGS ---
					service.getBoolean(ParametroChave.LOG_SISTEMA_ATIVO, true),
					service.getString(ParametroChave.LOG_NIVEL_PADRAO, "INFO"),
					service.getInt(ParametroChave.TEMPO_REFRESH_DASHBOARD, 60),

					// --- NOTIFICAÇÕES / EMAIL ---
					service.getInt(ParametroChave.LOGS_DIAS_RETENCAO, 90),
					service.getBoolean(ParametroChave.EMAIL_NOTIFICACAO_ATIVO, true),
					service.getString(ParametroChave.EMAIL_ALERTA_LICENCA, ""));

		}, dto -> {
			// --- LOGIN ---
			view.spnLoginMaxTentativas.setValue(dto.loginMaxTentativas());
			view.spnLoginTempoBloqueio.setValue(dto.loginTempoBloqueio());
			view.spnSenhaMinTamanho.setValue(dto.senhaMin());
			view.spnForcarTrocaSenha.setValue(dto.forcarTrocaSenhaDias());
			view.spnTempoSessaoMin.setValue(dto.tempoSessaoMin());
			view.txtSenhaResetPadrao.setText(dto.senhaResetPadrao());

			// --- LICENÇA ---
			view.spnLicencaExpiracaoAlertaDias.setValue(dto.licencaAlertaExpiracaoDias());
			view.spnLicencaMaxDispositivos.setValue(dto.licencaMaxDispositivos());
			view.chkLicencaAtivaPadrao.setSelected(dto.licencaAtivaPadrao());

			// --- SISTEMA / LOGS ---
			view.chkLogSistemaAtivo.setSelected(dto.logSistemaAtivo());
			view.cmbNivelLogPadrao.setSelectedItem(dto.logNivelPadrao());
			view.spnTempoRefreshDashboard.setValue(dto.tempoRefreshDashboard());

			// --- NOTIFICAÇÕES / EMAIL ---
			view.chkEmailNotificacaoAtivo.setSelected(dto.emailNotificacaoAtivo());
			view.txtEmailAlertaLicenca.setText(dto.emailAlertaLicenca());
			view.spnLogsDiasRetencao.setValue(dto.logsDiasRetencao());
		});
	}

	// -------------------- CONFIGURAR AÇÕES --------------------
	private void configurarAcoes() {
		view.btnSalvar.addActionListener(e -> salvar());
		view.btnCancelar.addActionListener(e -> view.dispose());
	}

	// -------------------- SALVAR PARÂMETROS --------------------
	private void salvar() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			List<ParametroSistema> lista = new ArrayList<>();

			String senhaPadrao = view.txtSenhaResetPadrao.getText();
			authService.validarComplexidade(senhaPadrao.toCharArray());

			// Montamos a lista de objetos primeiro
			lista.add(preparar(ParametroChave.LOGIN_MAX_TENTATIVAS, view.spnLoginMaxTentativas.getValue(),
					"Máx. tentativas de login"));
			lista.add(preparar(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, view.spnLoginTempoBloqueio.getValue(),
					"Tempo de bloqueio"));
			lista.add(preparar(ParametroChave.SENHA_MIN_TAMANHO, view.spnSenhaMinTamanho.getValue(),
					"Tamanho mínimo da senha"));
			lista.add(preparar(ParametroChave.FORCAR_TROCA_SENHA_DIAS, view.spnForcarTrocaSenha.getValue(),
					"Forçar troca de senha (dias)"));
			lista.add(
					preparar(ParametroChave.TEMPO_SESSAO_MIN, view.spnTempoSessaoMin.getValue(), "Tempo sessão (min)"));
			lista.add(preparar(ParametroChave.LICENCA_ALERTA_EXPIRACAO_DIAS,
					view.spnLicencaExpiracaoAlertaDias.getValue(), "Dias para alerta de expiração"));
			lista.add(preparar(ParametroChave.LICENCA_MAX_DISPOSITIVOS, view.spnLicencaMaxDispositivos.getValue(),
					"Máx. dispositivos por licença"));
			lista.add(preparar(ParametroChave.LICENCA_ATIVA_PADRAO, view.chkLicencaAtivaPadrao.isSelected(),
					"Licença ativa por padrão"));
			lista.add(preparar(ParametroChave.LOG_SISTEMA_ATIVO, view.chkLogSistemaAtivo.isSelected(),
					"Log do sistema ativo"));
			lista.add(preparar(ParametroChave.LOG_NIVEL_PADRAO, view.cmbNivelLogPadrao.getSelectedItem(),
					"Nível de log padrão"));
			lista.add(preparar(ParametroChave.TEMPO_REFRESH_DASHBOARD, view.spnTempoRefreshDashboard.getValue(),
					"Tempo refresh dashboard"));
			lista.add(preparar(ParametroChave.LOGS_DIAS_RETENCAO, view.spnLogsDiasRetencao.getValue(),
					"Dias de retenção de logs"));
			lista.add(preparar(ParametroChave.EMAIL_NOTIFICACAO_ATIVO, view.chkEmailNotificacaoAtivo.isSelected(),
					"Email notificações ativo"));
			lista.add(preparar(ParametroChave.EMAIL_ALERTA_LICENCA, view.txtEmailAlertaLicenca.getText(),
					"Email alerta licença"));
			lista.add(preparar(ParametroChave.SENHA_RESET_PADRAO, senhaPadrao, "Senha padrão para reset"));
			// CHAMADA ÚNICA: Se um falhar, a Service lança exceção, o rollback desfaz tudo
			// e a BaseController mostra o erro sem ter salvo nada "metade".
			service.salvarOuAtualizar(lista, Sessao.getUsuario());

			return null;
		}, r -> DialogFactory.informacao(view, "Parâmetros salvos com sucesso."));
	}

	private ParametroSistema preparar(ParametroChave chave, Object valor, String descricao) {
		if (valor == null) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "CAMPO NULO: " + descricao);
		}

		ParametroSistema p = new ParametroSistema();
		p.setChave(chave.getChaveBanco());
		p.setDescricao(descricao);

		// Normalização: Transforma o valor em String, garantindo que não seja null para
		// o banco
		p.setValor(String.valueOf(valor).trim());

		return p;
	}
}
