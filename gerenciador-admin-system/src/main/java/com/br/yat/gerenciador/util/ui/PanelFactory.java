package com.br.yat.gerenciador.util.ui;

import javax.swing.JPanel;

import com.br.yat.gerenciador.util.UITheme;

import net.miginfocom.swing.MigLayout;
/**
 * Classe utilitária para criação de painéis Swing com layout MigLayout.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar
 * {@link JPanel} e a biblioteca <b>MigLayout</b> ({@code net.miginfocom.swing.MigLayout})
 * para gerenciar o layout dos componentes. Os estilos visuais são aplicados via {@link UITheme}.
 * </p>
 * 
 * <p>
 * Não pode ser instanciada.
 * </p>
 */
public final class PanelFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private PanelFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria um {@link JPanel} com layout {@link MigLayout} configurado.
	 * <p>
	 * O painel é transparente por padrão.
	 * </p>
	 * 
	 * @param constraints restrições gerais do MigLayout (ex.: "fill")
	 * @param columns definição das colunas do MigLayout
	 * @param rows definição das linhas do MigLayout
	 * @return uma instância de {@link JPanel} configurada
	 */
	public static JPanel createPanel(String constraints, String columns, String rows) {
		JPanel panel = new JPanel(new MigLayout(constraints, columns, rows));
		panel.setOpaque(false);
		return panel;
	}

	/**
	 * Cria um {@link JPanel} estilizado como "card".
	 * <p>
	 * O painel recebe cor de fundo e borda definidos em {@link UITheme}, além de layout {@link MigLayout}.
	 * </p>
	 * 
	 * @return uma instância de {@link JPanel} configurada como card
	 */
	public static JPanel createCardPanel() {
		JPanel painel = new JPanel(new MigLayout("fill,insets 15", "[grow]", "[]10[]"));
		painel.setBackground(UITheme.CARD_BG);
		painel.setBorder(UITheme.BORDER_CARD);
		painel.setOpaque(true);
		return painel;
	}

}
