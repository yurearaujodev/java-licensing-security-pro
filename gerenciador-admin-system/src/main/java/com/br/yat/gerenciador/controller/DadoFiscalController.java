package com.br.yat.gerenciador.controller;

import javax.swing.JComponent;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.PorteEmpresa;
import com.br.yat.gerenciador.model.enums.RegimeTributario;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoFiscalPanel;

public class DadoFiscalController {

	private final DadoFiscalPanel view;
	private final EmpresaService service;

	public DadoFiscalController(DadoFiscalPanel view, EmpresaService service) {
		this.view = view;
		this.service = service;
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
			Empresa mock = new Empresa();
			var cnae = view.getCnae();
			mock.setCnaeEmpresa(cnae != null ? cnae.name() : "");
			service.validarEmpresaComplementar(mock);
			ValidationUtils.removerDestaque(view.getCbCnae());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getCbCnae(), e.getMessage());
		}
	}

	private void validarNaturezaJuridica() {
		var selecionado = view.getNaturezaJuridica();
		if (selecionado == null || selecionado == NaturezaJuridica.SELECIONE) {
			ValidationUtils.exibirErro(view.getCbNaturezaJuridica(), "POR FAVOR, SELECIONE UMA OPÇÃO VÁLIDA.");
			return;
		}
		ValidationUtils.removerDestaque(view.getCbNaturezaJuridica());

	}

	private void validarRegimeTributario() {
		try {
			Empresa mock = new Empresa();
			var regime = view.getRegimeTributario();
			mock.setRegimeTribEmpresa(regime != null ? regime.name() : "");
			var crtStr = view.getCrt();
			mock.setCrtEmpresa(ValidationUtils.isEmpty(crtStr)?0:Integer.parseInt(crtStr));
			service.validarEmpresaComplementar(mock);
			ValidationUtils.removerDestaque(view.getCbRegimeTributario());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getCbRegimeTributario(), e.getMessage());
		}
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(
				view.getCbCnae(),
				view.getCbRegimeTributario(),
				view.getCbNaturezaJuridica()
				);
		
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
		empresa.setCnaeEmpresa(view.getCnae().name()!=null? view.getCnae().name():Cnae.SELECIONE.name());
		empresa.setNaturezaJuriEmpresa(view.getNaturezaJuridica().name());
		empresa.setPorteEmpresa(view.getPorteEmpresa().name());
		empresa.setRegimeTribEmpresa(view.getRegimeTributario().name());
		empresa.setContribuinteIcmsEmpresa(view.getContribuinteIcms());
		var crtValue = view.getCrt();
		empresa.setCrtEmpresa(ValidationUtils.isEmpty(crtValue)?0: Integer.parseInt(crtValue));
		return empresa;
	}

	public void setDadosComplementar(Empresa empresa) {
		if (empresa == null)
			return;

		try {
			if (empresa.getCnaeEmpresa() != null) {
				view.setCnae(Cnae.valueOf(empresa.getCnaeEmpresa()));
			}
			if (empresa.getNaturezaJuriEmpresa() != null) {
				view.setNaturezaJuridica(NaturezaJuridica.valueOf(empresa.getNaturezaJuriEmpresa()));
			}
			if (empresa.getPorteEmpresa() != null) {
				view.setPorteEmpresa(PorteEmpresa.valueOf(empresa.getPorteEmpresa()));
			}
			if (empresa.getRegimeTribEmpresa() != null) {
				view.setRegimeTributario(RegimeTributario.valueOf(empresa.getRegimeTribEmpresa()));
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
