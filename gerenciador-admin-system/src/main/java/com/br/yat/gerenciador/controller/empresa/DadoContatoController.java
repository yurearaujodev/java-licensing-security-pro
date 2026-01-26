package com.br.yat.gerenciador.controller.empresa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.enums.TipoContato;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoContatoPanel;

public class DadoContatoController {

	private final DadoContatoPanel view;
	private final Map<TipoContato, Runnable> estrategiasConfiguracao = new HashMap<>();
	private final Map<String, String> mascaras = MaskFactory.createMask();

	public DadoContatoController(DadoContatoPanel view) {
		this.view = view;
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
		estrategiasConfiguracao.put(TipoContato.FIXO, () -> aplicarMascara(TipoContato.FIXO));
		estrategiasConfiguracao.put(TipoContato.CELULAR, () -> aplicarMascara(TipoContato.CELULAR));
		estrategiasConfiguracao.put(TipoContato.WHATSAPP, () -> aplicarMascara(TipoContato.WHATSAPP));
		estrategiasConfiguracao.put(TipoContato.EMAIL, this::limparMascara);
		estrategiasConfiguracao.put(TipoContato.SITE, this::limparMascara);
		estrategiasConfiguracao.put(TipoContato.REDESOCIAL, this::limparMascara);

	}

	private void alternarConfiguracao() {
		TipoContato tipo = view.getTipoContato();
		var campo = view.getFtxtContato();

		ValidationUtils.removerDestaque(campo);

		if (tipo == null || tipo == TipoContato.SELECIONE) {
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

	private void aplicarMascara(TipoContato tipo) {
		FormatterUtils.applyPhoneMask(view.getFtxtContato(), mascaras.get(tipo.name()));
	}

	private void limparMascara() {
		FormatterUtils.clearMask(view.getFtxtContato());
		view.setContato("");
	}

	private void validarTelefone() {
		var tipo = view.getTipoContato();
		var valor = view.getContato();
		if (tipo == TipoContato.SELECIONE || ValidationUtils.isEmpty(valor)) {
			return;
		}
		try {
			EmpresaValidationUtils.validarContato(tipo, valor);

			ValidationUtils.removerDestaque(view.getFtxtContato());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFtxtContato(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFtxtContato(), "ERRO NA VALIDAÇÃO");
		}

	}

	private void adicionarContato() {
		TipoContato tipo = view.getTipoContato();
		var valor = view.getContato();

		if (tipo == TipoContato.SELECIONE) {
			DialogFactory.aviso(view, "SELECIONE UM TIPO DE CONTATO.");
			return;
		}

		if (ValidationUtils.temCamposVazios(view.getFtxtContato(), view.getCbTipoContato())) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return;
		}

		validarTelefone();

		JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtContato(), view.getCbTipoContato());
		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS COM DADOS INVÁLIDOS.");
			return;
		}

		if (contatoJaExiste(tipo, valor)) {
			DialogFactory.aviso(view, "ESTE CONTATO JÁ FOI ADICIONADO.");
			return;
		}

		var model = (DefaultTableModel) view.getTabela().getModel();
		model.addRow(new Object[] { tipo, valor });

		view.limpar();
		ValidationUtils.removerDestaque(view.getFtxtContato());

	}

	private boolean contatoJaExiste(TipoContato tipo, String valor) {
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			if (Objects.equals(model.getValueAt(i, 0), tipo) && Objects.equals(model.getValueAt(i, 1), valor)) {
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

		TipoContato tipo = (TipoContato) view.getTabela().getValueAt(selectedRow, 0);
		String valor = view.getTabela().getValueAt(selectedRow, 1).toString();

		view.setTipoContato(tipo);
		view.setContato(valor);
	}

	public void limpar() {
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
	}

	public boolean isValido() {
		if (view.getTabela().getRowCount() == 0) {
			ValidationUtils.exibirErro(view.getTabela(), "ADICIONE PELO MENOS UM CONTATO.");
			DialogFactory.aviso(view, "A LISTA DE CONTATOS NÃO PODE ESTAR VAZIA.");
			return false;
		}

		if (!ValidationUtils.isEmpty(view.getContato())) {
			boolean adicionarAgora = DialogFactory.confirmacao(view,
					"EXISTE UM CONTATO DIGITADO QUE NÃO FOI ADICIONADO.\nDESEJA ADICIONÁ-LO AGORA?");

			if (adicionarAgora) {
				adicionarContato();
				JComponent erro = ValidationUtils.hasErroVisual(view.getFtxtContato(), view.getCbTipoContato());
				if (erro != null) {
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
			TipoContato tipo = (TipoContato) model.getValueAt(i, 0);
			var valor = model.getValueAt(i, 1).toString();
			contato.setTipoContato(tipo);
			if (tipo == TipoContato.FIXO || tipo == TipoContato.CELULAR || tipo == TipoContato.WHATSAPP) {
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
			contatos.forEach(c -> {
				String valorFormatado = c.getValorContato();
				if (c.getTipoContato() == TipoContato.FIXO || c.getTipoContato() == TipoContato.CELULAR
						|| c.getTipoContato() == TipoContato.WHATSAPP) {
					valorFormatado = FormatterUtils.formatValueWithMask(c.getValorContato(),
							mascaras.get(c.getTipoContato().name()));
				}

				model.addRow(new Object[] { c.getTipoContato(), valorFormatado });
			});
		}
	}

}
