package com.br.yat.gerenciador.controller.empresa;

import java.awt.Window;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.controller.BaseController;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.TableFactory;
import com.br.yat.gerenciador.view.factory.ViewFactory;

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

		if (DesktopUtils.reuseIfOpen(desk, idJanela))
			return;

		EmpresaView cadastroView = (empresa == null) ? ViewFactory.createEmpresaView(TipoCadastro.CLIENTE)
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

		DesktopUtils.showFrame(desk, cadastroView);
	}

	private void filtrar() {
		var termo = view.getTxtBusca().getText();

		if (debounceTask != null)
			debounceTask.cancel(false);

		if (termo.length() > 0) {
			ValidationUtils.removerDestaque(view.getTxtBusca());
		}

		if (!termo.isBlank()) {
			ValidationUtils.removerDestaque(view.getTxtBusca());
		}

		Window parent = SwingUtilities.getWindowAncestor(view);
		debounceTask = scheduler.schedule(() -> {
			runAsyncSilent(parent, () -> {
				List<Empresa> lista = service.filtrarClientes(termo);
				return lista;
			}, lista -> {
				view.getTableModel().setDados(lista);
			});

		}, 800, TimeUnit.MILLISECONDS);
	}

	private void carregarDados() {
		Window parent = SwingUtilities.getWindowAncestor(view);
		runAsync(parent, () -> {
			return service.listarClientesParaTabela();
		}, lista -> {
			view.getTableModel().setDados(lista);
		});
	}
}
