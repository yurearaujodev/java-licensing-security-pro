package com.br.yat.gerenciador.controller;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JFormattedTextField;
import javax.swing.text.JTextComponent;

import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.validation.DocumentValidator;
import com.br.yat.gerenciador.view.empresa.DadoEmpresaPanel;

public class DadoEmpresaController {

	private final DadoEmpresaPanel view;

	public DadoEmpresaController(DadoEmpresaPanel view) {
		this.view = view;

		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getCbTipoDoc().addActionListener(e -> aplicarMascaraDocumento());
		view.getFtxDocumento().addFocusListener(createValidationListener(view.getFtxDocumento(), this::validarDocumento));
		view.getTxtInscEst().addFocusListener(createValidationListener(view.getTxtInscEst(), this::validarInscricaoEstadual));
		view.getTxtInscMun().addFocusListener(createValidationListener(view.getTxtInscMun(), this::validarInscricaoMunicipal));
	}

	private FocusAdapter createValidationListener(JTextComponent campo, Runnable validator) {
		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				ValidationUtils.removerDestaque(campo);
			}

			@Override
			public void focusLost(FocusEvent e) {
				validator.run();
			}
		};
	}

	private void aplicarMascaraDocumento() {
		String selecionado = (String) view.getCbTipoDoc().getSelectedItem();
		JFormattedTextField ftxtDocumento = view.getFtxDocumento();

		if ("SELECIONE".equals(selecionado)) {
			ftxtDocumento.setFormatterFactory(null);
			ftxtDocumento.setValue(null);
			ftxtDocumento.setText("");
			ftxtDocumento.setEnabled(false);
			view.getCbTipoDoc().requestFocusInWindow();
			return;
		}

		ftxtDocumento.setValue(null);
		ftxtDocumento.setText("");
		ftxtDocumento.setEnabled(true);

		String mascara = MaskFactory.createMask().get(selecionado);
		// System.out.println("sel: "+selecionado+"|mask: "+mascara);
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
			ValidationUtils.exibirErro(view.getTxtInscMun(),view, "INSCRIÇÃO MUNICIPAL INVÁLIDA. USE 7 A 15 DÍGITOS OU ISENTO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getTxtInscMun());
	}

	private void validarInscricaoEstadual() {
		if (!DocumentValidator.isValidInscricaoEstadual(view.getTxtInscEst().getText())) {
			ValidationUtils.exibirErro(view.getTxtInscEst(),view, "INSCRIÇÃO ESTADUAL INVÁLIDA. USE 9 A 14 DÍGITOS OU ISENTO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getTxtInscEst());
	}

}
