package com.br.yat.gerenciador.util;

import com.br.yat.gerenciador.model.Empresa;

public class EmpresaTableModel extends BaseTableModel<Empresa> {

	private static final long serialVersionUID = 1L;
	private final String[] colunas = { "ID", "RAZÃO SOCIAL", "CNPJ/CPF", "CIDADE/UF" };

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
		Empresa e = dados.get(rowIndex);
		return switch (columnIndex) {
		case 0 -> e.getIdEmpresa();
		case 1 -> e.getRazaoSocialEmpresa();
		case 2 -> e.getDocumentoEmpresa();
		case 3 -> (e.getEndereco() != null)
				? e.getEndereco().getCidadeEndereco() + " / " + e.getEndereco().getEstadoEndereco()
				: "SEM ENDEREÇO";
		default -> null;
		};
	}

}
