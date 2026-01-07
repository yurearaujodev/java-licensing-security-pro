package com.br.yat.gerenciador.controller;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.validation.FormatValidator;
import com.br.yat.gerenciador.view.empresa.DadoContatoPanel;

public class DadoContatoController {
	
	private final DadoContatoPanel view;
	private JComponent campoAtual;
	
	private final Map<String, Runnable> estrategiasCampo = new HashMap<>();
	private final Map<String, Runnable> estrategiasValidacao = new HashMap<>();

	public DadoContatoController(DadoContatoPanel view) {
		this.view = view;
		registrarEstrategias();
		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getCbTipo().addActionListener(e->alternarCampo());
		view.getAdicionar().addActionListener(e->validarCampoAtual());
		view.getRemoverr().addActionListener(e->removerContatoSelecionado());
	}

	private void removerContatoSelecionado() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow!=-1) {
			DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();
			model.removeRow(selectedRow);
		}else {
			DialogFactory.informacao(view, "Selecione um contato pra remover");
		}
	}

	private void validarCampoAtual() {
		String tipo = (String) view.getCbTipo().getSelectedItem();
		Runnable estrategia = estrategiasValidacao.get(tipo);
		if (estrategia!=null) {
			estrategia.run();
		}
	}

	private void alternarCampo() {
		String tipo =(String) view.getCbTipo().getSelectedItem();
		JPanel painelCampo = view.getPainelCampo();
		
		painelCampo.removeAll();
		
		Runnable estrategia = estrategiasCampo.get(tipo);
		if (estrategia!=null) {
			estrategia.run();
		}
		if (campoAtual==null) {
			campoAtual=new JLabel("Selecione um tipo de contato");
		}
		
		
		painelCampo.add(campoAtual,"growx,h 25!");
		painelCampo.revalidate();
		painelCampo.repaint();
	}

	private void registrarEstrategias() {
		estrategiasCampo.put("FIXO", ()->usarTelefone("FIXO"));
		estrategiasCampo.put("CELULAR", ()->usarTelefone("CELULAR"));
		estrategiasCampo.put("WHATSAPP", ()->usarTelefone("WHATSAPP"));
		estrategiasCampo.put("E-MAIL", this::usarGenerico);
		estrategiasCampo.put("SITE", this::usarGenerico);
		estrategiasCampo.put("REDE SOCIAL", this::usarGenerico);
		
		estrategiasValidacao.put("FIXO", this::validarTelefone);
		estrategiasValidacao.put("CELULAR", this::validarTelefone);
		estrategiasValidacao.put("WHATSAPP", this::validarTelefone);	
		estrategiasValidacao.put("EMAIL", this::validarEmail);	
		estrategiasValidacao.put("SITE", this::validarGenerico);	
		estrategiasValidacao.put("REDE SOCIAL", this::validarGenerico);	
	}
	
	private void validarEmail() {
		String email = view.getTxtGenerico().getText();
		if (email==null||email.isBlank()) {
			ValidationUtils.exibirErro(view.getTxtGenerico(), view, "Email invalido");
			return;
		}
		ValidationUtils.removerDestaque(view.getTxtGenerico());
		adicionarNaTabela("EMAIL", email);
	}
	
	private void validarGenerico() {
		String valor = view.getTxtGenerico().getText();
		if (valor==null||valor.isBlank()) {
			ValidationUtils.exibirErro(view.getTxtGenerico(), view, "campo obrigatorio");
			return;
		}
		ValidationUtils.removerDestaque(view.getTxtGenerico());
		adicionarNaTabela("Outro", valor);
	}

	private void usarTelefone(String tipo) {
		campoAtual = view.getftxtTelefone();
		String mascara = MaskFactory.createMask().get(tipo);
		FormatterUtils.applyPhoneMask(view.getftxtTelefone(), mascara);
	}	
	private void usarGenerico() {
		campoAtual = view.getTxtGenerico();
	}
	
	private void validarTelefone() {
		String tipo = (String) view.getCbTipo().getSelectedItem();
		String valor = view.getftxtTelefone().getText();
		if (valor==null||valor.isBlank()||!FormatValidator.isValidPhoneNumberBR(valor)) {
			ValidationUtils.exibirErro(view.getftxtTelefone(),view, "Telefone inválido.");
			return;
		}
		ValidationUtils.removerDestaque(view.getftxtTelefone());
		adicionarNaTabela(tipo , valor);
	}
	
	private void adicionarNaTabela(String tipo, String valor) {
		DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();
		model.addRow(new Object[] {tipo,valor});
	}
	
}
