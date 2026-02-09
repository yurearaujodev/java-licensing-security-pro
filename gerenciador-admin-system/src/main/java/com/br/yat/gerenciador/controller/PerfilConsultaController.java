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
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.PerfilConsultaView;
import com.br.yat.gerenciador.view.PerfilView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.TableFactory;

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
		
		aplicarPermissoesEscopo();
		
		carregarDados();
	}

	private void configurarFiltros() {
		// Adiciona o listener de busca automática (igual ao de usuários)
		view.getTxtBusca().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtBusca(), this::filtrar));
	}

	private void registrarAcoes() {
		view.getBtnNovo().addActionListener(e -> abrirFormulario(null));
		view.getBtnEditar().addActionListener(e -> editarSelecionado());
		view.getBtnExcluir().addActionListener(e -> excluirSelecionado());
		view.getBtnPesquisar().addActionListener(e -> carregarDados());

		TableFactory.addDoubleClickAction(view.getTabela(), this::editarSelecionado);

		// Controle de estado dos botões (Double Check de segurança)
		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			Perfil sel = view.getSelecionado();
			boolean temSelecao = (sel != null);
			
			view.getBtnEditar().setEnabled(temSelecao);
			
			// Não permite excluir o MASTER nem se estiver selecionado
			if (temSelecao) {
				view.getBtnExcluir().setEnabled(!"MASTER".equalsIgnoreCase(sel.getNome()));
			} else {
				view.getBtnExcluir().setEnabled(false);
			}
		});
	}

	private void carregarDados() {		
		runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
			// Ajustar service para aceitar o termo de busca se necessário
			return service.listarTodos(); 
		}, lista -> view.getTableModel().setDados(lista));
	}

	private void filtrar() {
		String termo = view.getTxtBusca().getText();

		if (debounceTask != null)
			debounceTask.cancel(false);

		// Debounce de 500ms para não sobrecarregar o banco
		debounceTask = scheduler.schedule(() -> {
			runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
				// Aqui você pode filtrar a lista na memória ou criar um service.listar(termo)
				List<Perfil> lista = service.listarTodos();
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
	        // Opcional: Avisar que o MASTER tem restrições
	        if ("MASTER".equalsIgnoreCase(sel.getNome())) {
	             // Você pode passar um flag para o controller desabilitar o campo TXT_NOME
	        }
	        abrirFormulario(sel);
	    } else {
			DialogFactory.aviso(view, "SELECIONE UM PERFIL PARA EDITAR.");
		}
	}

	private void excluirSelecionado() {
		Perfil sel = view.getSelecionado();
		if (sel == null) return;

		if (DialogFactory.confirmacao(view, "DESEJA REALMENTE EXCLUIR O PERFIL: " + sel.getNome().toUpperCase() + "?")) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				service.excluirPerfil(sel.getIdPerfil(), Sessao.getUsuario());
				return null;
			}, unused -> {
				DialogFactory.informacao(view, "PERFIL EXCLUÍDO COM SUCESSO!");
				carregarDados();
			});
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

	private void abrirFormulario(Perfil perfil) {
		JDesktopPane desk = view.getDesktopPane();
		String idJanela = (perfil == null) ? "NOVO_PERFIL" : "EDIT_PERFIL_" + perfil.getIdPerfil();

		if (DesktopUtils.reuseIfOpen(desk, idJanela))
			return;

		PerfilView formView = new PerfilView();
		formView.setName(idJanela);

		PerfilController controller = new PerfilController(formView, service);
		formView.putClientProperty("controller", controller); 
		controller.setRefreshCallback(this::carregarDados);

		if (perfil != null) {
			controller.carregarParaEdicao(perfil);
		}

		DesktopUtils.showFrame(desk, formView);
	}
}