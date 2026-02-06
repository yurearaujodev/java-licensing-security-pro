package com.br.yat.gerenciador.controller;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.ParametroSistema;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.ParametroSistemaView;

public class ParametroSistemaController extends BaseController {

    private final ParametroSistemaView view;
    private final ParametroSistemaService service;

    public ParametroSistemaController(ParametroSistemaView view, ParametroSistemaService service) {
        this.view = view;
        this.service = service;
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

                    // --- LICENÇA ---
                    service.getInt(ParametroChave.LICENCA_ALERTA_EXPIRACAO_DIAS, 7),
                    service.getInt(ParametroChave.LICENCA_MAX_DISPOSITIVOS, 3),
                    service.getBoolean(ParametroChave.LICENCA_ATIVA_PADRAO, true),

                    // --- SISTEMA / LOGS ---
                    service.getBoolean(ParametroChave.LOG_SISTEMA_ATIVO, true),
                    service.getString(ParametroChave.LOG_NIVEL_PADRAO, "INFO"),
                    service.getInt(ParametroChave.TEMPO_REFRESH_DASHBOARD, 60),

                    // --- NOTIFICAÇÕES / EMAIL ---
                    service.getBoolean(ParametroChave.EMAIL_NOTIFICACAO_ATIVO, true),
                    service.getString(ParametroChave.EMAIL_ALERTA_LICENCA, "")
            );

        }, dto -> {
            // --- LOGIN ---
            view.spnLoginMaxTentativas.setValue(dto.loginMaxTentativas());
            view.spnLoginTempoBloqueio.setValue(dto.loginTempoBloqueio());
            view.spnSenhaMinTamanho.setValue(dto.senhaMin());
            view.spnForcarTrocaSenha.setValue(dto.forcarTrocaSenhaDias());
            view.spnTempoSessaoMin.setValue(dto.tempoSessaoMin());

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

            // --- LOGIN ---
            salvarParametro(ParametroChave.LOGIN_MAX_TENTATIVAS, view.spnLoginMaxTentativas.getValue(), "Máx. tentativas de login");
            salvarParametro(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, view.spnLoginTempoBloqueio.getValue(), "Tempo de bloqueio");
            salvarParametro(ParametroChave.SENHA_MIN_TAMANHO, view.spnSenhaMinTamanho.getValue(), "Tamanho mínimo da senha");
            salvarParametro(ParametroChave.FORCAR_TROCA_SENHA_DIAS, view.spnForcarTrocaSenha.getValue(), "Forçar troca de senha (dias)");
            salvarParametro(ParametroChave.TEMPO_SESSAO_MIN, view.spnTempoSessaoMin.getValue(), "Tempo sessão (min)");

            // --- LICENÇA ---
            salvarParametro(ParametroChave.LICENCA_ALERTA_EXPIRACAO_DIAS, view.spnLicencaExpiracaoAlertaDias.getValue(), "Dias para alerta de expiração");
            salvarParametro(ParametroChave.LICENCA_MAX_DISPOSITIVOS, view.spnLicencaMaxDispositivos.getValue(), "Máx. dispositivos por licença");
            salvarParametro(ParametroChave.LICENCA_ATIVA_PADRAO, view.chkLicencaAtivaPadrao.isSelected(), "Licença ativa por padrão");

            // --- SISTEMA / LOGS ---
            salvarParametro(ParametroChave.LOG_SISTEMA_ATIVO, view.chkLogSistemaAtivo.isSelected(), "Log do sistema ativo");
            salvarParametro(ParametroChave.LOG_NIVEL_PADRAO, view.cmbNivelLogPadrao.getSelectedItem(), "Nível de log padrão");
            salvarParametro(ParametroChave.TEMPO_REFRESH_DASHBOARD, view.spnTempoRefreshDashboard.getValue(), "Tempo refresh dashboard");

            // --- NOTIFICAÇÕES / EMAIL ---
            salvarParametro(ParametroChave.EMAIL_NOTIFICACAO_ATIVO, view.chkEmailNotificacaoAtivo.isSelected(), "Email notificações ativo");
            salvarParametro(ParametroChave.EMAIL_ALERTA_LICENCA, view.txtEmailAlertaLicenca.getText(), "Email alerta licença");

            return null;
        }, r -> DialogFactory.informacao(view, "Parâmetros salvos com sucesso."));
    }

    // -------------------- MÉTODO GENÉRICO DE SALVAR --------------------
    private void salvarParametro(ParametroChave chave, Object valor, String descricao) {
        if (valor == null) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "Valor inválido para: " + descricao);
        }

        ParametroSistema p = new ParametroSistema();
        p.setChave(chave.getChaveBanco());
        p.setDescricao(descricao);

        if (valor instanceof Boolean || valor instanceof Integer || valor instanceof String) {
            p.setValor(String.valueOf(valor));
        } else {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "Tipo de valor inválido para: " + descricao);
        }

        service.salvarOuAtualizar(p);
    }

    // -------------------- DTO PARA CARREGAMENTO --------------------
    private record ParametrosDTO(
            int loginMaxTentativas,
            int loginTempoBloqueio,
            int senhaMin,
            int forcarTrocaSenhaDias,
            int tempoSessaoMin,

            int licencaAlertaExpiracaoDias,
            int licencaMaxDispositivos,
            boolean licencaAtivaPadrao,

            boolean logSistemaAtivo,
            String logNivelPadrao,
            int tempoRefreshDashboard,

            boolean emailNotificacaoAtivo,
            String emailAlertaLicenca
    ) {}
}
