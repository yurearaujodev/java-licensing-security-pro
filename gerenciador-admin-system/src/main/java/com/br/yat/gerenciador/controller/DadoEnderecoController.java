package com.br.yat.gerenciador.controller;

import java.awt.EventQueue;

import com.br.yat.gerenciador.util.CepUtils;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoEnderecoPanel;

public class DadoEnderecoController {

	private final DadoEnderecoPanel view;

	public DadoEnderecoController(DadoEnderecoPanel view) {
		this.view = view;

		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getFtxCep().addActionListener(e->preencherEnderecoPorCep());
	}
	
	private void preencherEnderecoPorCep() {
		String cep = view.getFtxCep().getText().trim();
		
		CepUtils.searchCep(cep).thenAccept(optEndereco -> {
			EventQueue.invokeLater(() -> {
				if (optEndereco.isPresent()) {
					var e = optEndereco.get();
					view.setLogradouro(e.getLogradouroEndereco());
					view.setComplemento(e.getComplementoEndereco());
					view.setBairro(e.getBairroEndereco());
					view.setCidade(e.getCidadeEndereco());
					view.setEstado(e.getEstadoEndereco());
					view.setPais(e.getPaisEndereco().toUpperCase());
				} else {
					ValidationUtils.exibirErro(view.getFtxCep(), view, "CEP NÃO ENCONTRADO.");
					view.limparCampos();
					view.getFtxCep().requestFocusInWindow();
				}
				ValidationUtils.removerDestaque(view.getFtxCep());
			});
		});

	}

}
