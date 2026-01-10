package com.br.yat.gerenciador.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.FileStorageFactory;
import com.br.yat.gerenciador.view.empresa.DadoComplementarPanel;

public class DadoComplementarController {
	private final DadoComplementarPanel view;
	private final DateTimeFormatter dtf= DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	
	public DadoComplementarController(DadoComplementarPanel view) {
		this.view=view;
		registrarAcoes();
		}

		private void registrarAcoes() {
			view.getBtnLogoTipo().addActionListener(e->uploadLogo());
			view.getBtnAdicionar().addActionListener(e->anexarDocumento());
			view.getBtnRemover().addActionListener(e->removerDocumento());
		}

		private void removerDocumento() {
			int selectedRow = view.getTabela().getSelectedRow();
			if (selectedRow != -1) {
				DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();
				model.removeRow(selectedRow);
			} else {
				DialogFactory.informacao(view, "Selecione um representante na tabela para remover.");
			}
		}

		private void anexarDocumento() {
			File arquivo = abrirSeletor("Documentos (PDF,Imagens)", "pdf","jpg","png","docx");
			if (arquivo!= null) {
				try {
					String tipo = view.getCbTipo().getSelectedItem().toString();
					
					String caminhoRelativo = FileStorageFactory.salvarArquivo(arquivo, tipo);
					String dataAgora = LocalDateTime.now().format(dtf);
					
					adicionarTabela(tipo, caminhoRelativo, dataAgora);
				} catch (IOException ex) {
					DialogFactory.erro(view, "ERRO AO ANEXAR ARQUIVO: ",ex);
				}
			}
		}

		private void uploadLogo() {
			File arquivo = abrirSeletor("Imagens (PNG,JPG)","jpg","png","jpeg");
			if (arquivo!=null) {
				try {
					String caminho = FileStorageFactory.salvarLogo(arquivo, "fornecedor");
					view.getTxtLogo().setText(caminho);
				} catch (IOException ex) {
					DialogFactory.erro(view, "ERRO AO SALVAR LOGO: ",ex);
				}
			}
		}
		
		private File abrirSeletor(String descricao, String... extensoes) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(descricao, extensoes));
			int res = chooser.showOpenDialog(view);
			return (res==JFileChooser.APPROVE_OPTION)? chooser.getSelectedFile(): null;
		}
		
		private void adicionarTabela(String tipo, String caminho,String data) {
			DefaultTableModel model = (DefaultTableModel) view.getTabela().getModel();

			Object[] linha = {
					tipo,caminho,data
			};
			model.addRow(linha);

		}

}
