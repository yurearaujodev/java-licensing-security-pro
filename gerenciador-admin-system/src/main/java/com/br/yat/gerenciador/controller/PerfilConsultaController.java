package com.br.yat.gerenciador.controller;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
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

	public PerfilConsultaController(PerfilConsultaView view, PerfilService service) {
		this.view = view;
		this.service = service;
		configurar();
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

		Usuario logado = Sessao.getUsuario();

		if (logado != null && view.getChkVerExcluidos() != null) {

			boolean podeVerLixeira = logado.isMaster()
					|| service.listarPermissoesAtivasPorMenu(logado.getIdUsuario(), MenuChave.CONFIGURACAO_PERMISSAO)
							.contains(TipoPermissao.DELETE.name());

			view.getChkVerExcluidos().setVisible(podeVerLixeira);

			view.getChkVerExcluidos().addActionListener(e -> {
				boolean modoLixeira = view.getChkVerExcluidos().isSelected();

				view.getBtnExcluir().setText(modoLixeira ? "RESTAURAR" : "EXCLUIR");

				view.getBtnNovo().setEnabled(!modoLixeira);

				carregarDados();
			});
		}
	}

	private void atualizarBotoes() {

		Perfil sel = view.getSelecionado();
		boolean temSelecao = (sel != null);
		boolean modoLixeira = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		view.getBtnEditar().setEnabled(temSelecao && !modoLixeira);

		if (!temSelecao) {
			view.getBtnExcluir().setEnabled(false);
			return;
		}

		boolean podeExcluir;

		if (modoLixeira) {
			podeExcluir = true;
		} else {
			podeExcluir = !"MASTER".equalsIgnoreCase(sel.getNome());
		}

		view.getBtnExcluir().setEnabled(podeExcluir);
	}

	private void carregarDados() {

		boolean verExcluidos = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> verExcluidos ? service.listarExcluidos(Sessao.getUsuario()) : service.listarTodos(),
				lista -> view.getTableModel().setDados(lista));
	}

	private void filtrar() {

		String termo = view.getTxtBusca().getText();
		boolean verExcluidos = view.getChkVerExcluidos() != null && view.getChkVerExcluidos().isSelected();

		if (debounceTask != null) {
			debounceTask.cancel(false);
		}

		debounceTask = scheduler.schedule(() -> {

			runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
				List<Perfil> lista = verExcluidos ? service.listarExcluidos(Sessao.getUsuario())
						: service.listarTodos();

				if (!termo.isEmpty()) {
					lista.removeIf(p -> !p.getNome().toLowerCase().contains(termo.toLowerCase()));
				}

				return lista;
			}, lista -> view.getTableModel().setDados(lista));

		}, 500, TimeUnit.MILLISECONDS);
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

		boolean confirmou = DialogFactory.confirmacao(view,
				"DESEJA REALMENTE EXCLUIR O PERFIL: " + sel.getNome().toUpperCase() + "?");

		if (confirmou) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.excluirPerfil(sel.getIdPerfil(), Sessao.getUsuario());
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

		Usuario logado = Sessao.getUsuario();

		if (logado == null || logado.isMaster())
			return;

		List<String> permissoes = service.listarPermissoesAtivasPorMenu(logado.getIdUsuario(),
				MenuChave.CONFIGURACAO_PERMISSAO);

		boolean podeAcessar = aplicarRestricoesVisuais(permissoes, view.getBtnNovo(), view.getBtnEditar(),
				view.getBtnExcluir());

		if (!podeAcessar) {
			view.dispose();
			DialogFactory.aviso(null, "ACESSO NEGADO À GESTÃO DE PERFIS.");
			return;
		}

		runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> service.carregarPermissoesDetalhadas(logado.getIdUsuario()),
				minhas -> SwingUtilities.invokeLater(() -> {

					boolean podeCriarAlterar = minhas.stream()
							.anyMatch(p -> p.getChave().equals(MenuChave.CONFIGURACAO_PERMISSAO.name())
									&& p.getTipo().equals(TipoPermissao.WRITE.name()));

					boolean podeExcluir = minhas.stream()
							.anyMatch(p -> p.getChave().equals(MenuChave.CONFIGURACAO_PERMISSAO.name())
									&& p.getTipo().equals(TipoPermissao.DELETE.name()));

					view.getBtnNovo().setEnabled(podeCriarAlterar);
					view.getBtnEditar().setEnabled(podeCriarAlterar);
					view.getBtnExcluir().setEnabled(podeExcluir);
				}));
	}
	
	private void abrirFormulario(Perfil perfil) {
	    // 1. Validação de Acesso
	    if (!podeAbrirCadastro()) {
	        DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR O CADASTRO DE PERFIS.");
	        return;
	    }

	    JDesktopPane desk = view.getDesktopPane();

	    // 2. Definição do ID Único para reuso (ID de controle do DesktopUtils)
	    String idJanela = (perfil == null) ? "NOVO_PERFIL" : "EDIT_PERFIL_" + perfil.getIdPerfil();

	    // 3. Tenta reutilizar se já estiver aberta
	    if (DesktopUtils.reuseIfOpen(desk, idJanela)) {
	        return;
	    }

	    // 4. Cria a View através da Factory (Igual ao modelo do Usuário)
	    // Isso já traz a View com o Controller e Service injetados
	    PerfilView formView = ViewFactory.createPerfilView();
	    formView.setName(idJanela); 

	    // 5. Recupera o Controller pela propriedade do cliente para configurar o callback
	    PerfilController controller = (PerfilController) formView.getClientProperty("controller");
	    controller.setRefreshCallback(this::carregarDados);

	    // 6. Lógica de Título e Carregamento (Padronizado com Usuário)
	    if (perfil != null) {
	        formView.setTitle("EDITANDO PERFIL: " + perfil.getNome().toUpperCase());
	        controller.carregarParaEdicao(perfil);
	    } else {
	        formView.setTitle("NOVO PERFIL");
	        // Se o seu PerfilController tiver o método novoPerfil(), chame-o aqui:
	        // controller.novoPerfil(); 
	    }

	    // 7. Exibe a janela
	    DesktopUtils.showFrame(desk, formView);
	}

//	private void abrirFormulario(Perfil perfil) {
//
//		if (!podeAbrirCadastro()) {
//			DialogFactory.erro(view, "VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR O CADASTRO DE PERFIS.");
//			return;
//		}
//
//		JDesktopPane desk = view.getDesktopPane();
//
//		String idJanela = (perfil == null) ? "NOVO_PERFIL" : "EDIT_PERFIL_" + perfil.getIdPerfil();
//
//		if (DesktopUtils.reuseIfOpen(desk, idJanela))
//			return;
//
//		PerfilView formView = new PerfilView();
//		formView.setName(idJanela);
//
//		PerfilController controller = new PerfilController(formView, service);
//
//		formView.putClientProperty("controller", controller);
//		controller.setRefreshCallback(this::carregarDados);
//
//		if (perfil != null) {
//			controller.carregarParaEdicao(perfil);
//		}
//
//		DesktopUtils.showFrame(desk, formView);
//	}

	private boolean podeAbrirCadastro() {

		Usuario logado = Sessao.getUsuario();

		if (logado == null)
			return false;
		if (logado.isMaster())
			return true;

		List<String> permissoes = service.listarPermissoesAtivasPorMenu(logado.getIdUsuario(),
				MenuChave.CONFIGURACAO_PERMISSAO);

		return permissoes.contains(TipoPermissao.WRITE.name());
	}
}
