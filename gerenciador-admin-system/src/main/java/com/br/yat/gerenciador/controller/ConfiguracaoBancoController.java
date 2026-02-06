package com.br.yat.gerenciador.controller;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.configurations.DatabaseStatus;
import com.br.yat.gerenciador.model.dto.DatabaseConfigDTO;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.service.DatabaseConnectionService;
import com.br.yat.gerenciador.service.DatabaseSetupService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.validation.DatabaseValidationUtils;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;

public class ConfiguracaoBancoController extends BaseController {

	private final ConfiguracaoBancoView view;
	private final DatabaseSetupService service;
	private final DatabaseConnectionService connectionService;

	public ConfiguracaoBancoController(ConfiguracaoBancoView view, DatabaseSetupService service,
			DatabaseConnectionService connectionService) {
		this.view = view;
		this.service = service;
		this.connectionService = connectionService;
		registrarAcoes();
		carregarDados();

	}

	private void registrarAcoes() {
		view.getBtnSalvar().addActionListener(e -> salvar());
		view.getBtnTestar().addActionListener(e -> testar());
		view.getTxtNomeBanco().addFocusListener(ValidationUtils.createValidationListener(view.getTxtNomeBanco(), () -> {
			view.mostrarStatusAguardando("AGUARDANDO NOVO TESTE...");
		}));
		view.getBtnSalvar().setEnabled(false);
	}

	private void carregarDados() {
		DatabaseConfigDTO dto = service.carregarConfiguracao();
		if (dto != null) {
			preencherView(dto);
		}
	}

	private void preencherView(DatabaseConfigDTO dto) {
		String url = dto.url();
		String semPrefixo = url.replace("jdbc:mysql://", "");
		String[] partes = semPrefixo.split("/");
		String hostPorta = partes[0];
		String banco = partes[1];

		String[] hostPortaSplit = hostPorta.split(":");
		String ip = hostPortaSplit[0];
		String porta = hostPortaSplit[1];

		view.setEnderecoIp(ip);
		view.setPorta(porta);
		view.setNomeBanco(banco);
		view.setUser(dto.user());
	}

	private String montarJdbcUrl() {
		var ip = view.getEnderecoIp();
		var porta = view.getPorta();
		var nomeBanco = view.getNomeBanco();

		return "jdbc:mysql://" + ip + ":" + porta + "/" + nomeBanco;
	}

	public void salvar() {
		char[] password = view.getPassword();
		var url = montarJdbcUrl();
		var user = view.getUser();
		try {
			DatabaseValidationUtils.validarUrl(url);
			DatabaseValidationUtils.validarUsuario(user);
			DatabaseValidationUtils.validarSenha(password);

		} catch (Exception e) {
			handleException(e, SwingUtilities.getWindowAncestor(view));
			SensitiveData.safeClear(password);
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			DatabaseConfigDTO dto = new DatabaseConfigDTO(url, user, password);
			service.saveDatabaseConfigConfiguration(dto);
			return true;
		}, sucesso -> {
			DialogFactory.informacao(view, "CONFIGURAÇÃO SALVA COM SUCESSO!");
			view.dispose();
		});
	}

	public void testar() {
		char[] password = view.getPassword();
		var url = montarJdbcUrl();
		var user = view.getUser();

		try {
			DatabaseValidationUtils.validarConfiguracaoCompleta(url, user, password);
		} catch (Exception e) {
			handleException(e, SwingUtilities.getWindowAncestor(view));
			SensitiveData.safeClear(password);
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			DatabaseConfigDTO dto = new DatabaseConfigDTO(url, user, password);
			return (DatabaseStatus) connectionService.testarConfiguracao(dto);
		}, status -> {
			if (status.available()) {
				atualizarStatusVisual(true, "Sucesso");
				DialogFactory.informacao(view, status.message());
				view.getBtnSalvar().setEnabled(true);
			} else {
				atualizarStatusVisual(false, "Falha");
				DialogFactory.erro(view, status.message());
				view.getBtnSalvar().setEnabled(false);
			}

		});
	}

	private void atualizarStatusVisual(boolean conectado, String mensagem) {
		if (conectado) {
			view.mostrarStatusConexaoOk(mensagem);

		} else {
			view.mostrarStatusConexaoErro(mensagem);
		}
	}

}
