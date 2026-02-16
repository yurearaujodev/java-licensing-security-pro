package com.br.yat.gerenciador.controller.empresa;

import java.awt.Window;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.controller.BaseController;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.enums.MenuChave;
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
		configurar();
	}

	private void configurar() {
		configurarFiltros();
		registrarAcoes();
		carregarDados();

		boolean usuarioPodeAlterar = podeAlterar();

		view.getBtnNovo().setVisible(usuarioPodeAlterar);

		view.getChkInativos().setEnabled(usuarioPodeAlterar);

		if (!usuarioPodeAlterar) {
			view.getChkInativos().setSelected(false);
		}

		view.getBtnEditar().setEnabled(false);
		view.getBtnExcluir().setEnabled(false);
	}

	private boolean temPermissao(MenuChave chave) {
		List<MenuChave> ativas = Sessao.getPermissoes();
		return ativas != null && ativas.contains(chave);
	}

	private void configurarFiltros() {
		ValidationUtils.createDocumentFilter(view.getTxtBusca());
		view.getTxtBusca().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtBusca(), this::filtrar));
	}

	private void registrarAcoes() {
		view.getBtnEditar().addActionListener(e -> editarSelecionado());
		view.getBtnNovo().addActionListener(e -> abrirFormulario(null));

		view.getBtnExcluir().addActionListener(e -> {
			if (view.getChkInativos().isSelected()) {
				restaurarSelecionada();
			} else {
				excluirSelecionado();
			}
		});

		view.getChkInativos().addActionListener(e -> {
			configurarBotoesPorEstado();

			if (debounceTask != null)
				debounceTask.cancel(false);

			debounceTask = scheduler.schedule(() -> {
				SwingUtilities.invokeLater(() -> carregarDados());
			}, 300, TimeUnit.MILLISECONDS);
		});

		TableFactory.addDoubleClickAction(view.getTabela(), () -> {
			if (podeAlterar()) {
				editarSelecionado();
			} else {
				DialogFactory.aviso(view, "ACESSO NEGADO: VOCÊ NÃO TEM PERMISSÃO DE EDIÇÃO.");
			}
		});
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting())
				return;
			atualizarEstadoBotoes();
		});
	}

	private boolean podeAlterar() {
		return Sessao.getUsuario().isMaster() || temPermissao(MenuChave.CADASTROS_EMPRESA_CLIENTE);
	}

	private void atualizarEstadoBotoes() {
		boolean linhaSelecionada = view.getTabela().getSelectedRow() >= 0;
		boolean temPermissao = podeAlterar();
		boolean vendoInativos = view.getChkInativos().isSelected();

		view.getBtnEditar().setEnabled(linhaSelecionada && temPermissao);
		view.getBtnExcluir().setEnabled(linhaSelecionada && temPermissao);

		view.getBtnNovo().setEnabled(!vendoInativos && temPermissao);
	}

	private void configurarBotoesPorEstado() {
		boolean mostrarInativos = view.getChkInativos().isSelected();
		view.getBtnExcluir().setText(mostrarInativos ? "RESTAURAR" : "APAGAR");
		atualizarEstadoBotoes();
	}

	private void restaurarSelecionada() {
		int linha = view.getTabela().getSelectedRow();
		if (linha < 0)
			return;

		int modelIndex = view.getTabela().convertRowIndexToModel(linha);
		Empresa empresa = view.getTableModel().getAt(modelIndex);

		if (DialogFactory.confirmacao(view, "DESEJA RESTAURAR: " + empresa.getRazaoSocialEmpresa() + "?")) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.restaurarEmpresa(empresa.getIdEmpresa(), Sessao.getUsuario());
				return true;
			}, ok -> {
				DialogFactory.informacao(view, "EMPRESA RESTAURADA!");
				carregarDados();
			});
		}
	}

	private void editarSelecionado() {
		int linha = view.getTabela().getSelectedRow();
		if (linha < 0) {
			DialogFactory.aviso(view, "SELECIONE UM CLIENTE NA TABELA PARA ALTERAR.");
			return;
		}
		int modelIndex = view.getTabela().convertRowIndexToModel(linha);
		Empresa empresa = view.getTableModel().getAt(modelIndex);
		abrirFormulario(empresa);
	}

	private void abrirFormulario(Empresa empresa) {
		JDesktopPane desk = view.getDesktopPane();
		String idJanela = empresa == null ? "NOVA_EMPRESA" : "EDIT_EMPRESA_" + empresa.getIdEmpresa();

		if (DesktopUtils.reuseIfOpen(desk, idJanela))
			return;

		EmpresaView cadastroView = criarView(empresa);
		cadastroView.setName(idJanela);

		configurarController(cadastroView, empresa);
		DesktopUtils.showFrame(desk, cadastroView);
	}

	private EmpresaView criarView(Empresa empresa) {
		return empresa == null ? ViewFactory.createEmpresaView(TipoCadastro.CLIENTE)
				: ViewFactory.createEmpresaEdicaoView(empresa.getIdEmpresa());
	}

	private void configurarController(EmpresaView view, Empresa empresa) {
		EmpresaController controller = (EmpresaController) view.getClientProperty("controller");
		controller.setRefreshCallback(this::carregarDados);

		if (empresa == null) {
			view.setTitle("NOVO CADASTRO");
			controller.prepararNovo();
		} else {
			view.setTitle("EDITANDO: " + empresa.getRazaoSocialEmpresa());
			controller.carregarDados(empresa.getIdEmpresa());
		}
	}

	private void filtrar() {
		String termo = view.getTxtBusca().getText();
		boolean mostrarInativos = view.getChkInativos().isSelected(); // Pega o estado do checkbox

		if (debounceTask != null)
			debounceTask.cancel(false);

		if (!termo.isBlank())
			ValidationUtils.removerDestaque(view.getTxtBusca());

		Window parent = SwingUtilities.getWindowAncestor(view);

		debounceTask = scheduler.schedule(
				() -> runAsyncSilent(parent, () -> service.filtrarClientes(termo, mostrarInativos, Sessao.getUsuario()),
						lista -> view.getTableModel().setDados(lista)),
				800, TimeUnit.MILLISECONDS);
	}

	private void carregarDados() {
		boolean mostrarInativos = view.getChkInativos().isSelected();
		Window parent = SwingUtilities.getWindowAncestor(view);

		runAsyncSilent(parent, () -> service.listarClientesParaTabela(mostrarInativos, Sessao.getUsuario()),
				lista -> view.getTableModel().setDados(lista));
	}

	private void excluirSelecionado() {
		int linha = view.getTabela().getSelectedRow();

		if (linha < 0) {
			DialogFactory.aviso(view, "SELECIONE UM CLIENTE PRA EXCLUIR.");
			return;
		}

		int modelIndex = view.getTabela().convertRowIndexToModel(linha);
		Empresa empresa = view.getTableModel().getAt(modelIndex);

		boolean confirmar = DialogFactory.confirmacao(view,
				"DESEJA REALMENTE INATIVAR A EMPRESA?\n\n" + empresa.getRazaoSocialEmpresa());

		if (!confirmar)
			return;

		Window parent = SwingUtilities.getWindowAncestor(view);
		runAsync(parent, () -> {
			service.excluirEmpresa(empresa.getIdEmpresa(), Sessao.getUsuario());
			return true;
		}, ok -> {
			DialogFactory.informacao(view, "EMPRESA INATIVADA COM SUCESSO.");
			carregarDados();
		});

	}

}
