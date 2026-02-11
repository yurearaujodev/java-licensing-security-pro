package com.br.yat.gerenciador.view;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.FormatterUtils;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.MaskFactory;

import net.miginfocom.swing.MigLayout;

public class PermissoesPanel extends JPanel {

    private final Map<String, JCheckBox> marcarTodosPorCategoria = new LinkedHashMap<>();
    private final Map<MenuChave, PermissaoViewItem> permissoes = new LinkedHashMap<>();

    public PermissoesPanel(Map<String, List<MenuChave>> grupos) {
        setLayout(new MigLayout("wrap 1", "[grow]", "[]10[]"));
        setBorder(BorderFactory.createTitledBorder("Permissões do Usuário"));
        construir(grupos);
    }

    private void construir(Map<String, List<MenuChave>> grupos) {
        grupos.forEach((categoria, menus) -> {
            JPanel categoriaPanel = new JPanel(
                new MigLayout("fillx, insets 10", "[grow][50!][50!][50!][120!]", "[]5[]")
            );
            categoriaPanel.setBorder(BorderFactory.createTitledBorder(categoria));

            // "Marcar todos" da categoria
            JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR TODOS DESTA CATEGORIA");
            marcarTodosPorCategoria.put(categoria, chkTodos);
            categoriaPanel.add(chkTodos, "span 5, wrap");

            // Cabeçalho
            categoriaPanel.add(LabelFactory.createLabel("MENU"), "center");
            categoriaPanel.add(LabelFactory.createLabel("VER"), "center");
            categoriaPanel.add(LabelFactory.createLabel("ADD"), "center");
            categoriaPanel.add(LabelFactory.createLabel("DEL"), "center");
            categoriaPanel.add(LabelFactory.createLabel("EXPIRA EM"), "center, wrap");

            // Itens
            for (MenuChave menu : menus) {
                categoriaPanel.add(LabelFactory.createLabel(menu.name().replace("_", " ")));

                JFormattedTextField txtData = FieldFactory.createFormattedField();
                FormatterUtils.applyDateMask(txtData, MaskFactory.createMask().get("DATA_HORA"));

                PermissaoViewItem item = new PermissaoViewItem(menu, txtData);

                JCheckBox r = new JCheckBox();
                JCheckBox w = new JCheckBox();
                JCheckBox d = new JCheckBox();

                item.addCheck(TipoPermissao.READ, r);
                item.addCheck(TipoPermissao.WRITE, w);
                item.addCheck(TipoPermissao.DELETE, d);

                categoriaPanel.add(r, "center");
                categoriaPanel.add(w, "center");
                categoriaPanel.add(d, "center");
                categoriaPanel.add(txtData, "growx, wrap");

                permissoes.put(menu, item);
            }

            add(categoriaPanel, "growx");
        });
    }

    // ================================
    // API para o Controller
    // ================================

    public void marcarTodos(String categoria, boolean marcado) {
        JCheckBox chkTodos = marcarTodosPorCategoria.get(categoria);
        if (chkTodos != null) {
            chkTodos.setSelected(marcado);
            // atualiza todos os itens da categoria
            for (Map.Entry<MenuChave, PermissaoViewItem> entry : permissoes.entrySet()) {
                if (menuPertenceCategoria(entry.getKey(), categoria)) {
                    entry.getValue().getChecks().values().forEach(c -> c.setSelected(marcado));
                }
            }
        }
    }

    private boolean menuPertenceCategoria(MenuChave menu, String categoria) {
        // exemplo simples: categoria = prefixo do nome do menu
        return menu.name().startsWith(categoria.toUpperCase());
    }

    public void selecionarPermissoes(Map<MenuChave, List<TipoPermissao>> permissaoMap) {
        permissoes.forEach((menu, item) -> {
            item.getChecks().forEach((tipo, check) -> 
                check.setSelected(permissaoMap.getOrDefault(menu, List.of()).contains(tipo))
            );
        });
    }

    public Map<MenuChave, List<TipoPermissao>> coletarPermissoesComoMap() {
        Map<MenuChave, List<TipoPermissao>> resultado = new LinkedHashMap<>();
        permissoes.forEach((menu, item) -> {
            List<TipoPermissao> selecionadas = item.getChecks().entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();
            if (!selecionadas.isEmpty()) {
                resultado.put(menu, selecionadas);
            }
        });
        return resultado;
    }

    public void bloquearSeguindoExecutor(List<MenuChave> menusBloqueados) {
        permissoes.forEach((menu, item) -> item.setEnabled(!menusBloqueados.contains(menu)));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        permissoes.values().forEach(p -> p.setEnabled(enabled));
        marcarTodosPorCategoria.values().forEach(c -> c.setEnabled(enabled));
    }

    public void limpar() {
        permissoes.values().forEach(PermissaoViewItem::limpar);
        marcarTodosPorCategoria.values().forEach(c -> c.setSelected(false));
    }

    public Map<MenuChave, PermissaoViewItem> getPermissoes() {
        return permissoes;
    }

    public Map<String, JCheckBox> getMarcarTodosPorCategoria() {
        return marcarTodosPorCategoria;
    }
}

