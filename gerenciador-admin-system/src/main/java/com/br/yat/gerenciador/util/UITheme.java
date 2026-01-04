package com.br.yat.gerenciador.util;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
/**
 * Classe utilitária para definição de tema visual da aplicação.
 * <p>
 * Centraliza estilos de interface gráfica, incluindo:
 * <ul>
 * <li>Fontes padrão e específicas (menus, botões, campos).</li>
 * <li>Cores de fundo e texto.</li>
 * <li>Bordas utilizadas em componentes.</li>
 * </ul>
 * </p>
 * 
 * <p>Não deve ser instanciada.</p>
 */
public final class UITheme {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private UITheme() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Fonte padrão utilizada em textos gerais.
	 */
	public static Font FONT_DEFAULT = new Font("Segoe UI", Font.PLAIN, 12);
	/**
	 * Fonte em negrito para destaques.
	 */
	public static Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
	/**
	 * Fonte para títulos de janelas e seções.
	 */
	public static Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
	/**
	 * Fonte para menus principais.
	 */
	public static Font FONT_MENU = new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12);
	/**
	 * Fonte para itens de menu.
	 */
	public static Font FONT_MENU_ITEM = new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12);
	/**
	 * Fonte para botões.
	 */
	public static Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 12);
	/**
	 * Fonte para campos de entrada.
	 */
	public static Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 12);
	
	/**
	 * Cor de fundo dos menus.
	 */
	public static Color MENU_BG = new Color(240, 240, 240);
	/**
	 * Cor de texto dos menus.
	 */
	public static Color MENU_FG = Color.DARK_GRAY;
	/**
	 * Cor de fundo da barra de ferramentas.
	 */
	public static Color TOOLBAR_BG = new Color(245, 245, 245);
	/**
	 * Cor de fundo da área de trabalho.
	 */
	public static Color DESKTOP_BG = new Color(245, 245, 245);
	/**
	 * Cor de fundo dos cartões.
	 */
	public static Color CARD_BG = Color.WHITE;
	/**
	 * Cor de padrão do texto.
	 */
	public static Color FG_DEFAULT = Color.DARK_GRAY;
	/**
	 * Cor de texto de títulos.
	 */
	public static Color FG_TITLE = new Color(60, 60, 60);
	/**
	 * Cor para mensagens de erro.
	 */
	public static Color COLOR_ERROR = new Color(180, 50, 50);
	/**
	 * Cor para mensagens informativas.
	 */
	public static Color COLOR_INFO = new Color(60, 120, 180);

	/**
	 * Borda padrão para cartões, com linha externa e espaçamento interno.
	 */
	public static Border BORDER_CARD = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(220, 220, 220)), BorderFactory.createEmptyBorder(10, 10, 10, 10));

}
