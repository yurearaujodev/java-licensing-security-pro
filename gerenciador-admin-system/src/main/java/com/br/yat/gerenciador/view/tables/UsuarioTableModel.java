package com.br.yat.gerenciador.view.tables;

import com.br.yat.gerenciador.model.Usuario;

public class UsuarioTableModel extends BaseTableModel<Usuario> {

    private static final long serialVersionUID = 1L;
    private final String[] colunas = { "ID", "NOME", "E-MAIL", "STATUS", "EMPRESA" };

    @Override
    public int getColumnCount() {
        return colunas.length;
    }

    @Override
    public String getColumnName(int column) {
        return colunas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Usuario u = getAt(rowIndex);
        if (u == null) return null;

        return switch (columnIndex) {
            case 0 -> u.getIdUsuario();
            case 1 -> u.getNome() != null ? u.getNome().toUpperCase() : "";
            case 2 -> u.getEmail() != null ? u.getEmail().toLowerCase() : "";
            case 3 -> u.getStatus();
            case 4 -> (u.getEmpresa() != null && u.getEmpresa().getRazaoSocialEmpresa() != null) 
                      ? u.getEmpresa().getRazaoSocialEmpresa().toUpperCase() 
                      : "NÃO VINCULADA";
            default -> null;
        };
    }
}
