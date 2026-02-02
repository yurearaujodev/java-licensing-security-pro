package com.br.yat.gerenciador.view;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import net.miginfocom.swing.MigLayout;

public class UsuarioViewLogin extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtEmail;
	private JPasswordField txtSenha;
	private JButton btnEntrar;
	private JButton btnEsqueciSenha;

	public UsuarioViewLogin() {
		super("Acesso ao Sistema", false, false, false, false);
		setLayout(new MigLayout("wrap 2, insets 30, gap 15", "[right][grow,fill]", "[]20[]"));

		add(LabelFactory.createLabel("E-MAIL: "));
		txtEmail = FieldFactory.createTextField(20);
		add(txtEmail, "w 250!");

		add(LabelFactory.createLabel("SENHA: "));
		txtSenha = FieldFactory.createPasswordField(20);
		add(txtSenha, "w 250!");

		btnEntrar = ButtonFactory.createPrimaryButton("ENTRAR", null);
		add(btnEntrar, "span 2, center, h 40!, w 150!");

		// Botão secundário para recuperação
        btnEsqueciSenha = ButtonFactory.createPrimaryButton("ESQUECI MINHA SENHA", null);
        add(btnEsqueciSenha, "span 2, center, gapy 5");
		pack();
	}

	// Getters
	public String getEmail() {
		return txtEmail.getText();
	}

	public char[] getSenha() {
		return txtSenha.getPassword();
	}

	public JButton getBtnEntrar() {
		return btnEntrar;
	}
	
	public JButton getBtnEsqueciSenha() {
        return btnEsqueciSenha;
    }
	
	public void limpar() {
		txtSenha.setText("");
	}
}
