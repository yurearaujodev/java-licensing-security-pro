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

public class EmpresaConsultaController extends BaseController {
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
		view.getTxtBusca().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtBusca(), () -> filtrar()));

		view.getBtnEditar().addActionListener(e -> aoClicarEditar());
		view.getBtnNovo().addActionListener(e -> aoClicarNovo());
		TableFactory.addDoubleClickAction(view.getTabela(), () -> aoClicarEditar());
		// view.getBtnEditar().setEnabled(false);
	}

	private void aoClicarNovo() {
		abrirFormulario(null);
	}

	private void aoClicarEditar() {
		int linha = view.getTabela().getSelectedRow();
		if (linha < 0) {
			DialogFactory.aviso(view, "SELECIONE UM CLIENTE NA TABELA PARA ALTERAR.");
			return;
		}
		int linhaSelecionada = view.getTabela().convertRowIndexToModel(linha);
		Empresa selecionada = view.getTableModel().getAt(linhaSelecionada);

		abrirFormulario(selecionada);
	}

	private void abrirFormulario(Empresa empresa) {
		JDesktopPane desk = view.getDesktopPane();
		String idJanela = (empresa == null) ? "NOVA_EMPRESA" : "EDIT_EMPRESA_" + empresa.getIdEmpresa();

		if (DesktopFactory.reuseIfOpen(desk, idJanela))
			return;

		EmpresaView cadastroView = (empresa == null) 
				? ViewFactory.createEmpresaView("CLIENTE")
				: ViewFactory.createEmpresaEdicaoView(empresa.getIdEmpresa());
		
		cadastroView.setName(idJanela);
		
		EmpresaController cadastroCtrl = (EmpresaController) cadastroView.getClientProperty("controller");
		cadastroCtrl.setRefreshCallback(() -> carregarDados());

		if (empresa == null) {
			cadastroView.setTitle("NOVO CADASTRO");
			cadastroCtrl.prepararNovo();
		} else {
			cadastroView.setTitle("EDITANDO: " + empresa.getRazaoSocialEmpresa());
			cadastroCtrl.carregarDadosCliente(empresa.getIdEmpresa());
		}
	
		DesktopFactory.showFrame(desk, cadastroView);
		DesktopFactory.centerDesktopPane(desk, cadastroView);
	}

	private void filtrar() {
		var termo = view.getTxtBusca().getText();

		if (debounceTask != null)
			debounceTask.cancel(false);

		if (termo.length() > 0) {
			ValidationUtils.removerDestaque(view.getTxtBusca());
		}

		debounceTask = scheduler.schedule(() -> {
			executor.submit(() -> {
				try {
					List<Empresa> lista = service.filtrarClientes(termo);

					SwingUtilities.invokeLater(() -> view.getTableModel().setDados(lista));
				} catch (Exception e) {
					DialogFactory.erro(view, "ERRO: ", e);
				}
			});
		}, 800, TimeUnit.MILLISECONDS);
	}

	private void carregarDados() {
		executor.submit(() -> {
			try {
				List<Empresa> lista = service.listarClientesParaTabela();
				SwingUtilities.invokeLater(() -> view.getTableModel().setDados(lista));
			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> DialogFactory.erro(view, "ERRO AO CARREGAR: " + e.getMessage()));
			}
		});
	}
}
