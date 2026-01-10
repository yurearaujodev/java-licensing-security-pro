package com.br.yat.gerenciador.controller;

import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.validation.DocumentValidator;
import com.br.yat.gerenciador.util.validation.FormatValidator;
import com.br.yat.gerenciador.view.empresa.DadoRepresentantePanel;

public class DadoRepresentanteController {

	private final DadoRepresentantePanel view;

	public DadoRepresentanteController(DadoRepresentantePanel view) {
		this.view = view;

		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getFtxtCpf()
				.addFocusListener(ValidationUtils.createValidationListener(view.getFtxtCpf(), this::validarCpf));
		view.getFtxtTelefone().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtTelefone(), this::validarTelefone));
		view.getBtnAdicionar().addActionListener(e -> validarAdicionar());
		view.getBtnRemover().addActionListener(e -> removerRepresentante());
	}

	private void removerRepresentante() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow != -1) {
			DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();
			model.removeRow(selectedRow);
		} else {
			DialogFactory.informacao(view, "Selecione um representante na tabela para remover.");
		}
	}

	private void validarAdicionar() {
		adicionarTabela();
	}

	private void validarCpf() {
		String soDigitos = view.getFtxtCpf().getText().replaceAll("\\D", "");

		if (soDigitos.length() < 11) {
			view.setCpf(null);
			ValidationUtils.removerDestaque(view.getFtxtCpf());
			return;
		}

		if (!DocumentValidator.isValidaCPF(soDigitos)) {
			ValidationUtils.exibirErro(view.getFtxtCpf(), view, "CPF É INVÁLIDO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getFtxtCpf());

	}

	private void validarTelefone() {
		String soDigitos = view.getFtxtTelefone().getText().replaceAll("\\D", "");

		if (soDigitos.length() < 10) {
			view.setTelefone(null);
			ValidationUtils.removerDestaque(view.getFtxtTelefone());
			return;
		}

		if (!FormatValidator.isValidPhoneNumberBR(view.getFtxtTelefone().getText())) {
			ValidationUtils.exibirErro(view.getFtxtTelefone(), view, "TELEFONE É INVÁLIDO.");
			return;
		}
		ValidationUtils.removerDestaque(view.getFtxtTelefone());

	}

	private void adicionarTabela() {
		DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();

		Object[] linha = {
				view.getTxtNome().getText().trim().toUpperCase(), 
				view.getFtxtCpf().getText(),
				view.getTxtRg().getText().trim(), 
				view.getTxtCargo().getText().trim().toUpperCase(),
				view.getCbNacionalidade().getSelectedItem(), 
				view.getCbEstado().getSelectedItem(),
				view.getFtxtTelefone().getText(),
				view.getTxtEmail().getText().trim() };
		model.addRow(linha);

	}

}
