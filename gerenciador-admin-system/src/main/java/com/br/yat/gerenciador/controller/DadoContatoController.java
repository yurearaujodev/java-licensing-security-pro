package com.br.yat.gerenciador.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.view.empresa.DadoContatoPanel;

public class DadoContatoController {

	private final DadoContatoPanel view;
	private final EmpresaService service;
	private final Map<String, Runnable> estrategiasConfiguracao = new HashMap<>();
	private final Map<String, String> mascaras = MaskFactory.createMask();

	public DadoContatoController(DadoContatoPanel view, EmpresaService service) {
		this.view = view;
		this.service = service;
		registrarEstrategias();
		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getCbTipoContato().addActionListener(e -> alternarConfiguracao());
		view.getAdicionar().addActionListener(e -> adicionarContato());
		view.getRemover().addActionListener(e -> removerContatoSelecionado());
		view.getFtxtContato().addFocusListener(
				ValidationUtils.createValidationListener(view.getFtxtContato(), this::validarTelefone));
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				preencherCamposLinhaSelecionada();
			}
		});
	}

	private void registrarEstrategias() {
		estrategiasConfiguracao.put("FIXO", () -> aplicarMascara("FIXO"));
		estrategiasConfiguracao.put("CELULAR", () -> aplicarMascara("CELULAR"));
		estrategiasConfiguracao.put("WHATSAPP", () -> aplicarMascara("WHATSAPP"));
		estrategiasConfiguracao.put("E-MAIL", this::limparMascara);
		estrategiasConfiguracao.put("SITE", this::limparMascara);
		estrategiasConfiguracao.put("REDE SOCIAL", this::limparMascara);

	}

	private void alternarConfiguracao() {
		var tipo = view.getTipoContato();
		var campo = view.getFtxtContato();

		ValidationUtils.removerDestaque(campo);

		if ("SELECIONE".equalsIgnoreCase(tipo) || tipo == null) {
			campo.setEnabled(false);
			limparMascara();
			return;
		}
		campo.setEnabled(true);
		var config = estrategiasConfiguracao.get(tipo);
		if (config != null)
			config.run();
		campo.requestFocusInWindow();
	}

	private void aplicarMascara(String tipo) {
		FormatterUtils.applyPhoneMask(view.getFtxtContato(), mascaras.get(tipo));
	}

	private void limparMascara() {
		FormatterUtils.clearMask(view.getFtxtContato());
		view.setContato("");
	}

	private void validarTelefone() {
		var tipo = view.getTipoContato();
		var valor = view.getContato();
		if ("SELECIONE".equals(tipo) || ValidationUtils.isEmpty(valor)) {
			return;
		}
		try {
			Contato mock = new Contato();
			mock.setTipoContato(tipo);
			mock.setValorContato(valor);

			service.validarContatoIndividual(mock);

			ValidationUtils.removerDestaque(view.getFtxtContato());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getFtxtContato(), e.getMessage());
		}

	}

	private void adicionarContato() {
		var tipo = view.getTipoContato();
		var valor = view.getContato();

		if ("SELECIONE".equals(tipo)) {
			DialogFactory.aviso(view, "SELECIONE UM TIPO DE CONTATO.");
			return;
		}

		validarTelefone();

		if (ValidationUtils.isHighLighted(view.getFtxtContato())) {
			return;
		}

		if (contatoJaExiste(tipo, valor)) {
			DialogFactory.aviso(view, "ESTE CONTATO JÁ FOI ADICIONADO.");
			return;
		}

		var model = (DefaultTableModel) view.getTabela().getModel();
		model.addRow(new Object[] { tipo, valor });

		view.setContato("");
		view.getFtxtContato().setValue(null);
		ValidationUtils.removerDestaque(view.getFtxtContato());

	}

	private boolean contatoJaExiste(String tipo, String valor) {
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			if (model.getValueAt(i, 0).equals(tipo) && model.getValueAt(i, 1).equals(valor)) {
				return true;
			}
		}
		return false;
	}

	private void removerContatoSelecionado() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow < 0) {
			DialogFactory.informacao(view, "SELECIONE UM CONTATO PRA REMOVER");
			return;
		}
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.removeRow(selectedRow);
	}

	private void preencherCamposLinhaSelecionada() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow < 0)
			return;

		String tipo = view.getTabela().getValueAt(selectedRow, 0).toString();
		String valor = view.getTabela().getValueAt(selectedRow, 1).toString();

		view.setTipoContato(tipo);
		view.setContato(valor);
	}

	public boolean isValido() {
		var model = (DefaultTableModel) view.getTabela().getModel();

		if (model.getRowCount() == 0) {
			ValidationUtils.exibirErro(view.getCbTipoContato(), "ADICIONE PELO MENOS UM CONTATO.");
			DialogFactory.aviso(view, "A LISTA DE CONTATOS NÃO PODE ESTAR VAZIA.");
			return false;
		}

		if (!ValidationUtils.isEmpty(view.getContato())) {
			boolean adicionarAgora = DialogFactory.confirmacao(view,
					"EXISTE UM CONTATO DIGITADO QUE NÃO FOI ADICIONADO.\nDESEJA ADICIONÁ-LO AGORA?");

			if (adicionarAgora) {
				adicionarContato();
				if (ValidationUtils.isHighLighted(view.getFtxtContato())) {
					return false;
				}
			}
		}

		ValidationUtils.removerDestaque(view.getCbTipoContato());
		return true;
	}

	public List<Contato> getDados() {
		List<Contato> contatos = new ArrayList<>();
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			Contato contato = new Contato();
			var tipo = model.getValueAt(i, 0).toString();
			var valor = model.getValueAt(i, 1).toString();
			contato.setTipoContato(tipo);
			if (tipo.equals("FIXO") || tipo.equals("CELULAR") || tipo.equals("WHATSAPP")) {
				contato.setValorContato(ValidationUtils.onlyNumbers(valor));
			} else {
				contato.setValorContato(valor);
			}

			contatos.add(contato);
		}

		return contatos;
	}

	public void setDados(List<Contato> contatos) {
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);

		if (contatos != null) {
			contatos.forEach(c -> model.addRow(new Object[] { c.getTipoContato(), c.getValorContato() }));
		}
	}

}
