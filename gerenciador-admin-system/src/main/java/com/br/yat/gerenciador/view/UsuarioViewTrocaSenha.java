package com.br.yat.gerenciador.view;

import javax.swing.*;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import net.miginfocom.swing.MigLayout;

public class UsuarioViewTrocaSenha extends JDialog {

    private JPasswordField txtNovaSenha;
    private JPasswordField txtConfirmaSenha;
    private JButton btnSalvar;

    public UsuarioViewTrocaSenha(JFrame parent) {
        super(parent, "Troca de Senha Obrigatória", true); // Modal
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Impede fechar no "X"
        setLayout(new MigLayout("wrap 2, insets 25, gap 15", "[right][grow,fill]", "[]20[]20[]"));

        add(LabelFactory.createLabel("SUA SENHA EXPIROU OU FOI RESETADA."), "span 2, center, gapy 0 10");
        add(LabelFactory.createLabel("NOVA SENHA: "));
        txtNovaSenha = FieldFactory.createPasswordField(20);
        add(txtNovaSenha, "w 200!");

        add(LabelFactory.createLabel("CONFIRME A SENHA: "));
        txtConfirmaSenha = FieldFactory.createPasswordField(20);
        add(txtConfirmaSenha, "w 200!");

        btnSalvar = ButtonFactory.createPrimaryButton("ATUALIZAR E ENTRAR", null);
        add(btnSalvar, "span 2, center, h 40!, w 200!");

        pack();
        setLocationRelativeTo(parent);
    }

    public char[] getNovaSenha() { return txtNovaSenha.getPassword(); }
    public char[] getConfirmaSenha() { return txtConfirmaSenha.getPassword(); }
    public JButton getBtnSalvar() { return btnSalvar; }
}