package com.br.yat.gerenciador.controller.empresa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.FileStorageFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;
import com.br.yat.gerenciador.view.empresa.DadoComplementarPanel;

public class DadoComplementarController {
	private final DadoComplementarPanel view;
	private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private Complementar complementarAtual;

	public DadoComplementarController(DadoComplementarPanel view) {
		this.view = view;
		configurarFiltros();
		registrarAcoes();
	}

	private void registrarAcoes() {
		view.getTxtRamo()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtRamo(), this::validarRamo));
		view.getFuncionario().addFocusListener(
				ValidationUtils.createValidationListener(view.getFuncionario(), this::validarFuncionario));
		view.getBtnLogoTipo().addActionListener(e -> uploadLogo());
		view.getBtnAdicionar().addActionListener(e -> anexarDocumento());
		view.getBtnRemover().addActionListener(e -> removerDocumento());
	}

	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtRamo());
	}

	private void validarRamo() {
		try {
			EmpresaValidationUtils.validarRamoAtividade(view.getRamo());

			ValidationUtils.removerDestaque(view.getTxtRamo());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getTxtRamo(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getTxtRamo(), "ERRO NA VALIDAÇÃO");
		}
	}

	private void validarFuncionario() {
		try {
			EmpresaValidationUtils.validarNumeroFuncionarios(ValidationUtils.parseInt(view.getFuncionarios()));

			ValidationUtils.removerDestaque(view.getFuncionario());
		} catch (ValidationException e) {
			ValidationUtils.exibirErro(view.getFuncionario(), e.getMessage());
		} catch (Exception e) {
			ValidationUtils.exibirErro(view.getFuncionario(), "ERRO NA VALIDAÇÃO");
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
		boolean obrigatoriosVazios = ValidationUtils.temCamposVazios(view.getTxtRamo(), view.getFuncionario(),
				view.getCbTipo());

		if (obrigatoriosVazios) {
			DialogFactory.aviso(view, "POR FAVOR, PREENCHA OS CAMPOS OBRIGATÓRIOS DESTACADOS EM VERMELHO.");
			return false;
		}

		validarRamo();
		validarFuncionario();

		if (view.getTabela().getRowCount() == 0) {
			ValidationUtils.exibirErro(view.getTabela(), "ADICIONE PELO MENOS UM DOCUMENTO.");
			DialogFactory.aviso(view, "A LISTA DE DOCUMENTOS NÃO PODE ESTAR VAZIA.");
			return false;
		}

		JComponent erro = ValidationUtils.hasErroVisual(view.getTxtRamo(), view.getFuncionario(), view.getCbTipo());

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
			var nomeArquivo = Paths.get(caminho).getFileName().toString();

			int ponto = nomeArquivo.lastIndexOf(".");
			if (ponto != -1) {
				var semExtensao = nomeArquivo.substring(0, ponto);

				String[] partes = semExtensao.split("_");
				if (partes.length >= 2) {
					var dataStr = partes[partes.length - 2] + "_" + partes[partes.length - 1];
					DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
					LocalDateTime data = LocalDateTime.parse(dataStr, parser);
					return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "---";
		}
		return "---";
	}

	public void limpar() {
		var model = (DefaultTableModel) view.getTabela().getModel();
		model.setRowCount(0);
		view.limpar();
	}

	public void desativarAtivar(boolean ativa) {
		view.desativarAtivar(ativa);
	}

	public Complementar getComplementar() {
		Complementar c = (this.complementarAtual != null) ? this.complementarAtual : new Complementar();
		c.setLogoTipoComplementar(view.getLogo());
		c.setRamoAtividadeComplementar(view.getRamo());
		c.setNumFuncionariosComplementar(ValidationUtils.parseInt(view.getFuncionarios()));
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
