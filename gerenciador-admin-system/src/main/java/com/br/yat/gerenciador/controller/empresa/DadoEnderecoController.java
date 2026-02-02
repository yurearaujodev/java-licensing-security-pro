package com.br.yat.gerenciador.controller.empresa;

import java.awt.EventQueue;

import javax.swing.JComponent;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.service.CepUtils;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoEnderecoPanel;

public class DadoEnderecoController {

	private final DadoEnderecoPanel view;
	private String ultimoCepBuscado = "";
	private Endereco enderecoAtual;

	public DadoEnderecoController(DadoEnderecoPanel view) {
		this.view = view;
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
		view.getTxtBairro()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtBairro(), this::validarBairro));
		view.getTxtCidade()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtCidade(), this::validarCidade));
		view.getTxtEstado()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtEstado(), this::validarEstado));
		view.getTxtPais()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtPais(), this::validarPais));
	}

	private void preencherEnderecoPorCep() {
		String cep = ValidationUtils.onlyNumbers(view.getCep());

		if (ValidationUtils.isEmpty(cep) || cep.equals(ultimoCepBuscado)) {
			return;
		}

		try {
			EmpresaValidationUtils.validarCep(cep);
			ValidationUtils.removerDestaque(view.getFtxtCep());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtCep(), e.getMessage());
			return;
		}

		if (cep.equals(ultimoCepBuscado)) {
			return;
		}

		buscandoCep();

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
					view.limpar();
					view.getFtxtCep().requestFocusInWindow();
				}
			});
		});
	}

	private void buscandoCep() {
		view.setLogradouro("BUSCANDO...");
		view.setBairro("BUSCANDO...");
		view.setCidade("BUSCANDO...");
		view.setEstado("BUSCANDO...");
		view.setPais("BUSCANDO...");
	}

	private void validarLogradouro() {
		try {
			EmpresaValidationUtils.validarLogradouro(view.getLogradouro());

			ValidationUtils.removerDestaque(view.getTxtLogradouro());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtLogradouro(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtLogradouro(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarBairro() {
		try {
			EmpresaValidationUtils.validarBairro(view.getBairro());

			ValidationUtils.removerDestaque(view.getTxtBairro());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtBairro(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtBairro(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarCidade() {
		try {
			EmpresaValidationUtils.validarCidade(view.getCidade());

			ValidationUtils.removerDestaque(view.getTxtCidade());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtCidade(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtCidade(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarEstado() {
		try {
			EmpresaValidationUtils.validarEstado(view.getEstado());

			ValidationUtils.removerDestaque(view.getTxtEstado());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtEstado(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtEstado(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarPais() {
		try {
			EmpresaValidationUtils.validarPais(view.getPais());

			ValidationUtils.removerDestaque(view.getTxtPais());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtPais(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtPais(), "ERRO NA VALIDAÇÃO");
		}
	}

	public void limpar() {
		this.enderecoAtual = new Endereco();
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(view.getFtxtCep(), view.getTxtLogradouro(),
				view.getTxtBairro(), view.getTxtCidade(), view.getTxtEstado(), view.getTxtPais());

		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}

		validarLogradouro();
		validarBairro();
		validarCidade();
		validarEstado();
		validarPais();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtCep(), view.getTxtLogradouro(), view.getTxtBairro(),
				view.getTxtCidade(), view.getTxtEstado(), view.getTxtPais());

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
		this.enderecoAtual = endereco;

		this.ultimoCepBuscado = ValidationUtils.onlyNumbers(endereco.getCepEndereco());

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
