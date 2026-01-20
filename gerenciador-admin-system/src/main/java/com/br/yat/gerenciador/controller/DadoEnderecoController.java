package com.br.yat.gerenciador.controller;

import java.awt.EventQueue;

import javax.swing.JComponent;

import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.CepUtils;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoEnderecoPanel;

public class DadoEnderecoController {

	private final DadoEnderecoPanel view;
	private final EmpresaService service;
	private String ultimoCepBuscado = "";
	private Endereco enderecoAtual;

	public DadoEnderecoController(DadoEnderecoPanel view, EmpresaService service) {
		this.view = view;
		this.service = service;
		configurarFiltro();
		registrarAcoes();
	}

	private void configurarFiltro() {
		ValidationUtils.createDocumentFilter(view.getTxtLogradouro(), view.getTxtComplemento(), view.getTxtBairro(),
				view.getTxtCidade(), view.getTxtEstado(), view.getTxtPais());
	}

	private void registrarAcoes() {
		view.getFtxtCep().addActionListener(e -> preencherEnderecoPorCep());
		view.getFtxtCep().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtCep(), this::preencherEnderecoPorCep));
		view.getTxtLogradouro().addFocusListener(
				ValidationUtils.createValidationListener(view.getTxtLogradouro(), this::validarLogradouro));
		view.getTxtCidade()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtCidade(), this::validarCidade));
		view.getTxtEstado()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtEstado(), this::validarEstado));
		view.getTxtPais()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtPais(), this::validarPais));
	}

	private void preencherEnderecoPorCep() {
		String cep = ValidationUtils.onlyNumbers(view.getCep());

		try {
			Endereco mock = new Endereco();
			mock.setCepEndereco(cep);

			service.validarEndereco(mock);

			ValidationUtils.removerDestaque(view.getFtxtCep());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtCep(), e.getMessage());
			return;
		}

		if (cep.equals(ultimoCepBuscado)) {
			return;
		}

		view.setLogradouro("BUSCANDO...");

		CepUtils.searchCep(cep).thenAccept(optEndereco -> {
			EventQueue.invokeLater(() -> {
				if (optEndereco.isPresent()) {
					var e = optEndereco.get();
					ultimoCepBuscado = cep;

					setDados(e);
					ValidationUtils.removerDestaque(view.getFtxtCep());
				} else {
					ultimoCepBuscado = null;
					ValidationUtils.exibirErro(view.getFtxtCep(), "CEP NÃO ENCONTRADO.");
					view.setLogradouro("");
					view.getFtxtCep().requestFocusInWindow();
				}
			});
		});
	}

	private void validarLogradouro() {
		try {
			Endereco mock = new Endereco();
			mock.setLogradouroEndereco(view.getLogradouro());
			service.validarEndereco(mock);
			ValidationUtils.removerDestaque(view.getTxtLogradouro());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtLogradouro(), e.getMessage());
		}
	}

	private void validarCidade() {
		try {
			Endereco mock = new Endereco();
			mock.setCidadeEndereco(view.getCidade());
			service.validarEndereco(mock);
			ValidationUtils.removerDestaque(view.getTxtCidade());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtCidade(), e.getMessage());
		}
	}

	private void validarEstado() {
		try {
			Endereco mock = new Endereco();
			mock.setEstadoEndereco(view.getEstado());
			service.validarEndereco(mock);
			ValidationUtils.removerDestaque(view.getTxtEstado());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtEstado(), e.getMessage());
		}
	}

	private void validarPais() {
		try {
			Endereco mock = new Endereco();
			mock.setPaisEndereco(view.getPais());
			service.validarEndereco(mock);
			ValidationUtils.removerDestaque(view.getTxtPais());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtPais(), e.getMessage());
		}
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(view.getFtxtCep(), view.getTxtLogradouro(),
				view.getTxtCidade(), view.getTxtEstado(), view.getTxtPais());

		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}

		validarLogradouro();
		validarCidade();
		validarEstado();
		validarPais();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCep(), view.getTxtLogradouro(), view.getTxtCidade(),
				view.getTxtEstado(), view.getTxtPais());

		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS COM DADOS INVÁLIDOS.");
			return false;
		}

		return true;
	}

	public Endereco getDados() {
		Endereco endereco = (this.enderecoAtual != null) ? this.enderecoAtual : new Endereco();

		endereco.setCepEndereco(ValidationUtils.onlyNumbers(view.getCep()));
		endereco.setLogradouroEndereco(view.getLogradouro());
		endereco.setComplementoEndereco(view.getComplemento());
		endereco.setBairroEndereco(view.getBairro());
		endereco.setNumeroEndereco(view.getNumero());
		endereco.setCidadeEndereco(view.getCidade());
		endereco.setEstadoEndereco(view.getEstado());
		endereco.setPaisEndereco(view.getPais());

		return endereco;
	}

	public void setDados(Endereco endereco) {
		if (endereco == null)
			return;
		this.enderecoAtual =endereco;
		view.setCep(endereco.getCepEndereco());
		view.setLogradouro(endereco.getLogradouroEndereco());
		view.setComplemento(endereco.getComplementoEndereco());
		view.setBairro(endereco.getBairroEndereco());
		view.setNumero(endereco.getNumeroEndereco());
		view.setCidade(endereco.getCidadeEndereco());
		view.setEstado(endereco.getEstadoEndereco());
		view.setPais(endereco.getPaisEndereco());
	}

}
