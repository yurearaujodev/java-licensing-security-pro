package com.br.yat.gerenciador.controller.empresa;

import java.time.LocalDate;
import java.util.Map;

import javax.swing.JComponent;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.enums.TipoDocumento;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoPrincipalPanel;

public class DadoPrincipalController {

	private final DadoPrincipalPanel view;

	private final Map<String, String> mascaras = MaskFactory.createMask();

	private Empresa empresaAtual;

	public DadoPrincipalController(DadoPrincipalPanel view) {
		this.view = view;
		configurarFiltro();
		registrarAcoes();
		aplicarMascaraDocumento();
	}

	private void configurarFiltro() {
		ValidationUtils.createDocumentFilter(view.getTxtRazaoSocial(), view.getTxtFantasia(),
				view.getTxtInscricaoEstadual(), view.getTxtInscricaoMunicipal());
	}

	private void registrarAcoes() {
		view.getCbTipoDocumento().addActionListener(e -> {
			aplicarMascaraDocumento();
			validarDocumento();
		});
		view.getFtxtDocumento().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtDocumento(), this::validarDocumento));
		view.getTxtRazaoSocial().addFocusListener(
				ValidationUtils.createValidationListener(view.getTxtRazaoSocial(), this::validarRazaoSocial));
		view.getTxtInscricaoEstadual().addFocusListener(ValidationUtils
				.createValidationListener(view.getTxtInscricaoEstadual(), this::validarInscricaoEstadual));
		view.getTxtInscricaoMunicipal().addFocusListener(ValidationUtils
				.createValidationListener(view.getTxtInscricaoMunicipal(), this::validarInscricaoMunicipal));
		view.getFtxtCapital().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtCapital(), this::validarCapitalSocial));
		view.getFtxtFundacao().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtFundacao(), this::validarFundacao));
	}

	private void aplicarMascaraDocumento() {
		TipoDocumento tipo = view.getTipoDocumento();
		var campo = view.getFtxtDocumento();

		boolean isSelecionado = tipo != null && tipo != TipoDocumento.SELECIONE;

		campo.setEnabled(isSelecionado);
		if (!isSelecionado) {
			FormatterUtils.clearMask(campo);
			campo.setValue(null);
			campo.setText("");
			return;
		}

		var mascara = mascaras.get(tipo.name());
		if (mascara != null) {
			FormatterUtils.applyDocumentMask(campo, mascara);
		}
	}

	private void validarDocumento() {
		var campo = view.getFtxtDocumento();

		var combo = view.getCbTipoDocumento();
		TipoDocumento tipo = view.getTipoDocumento();

		if (tipo == null || tipo == TipoDocumento.SELECIONE) {
			ValidationUtils.removerDestaque(campo);
			return;
		}
		ValidationUtils.removerDestaque(combo);

		var docBruto = view.getDocumento();
		var doc = ValidationUtils.onlyNumbers(docBruto);

		if (ValidationUtils.isEmpty(doc)) {
			ValidationUtils.removerDestaque(campo);
			return;
		}

		if (docBruto.contains("_")) {
			campo.setValue(null);
			campo.setText("");
			ValidationUtils.exibirErro(campo, "DOCUMENTO INCOMPLETO.");
			return;
		}

		try {
			EmpresaValidationUtils.validarDocumento(tipo, doc);

			ValidationUtils.removerDestaque(view.getFtxtDocumento());

		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtDocumento(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtDocumento(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarRazaoSocial() {
		String texto = view.getRazaoSocial();
		if (ValidationUtils.isEmpty(texto)) {
			ValidationUtils.exibirErro(view.getTxtRazaoSocial(), "CAMPO OBRIGATÓRIO.");
			return;
		}
		try {
			EmpresaValidationUtils.validarRazaoSocial(texto);
			ValidationUtils.removerDestaque(view.getTxtRazaoSocial());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtRazaoSocial(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtRazaoSocial(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarCapitalSocial() {
		try {
			var valor = ValidationUtils.parseBigDecimal(view.getCapitalSocial());

			EmpresaValidationUtils.validarCapital(valor);
			ValidationUtils.removerDestaque(view.getFtxtCapital());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtCapital(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtCapital(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarFundacao() {
		var texto = view.getDataFundacao();

		if (ValidationUtils.onlyNumbers(texto).isBlank()) {
			ValidationUtils.removerDestaque(view.getFtxtFundacao());
			return;
		}

		LocalDate dataParsed = ValidationUtils.parseDate(texto);

		if (dataParsed == null) {
			ValidationUtils.exibirErro(view.getFtxtFundacao(), "DATA INEXISTENTE NO CALENDÁRIO.");
			return;
		}

		try {
			EmpresaValidationUtils.validarFundacao(dataParsed);

			ValidationUtils.removerDestaque(view.getFtxtFundacao());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtFundacao(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtFundacao(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarInscricaoMunicipal() {
		try {
			EmpresaValidationUtils.validarInscricaoMunicipal(view.getInscricaoMunicipal());

			ValidationUtils.removerDestaque(view.getTxtInscricaoMunicipal());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtInscricaoMunicipal(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtInscricaoMunicipal(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarInscricaoEstadual() {
		try {
			EmpresaValidationUtils.validarInscricaoEstadual(view.getInscricaoEstadual());

			ValidationUtils.removerDestaque(view.getTxtInscricaoEstadual());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtInscricaoEstadual(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtInscricaoEstadual(), "ERRO NA VALIDAÇÃO");
		}
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(view.getCbTipoDocumento(),
				view.getTxtRazaoSocial(), view.getFtxtFundacao(), view.getTxtInscricaoEstadual(),
				view.getTxtInscricaoMunicipal());

		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}

		validarDocumento();
		validarRazaoSocial();
		validarInscricaoEstadual();
		validarInscricaoMunicipal();
		validarFundacao();
		validarCapitalSocial();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtDocumento(), view.getFtxtFundacao(),
				view.getTxtInscricaoEstadual(), view.getTxtInscricaoMunicipal(), view.getFtxtCapital(),
				view.getTxtRazaoSocial());

		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS COM DADOS INVÁLIDOS.");
			return false;
		}

		return true;
	}

	public void limpar() {
		this.empresaAtual = new Empresa();
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
	}

	public Empresa getDados() {
		Empresa empresa = (this.empresaAtual != null) ? this.empresaAtual : new Empresa();
		empresa.setIdEmpresa(ValidationUtils.parseInt(view.getCodigo()));
		empresa.setRazaoSocialEmpresa(view.getRazaoSocial());
		empresa.setFantasiaEmpresa(view.getNomeFantasia());
		empresa.setTipoEmpresa(view.getTipoCadastro());
		empresa.setTipoDocEmpresa(view.getTipoDocumento());
		empresa.setDocumentoEmpresa(ValidationUtils.onlyNumbers(view.getDocumento()));
		empresa.setInscEst(view.getInscricaoEstadual());
		empresa.setInscMun(view.getInscricaoMunicipal());
		empresa.setSituacaoEmpresa(view.getSituacao());
		empresa.setCapitalEmpresa(ValidationUtils.parseBigDecimal(view.getCapitalSocial()));
		empresa.setFundacaoEmpresa(ValidationUtils.parseDate(view.getDataFundacao()));

		return empresa;
	}

	public void setDados(Empresa empresa) {
		if (empresa == null)
			return;
		this.empresaAtual = empresa;
		view.setCodigo(String.valueOf(empresa.getIdEmpresa()));
		view.setRazaoSocial(empresa.getRazaoSocialEmpresa());
		view.setNomeFantasia(empresa.getFantasiaEmpresa());
		view.setTipoCadastro(empresa.getTipoEmpresa());
		view.setTipoDocumento(empresa.getTipoDocEmpresa());
		view.setDocumento(empresa.getDocumentoEmpresa());
		view.setInscricaoEstadual(empresa.getInscEst());
		view.setInscricaoMunicipal(empresa.getInscMun());
		view.setSituacao(empresa.getSituacaoEmpresa());
		view.setCapitalSocial(ValidationUtils.formatBigDecimal(empresa.getCapitalEmpresa()));
		view.setDataFundacao(ValidationUtils.formatDate(empresa.getFundacaoEmpresa()));
	}
}
