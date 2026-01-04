package com.br.yat.gerenciador.util.ui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.util.UITheme;
/**
 * Classe utilitária para criação e configuração de tabelas swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar
 * {@link JTable}, {@link JScrollPane} e modelos de tabela ({@link DefaultTableModel}, {@link AbstractTableModel}),
 * aplicando estilos definidos em {@link UITheme}.
 * </p>
 * 
 * <p>
 * Não deve ser instanciada.
 * </p>
 */
public final class TableFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private TableFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria uma {@link JTable} com colunas definidas e modelo {@link DefaultTableModel}.
	 * <p>
	 * As células não são editáveis e a tabela recebe configuração padrão via {@link UIThme}.
	 * </p>
	 *  
	 * @param columns nomes das colunas da tabela
	 * @return uma instância de {@link JTable} configurada
	 */
	public static JTable createDefaultTable(String[] columns) {
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		return configureTable(new JTable(model));
	}
	
	/**
	 * Cria uma {@link JTable} com modelo abstrato definido pelo usuário.
	 * <p>
	 * A tabela recebe configuração padrão via {@link UITheme}.
	 * </p>
	 *  
	 * @param model instância de {@link AbstractTableModel} fornecida pelo usuário
	 * @return uma instância de {@link JTable} configurada
	 */
	public static JTable createAbstractTable(AbstractTableModel model) {
		return configureTable(new JTable(model));
	}

	/**
	 * Configura uma {@link JTable} com estilo padrão.
	 * <p>
	 * Define fonte, altura das linhas, fonte do cabeçalho, modo de seleção e ordenação automática.
	 * </p> 
	 * 
	 * @param table instância de {@link JTable} a ser configurada
	 * @return a tabela configurada
	 */
	private static JTable configureTable(JTable table) {
		table.setFont(UITheme.FONT_DEFAULT);
		table.setRowHeight(24);
		table.getTableHeader().setFont(UITheme.FONT_BOLD);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		return table;
	}

	/**
	 * Cria um {@link JScrollPane} contendo uma {@link JTable}.
	 * 
	 * @param table instância de {@link JTable} a ser encapsulada
	 * @return uma instância de {@link JScrollPane} contendo a tabela
	 */
	public static JScrollPane criarRolagemTabela(JTable table) {
		return new JScrollPane(table);
	}

}
