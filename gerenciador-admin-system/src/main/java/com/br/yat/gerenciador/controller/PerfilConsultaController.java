package com.br.yat.gerenciador.controller;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.security.PermissaoContexto;
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.PerfilConsultaView;
import com.br.yat.gerenciador.view.PerfilView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.TableFactory;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class PerfilConsultaController extends BaseController {

	private final PerfilConsultaView view;
	private final PerfilService service;
	private ScheduledFuture<?> debounceTask;

	private Usuario usuarioLogado;
	private PermissaoContexto permissaoContexto;

	public PerfilConsultaController(PerfilConsultaView view, PerfilService service) {
		this.view = view;
		this.service = service;
		inicializarEscopo();
		configurar();
	}

	private void inicializarEscopo() {

		this.usuarioLogado = Sessao.getUsuario();

		if (usuarioLogado == null) {
			permissaoContexto = PermissaoContexto.comum(Set.of());
			return;
		}

		if (usuarioLogado.isMaster()) {
			permissaoContexto = PermissaoContexto.master();
			return;
		}

		permissaoContexto = service.obterContextoPermissao(usuarioLogado.getIdUsuario(),
				MenuChave.CONFIGURACAO_PERMISSAO);
	}

	private void configurar() {
		registrarAcoes();
		configurarFiltros();
		configurarControleLixeira();
		aplicarPermissoesEscopo();
		carregarDados();
	}

	private void configurarFiltros() {
		view.getTxtBusca().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtBusca(), this::filtrar));
	}

	private void registrarAcoes() {

		view.getBtnNovo().addActionListener(e -> abrirFormulario(null));
		view.getBtnEditar().addActionListener(e -> editarSelecionado());

		view.getBtnExcluir().addActionListener(e -> {
			boolean modoLixeira = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

			if (modoLixeira) {
				restaurarSelecionado();
			} else {
				excluirSelecionado();
			}
		});

		view.getBtnPesquisar().addActionListener(e -> carregarDados());

		TableFactory.addDoubleClickAction(view.getTabela(), this::editarSelecionado);

		view.getTabela().getSelectionModel().addListSelectionListener(e -> atualizarBotoes());
	}

	private void configurarControleLixeira() {

		if (view.getChkVerExcluidos() == null)
			return;

		view.getChkVerExcluidos().setVisible(permissaoContexto.temDelete());

		view.getChkVerExcluidos().addActionListener(e -> {

			boolean modoLixeira = view.getChkVerExcluidos().isSelected();

			view.getBtnExcluir().setText(modoLixeira ? "RESTAURAR" : "EXCLUIR");
			view.getBtnNovo().setEnabled(!modoLixeira && permissaoContexto.temWrite());

			carregarDados();
		});
	}

	private void atualizarBotoes() {

		Perfil sel = view.getSelecionado();
		boolean temSelecao = (sel != null);
		boolean modoLixeira = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		view.getBtnEditar().setEnabled(temSelecao && !modoLixeira && permissaoContexto.temWrite());

		if (!temSelecao) {
			view.getBtnExcluir().setEnabled(false);
			return;
		}

		boolean podeExcluir;

		if (!permissaoContexto.temDelete()) {
			podeExcluir = false;
		} else if (modoLixeira) {
			podeExcluir = true;
		} else {
			podeExcluir = !"MASTER".equalsIgnoreCase(sel.getNome());
		}

		view.getBtnExcluir().setEnabled(podeExcluir);
	}

	private void carregarDados() {

		boolean verExcluidos = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> service.listarPerfisVisiveis("", verExcluidos, usuarioLogado),
				lista -> view.getTableModel().setDados(lista));
	}

	private void filtrar() {

		String termo = view.getTxtBusca().getText();
		boolean verExcluidos = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		if (debounceTask != null)
			debounceTask.cancel(false);

		debounceTask = scheduler.schedule(() -> runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> service.listarPerfisVisiveis(termo, verExcluidos, usuarioLogado),
				lista -> view.getTableModel().setDados(lista)), 500, TimeUnit.MILLISECONDS);
	}

	private void editarSelecionado() {

		Perfil sel = view.getSelecionado();

		if (sel != null) {
			abrirFormulario(sel);
		} else {
			DialogFactory.aviso(view, "SELECIONE UM PERFIL PARA EDITAR.");
		}
	}

	private void excluirSelecionado() {

		Perfil sel = view.getSelecionado();
		if (sel == null)
			return;

		if (!permissaoContexto.temDelete()) {
			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA EXCLUIR.");
			return;
		}

		boolean confirmou = DialogFactory.confirmacao(view,
				"DESEJA REALMENTE EXCLUIR O PERFIL: " + sel.getNome().toUpperCase() + "?");

		if (confirmou) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.excluirPerfil(sel.getIdPerfil(), usuarioLogado);
				return null;
			}, unused -> {
				DialogFactory.informacao(view, "PERFIL EXCLUÍDO COM SUCESSO!");
				carregarDados();
			});
		}
	}

	private void restaurarSelecionado() {

		Perfil sel = view.getSelecionado();
		if (sel == null)
			return;

		if (!permissaoContexto.temDelete()) {
			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA RESTAURAR.");
			return;
		}

		if (DialogFactory.confirmacao(view, "DESEJA RESTAURAR O PERFIL: " + sel.getNome().toUpperCase() + "?")) {

			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.restaurarPerfil(sel.getIdPerfil(), Sessao.getUsuario());
				return null;
			}, unused -> {
				DialogFactory.informacao(view, "PERFIL RESTAURADO COM SUCESSO!");
				carregarDados();
			});
		}
	}

	private void aplicarPermissoesEscopo() {

		if (!aplicarRestricoesVisuais(permissaoContexto, view.getBtnNovo(), view.getBtnEditar(),
				view.getBtnExcluir())) {

			DialogFactory.aviso(view, "ACESSO NEGADO À GESTÃO DE PERFIS.");
			view.dispose();
			return;
		}

		if (view.getChkVerExcluidos() != null) {
			view.getChkVerExcluidos().setVisible(permissaoContexto.temDelete());
		}
	}

	private void abrirFormulario(Perfil perfil) {
		if (!permissaoContexto.temWrite()) {
			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR O CADASTRO DE PERFIS.");
			return;
		}

		JDesktopPane desk = view.getDesktopPane();

		String idJanela = (perfil == null) ? "NOVO_PERFIL" : "EDIT_PERFIL_" + perfil.getIdPerfil();

		if (DesktopUtils.reuseIfOpen(desk, idJanela)) {
			return;
		}

		PerfilView formView = ViewFactory.createPerfilView();
		formView.setName(idJanela);

		PerfilController controller = (PerfilController) formView.getClientProperty("controller");
		controller.setRefreshCallback(this::carregarDados);

		if (perfil != null) {
			formView.setTitle("EDITANDO PERFIL: " + perfil.getNome().toUpperCase());
			controller.carregarParaEdicao(perfil);
		} else {
			formView.setTitle("NOVO PERFIL");
			controller.novoPerfil();
		}

		DesktopUtils.showFrame(desk, formView);
	}
}