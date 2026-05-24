package com.br.yat.gerenciador.controller;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.security.PermissaoContexto;
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.service.UsuarioPermissaoService;
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
	private final UsuarioPermissaoService usuarioPermissaoService;

	private ScheduledFuture<?> debounceTask;

	private Usuario usuarioLogado;
	private PermissaoContexto permissaoContexto;

	public PerfilConsultaController(PerfilConsultaView view, PerfilService service,
			UsuarioPermissaoService usuarioPermissaoService) {

		this.view = view;
		this.service = service;
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
				MenuChave.CONFIGURACAO_PERMISSAO), ctx -> {

					if (ctx == null || !ctx.temRead()) {

						DialogFactory.aviso(view, "ACESSO NEGADO À GESTÃO DE PERFIS.");

						view.dispose();

						return;
					}

					this.permissaoContexto = ctx;

					configurar();
				});
	}

	private void configurar() {

		if (!aplicarRestricoesVisuais(permissaoContexto, view.getBtnNovo(), view.getBtnEditar(),
				view.getBtnExcluir())) {

			DialogFactory.aviso(view, "ACESSO NEGADO À GESTÃO DE PERFIS.");

			view.dispose();

			return;
		}

		registrarAcoes();

		configurarFiltros();

		configurarControleLixeira();

		carregarDados();
	}

	private Window getWindow() {
		return SwingUtilities.getWindowAncestor(view);
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

	private void registrarAcoes() {

		resetListeners(view.getBtnNovo());
		resetListeners(view.getBtnEditar());
		resetListeners(view.getBtnExcluir());
		resetListeners(view.getBtnPesquisar());

		if (view.getChkVerExcluidos() != null) {
			resetListeners(view.getChkVerExcluidos());
		}

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

		TableFactory.addDoubleClickAction(view.getTabela(), () -> {

			boolean modoLixeira = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

			if (!modoLixeira) {
				editarSelecionado();
			}
		});

		view.getTabela().getSelectionModel().addListSelectionListener(e -> atualizarBotoes());
	}

	private void configurarControleLixeira() {

		if (view.getChkVerExcluidos() == null)
			return;

		view.getChkVerExcluidos().setVisible(permissaoContexto.temDelete());

		view.getChkVerExcluidos().addActionListener(e -> alternarModoLixeira());
	}

	private void alternarModoLixeira() {

		boolean modoLixeira = view.getChkVerExcluidos().isSelected();

		view.getBtnExcluir().setText(modoLixeira ? "RESTAURAR" : "EXCLUIR");

		view.getBtnNovo().setEnabled(!modoLixeira && permissaoContexto.temWrite());

		view.getTabela().clearSelection();

		carregarDados();
	}

	private void atualizarBotoes() {

		Perfil sel = view.getSelecionado();

		boolean modoLixeira = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		view.getBtnEditar().setEnabled(permissaoContexto.podeEditar(sel != null, modoLixeira));

		view.getBtnExcluir().setEnabled(permissaoContexto.podeExcluirPerfil(sel, modoLixeira));
	}

	private void carregarDados() {

		boolean verExcluidos = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		runAsyncSilent(getWindow(), () -> service.listarPerfisVisiveis("", verExcluidos, usuarioLogado), lista -> {

			view.getTableModel().setDados(lista);

			atualizarBotoes();
		});
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

				boolean verExcluidos = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

				runAsyncSilent(getWindow(), () -> service.listarPerfisVisiveis(termo, verExcluidos, usuarioLogado),
						lista -> {

							view.getTableModel().setDados(lista);

							atualizarBotoes();
						});
			});

		}, 500, TimeUnit.MILLISECONDS);
	}

	private void editarSelecionado() {

		Perfil sel = view.getSelecionado();

		if (sel == null) {

			DialogFactory.aviso(view, "SELECIONE UM PERFIL PARA EDITAR.");

			return;
		}

		abrirFormulario(sel);
	}

	private void excluirSelecionado() {

		if (!permissaoContexto.temDelete()) {

			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA EXCLUIR.");

			return;
		}

		Perfil sel = view.getSelecionado();

		if (sel == null)
			return;

		boolean confirmou = DialogFactory.confirmacao(view,
				"DESEJA REALMENTE EXCLUIR O PERFIL: " + sel.getNome().toUpperCase() + "?");

		if (confirmou) {

			runAsync(getWindow(), () -> {

				service.excluirPerfil(sel.getIdPerfil(), usuarioLogado);

				return null;

			}, unused -> {

				DialogFactory.informacao(view, "PERFIL EXCLUÍDO COM SUCESSO!");

				carregarDados();
			});
		}
	}

	private void restaurarSelecionado() {

		if (!permissaoContexto.temDelete()) {

			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA RESTAURAR.");

			return;
		}

		Perfil sel = view.getSelecionado();

		if (sel == null)
			return;

		if (DialogFactory.confirmacao(view, "DESEJA RESTAURAR O PERFIL: " + sel.getNome().toUpperCase() + "?")) {

			runAsync(getWindow(), () -> {

				service.restaurarPerfil(sel.getIdPerfil(), usuarioLogado);

				return null;

			}, unused -> {

				DialogFactory.informacao(view, "PERFIL RESTAURADO COM SUCESSO!");

				carregarDados();
			});
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

	@Override
	public void dispose() {

		cancelarDebounce();

		super.dispose();
	}
}