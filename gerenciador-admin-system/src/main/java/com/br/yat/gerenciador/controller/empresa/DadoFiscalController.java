package com.br.yat.gerenciador.controller.empresa;

import javax.swing.JComponent;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.PorteEmpresa;
import com.br.yat.gerenciador.model.enums.RegimeTributario;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoFiscalPanel;

public class DadoFiscalController {

	private final DadoFiscalPanel view;

	public DadoFiscalController(DadoFiscalPanel view) {
		this.view = view;
		registrarAcoes();
		atualizarCodigoCrt();
	}

	private void registrarAcoes() {
		view.getCbRegimeTributario().addActionListener(e -> atualizarCodigoCrt());
		view.getCbCnae()
				.addFocusListener(ValidationUtils.createValidationListener(view.getCbCnae(), this::validarCnae));
		view.getCbNaturezaJuridica().addFocusListener(
				ValidationUtils.createValidationListener(view.getCbNaturezaJuridica(), this::validarNaturezaJuridica));
		view.getCbRegimeTributario().addFocusListener(
				ValidationUtils.createValidationListener(view.getCbRegimeTributario(), this::validarRegimeTributario));
	}

	private void atualizarCodigoCrt() {
		var selecionado = (RegimeTributario) view.getRegimeTributario();

		if (selecionado != null && selecionado != RegimeTributario.SELECIONE) {
			view.setCrt(String.valueOf(selecionado.getCrt()));
		} else {
			view.setCrt("");
		}
	}

	private void validarCnae() {
		try {
			EmpresaValidationUtils.validarCnae(view.getCnae());
			ValidationUtils.removerDestaque(view.getCbCnae());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getCbCnae(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getCbCnae(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarNaturezaJuridica() {
		try {
			EmpresaValidationUtils.validarNaturezaJuridica(view.getNaturezaJuridica());
			ValidationUtils.removerDestaque(view.getCbNaturezaJuridica());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getCbNaturezaJuridica(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getCbNaturezaJuridica(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarRegimeTributario() {
		try {
			EmpresaValidationUtils.validarRegimeTributario(view.getRegimeTributario(),
					ValidationUtils.parseInt(view.getCrt()));

			ValidationUtils.removerDestaque(view.getCbRegimeTributario());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getCbRegimeTributario(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getCbRegimeTributario(), "ERRO NA VALIDAÇÃO");
		}
	}

	public void limpar() {
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(view.getCbCnae(), view.getCbRegimeTributario(),
				view.getCbNaturezaJuridica());

		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}

		validarCnae();
		validarNaturezaJuridica();
		validarRegimeTributario();

		JComponent erro = ValidationUtils.hasErroVisual(view.getCbCnae(), view.getCbNaturezaJuridica(),
				view.getCbRegimeTributario());

		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS INVÁLIDOS. VERIFIQUE OS DESTAQUES EM VERMELHO.");
			return false;
		}
		return true;
	}

	public Empresa getDadosComplementar(Empresa empresa) {
		empresa.setCnaeEmpresa(view.getCnae() != null ? view.getCnae() : Cnae.SELECIONE);
		empresa.setNaturezaJuriEmpresa(view.getNaturezaJuridica());
		empresa.setPorteEmpresa(view.getPorteEmpresa());
		empresa.setRegimeTribEmpresa(view.getRegimeTributario());
		empresa.setContribuinteIcmsEmpresa(view.getContribuinteIcms());
		empresa.setCrtEmpresa(ValidationUtils.parseInt(view.getCrt()));
		return empresa;
	}

	public void setDadosComplementar(Empresa empresa) {
		if (empresa == null)
			return;

		try {
			if (empresa.getCnaeEmpresa() != null) {
				view.setCnae(empresa.getCnaeEmpresa());
			}
			if (empresa.getNaturezaJuriEmpresa() != null) {
				view.setNaturezaJuridica(empresa.getNaturezaJuriEmpresa());
			}
			if (empresa.getPorteEmpresa() != null) {
				view.setPorteEmpresa(empresa.getPorteEmpresa());
			}
			if (empresa.getRegimeTribEmpresa() != null) {
				view.setRegimeTributario(empresa.getRegimeTribEmpresa());
			}
			view.setCrt(String.valueOf(empresa.getCrtEmpresa()));
			view.setContribuinteIcms(empresa.getContribuinteIcmsEmpresa());
		} catch (IllegalArgumentException e) {
			view.setCnae(Cnae.SELECIONE);
			view.setNaturezaJuridica(NaturezaJuridica.SELECIONE);
			view.setPorteEmpresa(PorteEmpresa.SELECIONE);
			view.setRegimeTributario(RegimeTributario.SELECIONE);
			DialogFactory.erro(view, "VALOR INVÁLIDO NO BANCO DE DADOS: ", e);
		}
	}
}
