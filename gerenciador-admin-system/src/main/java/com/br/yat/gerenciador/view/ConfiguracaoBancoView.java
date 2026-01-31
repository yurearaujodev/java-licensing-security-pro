package com.br.yat.gerenciador.view;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

public class ConfiguracaoBancoView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtNomeBanco;
	private JTextField txtEnderecoIp;
	private JTextField txtPorta;
	private JTextField txtRoot;
	private JPasswordField txtPassword;
	private JButton btnSalvar;
	private JButton btnTestar;
	private JLabel lblStatusConexao;

	public ConfiguracaoBancoView() {
		super("CONFIGURAÇÃO DE CONEXÃO COM O BANCO", false, true, false, false);
		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[grow][]"));
		montarTela();
		add(criarBotoes(), "cell 0 1,grow");
	}

	private JPanel criarBotoes() {
		JPanel panel = PanelFactory.createPanel("insets 5", "[grow]", "[]");

		btnTestar = ButtonFactory.createPrimaryButton("TESTAR CONEXÃO", IconFactory.pesquisar());
		panel.add(btnTestar, "cell 2 0 2 1,split 2,alignx center,w 195!, h 30!");

		btnSalvar = ButtonFactory.createPrimaryButton("SALVAR E CONTINUAR", IconFactory.salvar());
		panel.add(btnSalvar, "w 195!, h 30!");
		return panel;
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][]");
		montarCampos(panel);
		add(panel, "grow");
		setSize(450, 280);
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("NOME DO BANCO: "), "cell 0 0, alignx trailing");
		txtNomeBanco = FieldFactory.createTextField(20);
		panel.add(txtNomeBanco, "cell 1 0 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("ENDEREÇO IP: "), "cell 0 1, alignx trailing");
		txtEnderecoIp = FieldFactory.createTextField(20);
		panel.add(txtEnderecoIp, "cell 1 1,growx,w 110!, h 25!");

		panel.add(LabelFactory.createLabel("PORTA: "), "cell 2 1, alignx trailing");
		txtPorta = FieldFactory.createTextField(20);
		panel.add(txtPorta, "cell 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("USUÁRIO: "), "cell 0 2, alignx trailing");
		txtRoot = FieldFactory.createTextField(20);
		panel.add(txtRoot, "cell 1 2 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("SENHA: "), "cell 0 3, alignx trailing");
		txtPassword = FieldFactory.createPasswordField(20);
		panel.add(txtPassword, "cell 1 3 3 1,growx,h 25!");

		panel.add(LabelFactory.createLabel("STATUS: "), "cell 0 4,alignx trailing");
		lblStatusConexao = LabelFactory.createLabel("NÃO TESTADO");
		panel.add(lblStatusConexao, "cell 1 4,alignx trailing");
	}
	
	public JTextField getTxtNomeBanco() {
		return txtNomeBanco;
	}

	public String getNomeBanco() {
		return txtNomeBanco.getText();
	}

	public String getEnderecoIp() {
		return txtEnderecoIp.getText();
	}

	public String getPorta() {
		return txtPorta.getText();
	}

	public String getUser() {
		return txtRoot.getText();
	}

	public char[] getPassword() {
		return txtPassword.getPassword();
	}

	public void setNomeBanco(String nomeBanco) {
		txtNomeBanco.setText(nomeBanco);
	}

	public void setEnderecoIp(String ip) {
		txtEnderecoIp.setText(ip);
	}

	public void setPorta(String porta) {
		txtPorta.setText(porta);
	}

	public void setUser(String user) {
		txtRoot.setText(user);
	}

	public void setPassword(char[] password) {
		txtPassword.setText(new String(password));
	}

	public JButton getBtnSalvar() {
		return btnSalvar;
	}

	public JButton getBtnTestar() {
		return btnTestar;
	}

	public JLabel getLblStatusConexao() {
		return lblStatusConexao;
	}

	public void mostrarStatusConexaoOk(String mensagem) {
		lblStatusConexao.setText("CONECTADO");
		lblStatusConexao.setIcon(IconFactory.bancoOk());
	}

	public void mostrarStatusConexaoErro(String mensagem) {
		lblStatusConexao.setText("ERRO AO CONECTAR");
		lblStatusConexao.setIcon(IconFactory.bancoErro());
	}
	
	public void mostrarStatusAguardando(String mensagem) {
		lblStatusConexao.setText("AGUARDANDO NOVO TESTE...");
		lblStatusConexao.setIcon(null);
	}
}
