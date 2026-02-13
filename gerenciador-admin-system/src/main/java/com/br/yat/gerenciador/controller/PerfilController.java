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

public class PerfilController extends BaseController {

	private final PerfilView view;
	private final PerfilService service;
	private Perfil perfilAtual;
	private RefreshCallback refreshCallback;
	private static final TipoPermissao[] TIPOS = TipoPermissao.values();

	public PerfilController(PerfilView view, PerfilService service) {
		this.view = view;
		this.service = service;
		inicializar();
	}

	private void inicializar() {
		Usuario logado = Objects.requireNonNull(Sessao.getUsuario(), "Usuário não encontrado na sessão.");

		ValidationUtils.createDocumentFilter(view.getTxtNome());
		view.getTxtNome().addFocusListener(ValidationUtils.createValidationListener(view.getTxtNome(), () -> {
			if (ValidationUtils.isEmpty(view.getNome())) {
				ValidationUtils.exibirErro(view.getTxtNome(), "NOME DO PERFIL É OBRIGATÓRIO.");
			}
		}));
		Map<String, List<MenuChave>> grupos = logado.isMaster() ? MenuChaveGrouper.groupByCategoria()
				: MenuChaveGrouper
						.groupByCategoriaFiltrado(Optional.ofNullable(Sessao.getPermissoes()).orElse(List.of()));

		view.construirGradePermissoes(grupos);

		registrarAcoes();
		configurarMarcarTodosPorCategoria();

		view.getBtnNovo().setEnabled(true);
		view.getBtnSalvar().setEnabled(false);
	}

	private void registrarAcoes() {
		view.getBtnSalvar().addActionListener(e -> salvar());
		view.getBtnCancelar().addActionListener(e -> {
			view.getBtnNovo().setEnabled(true);
			view.getBtnSalvar().setEnabled(false);
			view.doDefaultCloseAction();
		});
		view.getBtnNovo().addActionListener(e -> novoPerfil());
	}

	private void configurarMarcarTodosPorCategoria() {

		for (String categoria : view.getCategoriasPermissoes()) {

			view.addListenerCategoria(categoria, marcado -> {
				view.marcarCategoria(categoria, marcado);
				atualizarEstadoCategorias();
			});
		}
	}

	private void atualizarEstadoCategorias() {
		for (String categoria : view.getCategoriasPermissoes()) {
			view.setCategoriaMarcada(categoria, view.isCategoriaTotalmenteMarcada(categoria));
		}
	}

	public void carregarParaEdicao(Perfil perfil) {
		this.perfilAtual = perfil;

		view.limpar();
		view.setNome(perfil.getNome());
		view.setDescricao(perfil.getDescricao());

		boolean isMaster = isMasterPerfil(perfil.getNome());
		view.setEdicaoNomeHabilitada(!isMaster);

		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.listarPermissoesDoPerfil(perfil.getIdPerfil()),
				permissoes -> {

					if (isMaster) {
						marcarTodasPermissoesNaGrade();
					} else if (permissoes != null) {

						permissoes.forEach(p -> {
							try {
								MenuChave chave = MenuChave.valueOf(p.getChave());
								TipoPermissao tipo = TipoPermissao.valueOf(p.getTipo());
								view.setPermissao(chave, tipo, true);
							} catch (IllegalArgumentException ex) {
								// ignora permissões obsoletas
							}
						});
					}

					atualizarEstadoCategorias();

					view.getBtnNovo().setEnabled(false);
					view.getBtnSalvar().setEnabled(true);
				});
	}

	private void salvar() {

		String nomePerfil = view.getNome();

		if (ValidationUtils.isEmpty(nomePerfil)) {
			ValidationUtils.exibirErro(view.getTxtNome(), "NOME DO PERFIL É OBRIGATÓRIO.");
			DialogFactory.aviso(view, "NOME DO PERFIL É OBRIGATÓRIO!");
			return;
		}

		nomePerfil = nomePerfil.trim();

		if (perfilAtual == null) {
			perfilAtual = new Perfil();
		}

		perfilAtual.setNome(nomePerfil);
		perfilAtual.setDescricao(Optional.ofNullable(view.getDescricao()).orElse("").trim());

		Map<MenuChave, List<String>> permissoes = isMasterPerfil(nomePerfil) ? gerarMapaTotal()
				: coletarPermissoesMarcadas();

		if (permissoes.isEmpty()) {
			DialogFactory.aviso(view, "SELECIONE AO MENOS UMA PERMISSÃO!");
			return;
		}

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			Usuario executor = Objects.requireNonNull(Sessao.getUsuario(), "Usuário executor não encontrado.");
			service.salvarPerfil(perfilAtual, permissoes, executor);
			return true;
		}, sucesso -> {
			DialogFactory.informacao(view, "PERFIL SALVO COM SUCESSO!");
			novoPerfil();
			if (refreshCallback != null) {
				refreshCallback.onSaveSuccess();
			}
		});
	}

	private Map<MenuChave, List<String>> coletarPermissoesMarcadas() {

		Map<MenuChave, List<String>> selecionadas = new LinkedHashMap<>();
		for (MenuChave chave : view.getChavesAtivas()) {
			List<String> tipos = Arrays.stream(TIPOS).filter(tipo -> view.isPermissaoSelecionada(chave, tipo))
					.map(Enum::name).toList();
			if (!tipos.isEmpty()) {
				selecionadas.put(chave, tipos);
			}
		}

		return selecionadas;
	}

	private boolean isMasterPerfil(String nome) {
		return "MASTER".equalsIgnoreCase(nome);
	}

	private Map<MenuChave, List<String>> gerarMapaTotal() {
		Map<MenuChave, List<String>> mapa = new LinkedHashMap<>();
		for (MenuChave chave : MenuChave.values()) {
			mapa.put(chave, Arrays.stream(TIPOS).map(Enum::name).toList());
		}
		return mapa;
	}

	private void marcarTodasPermissoesNaGrade() {
		for (MenuChave chave : MenuChave.values()) {
			for (TipoPermissao tipo : TipoPermissao.values()) {
				view.setPermissao(chave, tipo, true);
			}
		}
	}

	private void novoPerfil() {
		this.perfilAtual = null;

		view.limpar();
		view.setEdicaoNomeHabilitada(true);

		atualizarEstadoCategorias();

		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}
}