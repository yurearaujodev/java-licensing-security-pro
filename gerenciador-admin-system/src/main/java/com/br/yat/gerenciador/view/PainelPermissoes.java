package com.br.yat.gerenciador.view;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.view.factory.*;
import net.miginfocom.swing.MigLayout;

public class PainelPermissoes extends JPanel {

	private final Map<String, JCheckBox> chkTodosPorCategoria = new LinkedHashMap<>();
	private final Map<MenuChave, Map<TipoPermissao, JCheckBox>> permissoesGranulares = new LinkedHashMap<>();
	private final Map<MenuChave, JTextField> datasExpiracao = new LinkedHashMap<>();
	private final Map<String, List<MenuChave>> gruposPermissoes = new LinkedHashMap<>();

	public PainelPermissoes() {
		setLayout(new MigLayout("wrap 1, fillx", "[grow]", "[]10[]"));
		setBorder(BorderFactory.createTitledBorder("Permissões do Usuário"));
	}

	public void construirGrade(Map<String, List<MenuChave>> grupos) {
		limpar();
		
		if (grupos == null || grupos.isEmpty()) {
		    return;
		}

		gruposPermissoes.putAll(grupos);

		grupos.forEach((categoria, chaves) -> {
			
			JPanel pnlCat = new JPanel(new MigLayout("fillx, insets 10", "[grow,fill][50!][50!][50!][130!]", "[]5[]"));

			pnlCat.setBorder(BorderFactory.createTitledBorder(categoria));

			JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR TODOS DE: " + categoria);
			chkTodosPorCategoria.put(categoria, chkTodos);
			chkTodos.putClientProperty("categoria", categoria);
			pnlCat.add(chkTodos, "span 5, left, gapy 5 10, wrap");

			pnlCat.add(LabelFactory.createLabel("FUNCIONALIDADE / DESCRIÇÃO"), "center");
			pnlCat.add(LabelFactory.createLabel("VER"), "center");
			pnlCat.add(LabelFactory.createLabel("ADD"), "center");
			pnlCat.add(LabelFactory.createLabel("DEL"), "center");
			pnlCat.add(LabelFactory.createLabel("EXPIRA EM"), "center, wrap");

			for (MenuChave chave : chaves) {

				String labelHtml = "<html><b>" + chave.name().replace("_", " ") + "</b><br>"
						+ "<font color='#666666' size='2'>" + chave.getDescricao() + "</font></html>";

				pnlCat.add(LabelFactory.createLabel(labelHtml));

				Map<TipoPermissao, JCheckBox> tiposMap = new LinkedHashMap<>();

				JCheckBox chkRead = new JCheckBox();
				JCheckBox chkWrite = new JCheckBox();
				JCheckBox chkDelete = new JCheckBox();

				pnlCat.add(chkRead, "center");
				pnlCat.add(chkWrite, "center");
				pnlCat.add(chkDelete, "center");

				tiposMap.put(TipoPermissao.READ, chkRead);
				tiposMap.put(TipoPermissao.WRITE, chkWrite);
				tiposMap.put(TipoPermissao.DELETE, chkDelete);

				permissoesGranulares.put(chave, tiposMap);

				JFormattedTextField txtData = FieldFactory.createFormattedField();
				FormatterUtils.applyDateMask(txtData, MaskFactory.createMask().get("DATA_HORA"));

				datasExpiracao.put(chave, txtData);

				pnlCat.add(txtData, "growx, h 25!, wrap");
			}

			this.add(pnlCat, "growx, wrap 10");
		});

		revalidate();
		repaint();
	}

	public void limpar() {
		removeAll();
		chkTodosPorCategoria.clear();
		permissoesGranulares.clear();
		datasExpiracao.clear();
		gruposPermissoes.clear();
	}

	// ================================
	// MÉTODOS DE ALTO NÍVEL (MVC LIMPO)
	// ================================

	public void setPermissao(MenuChave chave, TipoPermissao tipo, boolean valor) {
		Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
		if (mapa != null && mapa.get(tipo) != null) {
			mapa.get(tipo).setSelected(valor);
		}
	}

	public boolean isPermissaoSelecionada(MenuChave chave, TipoPermissao tipo) {
		Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
		return mapa != null && mapa.get(tipo) != null && mapa.get(tipo).isSelected();
	}

	public void setHabilitado(boolean habilitado) {
		permissoesGranulares.values().forEach(mapa -> mapa.values().forEach(chk -> chk.setEnabled(habilitado)));

		datasExpiracao.values().forEach(txt -> txt.setEnabled(habilitado));

		chkTodosPorCategoria.values().forEach(chk -> chk.setEnabled(habilitado));
	}

	public void setDataExpiracao(MenuChave chave, String data) {
		JTextField campo = datasExpiracao.get(chave);
		if (campo != null) {
			campo.setText(data);
		}
	}

	public String getDataExpiracao(MenuChave chave) {
		JTextField campo = datasExpiracao.get(chave);
		return campo != null ? campo.getText() : null;
	}
	
	public Set<MenuChave> getChavesAtivas() {
	    return new LinkedHashSet<>(permissoesGranulares.keySet());
	}
	
	// ==========================================
	// SUPORTE AO CONTROLLER (SEM QUEBRAR MVC)
	// ==========================================

	public void addListenerMarcarTodos(String categoria, java.awt.event.ActionListener listener) {
	    JCheckBox chk = chkTodosPorCategoria.get(categoria);
	    if (chk != null) {
	        chk.addActionListener(listener);
	    }
	}

	public List<MenuChave> getChavesDaCategoria(String categoria) {
	    return gruposPermissoes.get(categoria);
	}

	public JCheckBox getCheckBox(MenuChave chave, TipoPermissao tipo) {
	    Map<TipoPermissao, JCheckBox> mapa = permissoesGranulares.get(chave);
	    return mapa != null ? mapa.get(tipo) : null;
	}

	public Set<String> getCategorias() {
	    return chkTodosPorCategoria.keySet();
	}
	
	public JCheckBox getCheckBoxCategoria(String categoria) {
	    return chkTodosPorCategoria.get(categoria);
	}
	
	public void resetarSelecoes() {
	    // Desmarca todos os checkboxes de permissões
	    permissoesGranulares.values().forEach(mapa -> 
	        mapa.values().forEach(chk -> chk.setSelected(false))
	    );
	    
	    // Limpa todos os campos de data
	    datasExpiracao.values().forEach(txt -> txt.setText(""));
	    
	    // Desmarca os "Marcar Todos" do topo de cada categoria
	    chkTodosPorCategoria.values().forEach(chk -> chk.setSelected(false));
	}

}
