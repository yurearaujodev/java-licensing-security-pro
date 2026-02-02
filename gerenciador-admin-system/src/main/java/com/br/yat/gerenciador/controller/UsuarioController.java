package com.br.yat.gerenciador.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuChaveGrouper;
import com.br.yat.gerenciador.view.UsuarioView;

public class UsuarioController extends BaseController {
	private final UsuarioView view;
	private final UsuarioService service;
	private Usuario usuarioAtual;
	private RefreshCallback refreshCallback;

	public UsuarioController(UsuarioView view, UsuarioService service) {
		this.view = view;
		this.service = service;
		inicializar();
	}

	private void inicializar() {
		Usuario logado = Sessao.getUsuario();
		Map<String, List<MenuChave>> grupos;

		if (logado != null && logado.isMaster()) {
			grupos = MenuChaveGrouper.groupByCategoria();
		} else {
			List<MenuChave> minhasPermissoes = Sessao.getPermissoes();
			grupos = MenuChaveGrouper.groupByCategoriaFiltrado(minhasPermissoes);
		}

		view.construirGradePermissoes(grupos);
		registrarAcoes();
		carregarEmpresaPadrao();
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}

	private void registrarAcoes() {
		view.getChkTodosPorCategoria().forEach((categoria, chkTodos) -> {
			chkTodos.addActionListener(e -> view.marcarTodosDaCategoria(categoria, chkTodos.isSelected()));
		});

		view.getBtnSalvar().addActionListener(e -> salvarUsuario());
		view.getBtnCancelar().addActionListener(e -> view.doDefaultCloseAction());
		view.getBtnNovo().addActionListener(e -> novoUsuario());
	}

	public void novoUsuario() {
		this.usuarioAtual = null;
		view.desativarAtivar(true);
		view.setMaster(false);
		view.getChkMaster().setEnabled(false);

		view.limpar();
		carregarEmpresaPadrao();
	}

	public void carregarUsuarioParaEdicao(Usuario usuario) {
		this.usuarioAtual = usuario;
		view.limpar();
		view.setNome(usuario.getNome());
		view.setEmail(usuario.getEmail());
		view.setStatus(usuario.getStatus());
		view.setMaster(usuario.isMaster());

		view.getChkMaster().setEnabled(false);

		if (usuario.getIdEmpresa() != null) {
			view.setEmpresa(usuario.getIdEmpresa().getIdEmpresa(), usuario.getIdEmpresa().getRazaoSocialEmpresa());
		}

		view.desativarAtivar(true);

		if (usuario.isMaster()) {
			view.bloquearStatus(false);
			view.getChkMaster().setEnabled(false);
		} else {
			view.bloquearStatus(true);
			view.getChkMaster().setEnabled(false);
		}

		runAsync(null, () -> service.carregarPermissoesAtivas(usuario.getIdUsuario()), chavesDoBanco -> {
			view.desmarcarTodasPermissoes();
			for (MenuChave chave : chavesDoBanco) {
				view.setPermissaoSelecionada(chave, true);
			}

			if (usuario.isMaster()) {
				view.bloquearGradePermissoes(false);
			} else {
				view.bloquearGradePermissoes(true);
			}

			Map<String, List<MenuChave>> grupos = view.getGruposPermissoes();
			grupos.forEach((cat, lista) -> {
				view.atualizarStatusMarcarTodos(cat, chavesDoBanco.containsAll(lista));
			});
		});
	}

	private void salvarUsuario() {
		if (usuarioAtual == null)
			usuarioAtual = new Usuario();

		usuarioAtual.setNome(view.getNome());
		usuarioAtual.setEmail(view.getEmail());
		usuarioAtual.setSenhaHash(view.getSenha());
		usuarioAtual.setStatus(view.getStatus());
		usuarioAtual.setMaster(view.isMaster());

		Empresa emp = new Empresa();
		emp.setIdEmpresa(view.getEmpresa());
		usuarioAtual.setIdEmpresa(emp);

		List<MenuChave> permissoes = coletarPermissoesMarcadas();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarUsuarioCompleto(usuarioAtual, permissoes, Sessao.getUsuario());
			return true;
		}, sucesso -> {
			DialogFactory.informacao(view, "USUÁRIO SALVO COM SUCESSO!");
			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();
			view.doDefaultCloseAction();
		});
	}

	private List<MenuChave> coletarPermissoesMarcadas() {
		List<MenuChave> marcadas = new ArrayList<>();
		view.getPermissoes().forEach((chave, chk) -> {
			if (chk.isSelected())
				marcadas.add(chave);
		});
		return marcadas;
	}

	private void carregarEmpresaPadrao() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.buscarEmpresaFornecedora(), empresa -> {
			if (empresa != null) {
				SwingUtilities.invokeLater(() -> {
					view.setEmpresa(empresa.getIdEmpresa(), empresa.getRazaoSocialEmpresa());
				});
			}
		});
	}

}
