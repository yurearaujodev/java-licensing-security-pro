package com.br.yat.gerenciador.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.view.empresa.DadoPrincipalPanel;

public class DadoPrincipalController {

	private final DadoPrincipalPanel view;
	private final EmpresaService service;
	private final Map<String, String> mascaras = MaskFactory.createMask();
	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd/MM/uuuu")
			.toFormatter().withResolverStyle(ResolverStyle.STRICT);
	private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
	private static final DecimalFormat DECIMAL_FORMATTER;
	private Empresa empresaAtual;

	static {
		var symbols = DecimalFormatSymbols.getInstance(LOCALE_BR);
		DECIMAL_FORMATTER = new DecimalFormat("#,##0.00", symbols);
		DECIMAL_FORMATTER.setParseBigDecimal(true);
	}

	public DadoPrincipalController(DadoPrincipalPanel view,EmpresaService service) {
		this.view = view;
		this.service=service;
		configurarFiltro();
		registrarAcoes();
		aplicarMascaraDocumento();
	}

	private void configurarFiltro() {
		ValidationUtils.createDocumentFilter(view.getTxtRazaoSocial(), view.getTxtFantasia(),
				view.getTxtInscricaoEstadual(), view.getTxtInscricaoMunicipal());
	}

	private void registrarAcoes() {
		view.getCbTipoDocumento().addActionListener(e -> aplicarMascaraDocumento());
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
		var tipo = view.getTipoDocumento();
		var campo = view.getFtxtDocumento();

		boolean isSelecionado = tipo != null && !"SELECIONE".equals(tipo);
		campo.setEnabled(isSelecionado);
		if (!isSelecionado) {
			FormatterUtils.clearMask(campo);
			campo.setValue(null);
			campo.setText("");
			return;
		}

		var mascara = mascaras.get(tipo);
		if (mascara != null) {
			FormatterUtils.applyDocumentMask(campo, mascara);
		}
	}

	private void validarDocumento() {
		try {
			Empresa mock = new Empresa();
			mock.setTipoDocEmpresa(view.getTipoDocumento());
			mock.setDocumentoEmpresa(view.getDocumento());
			
			service.validarEmpresa(mock);
			ValidationUtils.removerDestaque(view.getFtxtDocumento());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtDocumento(), e.getMessage());
		}
	}

	private void validarRazaoSocial() {
		String texto = view.getRazaoSocial();
		if(ValidationUtils.isEmpty(texto))return;
		try {
			Empresa mock = new Empresa();
			mock.setRazaoSocialEmpresa(texto);

			service.validarEmpresa(mock);
			ValidationUtils.removerDestaque(view.getTxtRazaoSocial());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtRazaoSocial(), e.getMessage());
		}
	}

	private void validarInscricaoMunicipal() {
		try {
			Empresa mock = new Empresa();
			mock.setInscMun(view.getInscricaoMunicipal());

			service.validarEmpresa(mock);
			ValidationUtils.removerDestaque(view.getTxtInscricaoMunicipal());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtInscricaoMunicipal(), e.getMessage());
		}
	}

	private void validarInscricaoEstadual() {
		try {
			Empresa mock = new Empresa();
			mock.setInscEst(view.getInscricaoEstadual());

			service.validarEmpresa(mock);
			ValidationUtils.removerDestaque(view.getTxtInscricaoEstadual());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtInscricaoEstadual(), e.getMessage());
		}
	}

	private void validarCapitalSocial() {
		try {
			Empresa mock = new Empresa();
			mock.setCapitalEmpresa(parseBigDecimal(view.getCapitalSocial()));

			service.validarEmpresa(mock);
			ValidationUtils.removerDestaque(view.getFtxtCapital());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtCapital(), e.getMessage());
		}
	}

	private void validarFundacao() {
		try {
			Empresa mock = new Empresa();
			mock.setFundacaoEmpresa(parseDate(view.getDataFundacao()));

			service.validarEmpresa(mock);
			ValidationUtils.removerDestaque(view.getFtxtFundacao());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtFundacao(), e.getMessage());
		}
	}

	private int parseInt(String valor) {
		if (valor == null || valor.isBlank())
			return 0;
		try {
			return Integer.parseInt(valor.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	private LocalDate parseDate(String valor) {
		if (valor == null || valor.isBlank())
			return null;
		try {
			return LocalDate.parse(valor, DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String valor) {
		if (valor == null || valor.isBlank())
			return null;
		try {
			return (BigDecimal) DECIMAL_FORMATTER.parse(valor.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(
				view.getTxtRazaoSocial(),
				view.getFtxtDocumento(),
				view.getFtxtCapital(),
				view.getFtxtFundacao()
				);
		
		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}
		
		validarRazaoSocial();
		validarDocumento();
		validarInscricaoEstadual();
		validarInscricaoMunicipal();
		validarFundacao();
		validarCapitalSocial();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtDocumento(), view.getFtxtFundacao(),
				view.getTxtInscricaoEstadual(), view.getTxtInscricaoMunicipal(), view.getFtxtCapital(),
				view.getTxtRazaoSocial());

		if (erro!=null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS COM DADOS INVÁLIDOS.");
			return false;
		}
		
		return true;
	}

	public Empresa getDados() {
		Empresa empresa = (this.empresaAtual!=null)?this.empresaAtual:new Empresa();
		empresa.setIdEmpresa(parseInt(view.getCodigo()));
		empresa.setRazaoSocialEmpresa(view.getRazaoSocial());
		empresa.setFantasiaEmpresa(view.getNomeFantasia());
		empresa.setTipoEmpresa(view.getTipoCadastro());
		empresa.setTipoDocEmpresa(view.getTipoDocumento());
		empresa.setDocumentoEmpresa(view.getDocumento());
		empresa.setInscEst(view.getInscricaoEstadual());
		empresa.setInscMun(view.getInscricaoMunicipal());
		empresa.setSituacaoEmpresa(view.getSituacao());
		empresa.setCapitalEmpresa(parseBigDecimal(view.getCapitalSocial()));
		empresa.setFundacaoEmpresa(parseDate(view.getDataFundacao()));

		return empresa;
	}

	public void setDados(Empresa empresa) {
		if (empresa == null)
			return;
		this.empresaAtual=empresa;
		view.setCodigo(String.valueOf(empresa.getIdEmpresa()));
		view.setRazaoSocial(empresa.getRazaoSocialEmpresa());
		view.setNomeFantasia(empresa.getFantasiaEmpresa());
		view.setTipoCadastro(empresa.getTipoEmpresa());
		view.setTipoDocumento(empresa.getTipoDocEmpresa());
		view.setDocumento(empresa.getDocumentoEmpresa());
		view.setInscricaoEstadual(empresa.getInscEst());
		view.setInscricaoMunicipal(empresa.getInscMun());
		view.setSituacao(empresa.getSituacaoEmpresa());
		String capitalStr = (empresa.getCapitalEmpresa() != null)
				? DECIMAL_FORMATTER.format(empresa.getCapitalEmpresa())
				: "";
		view.setCapitalSocial(capitalStr);

		view.setDataFundacao(
				empresa.getFundacaoEmpresa() != null ? empresa.getFundacaoEmpresa().format(DATE_FORMATTER) : "");
	}
}
