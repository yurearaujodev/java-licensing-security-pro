package com.br.yat.gerenciador.controller;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.security.PermissaoContexto;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.TableFactory;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class UsuarioConsultaController extends BaseController {

	private final UsuarioConsultaView view;
	private final UsuarioService service;
	private final AutenticacaoService authService;

	private ScheduledFuture<?> debounceTask;

	private Usuario usuarioLogado;
	private PermissaoContexto permissaoContexto;

	public UsuarioConsultaController(UsuarioConsultaView view, UsuarioService service,
			AutenticacaoService authService) {

		this.view = view;
		this.service = service;
		this.authService = authService;

		inicializarEscopo();
	}

	private void inicializarEscopo() {

		this.usuarioLogado = Sessao.getUsuario();

		if (usuarioLogado == null) {
			view.dispose();
			return;
		}

		if (usuarioLogado.isMaster()) {
			this.permissaoContexto = PermissaoContexto.master();
			configurar();
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view), () -> service
				.obterContextoPermissao(usuarioLogado.getIdUsuario(), MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES),
				ctx -> {

					if (!ctx.temRead()) {
						DialogFactory.aviso(null, "ACESSO NEGADO À GESTÃO DE USUÁRIOS.");
						view.dispose();
						return;
					}

					this.permissaoContexto = ctx;

					configurar();
				});
	}

	private void configurar() {

		aplicarRestricoesVisuais(permissaoContexto, view.getBtnNovo(), view.getBtnEditar(), view.getBtnExcluir());

		registrarAcoes();
		configurarFiltros();

		view.getChkVerExcluidos().setVisible(permissaoContexto.temDelete());

		carregarDados();
	}

	private void configurarFiltros() {
		view.getTxtBusca().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtBusca(), this::filtrar));
	}

	private void registrarAcoes() {

		view.getBtnNovo().addActionListener(e -> abrirFormulario(null));
		view.getBtnEditar().addActionListener(e -> editarSelecionado());
		view.getBtnResetarSenha().addActionListener(e -> resetarSenhaSelecionado());

		view.getBtnExcluir().addActionListener(e -> {
			if (view.getChkVerExcluidos().isSelected()) {
				restaurarSelecionado();
			} else {
				excluirSelecionado();
			}
		});

		TableFactory.addDoubleClickAction(view.getTabela(), this::editarSelecionado);

		view.getTabela().getSelectionModel().addListSelectionListener(e -> {

			Usuario sel = view.getSelecionado();
			boolean temSelecao = sel != null;
			boolean modoLixeira = view.getChkVerExcluidos().isSelected();

			view.getBtnEditar().setEnabled(temSelecao && !modoLixeira && permissaoContexto.temWrite());

			view.getBtnResetarSenha().setEnabled(temSelecao && !modoLixeira && permissaoContexto.temDelete());

			view.getBtnExcluir().setEnabled(temSelecao && podeExcluirSelecionado(sel, modoLixeira));
		});

		view.getChkVerExcluidos().addActionListener(e -> {

			boolean modoLixeira = view.getChkVerExcluidos().isSelected();

			view.getBtnExcluir().setText(modoLixeira ? "RESTAURAR" : "EXCLUIR");

			view.getBtnNovo().setEnabled(!modoLixeira && permissaoContexto.temWrite());

			carregarDados();
		});
	}

	private boolean podeExcluirSelecionado(Usuario sel, boolean modoLixeira) {

		if (sel == null)
			return false;

		if (!permissaoContexto.temDelete())
			return false;

		if (!modoLixeira && sel.isMaster())
			return false;

		return true;
	}

	private void carregarDados() {

		boolean verExcluidos = view.getChkVerExcluidos().isSelected();

		runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> verExcluidos ? service.listarExcluidosVisiveis(usuarioLogado)
						: service.listarUsuariosVisiveis("", usuarioLogado),
				lista -> view.getTableModel().setDados(lista));
	}

	private void excluirSelecionado() {

		if (!permissaoContexto.temDelete())
			return;

		Usuario sel = view.getSelecionado();
		if (sel == null)
			return;

		if (DialogFactory.confirmacao(view,
				"DESEJA REALMENTE EXCLUIR O USUÁRIO: " + sel.getNome().toUpperCase() + "?")) {

			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.excluirUsuario(sel.getIdUsuario(), usuarioLogado);
				return null;
			}, unused -> {
				DialogFactory.informacao(view, "USUÁRIO EXCLUÍDO COM SUCESSO!");
				carregarDados();
			});
		}
	}

	private void restaurarSelecionado() {

		if (!permissaoContexto.temDelete())
			return;

		Usuario sel = view.getSelecionado();
		if (sel == null)
			return;

		if (DialogFactory.confirmacao(view, "DESEJA RESTAURAR O USUÁRIO: " + sel.getNome().toUpperCase() + "?")) {

			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.restaurarUsuario(sel.getIdUsuario(), usuarioLogado);
				return null;
			}, unused -> {
				DialogFactory.informacao(view, "USUÁRIO RESTAURADO COM SUCESSO!");
				carregarDados();
			});
		}
	}

	private void abrirFormulario(Usuario usuario) {

		if (!permissaoContexto.temWrite()) {
			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR O CADASTRO.");
			return;
		}

		JDesktopPane desk = view.getDesktopPane();

		String idJanela = (usuario == null) ? "NOVO_USUARIO" : "EDIT_USUARIO_" + usuario.getIdUsuario();

		if (DesktopUtils.reuseIfOpen(desk, idJanela))
			return;

		UsuarioView cadastroView = ViewFactory.createUsuarioViewComController();

		cadastroView.setName(idJanela);

		UsuarioController controller = (UsuarioController) cadastroView.getClientProperty("controller");

		controller.setRefreshCallback(this::carregarDados);

		if (usuario != null) {
			cadastroView.setTitle("EDITANDO USUÁRIO: " + usuario.getNome().toUpperCase());
			controller.carregarUsuarioParaEdicao(usuario);
		} else {
			cadastroView.setTitle("NOVO USUÁRIO");
			controller.novoUsuario();
		}

		DesktopUtils.showFrame(desk, cadastroView);
	}

	private void editarSelecionado() {

		Usuario sel = view.getSelecionado();

		if (sel == null) {
			DialogFactory.aviso(view, "SELECIONE UM USUÁRIO PARA EDITAR.");
			return;
		}

		abrirFormulario(sel);
	}

	private void resetarSenhaSelecionado() {

		if (!permissaoContexto.temDelete())
			return;

		Usuario sel = view.getSelecionado();
		if (sel == null)
			return;

		if (DialogFactory.confirmacao(view,
				"DESEJA REALMENTE RESETAR A SENHA DE: " + sel.getNome().toUpperCase() + "?")) {

			runAsync(SwingUtilities.getWindowAncestor(view),
					() -> authService.resetarSenha(sel.getIdUsuario(), usuarioLogado), senhaPadrao -> {
						DialogFactory.informacao(view, "SENHA RESETADA COM SUCESSO!\n" + "NOVA SENHA: " + senhaPadrao);
						carregarDados();
					});
		}
	}

	private void filtrar() {

		String termo = view.getTxtBusca().getText();
		boolean verExcluidos = view.getChkVerExcluidos().isSelected();

		if (debounceTask != null)
			debounceTask.cancel(false);

		debounceTask = scheduler.schedule(() -> runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> verExcluidos ? service.listarExcluidosVisiveis(usuarioLogado)
						: service.listarUsuariosVisiveis(termo, usuarioLogado),
				lista -> view.getTableModel().setDados(lista)), 500, TimeUnit.MILLISECONDS);
	}
}