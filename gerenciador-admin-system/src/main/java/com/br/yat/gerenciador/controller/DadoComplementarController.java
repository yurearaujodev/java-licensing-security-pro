package com.br.yat.gerenciador.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.FileStorageFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoComplementarPanel;

public class DadoComplementarController {
	private final DadoComplementarPanel view;
	private final EmpresaService service;
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private Complementar complementarAtual;

	public DadoComplementarController(DadoComplementarPanel view, EmpresaService service) {
		this.view = view;
		this.service = service;
		configurarFiltros();
		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getTxtRamo()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtRamo(), this::validarRamo));
		view.getBtnLogoTipo().addActionListener(e -> uploadLogo());
		view.getBtnAdicionar().addActionListener(e -> anexarDocumento());
		view.getBtnRemover().addActionListener(e -> removerDocumento());
	}

	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtRamo());
	}

	private void validarRamo() {
		try {
			Complementar mock = new Complementar();
			mock.setRamoAtividadeComplementar(view.getRamo());
			service.validarComplementarIndividual(mock);
			ValidationUtils.removerDestaque(view.getTxtRamo());
		} catch (IllegalArgumentException e) {
			ValidationUtils.exibirErro(view.getTxtRamo(), e.getMessage());
		}
	}

	private void uploadLogo() {
		File arquivo = abrirSeletor("Imagens (PNG,JPG)", "jpg", "png", "jpeg");
		if (arquivo != null) {
			try {
				String caminho = FileStorageFactory.salvarLogo(arquivo, "fornecedor");
				view.setLogo(caminho);
				ValidationUtils.removerDestaque(view.getTxtLogo());
			} catch (IOException ex) {
				DialogFactory.erro(view, "ERRO AO SALVAR LOGO: ", ex);
			}
		}
	}

	private void anexarDocumento() {
		File arquivo = abrirSeletor("Documentos (PDF,Imagens)", "pdf", "jpg", "png", "docx");
		if (arquivo != null) {
			try {
				String tipo = view.getTipoDocumento();

				String caminhoRelativo = FileStorageFactory.salvarArquivo(arquivo, tipo);
				String dataAgora = LocalDateTime.now().format(dtf);

				DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();

				Object[] linha = { tipo, caminhoRelativo, dataAgora };
				model.addRow(linha);
			} catch (IOException ex) {
				DialogFactory.erro(view, "ERRO AO ANEXAR ARQUIVO: ", ex);
			}
		}
	}

	private void removerDocumento() {
		int selectedRow = view.getTabela().getSelectedRow();
		if (selectedRow != -1) {
			var model = (DefaultTableModel) view.getTabela().getModel();
			model.removeRow(selectedRow);
			return;
		}
		DialogFactory.informacao(view, "SELECIONE UM DOCUMENTO NA TABELA PArA REMOVER.");

	}

	private File abrirSeletor(String descricao, String... extensoes) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter(descricao, extensoes));
		int res = chooser.showOpenDialog(view);
		return (res == JFileChooser.APPROVE_OPTION) ? chooser.getSelectedFile() : null;
	}

	public boolean isValido() {
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(view.getTxtRamo(), view.getTxtLogo());

		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}

		validarRamo();

		JComponent erro = ValidationUtils.hasErroVisual(view.getTxtLogo(), view.getTxtRamo());

		if (erro != null) {
			DialogFactory.aviso(view, "EXISTEM CAMPOS COM DADOS INVÁLIDOS.");
			return false;
		}

		return true;
	}

	private String extrairDataCaminho(String caminho) {
		if (ValidationUtils.isEmpty(caminho))
			return "---";

		try {
			int ultimaUnderline = caminho.lastIndexOf("_");
			if (ultimaUnderline != -1 && caminho.length() > 16) {
				return caminho.substring(ultimaUnderline - 10, ultimaUnderline + 6);
			}
		} catch (Exception e) {
			return "---";
		}
		return "---";
	}

	public Complementar getComplementar() {
		Complementar c = (this.complementarAtual != null) ? this.complementarAtual : new Complementar();
		c.setLogoTipoComplementar(view.getLogo());
		c.setRamoAtividadeComplementar(view.getRamo());
		c.setNumFuncionariosComplementar(
				view.getFuncionarios().isEmpty() ? 0 : Integer.parseInt(view.getFuncionarios()));
		c.setObsComplementar(view.getObservacoes());
		return c;
	}

	public List<Documento> getDocumentos() {
		List<Documento> documentos = new ArrayList<>();
		var model = (DefaultTableModel) view.getTabela().getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			Documento d = new Documento();
			d.setTipoDocumento((String) model.getValueAt(i, 0));
			d.setArquivoDocumento((String) model.getValueAt(i, 1));
			documentos.add(d);
		}
		return documentos;
	}

	public void setDados(Complementar comp, List<Documento> docs) {
		this.complementarAtual = comp;
		if (comp != null) {
			view.setLogo(comp.getLogoTipoComplementar());
			view.setRamo(comp.getRamoAtividadeComplementar());
			view.setFuncionarios(comp.getNumFuncionariosComplementar());
			view.setObservacoes(comp.getObsComplementar());
		}

		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);
		if (docs != null) {
			for (Documento d : docs) {
				var caminho = d.getArquivoDocumento();
				var dataParaExibir = extrairDataCaminho(caminho);

				model.addRow(new Object[] { d.getTipoDocumento(), caminho, dataParaExibir });
			}

		}
	}
}
