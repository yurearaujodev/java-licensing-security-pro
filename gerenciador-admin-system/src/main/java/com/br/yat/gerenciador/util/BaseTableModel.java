package com.br.yat.gerenciador.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * Classe base abstrata para modelos de tabela Swing.
 * <p>
 * Responsável por:
 * <ul>
 * <li>Gerenciar lista de dados genéricos ({@code T}).</li>
 * <li>Fornecer operações básicas de adição, remoção e limpeza.</li>
 * <li>Disparar eventos de atualização da tabela.</li>
 * <li>Definir tabela como não editável por padrão.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Subclasses devem implementar métodos de {@link AbstractTableModel} como
 * {@link #getColumnCount()}, {@link #getColumnName(int)} e
 * {@link #getValueAt(int, int)}
 * </p>
 * 
 * @param <T> tipo de dado representado nas linhas da tabela
 */
public abstract class BaseTableModel<T> extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	protected final List<T> dados = new ArrayList<>();

	/**
	 * Substitui os dados da tabela.
	 * 
	 * @param lista nova lista de dados; se {@code null}, a tabela será esvaziada
	 */
	public void setDados(List<T> lista) {
		SwingUtilities.invokeLater(() -> {
			dados.clear();
			if (lista != null) {
				dados.addAll(lista);
			}
			fireTableDataChanged();
		});
	}

	/**
	 * Retorna o objeto da linha especificada.
	 * 
	 * @param row índice da linha
	 * @return objeto correspondente
	 */
	public T getAt(int row) {
		if (row >= 0 && row < dados.size()) {
			return dados.get(row);
		}
		return null;
	}

	/**
	 * Adiciona um objeto á tabela.
	 * 
	 * @param obj objeto a ser adicionado
	 */
	public void add(T obj) {
		dados.add(obj);
		int lastRow = dados.size() - 1;
		fireTableRowsInserted(lastRow, lastRow);
	}

	public void updateRow(int row, T obj) {
		if (row >= 0 && row < dados.size()) {
			dados.set(row, obj);
			fireTableRowsUpdated(row, row);
		}
	}

	/**
	 * Remove objeto da linha especificada.
	 * 
	 * @param row índice da linha
	 */
	public void remove(int row) {
		if (row >= 0 && row < dados.size()) {
			dados.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	/**
	 * Limpa todos os dados da tabela.
	 */
	public void clear() {
		dados.clear();
		fireTableDataChanged();
	}

	/**
	 * Define se uma célula é editável.
	 * <p>
	 * Por padrão, retorna {@code false}, retornando a tabela somente leitura.
	 * </p>
	 * 
	 * @param rowIndex    índice da linha
	 * @param columnIndex índice da coluna
	 * @return sempre {@code false}
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public int getRowCount() {
		return dados.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(getRowCount()==0)return Object.class;
		Object value = getValueAt(0, columnIndex);
		return (value != null) ? value.getClass() : Object.class;
	}
	
	public List<T> getTodos(){
		return new ArrayList<>(dados);
	}

}
