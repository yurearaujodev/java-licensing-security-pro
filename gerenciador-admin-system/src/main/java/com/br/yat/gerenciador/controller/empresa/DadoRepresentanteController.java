package com.br.yat.gerenciador.controller.empresa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoRepresentantePanel;
import com.br.yat.gerenciador.view.factory.FormatterUtils;
import com.br.yat.gerenciador.view.factory.MaskFactory;

public class DadoRepresentanteController {

	private final DadoRepresentantePanel view;

	public DadoRepresentanteController(DadoRepresentantePanel view) {
		this.view = view;
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
		view.getBtnAdicionar().addActionListener(e -> adicionarRepresentante());
		view.getBtnRemover().addActionListener(e -> removerRepresentante());
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				preencherCamposLinhaSelecionada();
			}
		});
	}

	private void validarNome() {
		try {
			EmpresaValidationUtils.validarNomeRepresentante(view.getNome());
			ValidationUtils.removerDestaque(view.getTxtNome());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtNome(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtNome(), "ERRO NA VALIDAÇÃO");
		}

	}

	private void validarCpf() {
		try {
			EmpresaValidationUtils.validarCpfRepresentante(ValidationUtils.onlyNumbers(view.getCpf()));

			ValidationUtils.removerDestaque(view.getFtxtCpf());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtCpf(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtCpf(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarTelefone() {
		try {
			EmpresaValidationUtils.validarTelefoneRepresentante(ValidationUtils.onlyNumbers(view.getTelefone()));

			ValidationUtils.removerDestaque(view.getFtxtTelefone());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtTelefone(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtTelefone(), "ERRO NA VALIDAÇÃO");
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

	private void adicionarRepresentante() {
		var cpf = view.getCpf();
		var rg = view.getRg();

		if (ValidationUtils.temCamposVazios(view.getTxtNome(), view.getFtxtCpf(), view.getFtxtTelefone())) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return;
		}

		validarNome();
		validarCpf();
		validarTelefone();

		JComponent erro = ValidationUtils.hasErroVisual(view.getTxtNome(), view.getFtxtCpf(), view.getFtxtTelefone());
		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS COM DADOS INVÁLIDOS.");
			return;
		}

		if (representanteJaExiste(cpf, rg)) {
			DialogFactory.aviso(view, "ESTE REPRESENTANTE JÁ FOI ADICIONADO.");
			return;
		}

		var model = (DefaultTableModel) view.getTabela().getModel();

		Object[] linha = { view.getNome(), view.getCpf(), view.getRg(), view.getCargo(), view.getNacionalidade(),
				view.getEstadoCivil(), view.getTelefone(), view.getEmail() };
		model.addRow(linha);

		view.limpar();
		ValidationUtils.removerDestaque(view.getTxtNome(), view.getFtxtCpf(), view.getFtxtTelefone());
	}

	public boolean isValido() {
		if (view.getTabela().getRowCount() == 0) {
			ValidationUtils.exibirErro(view.getTabela(), "ADICIONE PELO MENOS UM REPRESENTANTE.");
			DialogFactory.aviso(view, "A LISTA DE REPRESENTANTES NÃO PODE ESTAR VAZIA.");
			return false;
		}

		if (!ValidationUtils.isEmpty(view.getNome()) || !ValidationUtils.isEmpty(view.getCpf())
				|| !ValidationUtils.isEmpty(view.getTelefone()) || !ValidationUtils.isEmpty(view.getRg())
				|| !ValidationUtils.isEmpty(view.getCargo()) || !ValidationUtils.isEmpty(view.getEmail())
				) {
			boolean adicionarAgora = DialogFactory.confirmacao(view,
					"EXISTE UM REPRESENTANTE DIGITADO QUE NÃO FOI ADICIONADO.\nDESEJA ADICIONÁ-LO AGORA?");

			if (adicionarAgora) {
				adicionarRepresentante();
				JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCpf(), view.getTxtNome(),
						view.getFtxtTelefone());
				if (erro != null) {
					return false;
				}
			}
		}
		return true;
	}

	private void preencherCamposLinhaSelecionada() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow < 0)
			return;
		view.setNome((String) view.getTabela().getValueAt(selectedRow, 0));
		view.setCpf((String) view.getTabela().getValueAt(selectedRow, 1));
		view.setRg((String) view.getTabela().getValueAt(selectedRow, 2));
		view.setCargo((String) view.getTabela().getValueAt(selectedRow, 3));
		view.setNacionalidade((String) view.getTabela().getValueAt(selectedRow, 4));
		view.setEstadoCivil((String) view.getTabela().getValueAt(selectedRow, 5));
		view.setTelefone((String) view.getTabela().getValueAt(selectedRow, 6));
		view.setEmail((String) view.getTabela().getValueAt(selectedRow, 7));
	}

	private boolean representanteJaExiste(String cpf, String rg) {
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			if (Objects.equals(model.getValueAt(i, 1), cpf) && Objects.equals(model.getValueAt(i, 2), rg)) {
				return true;
			}
		}
		return false;
	}

	public void limpar() {
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
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
			representantes
					.forEach(
							r -> model
									.addRow(new Object[] { r.getNomeRepresentante(),
											FormatterUtils.formatValueWithMask(r.getCpfRepresentante(),
													MaskFactory.createMask().get("CPF")),
											r.getRgRepresentante(), r.getCargoRepresentante(),
											r.getNacionalidadeRepresentante(), r.getEstadoCivilRepresentante(),
											FormatterUtils.formatValueWithMask(r.getTelefoneRepresentante(),
													MaskFactory.createMask().get("CELULAR")),
											r.getEmailRepresentante() }));
		}
	}
}
