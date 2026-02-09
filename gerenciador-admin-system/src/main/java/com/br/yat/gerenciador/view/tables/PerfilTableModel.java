package com.br.yat.gerenciador.view.tables;

import com.br.yat.gerenciador.model.Perfil;

public class PerfilTableModel extends BaseTableModel<Perfil> {

    private static final long serialVersionUID = 1L;
    private final String[] colunas = {"ID", "NOME DO PERFIL", "DESCRIÇÃO"};

    @Override
    public int getColumnCount() {
        return colunas.length;
    }

    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Perfil p = getAt(row);
        if (p == null) return null;

        return switch (col) {
            case 0 -> p.getIdPerfil();
            case 1 -> p.getNome();
            case 2 -> p.getDescricao();
            default -> null;
        };
    }
}