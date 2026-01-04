package com.br.yat.gerenciador.teste;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;


public class Teste extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public Teste() {
		setBackground(new Color(0, 255, 255));
		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,right][grow,fill][right][grow,fill][right][grow,fill]", "[25,grow]10[25]10[25]"));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(new Color(0, 255, 255));
		add(tabbedPane, "cell 0 0 6 1,grow");
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 255, 255));
		tabbedPane.addTab("New tab", null, panel, null);
		panel.setLayout(new MigLayout("", "[][][][][][][][][][]", "[][]"));
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		panel.add(lblNewLabel, "cell 9 1");
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("New tab", null, panel_1, null);
		
		JButton btnNewButton = new JButton("New button");
		add(btnNewButton, "cell 5 1");
	

	}

}
