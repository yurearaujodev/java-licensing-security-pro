package com.br.yat.gerenciador.controller;

import java.util.*;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.view.ICadastroView;

public abstract class BaseCadastroController<V extends ICadastroView> extends BaseController {

	protected final V view;
	protected static final TipoPermissao[] TIPOS = TipoPermissao.values();

	public BaseCadastroController(V view) {
		this.view = view;
	}

	protected void configurarAcoesBase(Runnable acaoSalvar, Runnable acaoNovo) {
		view.getBtnSalvar().addActionListener(e -> acaoSalvar.run());
		view.getBtnNovo().addActionListener(e -> acaoNovo.run());
		view.getBtnCancelar().addActionListener(e -> {
			view.setCamposHabilitados(false);
			view.getBtnNovo().setEnabled(true);
			view.getBtnSalvar().setEnabled(false);
			view.doDefaultCloseAction();
		});
		configurarEventosPermissoes();
	}

	private void configurarEventosPermissoes() {
		for (String categoria : view.getCategoriasPermissoes()) {
			view.addListenerCategoria(categoria, marcado -> {
				view.marcarCategoria(categoria, marcado);
				atualizarEstadoCheckBoxesCategoria();
			});
		}
	}

	protected void atualizarEstadoCheckBoxesCategoria() {
		for (String categoria : view.getCategoriasPermissoes()) {
			view.setCategoriaMarcada(categoria, view.isCategoriaTotalmenteMarcada(categoria));
		}
	}

	protected Map<MenuChave, List<String>> coletarPermissoesMarcadas() {
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

	protected void marcarTodasPermissoesNaGrade() {
		for (MenuChave chave : view.getChavesAtivas()) {
			for (TipoPermissao tipo : TIPOS) {
				view.setPermissao(chave, tipo, true);
			}
		}
		atualizarEstadoCheckBoxesCategoria();
	}
}
