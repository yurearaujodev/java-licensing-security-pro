package com.br.yat.gerenciador.controller.empresa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.enums.TipoContato;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoContatoPanel;
import com.br.yat.gerenciador.view.factory.FormatterUtils;
import com.br.yat.gerenciador.view.factory.MaskFactory;
import com.br.yat.gerenciador.view.factory.TableFactory;

public class DadoContatoController {

	private final DadoContatoPanel view;
	private final Map<TipoContato, Runnable> estrategiasConfiguracao = new HashMap<>();
	private final Map<String, String> mascaras = MaskFactory.createMask();
	private Integer linhaEmAlteracao = null;

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
		TableFactory.addEmptySpaceClickAction(view.getTabela(), () -> {
			linhaEmAlteracao = null;
			view.limpar();
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

		var model = (DefaultTableModel) view.getTabela().getModel();

		if (linhaEmAlteracao != null) {
			model.setValueAt(tipo, linhaEmAlteracao, 1);
			model.setValueAt(valor, linhaEmAlteracao, 2);
			linhaEmAlteracao = null;
		} else {

			if (contatoJaExiste(tipo, valor)) {
				DialogFactory.aviso(view, "ESTE CONTATO JÁ FOI ADICIONADO.");
				return;
			}
			Object[] linha = { 0, tipo, valor };
			model.addRow(linha);
		}

		view.limpar();
		ValidationUtils.removerDestaque(view.getFtxtContato());
		view.getTabela().clearSelection();
	}

	private boolean contatoJaExiste(TipoContato tipo, String valor) {
		var model = (DefaultTableModel) view.getTabela().getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			if (Objects.equals(model.getValueAt(i, 1), tipo) && Objects.equals(model.getValueAt(i, 2), valor)) {
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
		linhaEmAlteracao = null;
		view.limpar();
	}

	private void preencherCamposLinhaSelecionada() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow < 0)
			return;

		linhaEmAlteracao = selectedRow;

		TipoContato tipo = (TipoContato) view.getTabela().getValueAt(selectedRow, 1);
		String valor = (String) view.getTabela().getValueAt(selectedRow, 2);

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
			Object idObj = model.getValueAt(i, 0);
			if (idObj != null) {
				contato.setIdContato((int) idObj);
			}

			TipoContato tipo = (TipoContato) model.getValueAt(i, 1);
			var valor = model.getValueAt(i, 2).toString();
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

				model.addRow(new Object[] { c.getIdContato(), c.getTipoContato(), valorFormatado });
			});
		}
	}

}
