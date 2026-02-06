package com.br.yat.gerenciador.controller;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuChaveGrouper;
import com.br.yat.gerenciador.util.ValidationUtils;
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

		Map<String, List<MenuChave>> grupos = Optional.ofNullable(logado)
				.map(u -> u.isMaster() ? MenuChaveGrouper.groupByCategoria()
						: MenuChaveGrouper.groupByCategoriaFiltrado(
								Optional.ofNullable(Sessao.getPermissoes()).orElse(List.of())))

				.orElse(MenuChaveGrouper.groupByCategoria());

		view.construirGradePermissoes(grupos);
		registrarAcoes();
		carregarEmpresaPadrao();
		view.desativarAtivar(false);
		view.getBtnNovo().setEnabled(true);
		view.getBtnNovo().requestFocusInWindow();
		view.getBtnSalvar().setEnabled(false);
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
		view.getTxtSenha().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtSenha(), () -> {
					String senha = new String(view.getTxtSenha().getPassword());
					int forca = ValidationUtils.calcularForcaSenha(senha);

					view.getBarraForcaSenha().setValue(forca);
					switch (forca) {
					case 0, 1 -> {
						view.getBarraForcaSenha().setForeground(Color.RED);
						view.getBarraForcaSenha().setString("Fraca");
					}
					case 2 -> {
						view.getBarraForcaSenha().setForeground(Color.ORANGE);
						view.getBarraForcaSenha().setString("Média");
					}
					case 3, 4 -> {
						view.getBarraForcaSenha().setForeground(new Color(0, 128, 0));
						view.getBarraForcaSenha().setString("Forte");
					}
					}
				}));

	}

	public void novoUsuario() {
		this.usuarioAtual = null;
		view.limpar();
		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
		view.desativarAtivar(true);
		view.getTxtSenhaAntiga().setEnabled(false);
		view.setTextoBotaoSalvar("SALVAR");
		carregarEmpresaPadrao();

		view.setMaster(!service.existeUsuarioMaster());
	}

	public void carregarUsuarioParaEdicao(Usuario usuario) {
		this.usuarioAtual = usuario;
		view.limpar();
		view.setNome(usuario.getNome());
		view.setEmail(usuario.getEmail());
		view.setStatus(usuario.getStatus());
		view.setMaster(usuario.isMaster());

		view.setTextoBotaoSalvar("ALTERAR");
		if (usuario.getEmpresa() != null) {
			view.setEmpresa(usuario.getEmpresa().getIdEmpresa(), usuario.getEmpresa().getRazaoSocialEmpresa());
		}
		view.desativarAtivar(true);
		view.setMaster(usuario.isMaster());
		view.getChkMaster().setEnabled(false);
		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
		view.getTxtSenhaAntiga().setEnabled(true);
		view.bloquearStatus(!usuario.isMaster());

		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.carregarPermissoesAtivas(usuario.getIdUsuario()),
				chavesDoBanco -> {
					view.desmarcarTodasPermissoes();
					for (MenuChave chave : chavesDoBanco) {
						view.setPermissaoSelecionada(chave, true);
					}

					view.bloquearGradePermissoes(!(service.podeEditarPermissoes(usuario) && usuario.isMaster()));

					Map<String, List<MenuChave>> grupos = view.getGruposPermissoes();
					grupos.forEach((cat, lista) -> {
						view.atualizarStatusMarcarTodos(cat, chavesDoBanco.containsAll(lista));
					});
				});
	}

	private void salvarUsuario() {
		boolean isNovo = (usuarioAtual == null || usuarioAtual.getIdUsuario() == null);

		if (isNovo)
			usuarioAtual = new Usuario();

		usuarioAtual.setNome(view.getNome());
		usuarioAtual.setEmail(view.getEmail());
		char[] senha = view.getSenhaNova();
		char[] senhaAntiga = view.getSenhaAntiga();
		char[] senhaConfirmar = view.getConfirmarSenha();

		if (senha != null && senha.length > 0) {
			if (senhaConfirmar == null || !Arrays.equals(senha, senhaConfirmar)) {
				DialogFactory.erro(view, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
				return;
			}

			usuarioAtual.setSenhaHash(senha);
			usuarioAtual.setConfirmarSenha(senhaConfirmar);
			usuarioAtual.setSenhaAntiga(senhaAntiga);
		}
		StatusUsuario status = view.getStatus();

		if (status == StatusUsuario.SELECIONE) {
			DialogFactory.aviso(view, "SELECIONE UM STATUS VÁLIDO!");
			return;
		}

		usuarioAtual.setStatus(status);

		usuarioAtual.setMaster(view.isMaster());

		Empresa emp = new Empresa();
		emp.setIdEmpresa(view.getEmpresa());
		usuarioAtual.setEmpresa(emp);

		List<MenuChave> permissoes = coletarPermissoesMarcadas();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarUsuario(usuarioAtual, permissoes, Sessao.getUsuario());
			return true;
		}, sucesso -> {
			if (isNovo) {
				DialogFactory.informacao(view, "USUÁRIO SALVO COM SUCESSO!");
				view.getBtnNovo().setEnabled(true);
				view.desativarAtivar(false);
				view.limpar();
				view.getBtnSalvar().setEnabled(false);
			} else {
				DialogFactory.informacao(view, "ALTERAÇÃO REALIZADA COM SUCESSO!");
				SwingUtilities.invokeLater(() -> view.doDefaultCloseAction());
			}

			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();

		});
	}

	private List<MenuChave> coletarPermissoesMarcadas() {
		return view.getPermissoes().entrySet().stream().filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey)
				.toList();
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
