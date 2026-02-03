package com.br.yat.gerenciador.util;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class UIThemes {

	private UIThemes() {
	}

	// Fonte Principal do Banco
	public static Font getFontDefault() {
		return UIManager.getFont("defaultFont") != null ? UIManager.getFont("defaultFont")
				: new Font("Segoe UI", Font.PLAIN, 12);
	}

	// Fonte de Título (Derivada da principal)
	public static Font getFontTitle() {
		return getFontDefault().deriveFont(Font.BOLD, 16f);
	}

	// Fonte de Campos/Botões (Derivada da principal)
	public static Font getFontComponent() {
		return getFontDefault().deriveFont(Font.PLAIN, 12f);
	}

	public static Color getFgDefault() {
		Color c = UIManager.getColor("Label.foreground");
        return c != null ? c : new Color(187, 187, 187); 
	}
	public static Border getBorderCard() {
        Color borderColor = UIManager.getColor("Component.borderColor");
        if(borderColor == null) borderColor = new Color(80, 80, 80);
        
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor), 
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

	public static Border BORDER_CARD = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(220, 220, 220)), BorderFactory.createEmptyBorder(10, 10, 10, 10));
}
