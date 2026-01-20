package com.br.yat.gerenciador.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.ViewFactory;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;

public class EmpresaConsultaController {
	private final EmpresaConsultaView view;
	private final EmpresaService service;
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	
	public EmpresaConsultaController(EmpresaConsultaView view, EmpresaService service) {
		this.view = view;
		this.service = service;
		
		registrarAcoes();
		carregarDados();
		configurarFiltros();
	}
	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtBusca());
	}
	
	private void registrarAcoes() {
		view.getTxtBusca().getDocument().addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtBusca(),()-> filtrar()));
		
		view.getBtnEditar().addActionListener(e->aoClicarEditar());
	}

	
	private void aoClicarEditar() {
		int linha = view.getTabela().getSelectedRow();
		if (linha<0) {
			DialogFactory.aviso(view, "SELECIONE UM CLIENTE NA TABELA PARA ALTERAR.");
		}
		Empresa selecionada = view.getTableModel().getEmpresaAt(linha);
		
		EmpresaView cadastroView = ViewFactory.createEmpresaEdicaoView(selecionada.getIdEmpresa());
		
		view.getDesktopPane().add(cadastroView);
		cadastroView.setVisible(true);
		cadastroView.toFront();
		
		view.dispose();
	}
	private void filtrar() {
		var termo = view.getTxtBusca().getText();
		if (termo.length()>0) {
			ValidationUtils.removerDestaque(view.getTxtBusca());
		}
		
		executor.submit(()->{
			try {
				List<Empresa> lista = service.filtrarClientes(termo);
					
				SwingUtilities.invokeLater(()->view.getTableModel().setLista(lista));
			} catch (Exception e) {
				DialogFactory.erro(view, "ERRO: ", e);
			}
		});
	}
	
	private void carregarDados() {
		executor.submit(()->{
			try {
				List<Empresa> lista = service.listarClientesParaTabela();
				SwingUtilities.invokeLater(()->view.getTableModel().setLista(lista));
			} catch (Exception e) {
				SwingUtilities.invokeLater(()->DialogFactory.erro(view, "ERRO AO CARREGAR: "+e.getMessage()));
			}
		});
	}
}
