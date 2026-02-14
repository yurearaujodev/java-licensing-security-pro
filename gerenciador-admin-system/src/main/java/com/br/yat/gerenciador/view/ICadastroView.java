package com.br.yat.gerenciador.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JButton;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;

public interface ICadastroView {
	// Botões padrão
	JButton getBtnSalvar();

	JButton getBtnNovo();

	JButton getBtnCancelar();

	// Controle de tela
	void limpar();

	void doDefaultCloseAction();
	
	void setCamposHabilitados(boolean habilitado);

	// Delegação de Permissões (vêm do PainelPermissoes)
	void construirGradePermissoes(Map<String, List<MenuChave>> grupos);

	void setPermissao(MenuChave chave, TipoPermissao tipo, boolean valor);

	boolean isPermissaoSelecionada(MenuChave chave, TipoPermissao tipo);

	void setPermissoesHabilitadas(boolean habilitado);

	void limparPermissoes();

	void aplicarRestricaoPermissao(MenuChave chave, TipoPermissao tipo, boolean permitido);

	Set<MenuChave> getChavesAtivas();

	// Delegação de Categorias
	Set<String> getCategoriasPermissoes();

	List<MenuChave> getChavesDaCategoria(String categoria);

	void marcarCategoria(String categoria, boolean marcar);

	boolean isCategoriaTotalmenteMarcada(String categoria);

	void setCategoriaMarcada(String categoria, boolean marcada);

	void addListenerCategoria(String categoria, Consumer<Boolean> listener);
}
