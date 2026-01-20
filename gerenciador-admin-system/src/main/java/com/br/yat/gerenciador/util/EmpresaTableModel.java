package com.br.yat.gerenciador.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.br.yat.gerenciador.model.Empresa;

public class EmpresaTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private final String[] colunas = { "ID", "RAZÃO SOCIAL", "CNPJ/CPF", "CIDADE/UF" };
	private List<Empresa> empresas = new ArrayList<>();

	@Override
	public int getRowCount() {
		return empresas.size();
	}

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
		Empresa e = empresas.get(rowIndex);
		return switch (columnIndex) {
		case 0 -> e.getIdEmpresa();
		case 1 -> e.getRazaoSocialEmpresa();
		case 2 -> e.getDocumentoEmpresa();
		case 3 -> e.getEndereco().getCidadeEndereco() + "/" + e.getEndereco().getEstadoEndereco();
		default -> null;
		};
	}

	public void setLista(List<Empresa> lista) {
		this.empresas = lista;
		fireTableDataChanged();
	}

	public Empresa getEmpresaAt(int row) {
		if(row>=0&&row<empresas.size())return empresas.get(row);
		return null;
	}
}
