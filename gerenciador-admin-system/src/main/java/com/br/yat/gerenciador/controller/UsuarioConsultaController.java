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
	}

	private void configurar() {
		registrarAcoes();
		configurarFiltros();
		Usuario logado = Sessao.getUsuario();
		if (logado != null) {
			boolean podeVerLixeira = logado.isMaster() || 
                    service.carregarPermissoesAtivas(logado.getIdUsuario())
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

			// REGRA: Se for lixeira, habilita Restaurar. Se não, exclui (protegendo Master)
			if (modoLixeira) {
				view.getBtnExcluir().setEnabled(temSelecao);
			} else {
				view.getBtnExcluir().setEnabled(temSelecao && !sel.isMaster());
			}
		});
	}

	private void carregarDados() {
		boolean verExcluidos = view.getChkVerExcluidos().isSelected();

		// Use runAsyncSilent aqui para evitar que o "Loading" trave a tela em consultas
		// rápidas
		runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
			List<Usuario> lista = verExcluidos ? service.listarExcluidos(Sessao.getUsuario())
					: service.listarUsuarios("");

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

		// 1. Confirmação (ainda na Thread do Swing)
		boolean confirmou = DialogFactory.confirmacao(view,
				"DESEJA REALMENTE EXCLUIR O USUÁRIO: " + sel.getNome().toUpperCase() + "?");

		if (confirmou) {
			// 2. Uso do seu runAsync padrão da BaseController
			// Passamos: a view, a tarefa (TaskWithResult), e o que fazer no sucesso
			// (Consumer)
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {

				// Aqui roda em Virtual Thread (Executor da BaseController)
				service.excluirUsuario(sel.getIdUsuario(), Sessao.getUsuario());
				return null; // TaskWithResult precisa retornar algo, como é void usamos null

			}, unused -> {
				// Aqui volta para a UI Thread (SwingUtilities.invokeLater da BaseController)
				DialogFactory.informacao(view, "USUÁRIO EXCLUÍDO COM SUCESSO!");
				carregarDados(); // Atualiza a tabela
			});
		}
	}

	private void filtrar() {
		String termo = view.getTxtBusca().getText();
		boolean verExcluidos = view.getChkVerExcluidos().isSelected(); // Adicionado

		if (debounceTask != null)
			debounceTask.cancel(false);

		debounceTask = scheduler.schedule(() -> {
			// Passamos a janela correta em vez de null
			runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
				// Se estiver na lixeira, você pode decidir se filtra os excluídos ou se limpa a
				// busca
				// Por enquanto, vamos manter a lógica de listarUsuarios, mas o ideal seria o
				// DAO filtrar deletados
				List<Usuario> lista = verExcluidos ? service.listarExcluidos(Sessao.getUsuario())
						: service.listarUsuarios(termo);

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

	private void abrirFormulario(Usuario usuario) {
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
}
