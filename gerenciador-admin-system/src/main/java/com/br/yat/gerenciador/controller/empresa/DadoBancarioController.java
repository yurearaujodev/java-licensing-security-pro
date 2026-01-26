package com.br.yat.gerenciador.controller.empresa;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.view.empresa.DadoBancarioPanel;

public class DadoBancarioController {

	private final DadoBancarioPanel view;
	private final EmpresaService service;

	public DadoBancarioController(DadoBancarioPanel view, EmpresaService service) {
		this.view = view;
		this.service = service;
		configurarFiltros();
		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getFtxtCodBanco().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtCodBanco(), this::validarCodigoBanco));
		view.getTxtBanco()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtBanco(), this::validarBanco));
		view.getBtnAdicionar().addActionListener(e -> validarAdicionar());
		view.getBtnRemover().addActionListener(e -> removerBanco());
	}

	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtBanco(), view.getTxtConta(), view.getTxtTipo());
	}

	private void validarCodigoBanco() {
		try {
			var codigo = ValidationUtils.onlyNumbers(view.getCodigoBanco());
			if(ValidationUtils.isEmpty(codigo))return;
			
			Banco mock = new Banco();
			mock.setCodBanco(Integer.parseInt(codigo));
			service.validarBanco(mock);
			ValidationUtils.removerDestaque(view.getFtxtCodBanco());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtCodBanco(), e.getMessage());
		}catch(Exception e) {
			ValidationUtils.exibirErro(view.getFtxtCodBanco(), "ERRO NA VALIDAÇÃO");
		}

	}

	private void validarBanco() {
		try {
			Banco mock = new Banco();
			mock.setNomeBanco(view.getBanco());
			service.validarBanco(mock);
			ValidationUtils.removerDestaque(view.getTxtBanco());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtBanco(), e.getMessage());
		}catch(Exception e) {
			ValidationUtils.exibirErro(view.getTxtBanco(), "ERRO NA VALIDAÇÃO");
		}
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

	private void validarAdicionar() {
		validarCodigoBanco();
		validarBanco();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCodBanco(), view.getTxtBanco());
		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS INVÁLIDOS. VERIFIQUE OS DESTAQUES EM VERMELHO.");
			return;
		}

		var model = (DefaultTableModel) view.getTabela().getModel();

		var codigoLimpo = ValidationUtils.onlyNumbers(view.getCodigoBanco());
		var codigoFormatado = String.format("%03d", Integer.parseInt(codigoLimpo));
		
		Object[] linha = { codigoFormatado, view.getBanco(),
				view.getAgencia(), view.getConta(),
				view.getTipoConta() };
		model.addRow(linha);
	}

	public boolean isValido() {
		if (view.getTabela().getRowCount() == 0) {
			DialogFactory.aviso(view, "A LISTA DE BANCOS NÃO PODE ESTAR VAZIA.");
			return false;
		}

		boolean temDadosPendentes = !ValidationUtils.isEmpty(view.getBanco())
				|| !ValidationUtils.isEmpty(view.getCodigoBanco());
		if (temDadosPendentes) {
			var add = DialogFactory.confirmacao(view,
					"EXISTEM DADOS BANCÁRIOS DIGITADOS QUE NÃO FOI ADICIONADO. DESEJA ADICIONÁ-LO?");
			if (add) {
				validarAdicionar();
				return ValidationUtils.hasErroVisual(view.getFtxtCodBanco(), view.getTxtBanco()) == null;
			}
		}
		return true;
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
			b.setTipoBanco((String) model.getValueAt(i, 4));
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
