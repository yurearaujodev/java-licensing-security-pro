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
		configurarFiltro();
	}

	private void inicializar() {
		carregarGruposDeMenu();
		registrarAcoes();
		configurarAcoesBase(this::salvar, this::novoPerfil);
		carregarDadosIniciais();
		aplicarPermissoesDaTela();
	}

	private void registrarAcoes() {
		view.getTxtNome()
				.addFocusListener(ValidationUtils.createValidationListener(view.getTxtNome(), this::validarNome));

	}

	private void carregarGruposDeMenu() {

		Usuario logado = Sessao.getUsuario();

		if (logado == null) {
			view.doDefaultCloseAction();
			return;
		}

		Map<String, List<MenuChave>> grupos = logado.isMaster() ? MenuChaveGrouper.groupByCategoria()
				: MenuChaveGrouper
						.groupByCategoriaFiltrado(Optional.ofNullable(Sessao.getPermissoes()).orElse(List.of()));

		view.construirGradePermissoes(grupos);
	}

	private void validarNome() {
		if (ValidationUtils.isEmpty(view.getNome())) {
			ValidationUtils.exibirErro(view.getTxtNome(), "NOME DO PERFIL É OBRIGATÓRIO.");
		}
	}

	private void configurarFiltro() {
		ValidationUtils.createDocumentFilter(view.getTxtNome(), view.getTxtDescricao());
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

		boolean perfilEhMaster = service.isPerfilMaster(perfil);

		view.setEdicaoNomeHabilitada(!perfilEhMaster);
		view.entrarModoEdicao(perfilEhMaster);
		atualizarEstadoInterface();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.listarPermissoesDoPerfil(perfil.getIdPerfil()),
				permissoes -> {
					if (permissoes != null) {
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

		Usuario logado = Sessao.getUsuario();
		if (logado == null) {
			DialogFactory.erro(view, "SESSÃO EXPIRADA.");
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarPerfil(perfilAtual, permissoes, logado);
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

		if (logado == null) {
			view.doDefaultCloseAction();
			return;
		}

		if (logado.isMaster()) {
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view),
				() -> service.obterContextoPermissao(logado.getIdUsuario(), MenuChave.CONFIGURACAO_PERMISSAO), ctx -> {

					if (!ctx.temRead()) {
						view.doDefaultCloseAction();
						DialogFactory.aviso(view, "ACESSO NEGADO À CONFIGURAÇÃO DE PERFIS.");
						return;
					}

					aplicarRestricoesVisuais(ctx, view.getBtnNovo(), null, null);

					view.getBtnSalvar().setVisible(ctx.temWrite());
				});
	}

	public void novoPerfil() {
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