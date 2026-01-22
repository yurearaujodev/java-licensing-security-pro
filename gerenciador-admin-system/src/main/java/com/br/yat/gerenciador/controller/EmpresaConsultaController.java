package com.br.yat.gerenciador.controller;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;
import com.br.yat.gerenciador.util.ui.ViewFactory;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;

public class EmpresaConsultaController extends BaseController{
	private final EmpresaConsultaView view;
	private final EmpresaService service;
	private ScheduledFuture<?> debounceTask;
	
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
		TableFactory.addDoubleClickAction(view.getTabela(), ()->aoClicarEditar());
	}

	
	private void aoClicarEditar() {
		int linha = view.getTabela().getSelectedRow();
		if (linha<0) {
			DialogFactory.aviso(view, "SELECIONE UM CLIENTE NA TABELA PARA ALTERAR.");
			return;
		}
		int linhaSelecionada = view.getTabela().convertRowIndexToModel(linha);
		Empresa selecionada = view.getTableModel().getAt(linhaSelecionada);
		if (selecionada==null) {
			DialogFactory.aviso(view, "NÃO FOI POSSÍVEL RECUPERAR OS DADOS DO CLIENTE.");
			return;
		}
		
		JDesktopPane desk = view.getDesktopPane();
		String idJanela = "EDIT_JANELA_"+selecionada.getIdEmpresa();
		
		if (DesktopFactory.reuseIfOpen(desk, idJanela)) {
			return;
		}
		
		EmpresaView cadastroView = ViewFactory.createEmpresaEdicaoView(selecionada.getIdEmpresa());
		cadastroView.setName(idJanela);
		cadastroView.setTitle("EDITANDO: "+selecionada.getRazaoSocialEmpresa());
		
		EmpresaController cadastroCtrl = (EmpresaController)cadastroView.getClientProperty("controller");
		cadastroCtrl.setRefreshCallback(()->carregarDados());
		
		DesktopFactory.showFrame(desk, cadastroView);
		DesktopFactory.centerDesktopPane(desk, cadastroView);
	}
	private void filtrar() {
		var termo = view.getTxtBusca().getText();
		
		if(debounceTask!=null)debounceTask.cancel(false);
		
		if (termo.length()>0) {
			ValidationUtils.removerDestaque(view.getTxtBusca());
		}
		
		debounceTask = scheduler.schedule(()->{
		executor.submit(()->{
			try {
				List<Empresa> lista = service.filtrarClientes(termo);
					
				SwingUtilities.invokeLater(()->view.getTableModel().setDados(lista));
			} catch (Exception e) {
				DialogFactory.erro(view, "ERRO: ", e);
			}
		});
		},800,TimeUnit.MILLISECONDS);
	}
	
	private void carregarDados() {
		executor.submit(()->{
			try {
				List<Empresa> lista = service.listarClientesParaTabela();
				SwingUtilities.invokeLater(()->view.getTableModel().setDados(lista));
			} catch (Exception e) {
				SwingUtilities.invokeLater(()->DialogFactory.erro(view, "ERRO AO CARREGAR: "+e.getMessage()));
			}
		});
	}
}
