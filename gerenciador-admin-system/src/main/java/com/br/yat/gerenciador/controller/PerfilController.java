package com.br.yat.gerenciador.controller;

import java.util.*;
import javax.swing.SwingUtilities;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuChaveGrouper;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.PerfilView;

public class PerfilController extends BaseCadastroController<PerfilView> {

	private final PerfilService service;
	private Perfil perfilAtual;
	private RefreshCallback refreshCallback;

	public PerfilController(PerfilView view, PerfilService service) {
		super(view);
		this.service = service;
		inicializar();
	}

	private void inicializar() {
		Usuario logado = Objects.requireNonNull(Sessao.getUsuario(), "USUÁRIO NÃO ENCONTRADO.");
		ValidationUtils.createDocumentFilter(view.getTxtNome());

		Map<String, List<MenuChave>> grupos = logado.isMaster() ? MenuChaveGrouper.groupByCategoria()
				: MenuChaveGrouper
						.groupByCategoriaFiltrado(Optional.ofNullable(Sessao.getPermissoes()).orElse(List.of()));
		view.getTxtNome().addFocusListener(ValidationUtils.createValidationListener(view.getTxtNome(), () -> {
			if (ValidationUtils.isEmpty(view.getNome())) {
				ValidationUtils.exibirErro(view.getTxtNome(), "NOME DO PERFIL É OBRIGATÓRIO.");
			}
		}));
		view.construirGradePermissoes(grupos);
		configurarAcoesBase(this::salvar, this::novoPerfil);
		carregarDadosIniciais();
		aplicarPermissoesDaTela();
	}

	private void carregarDadosIniciais() {
		view.setCamposHabilitados(false);
		view.getBtnNovo().setEnabled(true);
		view.getBtnSalvar().setEnabled(false);
	}

	public void carregarParaEdicao(Perfil perfil) {
		this.perfilAtual = perfil;
		view.limpar();
		view.setNome(perfil.getNome());
		view.setDescricao(perfil.getDescricao());

		boolean isMaster = "MASTER".equalsIgnoreCase(perfil.getNome());
		view.setEdicaoNomeHabilitada(!isMaster);

		view.entrarModoEdicao(isMaster);
		atualizarEstadoInterface();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.listarPermissoesDoPerfil(perfil.getIdPerfil()),
				permissoes -> {
					if (isMaster) {
						marcarTodasPermissoesNaGrade();
					} else if (permissoes != null) {
						permissoes.forEach(p -> {
							try {
								view.setPermissao(MenuChave.valueOf(p.getChave()), TipoPermissao.valueOf(p.getTipo()),
										true);
							} catch (Exception ex) {
								System.err.println("PERMISSÃO INVÁLIDA AO CARREGAR PERFIL: " + ex.getMessage());
							}
						});
					}
					atualizarEstadoCheckBoxesCategoria();
					view.getBtnNovo().setEnabled(false);
					view.getBtnSalvar().setEnabled(true);
				});
	}

	private void salvar() {
		if (ValidationUtils.isEmpty(view.getNome())) {
			DialogFactory.aviso(view, "NOME DO PERFIL É OBRIGATÓRIO!");
			return;
		}

		if (perfilAtual == null)
			perfilAtual = new Perfil();
		perfilAtual.setNome(view.getNome().trim());
		perfilAtual.setDescricao(view.getDescricao());

		Map<MenuChave, List<String>> permissoes = coletarPermissoesMarcadas();
		if (permissoes.isEmpty()) {
			DialogFactory.aviso(view, "SELECIONE AO MENOS UMA PERMISSÃO!");
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarPerfil(perfilAtual, permissoes, Sessao.getUsuario());
			return true;
		}, sucesso -> {
			DialogFactory.informacao(view, "PERFIL SALVO COM SUCESSO!");
			novoPerfil();
			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();
		});
	}

	private void aplicarPermissoesDaTela() {
		Usuario logado = Sessao.getUsuario();
		if (logado == null || logado.isMaster())
			return;
		runAsync(SwingUtilities.getWindowAncestor(view),
				() -> service.listarPermissoesAtivasPorMenu(logado.getIdUsuario(), MenuChave.CONFIGURACAO_PERMISSAO), p -> {
					if (!aplicarRestricoesVisuais(p, view.getBtnNovo(), null, null))
						view.doDefaultCloseAction();
					view.getBtnSalvar().setVisible(p.contains(TipoPermissao.WRITE.name()));
				});
	}

	private void novoPerfil() {
		this.perfilAtual = null;
		view.limpar();
		view.setEdicaoNomeHabilitada(true);
		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
		view.setCamposHabilitados(true);
		atualizarEstadoInterface();
	}

	private void atualizarEstadoInterface() {
		boolean isNovo = (perfilAtual == null || perfilAtual.getIdPerfil() == null);
		view.setTextoBotaoSalvar(isNovo ? "SALVAR" : "ALTERAR");
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}
}