package com.br.yat.gerenciador.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.*;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.*;
import net.miginfocom.swing.MigLayout;

public class PerfilView extends JInternalFrame {

	private static final long serialVersionUID = 1L;

	// ===============================
	// COMPONENTES DE DADOS
	// ===============================
	private final JTextField txtNome;
	private final JTextField txtDescricao;

	// ===============================
	// COMPONENTE DE PERMISSÕES
	// ===============================
	private final PainelPermissoes pnlPermissoes;

	// ===============================
	// BOTÕES
	// ===============================
	private final JButton btnSalvar;
	private final JButton btnNovo;
	private final JButton btnCancelar;

	// ===============================
	// CONSTRUTOR
	// ===============================
	public PerfilView() {
		super("Cadastro de Perfil de Acesso", true, true, true, true);

		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[][grow][]"));

		// Instancia componentes usando as Factories
		txtNome = FieldFactory.createTextField(30);
		txtDescricao = FieldFactory.createTextField(30);
		// Painel de permissões (false pois perfil geralmente não expira como o usuário)
		pnlPermissoes = new PainelPermissoes(false);

		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		btnCancelar = ButtonFactory.createPrimaryButton("CANCELAR", IconFactory.cancelar());
		btnSalvar = ButtonFactory.createPrimaryButton("SALVAR", IconFactory.salvar());

		montarLayout();

		setSize(800, 600);
	}

	// ===============================
	// MONTAGEM DA TELA
	// ===============================
	private void montarLayout() {
		add(montarPainelIdentificacao(), "cell 0 0, growx");
		add(DesktopFactory.createScroll(pnlPermissoes), "cell 0 1, grow");
		add(montarPainelBotoes(), "cell 0 2, right");
	}

	private JPanel montarPainelIdentificacao() {
		JPanel panel = PanelFactory.createPanel("gapx 10", "[right][grow,fill]", "[]");
		panel.setBorder(BorderFactory.createTitledBorder("Identificação"));

		panel.add(LabelFactory.createLabel("NOME DO PERFIL: "));
		panel.add(txtNome, "h 25!");
		
		panel.add(LabelFactory.createLabel("DESCRIÇÃO: "));
	    panel.add(txtDescricao, "h 25!");

		return panel;
	}

	private JPanel montarPainelBotoes() {
		JPanel panel = new JPanel(new MigLayout("insets 5", "[left][grow][right]", "[]"));

		panel.add(btnNovo, "w 140!, h 35!");
		panel.add(btnCancelar, "gapleft push, w 140!, h 35!");
		panel.add(btnSalvar, "w 140!, h 35!");

		return panel;
	}

	// ===============================
	// API DE PERMISSÕES (ENCAPSULADA)
	// ===============================
	public void construirGradePermissoes(Map<String, List<MenuChave>> grupos) {
		pnlPermissoes.construirGrade(grupos);
	}

	public void setPermissao(MenuChave chave, TipoPermissao tipo, boolean valor) {
		pnlPermissoes.setPermissao(chave, tipo, valor);
	}

	public boolean isPermissaoSelecionada(MenuChave chave, TipoPermissao tipo) {
		return pnlPermissoes.isPermissaoSelecionada(chave, tipo);
	}

	public void setPermissoesHabilitadas(boolean habilitado) {
		pnlPermissoes.setHabilitado(habilitado);
	}

	public void limparPermissoes() {
		pnlPermissoes.resetarSelecoes();
	}

	public void aplicarRestricaoPermissao(MenuChave chave, TipoPermissao tipo, boolean permitido) {
		pnlPermissoes.aplicarRestricaoPermissao(chave, tipo, permitido);
	}

	// ===============================
	// API DE CATEGORIAS
	// ===============================
	public Set<String> getCategoriasPermissoes() {
		return pnlPermissoes.getCategorias();
	}

	public List<MenuChave> getChavesDaCategoria(String categoria) {
		return pnlPermissoes.getChavesDaCategoria(categoria);
	}

	public void marcarCategoria(String categoria, boolean marcar) {
		pnlPermissoes.marcarCategoria(categoria, marcar);
	}

	public boolean isCategoriaTotalmenteMarcada(String categoria) {
		return pnlPermissoes.isCategoriaTotalmenteMarcada(categoria);
	}

	public void setCategoriaMarcada(String categoria, boolean marcada) {
		pnlPermissoes.setCategoriaMarcada(categoria, marcada);
	}

	public void addListenerCategoria(String categoria, Consumer<Boolean> listener) {
		pnlPermissoes.addListenerCategoria(categoria, listener);
	}

	// ===============================
	// API DE DADOS
	// ===============================
	public String getNome() {
		return txtNome.getText();
	}

	public void setNome(String nome) {
		txtNome.setText(nome);
	}
	
	public String getDescricao() {
	    return txtDescricao.getText();
	}

	public void setDescricao(String descricao) {
	    txtDescricao.setText(descricao);
	}

	public void setEdicaoNomeHabilitada(boolean habilitado) {
		txtNome.setEditable(habilitado);
		txtNome.setEnabled(habilitado);
	}

	public void limpar() {
		txtNome.setText("");
		txtDescricao.setText("");
		limparPermissoes();
	}

	public Set<MenuChave> getChavesAtivas() {
		return pnlPermissoes.getChavesConstruidas();
	}

	// ===============================
	// GETTERS DE AÇÃO (Controller)
	// ===============================
	public JButton getBtnSalvar() {
		return btnSalvar;
	}

	public JButton getBtnNovo() {
		return btnNovo;
	}

	public JButton getBtnCancelar() {
		return btnCancelar;
	}
	
	public JTextField getTxtNome() {
	    return txtNome;
	}
}