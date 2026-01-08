package com.br.yat.gerenciador.controller;

import java.util.Map;

import javax.swing.JFormattedTextField;

import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.validation.DocumentValidator;
import com.br.yat.gerenciador.util.validation.FormatValidator;
import com.br.yat.gerenciador.view.empresa.DadoEmpresaPanel;

public class DadoEmpresaController {

	private final DadoEmpresaPanel view;
	Map<String, String> mascaras = MaskFactory.createMask();

	public DadoEmpresaController(DadoEmpresaPanel view) {
		this.view = view;

		registrarAcoes();
		aplicarMascaraDocumento();
	}

	private void registrarAcoes() {
		view.getCbTipoDoc().addActionListener(e -> aplicarMascaraDocumento());
		view.getFtxDocumento().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxDocumento(), this::validarDocumento));
		view.getTxtInscEst().addFocusListener(
				ValidationUtils.createValidationListener(view.getTxtInscEst(), this::validarInscricaoEstadual));
		view.getTxtInscMun().addFocusListener(
				ValidationUtils.createValidationListener(view.getTxtInscMun(), this::validarInscricaoMunicipal));
		view.getFtxCapitalSocial().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxCapitalSocial(), this::validarCapitalSocial));
		view.getFtxFundacao().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxFundacao(), this::validarFundacao));
	}

	private void aplicarMascaraDocumento() {
		String selecionado = (String) view.getCbTipoDoc().getSelectedItem();
		JFormattedTextField ftxtDocumento = view.getFtxDocumento();

		boolean isSelecionado = selecionado != null && !"SELECIONE".equals(selecionado);
		ftxtDocumento.setEnabled(isSelecionado);
		if (!isSelecionado) {
			ftxtDocumento.setValue(null);
			ftxtDocumento.setText("");
			view.getCbTipoDoc().requestFocusInWindow();
			return;
		}

		String mascara = mascaras.get(selecionado);
		if (mascara != null) {
			FormatterUtils.applyDocumentMask(ftxtDocumento, mascara);
		}
	}

	private void validarDocumento() {
		String doc = view.getFtxDocumento().getText();
		String tipo = (String) view.getCbTipoDoc().getSelectedItem();

		if ("SELECIONE".equals(tipo)) {
			return;
		}

		if (doc == null || doc.isBlank()) {
			ValidationUtils.exibirErro(view.getFtxDocumento(), view, "DOCUMENTO É OBRIGATÓRIO.");
			return;
		}
		String soDigitos = doc.replaceAll("\\D", "");
		boolean completo = ("CPF".equals(tipo) && soDigitos.length() == 11)
				|| ("CNPJ".equals(tipo) && soDigitos.length() == 14);

		if (completo && !DocumentValidator.isValidaCpfCnpj(soDigitos)) {
			ValidationUtils.exibirErro(view.getFtxDocumento(), view, "DOCUMENTO INVÁLIDO. VERIFIQUE CPF OU CNPJ.");
			return;
		}
		ValidationUtils.removerDestaque(view.getFtxDocumento());
	}

	private void validarInscricaoMunicipal() {
		if (!DocumentValidator.isValidInscricaoMunicipal(view.getTxtInscMun().getText())) {
			ValidationUtils.exibirErro(view.getTxtInscMun(), view,
					"INSCRIÇÃO MUNICIPAL INVÁLIDA. USE 7 A 15 DÍGITOS OU ISENTO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getTxtInscMun());
	}

	private void validarInscricaoEstadual() {
		if (!DocumentValidator.isValidInscricaoEstadual(view.getTxtInscEst().getText())) {
			ValidationUtils.exibirErro(view.getTxtInscEst(), view,
					"INSCRIÇÃO ESTADUAL INVÁLIDA. USE 9 A 14 DÍGITOS OU ISENTO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getTxtInscEst());
	}

	private void validarCapitalSocial() {
		Object valor = view.getFtxCapitalSocial().getValue();
		if (valor == null) {
			ValidationUtils.removerDestaque(view.getFtxCapitalSocial());
			return;
		}
		try {
			double capital = ((Number) valor).doubleValue();

			if (capital <= 0) {
				ValidationUtils.exibirErro(view.getFtxCapitalSocial(), view, "CAPITAL SOCIAL DEVE SER MAIOR QUE ZERO.");
				return;
			}
		} catch (NumberFormatException e) {
			ValidationUtils.exibirErro(view.getFtxCapitalSocial(), view, "FORMATO DE CAPITAL SOCIAL INVÁLIDO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getFtxCapitalSocial());
	}

	private void validarFundacao() {
		String data = view.getFtxFundacao().getText().trim();

		if (data == null || data.isBlank()) {
			ValidationUtils.exibirErro(view.getFtxFundacao(), view, "DATA DE FUNDAÇÂO É OBRIGATÓRIA.");
			return;
		}

		String soDigitos = data.replaceAll("\\D", "");
		if (soDigitos.length() != 8) {
			ValidationUtils.exibirErro(view.getFtxFundacao(), view, "DATA INCOMPLETA. USE O FORMATO dd/mm/aaaa.");
			return;
		}

		String formatada = soDigitos.substring(0, 2) + "/" + soDigitos.substring(2, 4) + "/"
				+ soDigitos.substring(4, 8);

		if (!FormatValidator.isValidFoundationDate(formatada)) {
			ValidationUtils.exibirErro(view.getFtxFundacao(), view, "DATA DE FUNDAÇÂO INVÁLIDA OU FUTURA.");
			return;
		}
		ValidationUtils.removerDestaque(view.getFtxFundacao());
	}

}
