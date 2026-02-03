package com.br.yat.gerenciador.view;

import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import com.br.yat.gerenciador.model.enums.Tema;

import net.miginfocom.swing.MigLayout;

public class ParametroSistemaView extends JInternalFrame{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	 public JComboBox<Tema> comboTema;
	    public JComboBox<String> comboFonte;
	    public JButton btnCorTexto;
	    public JButton btnCorFundo;
	    public JButton btnSalvar;
	    public JButton btnCancelar;

	    public ParametroSistemaView() {
	        setTitle("Configurações do Sistema");
	        setClosable(true);
	        setSize(500, 300);
	        setLayout(new MigLayout("", "[][grow]", "[][][][][][]"));

	        comboTema = new JComboBox<>(Tema.values());
	        comboFonte = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

	        btnCorTexto = new JButton("Selecionar Cor do Texto");
	        btnCorFundo = new JButton("Selecionar Cor de Fundo");

	        btnSalvar = new JButton("Salvar");
	        btnCancelar = new JButton("Cancelar");

	        add(new JLabel("Tema:"), "cell 0 0");
	        add(comboTema, "cell 1 0, growx");

	        add(new JLabel("Fonte:"), "cell 0 1");
	        add(comboFonte, "cell 1 1, growx");

	        add(new JLabel("Cor do Texto:"), "cell 0 2");
	        add(btnCorTexto, "cell 1 2, growx");

	        add(new JLabel("Cor de Fundo:"), "cell 0 3");
	        add(btnCorFundo, "cell 1 3, growx");

	        add(btnSalvar, "cell 0 5");
	        add(btnCancelar, "cell 1 5, align right");

	        setVisible(true);
	    }
	}

