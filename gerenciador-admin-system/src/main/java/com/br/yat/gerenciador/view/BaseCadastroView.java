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

public abstract class BaseCadastroView extends JInternalFrame implements ICadastroView {
	private static final long serialVersionUID = 1L;

	protected final PainelPermissoes pnlPermissoes;
	protected final JButton btnSalvar;
	protected final JButton btnNovo;
	protected final JButton btnCancelar;

	public BaseCadastroView(String titulo, boolean permitirExpiracao) {
		super(titulo, true, true, true, true);
		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[][grow][]"));

		this.pnlPermissoes = new PainelPermissoes(permitirExpiracao);
		this.btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		this.btnCancelar = ButtonFactory.createPrimaryButton("CANCELAR", IconFactory.cancelar());
		this.btnSalvar = ButtonFactory.createPrimaryButton("SALVAR", IconFactory.salvar());
	}

	protected void montarLayoutPrincipal(JPanel pnlDados) {
		add(pnlDados, "cell 0 0, growx");
		add(DesktopFactory.createScroll(pnlPermissoes), "cell 0 1, grow");

		JPanel pnlBotoes = new JPanel(new MigLayout("insets 5", "[left][grow][right]", "[]"));
		pnlBotoes.add(btnNovo, "w 140!, h 35!");
		pnlBotoes.add(btnCancelar, "gapleft push, w 140!, h 35!");
		pnlBotoes.add(btnSalvar, "w 140!, h 35!");

		add(pnlBotoes, "cell 0 2, right");
	}

	// === IMPLEMENTAÇÃO AUTOMÁTICA DA INTERFACE (DELEGAÇÃO) ===
	@Override
	public void construirGradePermissoes(Map<String, List<MenuChave>> g) {
		pnlPermissoes.construirGrade(g);
	}

	@Override
	public void setPermissao(MenuChave c, TipoPermissao t, boolean v) {
		pnlPermissoes.setPermissao(c, t, v);
	}

	@Override
	public boolean isPermissaoSelecionada(MenuChave c, TipoPermissao t) {
		return pnlPermissoes.isPermissaoSelecionada(c, t);
	}

	@Override
	public void setPermissoesHabilitadas(boolean h) {
		pnlPermissoes.setHabilitado(h);
	}

	@Override
	public void limparPermissoes() {
		pnlPermissoes.resetarSelecoes();
	}

	@Override
	public void aplicarRestricaoPermissao(MenuChave c, TipoPermissao t, boolean p) {
		pnlPermissoes.aplicarRestricaoPermissao(c, t, p);
	}

	@Override
	public Set<MenuChave> getChavesAtivas() {
		return pnlPermissoes.getChavesConstruidas();
	}

	@Override
	public Set<String> getCategoriasPermissoes() {
		return pnlPermissoes.getCategorias();
	}

	@Override
	public List<MenuChave> getChavesDaCategoria(String c) {
		return pnlPermissoes.getChavesDaCategoria(c);
	}

	@Override
	public void marcarCategoria(String c, boolean m) {
		pnlPermissoes.marcarCategoria(c, m);
	}

	@Override
	public boolean isCategoriaTotalmenteMarcada(String c) {
		return pnlPermissoes.isCategoriaTotalmenteMarcada(c);
	}

	@Override
	public void setCategoriaMarcada(String c, boolean m) {
		pnlPermissoes.setCategoriaMarcada(c, m);
	}

	@Override
	public void addListenerCategoria(String c, Consumer<Boolean> l) {
		pnlPermissoes.addListenerCategoria(c, l);
	}

	@Override
	public JButton getBtnSalvar() {
		return btnSalvar;
	}

	@Override
	public JButton getBtnNovo() {
		return btnNovo;
	}

	@Override
	public JButton getBtnCancelar() {
		return btnCancelar;
	}
	
	public void entrarModoEdicao(boolean isMaster) {
	    setCamposHabilitados(true);
	    getBtnNovo().setEnabled(false);
	    getBtnSalvar().setEnabled(true);
	    // Permissões padrão
	    setPermissoesHabilitadas(!isMaster);
	}

	public void entrarModoNovo() {
	    setCamposHabilitados(true);
	    getBtnNovo().setEnabled(false);
	    getBtnSalvar().setEnabled(true);
	    setPermissoesHabilitadas(true);
	}
}