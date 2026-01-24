package com.br.yat.gerenciador.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.view.empresa.DadoRepresentantePanel;

public class DadoRepresentanteController {

	private final DadoRepresentantePanel view;
	private final EmpresaService service;

	public DadoRepresentanteController(DadoRepresentantePanel view, EmpresaService service) {
		this.view = view;
		this.service = service;
		registrarAcoes();
		configurarFiltros();
	}

	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtNome(), view.getTxtCargo());
	}

	private void registrarAcoes() {
		view.getTxtNome()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtNome(), this::validarNome));
		view.getFtxtCpf()
				.addFocusListener(ValidationUtils.createValidationListener(view.getFtxtCpf(), this::validarCpf));
		view.getFtxtTelefone().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtTelefone(), this::validarTelefone));
		view.getBtnAdicionar().addActionListener(e -> validarAdicionar());
		view.getBtnRemover().addActionListener(e -> removerRepresentante());
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				preencherCamposLinhaSelecionada();
			}});
	}

	private void validarNome() {
		try {
			Representante mock = new Representante();
			mock.setNomeRepresentante(view.getNome());
			service.validarRepresentante(mock);
			ValidationUtils.removerDestaque(view.getTxtNome());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtNome(), e.getMessage());
		}

	}

	private void validarCpf() {
		try {
			Representante mock = new Representante();
			mock.setCpfRepresentante(ValidationUtils.onlyNumbers(view.getCpf()));
			service.validarRepresentante(mock);
			ValidationUtils.removerDestaque(view.getFtxtCpf());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtCpf(), e.getMessage());
		}
	}

	private void validarTelefone() {
		try {
			Representante mock = new Representante();
			mock.setTelefoneRepresentante(ValidationUtils.onlyNumbers(view.getTelefone()));
			service.validarRepresentante(mock);
			ValidationUtils.removerDestaque(view.getFtxtTelefone());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtTelefone(), e.getMessage());
		}
	}

	private void removerRepresentante() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow == -1) {
			DialogFactory.informacao(view, "SELECIONE UM REPRESENTANTE NA TABELA PARA REMOVER.");
			return;
		}
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.removeRow(selectedRow);

	}

	private void validarAdicionar() {
		validarNome();
		validarCpf();
		validarTelefone();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCpf(), view.getFtxtTelefone(), view.getTxtNome());
		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS INVÁLIDOS. VERIFIQUE OS DESTAQUES EM VERMELHO.");
			return;
		}
		var model = (DefaultTableModel) view.getTabela().getModel();

		Object[] linha = { view.getNome(),view.getCpf(), view.getRg(), view.getCargo(), view.getNacionalidade(),
				view.getEstadoCivil(), view.getTelefone(), view.getEmail() };
		model.addRow(linha);
	}

	public boolean isValido() {
		if (view.getTabela().getRowCount() == 0) {
			DialogFactory.aviso(view, "A LISTA DE REPRESENTANTES NÃO PODE ESTAR VAZIA.");
			return false;
		}

		var temDadosPendentes = !ValidationUtils.isEmpty(view.getNome()) || !ValidationUtils.isEmpty(view.getCpf());
		if (temDadosPendentes) {
			var add = DialogFactory.confirmacao(view,
					"EXISTE UM REPRESENTANTE DIGITADO QUE NÃO FOI ADICIONADO. DESEJA ADICIONÁ-LO?");
			if (add) {
				validarAdicionar();
				return ValidationUtils.hasErroVisual(view.getTxtNome(), view.getFtxtCpf(),
						view.getFtxtTelefone()) == null;
			}
		}
		return true;
	}

	private void preencherCamposLinhaSelecionada() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow < 0)
			return;
		view.setNome(view.getTabela().getValueAt(selectedRow, 0).toString());
		view.setCpf(view.getTabela().getValueAt(selectedRow, 1).toString());

	}
	
	public List<Representante> getDados() {
		List<Representante> representantes = new ArrayList<>();
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			Representante r = new Representante();
			r.setNomeRepresentante((String) model.getValueAt(i, 0));
			r.setCpfRepresentante(ValidationUtils.onlyNumbers((String) model.getValueAt(i, 1)));
			r.setRgRepresentante((String) model.getValueAt(i, 2));
			r.setCargoRepresentante((String) model.getValueAt(i, 3));
			r.setNacionalidadeRepresentante((String) model.getValueAt(i, 4));
			r.setEstadoCivilRepresentante((String) model.getValueAt(i, 5));
			r.setTelefoneRepresentante(ValidationUtils.onlyNumbers((String) model.getValueAt(i, 6)));
			r.setEmailRepresentante((String) model.getValueAt(i, 7));
			representantes.add(r);
		}
		return representantes;
	}

	public void setDados(List<Representante> representantes) {
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);

		if (representantes != null) {
			representantes.forEach(r -> model.addRow(new Object[] { 
					r.getNomeRepresentante(), 
					FormatterUtils.formatValueWithMask(r.getCpfRepresentante(),MaskFactory.createMask().get("CPF")),
					r.getRgRepresentante(), 
					r.getCargoRepresentante(),
					r.getNacionalidadeRepresentante(),
					r.getEstadoCivilRepresentante(), 
					FormatterUtils.formatValueWithMask(r.getTelefoneRepresentante(),MaskFactory.createMask().get("CELULAR")),
					r.getEmailRepresentante() }));
		}
	}
}
