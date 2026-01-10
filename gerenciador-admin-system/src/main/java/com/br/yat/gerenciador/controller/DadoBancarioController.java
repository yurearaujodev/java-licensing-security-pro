package com.br.yat.gerenciador.controller;

import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.empresa.DadoBancarioPanel;

public class DadoBancarioController {
	
	private DadoBancarioPanel view;
	
	public DadoBancarioController(DadoBancarioPanel view) {
		this.view = view;
		
		registrarAcoes();
	}
	
	private void registrarAcoes() {
		view.getBtnAdicionar().addActionListener(e -> validarAdicionar());
		view.getBtnRemover().addActionListener(e -> removerBanco());
	}
	
	private void removerBanco() {
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
	
	private void adicionarTabela() {
		DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();

		Object[] linha = {
				view.getFtxtCodBanco().getText(),
				view.getTxtBanco().getText().trim().toUpperCase(), 
				view.getTxtAgencia().getText().trim(), 
				view.getTxtConta().getText().trim().toUpperCase(),
				view.getTxtTipo().getText().trim() };
		model.addRow(linha);

	}

}
