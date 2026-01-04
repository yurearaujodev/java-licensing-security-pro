package com.br.yat.gerenciador.util;

import java.util.ArrayList;
import java.util.List;

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
 * <p>Subclasses devem implementar métodos de {@link AbstractTableModel} como
 * {@link #getColumnCount()}, {@link #getColumnName(int)} e {@link #getValueAt(int, int)}</p>
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
		dados.clear();
		if (lista != null) {
			dados.addAll(lista);
		}
		fireTableDataChanged();
	}

	/**
	 * Retorna o objeto da linha especificada.
	 * 
	 * @param row índice da linha
	 * @return objeto correspondente
	 */
	public T get(int row) {
		return dados.get(row);
	}

	/**
	 * Adiciona um objeto á tabela.
	 * 
	 * @param obj objeto a ser adicionado
	 */
	public void add(T obj) {
		dados.add(obj);
		fireTableRowsInserted(dados.size() - 1, dados.size() - 1);
	}

	/**
	 * Remove objeto da linha especificada.
	 * 
	 * @param row índice da linha
	 */
	public void remove(int row) {
		dados.remove(row);
		fireTableRowsDeleted(row, row);
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
	 * @param rowIndex índice da linha
	 * @param columnIndex índice da coluna
	 * @return sempre {@code false}
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

}
