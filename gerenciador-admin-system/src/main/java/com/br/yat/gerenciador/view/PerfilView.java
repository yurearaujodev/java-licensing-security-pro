package com.br.yat.gerenciador.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.*;
import net.miginfocom.swing.MigLayout;

public class PerfilView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtNome;
	private JButton btnSalvar, btnNovo, btnCancelar;
	private JPanel pnlPermissoesContainer;

	private final Map<String, JCheckBox> chkTodosPorCategoria = new LinkedHashMap<>();
	private final Map<String, List<MenuChave>> gruposPermissoes = new LinkedHashMap<>();
	private final Map<MenuChave, Map<String, JCheckBox>> permissoesGranulares = new LinkedHashMap<>();

	public PerfilView() {
		super("Cadastro de Perfil de Acesso", true, true, true, true);
		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[][grow][]"));

		montarCabecalho();
		add(criarGradeScroll(), "cell 0 1, grow");
		add(criarBotoes(), "cell 0 2, right");
		setSize(750, 550);
	}

	private void montarCabecalho() {
		JPanel panel = PanelFactory.createPanel("gapx 10", "[right][grow,fill]", "[]");
		panel.setBorder(BorderFactory.createTitledBorder("Identificação"));
		panel.add(LabelFactory.createLabel("NOME DO PERFIL: "));
		txtNome = FieldFactory.createTextField(30);
		panel.add(txtNome, "h 25!");
		add(panel, "cell 0 0, grow");
	}

	private JScrollPane criarGradeScroll() {
		pnlPermissoesContainer = new JPanel(new MigLayout("wrap 1", "[grow]", "[]10[]"));
		pnlPermissoesContainer.setBorder(BorderFactory.createTitledBorder("Definição de Permissões"));
		return DesktopFactory.createScroll(pnlPermissoesContainer);
	}

	// Métodos de construção da grade (Iguais aos da UsuarioView para manter o
	// padrão)
	public void construirGradePermissoes(Map<String, List<MenuChave>> grupos) {
		gruposPermissoes.clear();
		gruposPermissoes.putAll(grupos);
		pnlPermissoesContainer.removeAll();
		permissoesGranulares.clear();

		for (var entry : grupos.entrySet()) {
			JPanel categoriaPanel = new JPanel(new MigLayout("wrap 4", "[grow,fill][center][center][center]"));
			categoriaPanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));

			JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR CATEGORIA");
			chkTodosPorCategoria.put(entry.getKey(), chkTodos);
			categoriaPanel.add(chkTodos, "span 4, left, wrap");

			categoriaPanel.add(LabelFactory.createLabel("MENU"), "growx");
			categoriaPanel.add(LabelFactory.createLabel("VER (R)"), "w 50!");
			categoriaPanel.add(LabelFactory.createLabel("EDITAR (W)"), "w 50!");
			categoriaPanel.add(LabelFactory.createLabel("EXCLUIR (D)"), "w 50!");

			for (MenuChave chave : entry.getValue()) {
				categoriaPanel.add(LabelFactory.createLabel(chave.name().replace("_", " ")));
				Map<String, JCheckBox> tipos = new LinkedHashMap<>();
				tipos.put("READ", new JCheckBox());
				tipos.put("WRITE", new JCheckBox());
				tipos.put("DELETE", new JCheckBox());

				tipos.values().forEach(categoriaPanel::add);
				permissoesGranulares.put(chave, tipos);
			}
			pnlPermissoesContainer.add(categoriaPanel, "growx, wrap");
		}
	}

	private JPanel criarBotoes() {
		JPanel panel = PanelFactory.createPanel("insets 5", "[left][grow][right]", "[]");
		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		btnCancelar = ButtonFactory.createPrimaryButton("CANCELAR", IconFactory.cancelar());
		btnSalvar = ButtonFactory.createPrimaryButton("SALVAR", IconFactory.salvar());
		panel.add(btnNovo, "w 120!");
		panel.add(new JLabel(), "growx");
		panel.add(btnCancelar, "w 120!");
		panel.add(btnSalvar, "w 120!");
		return panel;
	}

	// Getters e Setters Atômicos
	public String getNome() {
		return txtNome.getText();
	}

	public void setNome(String nome) {
		txtNome.setText(nome);
	}
	public void setEdicaoNomeHabilitada(boolean habilitado) {
	    txtNome.setEditable(habilitado);
	    // Opcional: mudar a cor para indicar que está travado
	    txtNome.setEnabled(habilitado);
	}

	public void limpar() {
		txtNome.setText("");
		permissoesGranulares.values().forEach(m -> m.values().forEach(c -> c.setSelected(false)));
	}

	public JButton getBtnSalvar() {
		return btnSalvar;
	}

	public JButton getBtnNovo() {
		return btnNovo;
	}

	public JButton getBtnCancelar() {
		return btnCancelar;
	}

	public Map<MenuChave, Map<String, JCheckBox>> getPermissoesGranulares() {
		return permissoesGranulares;
	}

	public Map<String, JCheckBox> getChkTodosPorCategoria() {
		return chkTodosPorCategoria;
	}

	public Map<String, List<MenuChave>> getGruposPermissoes() {
		return gruposPermissoes;
	}

	public void setPermissao(MenuChave c, String t, boolean s) {
		if (permissoesGranulares.containsKey(c))
			permissoesGranulares.get(c).get(t).setSelected(s);
	}
}
