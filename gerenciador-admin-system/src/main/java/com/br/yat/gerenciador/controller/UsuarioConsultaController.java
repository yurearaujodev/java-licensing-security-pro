package com.br.yat.gerenciador.controller;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.security.PermissaoContexto;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.service.UsuarioPermissaoService;
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
	private final UsuarioPermissaoService usuarioPermissaoService;

	private ScheduledFuture<?> debounceTask;

	private Usuario usuarioLogado;
	private PermissaoContexto permissaoContexto;

	public UsuarioConsultaController(UsuarioConsultaView view, UsuarioService service, AutenticacaoService authService,
			UsuarioPermissaoService usuarioPermissaoService) {

		this.view = view;
		this.service = service;
		this.authService = authService;
		this.usuarioPermissaoService = usuarioPermissaoService;

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

		runAsync(getWindow(), () -> usuarioPermissaoService.obterContextoPermissao(usuarioLogado.getIdUsuario(),
				MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES), ctx -> {

					if (ctx == null || !ctx.temRead()) {
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

	private void resetListeners(AbstractButton btn) {

		for (ActionListener al : btn.getActionListeners()) {
			btn.removeActionListener(al);
		}
	}

	private Window getWindow() {
		return SwingUtilities.getWindowAncestor(view);
	}

	private void registrarAcoes() {

		resetListeners(view.getBtnNovo());
		resetListeners(view.getBtnEditar());
		resetListeners(view.getBtnResetarSenha());
		resetListeners(view.getBtnExcluir());
		resetListeners(view.getChkVerExcluidos());

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

		view.getTabela().getSelectionModel().addListSelectionListener(e -> atualizarEstadoBotoes());

		view.getChkVerExcluidos().addActionListener(e -> alternarModoLixeira());
	}

	private void atualizarEstadoBotoes() {

		Usuario sel = view.getSelecionado();

		boolean temSelecao = sel != null;

		boolean modoLixeira = view.getChkVerExcluidos().isSelected();

		view.getBtnEditar().setEnabled(permissaoContexto.podeEditar(temSelecao, modoLixeira));

		view.getBtnResetarSenha().setEnabled(permissaoContexto.podeResetarSenha(temSelecao, modoLixeira));

		view.getBtnExcluir().setEnabled(permissaoContexto.podeExcluirUsuario(sel, modoLixeira));
	}

	private void alternarModoLixeira() {

		boolean modoLixeira = view.getChkVerExcluidos().isSelected();

		view.getBtnExcluir().setText(modoLixeira ? "RESTAURAR" : "EXCLUIR");

		view.getBtnNovo().setEnabled(!modoLixeira && permissaoContexto.temWrite());

		carregarDados();
	}

	private void carregarDados() {

		boolean verExcluidos = view.getChkVerExcluidos().isSelected();

		runAsyncSilent(getWindow(),
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

			runAsync(getWindow(), () -> {

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

			runAsync(getWindow(), () -> {

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

			runAsync(getWindow(), () -> authService.resetarSenha(sel.getIdUsuario(), usuarioLogado), senhaPadrao -> {

				DialogFactory.informacao(view, "SENHA RESETADA COM SUCESSO!\n" + "NOVA SENHA: " + senhaPadrao);

				carregarDados();
			});
		}
	}

	private void cancelarDebounce() {

		if (debounceTask != null) {
			debounceTask.cancel(false);
		}
	}

	private void filtrar() {

		cancelarDebounce();

		debounceTask = scheduler.schedule(() -> {

			if (view.isClosed()) {
				return;
			}

			SwingUtilities.invokeLater(() -> {

				String termo = view.getTxtBusca().getText();

				boolean verExcluidos = view.getChkVerExcluidos().isSelected();

				runAsyncSilent(getWindow(),
						() -> verExcluidos ? service.listarExcluidosVisiveis(usuarioLogado)
								: service.listarUsuariosVisiveis(termo, usuarioLogado),
						lista -> view.getTableModel().setDados(lista));
			});

		}, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void dispose() {
		cancelarDebounce();
		super.dispose();
	}

}