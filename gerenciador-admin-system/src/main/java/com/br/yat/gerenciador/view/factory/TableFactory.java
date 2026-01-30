package com.br.yat.gerenciador.view.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.br.yat.gerenciador.util.UITheme;

/**
 * Classe utilitária para criação e configuração de tabelas swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para
 * criar {@link JTable}, {@link JScrollPane} e modelos de tabela
 * ({@link DefaultTableModel}, {@link AbstractTableModel}), aplicando estilos
 * definidos em {@link UITheme}.
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
	 * Cria uma {@link JTable} com colunas definidas e modelo
	 * {@link DefaultTableModel}.
	 * <p>
	 * As células não são editáveis e a tabela recebe configuração padrão via
	 * {@link UITheme}.
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
	 * Define fonte, altura das linhas, fonte do cabeçalho, modo de seleção e
	 * ordenação automática.
	 * </p>
	 * 
	 * @param table instância de {@link JTable} a ser configurada
	 * @return a tabela configurada
	 */
	private static JTable configureTable(JTable table) {
		table.setFont(UITheme.FONT_DEFAULT);
		table.setRowHeight(26);
		table.getTableHeader().setFont(UITheme.FONT_BOLD);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setBackground(UITheme.COLOR_TABLE_HEADER_BG);
		table.getTableHeader().setForeground(UITheme.COLOR_TABLE_HEADER_FG);
		table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.GRAY));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(UITheme.COLOR_SELECTION_BG);
		table.setSelectionForeground(UITheme.COLOR_SELECTION_FG);
		
		table.setAutoCreateRowSorter(true);
		
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(false);
		table.setGridColor(UITheme.COLOR_GRID);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		aplicarEfeitoZebra(table);
		return table;
	}

	/**
	 * Cria um {@link JScrollPane} contendo uma {@link JTable}.
	 * 
	 * @param table instância de {@link JTable} a ser encapsulada
	 * @return uma instância de {@link JScrollPane} contendo a tabela
	 */
	public static JScrollPane createTableScrolling(JTable table) {
		return new JScrollPane(table);
	}

	private static void aplicarEfeitoZebra(JTable table) {
		DefaultTableCellRenderer zebra =  new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

				if (!isSelected) {
					setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
				}
			
				setHorizontalAlignment((column==0)? SwingConstants.CENTER:SwingConstants.CENTER);
				
				return this;
			}
		};
		table.setDefaultRenderer(String.class, zebra);
		table.setDefaultRenderer(Object.class, zebra);
		table.setDefaultRenderer(Integer.class, zebra);
	}
	
	public static void addDoubleClickAction(JTable table, Runnable doubleClickAction) {
		table.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2 &&SwingUtilities.isLeftMouseButton(e)) {
				doubleClickAction.run();;
			}
		}
		});
	}

	public static void addEmptySpaceClickAction(JTable table, Runnable emptyClickAction) {
	    table.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mousePressed(MouseEvent e) {
	            if (table.rowAtPoint(e.getPoint()) == -1) {
	                table.clearSelection();
	                emptyClickAction.run();
	            }
	        }
	    });
	}
}
