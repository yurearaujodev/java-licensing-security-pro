package com.br.yat.gerenciador.controller;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
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
	private ScheduledFuture<?> debounceTask;

	public UsuarioConsultaController(UsuarioConsultaView view, UsuarioService service) {
		this.view = view;
		this.service = service;
		configurar();
		aplicarPermissoesEscopo();
	}

	private void configurar() {
		registrarAcoes();
		configurarFiltros();
		Usuario logado = Sessao.getUsuario();
		if (logado != null) {
			boolean podeVerLixeira = logado.isMaster() || service.carregarPermissoesAtivas(logado.getIdUsuario())
					.contains(MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES);
			view.getChkVerExcluidos().setVisible(podeVerLixeira);
		}

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
			if (view.getChkVerExcluidos().isSelected()) {
				restaurarSelecionado();
			} else {
				excluirSelecionado();
			}
		});
		TableFactory.addDoubleClickAction(view.getTabela(), this::editarSelecionado);
		view.getChkVerExcluidos().addActionListener(e -> {
			boolean modoLixeira = view.getChkVerExcluidos().isSelected();
			view.getBtnExcluir().setText(modoLixeira ? "RESTAURAR" : "EXCLUIR");
			view.getBtnNovo().setEnabled(!modoLixeira);
			carregarDados();
		});
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			Usuario sel = view.getSelecionado();
			boolean temSelecao = (sel != null);
			boolean modoLixeira = view.getChkVerExcluidos().isSelected();

			view.getBtnEditar().setEnabled(temSelecao && !modoLixeira);
			
			if (modoLixeira) {
				view.getBtnExcluir().setEnabled(temSelecao);
			} else {
				view.getBtnExcluir().setEnabled(temSelecao && !sel.isMaster());
			}
		});
	}

	private void carregarDados() {
		boolean verExcluidos = view.getChkVerExcluidos().isSelected();

		runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
			List<Usuario> lista = verExcluidos ? service.listarExcluidos(Sessao.getUsuario())
					: service.listarUsuarios("",Sessao.getUsuario());

			Usuario logado = Sessao.getUsuario();
			if (logado != null && !logado.isMaster()) {
				lista.removeIf(u -> u.isMaster());
			}
			return lista;
		}, lista -> view.getTableModel().setDados(lista));
	}

	private void restaurarSelecionado() {
		Usuario sel = view.getSelecionado();
		if (sel == null)
			return;

		if (DialogFactory.confirmacao(view, "DESEJA RESTAURAR O USUÁRIO: " + sel.getNome().toUpperCase() + "?")) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.restaurarUsuario(sel.getIdUsuario(), Sessao.getUsuario());
				return null;
			}, unused -> {
				DialogFactory.informacao(view, "USUÁRIO RESTAURADO COM SUCESSO!");
				carregarDados();
			});
		}
	}

	private void excluirSelecionado() {
		Usuario sel = view.getSelecionado();
		if (sel == null)
			return;

		boolean confirmou = DialogFactory.confirmacao(view,
				"DESEJA REALMENTE EXCLUIR O USUÁRIO: " + sel.getNome().toUpperCase() + "?");

		if (confirmou) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {

				service.excluirUsuario(sel.getIdUsuario(), Sessao.getUsuario());
				return null;

			}, unused -> {
				DialogFactory.informacao(view, "USUÁRIO EXCLUÍDO COM SUCESSO!");
				carregarDados();
			});
		}
	}

	private void filtrar() {
		String termo = view.getTxtBusca().getText();
		boolean verExcluidos = view.getChkVerExcluidos().isSelected(); // Adicionado

		if (debounceTask != null)
			debounceTask.cancel(false);

		debounceTask = scheduler.schedule(() -> {
			runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
				List<Usuario> lista = verExcluidos ? service.listarExcluidos(Sessao.getUsuario())
						: service.listarUsuarios(termo,Sessao.getUsuario());

				Usuario logado = Sessao.getUsuario();
				if (logado != null && !logado.isMaster()) {
					lista.removeIf(u -> u.isMaster());
				}
				return lista;
			}, lista -> view.getTableModel().setDados(lista));
		}, 500, TimeUnit.MILLISECONDS);
	}

	private void editarSelecionado() {
		Usuario sel = view.getSelecionado();
		if (sel != null) {
			abrirFormulario(sel);
		} else {
			DialogFactory.aviso(view, "SELECIONE UM USUÁRIO PARA EDITAR.");
		}
	}
	
	private void aplicarPermissoesEscopo() {
	    Usuario logado = Sessao.getUsuario();
	    if (logado == null || logado.isMaster()) return;

	    // Busca as permissões na service
	    List<String> permissoes = service.listarPermissoesAtivasPorMenu(
	        logado.getIdUsuario(), 
	        MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES
	    );

	    // Usa o método da BaseController
	    boolean podeAcessar = aplicarRestricoesVisuais(
	        permissoes, 
	        view.getBtnNovo(), 
	        view.getBtnEditar(), 
	        view.getBtnExcluir()
	    );

	    if (!podeAcessar) {
	        view.dispose();
	        DialogFactory.aviso(null, "ACESSO NEGADO À GESTÃO DE USUÁRIOS.");
	    }
	}
	private void abrirFormulario(Usuario usuario) {
		if (!podeAbrirCadastro()) {
			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR O CADASTRO DE USUÁRIOS.");
			return;
		}

		JDesktopPane desk = view.getDesktopPane();
		String idJanela = (usuario == null) ? "NOVO_USUARIO" : "EDIT_USUARIO_" + usuario.getIdUsuario();

		if (DesktopUtils.reuseIfOpen(desk, idJanela))
			return;

		UsuarioView cadastroView = ViewFactory.createUsuarioView();
		cadastroView.setName(idJanela);

		UsuarioController controller = (UsuarioController) cadastroView.getClientProperty("controller");
		controller.setRefreshCallback(this::carregarDados);

		if (usuario != null) {
			controller.carregarUsuarioParaEdicao(usuario);
		} else {
			controller.novoUsuario();
		}

		DesktopUtils.showFrame(desk, cadastroView);
	}

	private boolean podeAbrirCadastro() {
		Usuario logado = Sessao.getUsuario();
		return logado != null && (logado.isMaster()
				|| service.carregarPermissoesAtivas(logado.getIdUsuario()).contains(MenuChave.CADASTROS_USUARIO));
	}

}
