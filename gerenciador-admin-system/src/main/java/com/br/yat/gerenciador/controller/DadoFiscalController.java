package com.br.yat.gerenciador.controller;

import com.br.yat.gerenciador.model.enums.RegimeTributario;
import com.br.yat.gerenciador.view.empresa.DadoFiscalPanel;

public class DadoFiscalController {
	
	private final DadoFiscalPanel view;

	public DadoFiscalController(DadoFiscalPanel view) {
		this.view = view;

		registrarAcoes();
		atualizarCodigoCrt();
	}

	private void registrarAcoes() {
		view.getCbRegTri().addActionListener(e-> atualizarCodigoCrt());
	}

	private void atualizarCodigoCrt() {
		RegimeTributario selecionado = (RegimeTributario) view.getCbRegTri().getSelectedItem();
		
		if (selecionado != null) {
			view.setCrt(selecionado.getCrt());
		}else {
			view.setCrt(null);
		}
	}

}
