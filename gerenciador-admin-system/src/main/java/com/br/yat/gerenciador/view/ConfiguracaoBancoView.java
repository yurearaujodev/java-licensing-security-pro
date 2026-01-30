package com.br.yat.gerenciador.view;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.br.yat.gerenciador.controller.ConfiguracaoBancoController;
import com.br.yat.gerenciador.security.SensitiveData;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.JPasswordField;
import javax.swing.JButton;

public class ConfiguracaoBancoView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtUrl;
	private JTextField txtRoot;
	private JPasswordField txtPassword;
	private JTextField txtDriver;

	private final ConfiguracaoBancoController controller = new ConfiguracaoBancoController();

	/**
	 * Create the frame.
	 */
	public ConfiguracaoBancoView() {
		super("CONFIGURAÇÃO DE CONEXÃO COM O BANCO",false,true,false,false);
		init();

	}

	private void init() {
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(6, 6, 424, 251);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblNewLabel = new JLabel("URL:");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel.setBounds(6, 22, 49, 16);
		panel.add(lblNewLabel);

		txtUrl = new JTextField();
		txtUrl.setText("jdbc:mysql://localhost:3306/db_gerenciador");
		txtUrl.setBounds(80, 16, 283, 28);
		panel.add(txtUrl);
		txtUrl.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Usuário:");
		lblNewLabel_1.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel_1.setBounds(6, 62, 64, 16);
		panel.add(lblNewLabel_1);

		txtRoot = new JTextField();
		txtRoot.setText("root");
		txtRoot.setBounds(80, 56, 283, 28);
		panel.add(txtRoot);
		txtRoot.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Senha:");
		lblNewLabel_2.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel_2.setBounds(6, 104, 49, 16);
		panel.add(lblNewLabel_2);

		txtPassword = new JPasswordField();
		txtPassword.setBounds(80, 96, 283, 28);
		panel.add(txtPassword);

		JLabel lblNewLabel_3 = new JLabel("Driver:");
		lblNewLabel_3.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblNewLabel_3.setBounds(6, 144, 49, 16);
		panel.add(lblNewLabel_3);

		txtDriver = new JTextField();
		txtDriver.setText("com.mysql.cj.jdbc.Driver");
		txtDriver.setBounds(80, 138, 283, 28);
		panel.add(txtDriver);
		txtDriver.setColumns(10);

		JButton btnNewButton = new JButton("Salvar e Continuar");
		btnNewButton.addActionListener(e -> salvarConfiguracao());
		btnNewButton.setBounds(123, 217, 181, 28);
		panel.add(btnNewButton);
	}

	private void salvarConfiguracao() {
		String url = txtUrl.getText().trim();
		String root = txtRoot.getText().trim();
		char[] password = txtPassword.getPassword();

		try {
			if (url.isEmpty() || root.isEmpty() || password.length == 0) {
				JOptionPane.showMessageDialog(this, "Peencha todos os campos!", "Atenção", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String mensagem = controller.salvarConfiguracao(url, root, password);
			JOptionPane.showMessageDialog(this, mensagem);
			if (mensagem.toLowerCase().contains("sucesso")) {
				dispose();
			}

		} finally {
			SensitiveData.safeClear(password);
		}
	}
}
