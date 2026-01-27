package com.br.yat.gerenciador.controller.empresa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.enums.TipoConta;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoBancarioPanel;

public class DadoBancarioController {

	private final DadoBancarioPanel view;

	public DadoBancarioController(DadoBancarioPanel view) {
		this.view = view;
		configurarFiltros();
		registrarAcoes();
		configurarFiltrosNumerico();
	}

	private void registrarAcoes() {
		view.getFtxtCodBanco().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtCodBanco(), this::validarCodigoBanco));
		view.getTxtBanco()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtBanco(), this::validarBanco));
		view.getTxtAgencia()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtAgencia(), this::validarAgencia));
		view.getTxtConta()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtConta(), this::validarConta));
		view.getBtnAdicionar().addActionListener(e -> adicionarBanco());
		view.getBtnRemover().addActionListener(e -> removerBanco());
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				preencherCamposLinhaSelecionada();
			}
		});
	}

	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtBanco());
	}

	private void configurarFiltrosNumerico() {
		ValidationUtils.createDocumentFilterNumeric(view.getTxtAgencia(), view.getTxtConta());
	}

	private void validarCodigoBanco() {
		try {
			var codigo = ValidationUtils.onlyNumbers(view.getCodigoBanco());
			if (ValidationUtils.isEmpty(codigo))
				return;

			EmpresaValidationUtils.validarCodigoBanco(ValidationUtils.parseInt(codigo));

			ValidationUtils.removerDestaque(view.getFtxtCodBanco());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtCodBanco(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtCodBanco(), "ERRO NA VALIDAÇÃO");
		}

	}

	private void validarBanco() {
		try {
			EmpresaValidationUtils.validarNomeBanco(view.getBanco());

			ValidationUtils.removerDestaque(view.getTxtBanco());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtBanco(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtBanco(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarAgencia() {
		var agencia = view.getAgencia();
		if (agencia.length() < 4) {
			ValidationUtils.exibirErro(view.getTxtAgencia(), "DIGÍTE PELO MENOS 4 NÚMEROS.");
		}
		try {
			EmpresaValidationUtils.validarAgencia(agencia);

			ValidationUtils.removerDestaque(view.getTxtAgencia());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtAgencia(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtAgencia(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarConta() {
		var conta = view.getConta();
		if (conta.length() < 6) {
			ValidationUtils.exibirErro(view.getTxtConta(), "DIGÍTE PELO MENOS 6 NÚMEROS.");
		}
		try {
			EmpresaValidationUtils.validarConta(conta);

			ValidationUtils.removerDestaque(view.getTxtConta());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtConta(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtConta(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void preencherCamposLinhaSelecionada() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow < 0)
			return;
		view.setCodigoBanco((String) view.getTabela().getValueAt(selectedRow, 0));
		view.setBanco((String) view.getTabela().getValueAt(selectedRow, 1));
		view.setAgencia((String) view.getTabela().getValueAt(selectedRow, 2));
		view.setConta((String) view.getTabela().getValueAt(selectedRow, 3));
		view.setTipoConta((TipoConta) view.getTabela().getValueAt(selectedRow, 4));
	}

	private void removerBanco() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow == -1) {
			DialogFactory.informacao(view, "SELECIONE UM BANCO NA TABELA PARA REMOVER");
			return;
		}
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.removeRow(selectedRow);
	}

	private void adicionarBanco() {
		var agencia = view.getAgencia();
		var conta = view.getConta();

		if (ValidationUtils.temCamposVazios(view.getFtxtCodBanco(), view.getTxtBanco())) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return;
		}

		validarCodigoBanco();
		validarBanco();
		validarAgencia();
		validarConta();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCodBanco(), view.getTxtBanco());
		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS INVÁLIDOS. VERIFIQUE OS DESTAQUES EM VERMELHO.");
			return;
		}

		if (BancoJaExiste(agencia, conta)) {
			DialogFactory.aviso(view, "ESTE BANCO JÁ FOI ADICIONADO.");
			return;
		}

		var model = (DefaultTableModel) view.getTabela().getModel();

		var codigoLimpo = ValidationUtils.onlyNumbers(view.getCodigoBanco());
		var codigoFormatado = String.format("%03d", Integer.parseInt(codigoLimpo));

		Object[] linha = { codigoFormatado, view.getBanco(), view.getAgencia(), view.getConta(), view.getTipoConta() };
		model.addRow(linha);
	}

	private boolean BancoJaExiste(String agencia, String conta) {
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			if (Objects.equals(model.getValueAt(i, 2), agencia) && Objects.equals(model.getValueAt(i, 3), conta)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValido() {
		if (view.getTabela().getRowCount() == 0) {
			ValidationUtils.exibirErro(view.getTabela(), "ADICIONE PELO MENOS UM BANCO.");
			DialogFactory.aviso(view, "A LISTA DE BANCOS NÃO PODE ESTAR VAZIA.");
			return false;
		}

		if (!ValidationUtils.isEmpty(view.getBanco()) || !ValidationUtils.isEmpty(view.getCodigoBanco())
				|| !ValidationUtils.isEmpty(view.getAgencia()) || !ValidationUtils.isEmpty(view.getConta())) {
			boolean adicionarAgora = DialogFactory.confirmacao(view,
					"EXISTEM DADOS BANCÁRIOS DIGITADOS QUE NÃO FOI ADICIONADO. DESEJA ADICIONÁ-LO?");
			if (adicionarAgora) {
				adicionarBanco();
				JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCodBanco(), view.getTxtBanco());
				if (erro != null) {
					return false;
				}
			}
		}
		return true;
	}

	public void limpar() {
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
	}

	public List<Banco> getDados() {
		List<Banco> bancos = new ArrayList<>();
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			Banco b = new Banco();
			var codigo = ValidationUtils.onlyNumbers((String) model.getValueAt(i, 0));
			b.setCodBanco(Integer.parseInt(codigo));
			b.setNomeBanco((String) model.getValueAt(i, 1));
			b.setAgenciaBanco((String) model.getValueAt(i, 2));
			b.setContaBanco((String) model.getValueAt(i, 3));
			b.setTipoBanco((TipoConta) model.getValueAt(i, 4));
			bancos.add(b);
		}
		return bancos;
	}

	public void setDados(List<Banco> bancos) {
		var model = (DefaultTableModel) view.getTabela().getModel();
		if (bancos != null) {
			for (Banco b : bancos) {
				String codFormatado = String.format("%03d", b.getCodBanco());
				model.addRow(new Object[] { codFormatado, b.getNomeBanco(), b.getAgenciaBanco(), b.getContaBanco(),
						b.getTipoBanco() });
			}

		}

	}
}
