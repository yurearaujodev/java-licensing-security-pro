package com.br.yat.gerenciador.view;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import java.awt.Font;
import java.net.URL;

import javax.swing.SwingConstants;

import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.view.empresa.DadoContatoPanel;
import com.br.yat.gerenciador.view.empresa.DadoEmpresaPanel;
import com.br.yat.gerenciador.view.empresa.DadoEnderecoPanel;
import com.br.yat.gerenciador.view.empresa.DadoFiscalPanel;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;

public class EmpresaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;

	private JButton btnSalvar;

	public EmpresaView() {
		super("Cadastro de Empresa Fornecedor/Cliente", true, true, true, true);
		setLayout(new MigLayout("gapx 15, gapy 15", "[right][grow,fill][right][grow,fill]", "[][]"));

		JTabbedPane tabbedPane = DesktopFactory.createTabbedPane();

		JPanel painelEmpresa = new DadoEmpresaPanel();
		tabbedPane.addTab("DADOS PRINCIPAIS", null, painelEmpresa, null);

		JPanel painelEndereco = new DadoEnderecoPanel();
		tabbedPane.addTab("ENDEREÇO", null, painelEndereco, null);

		JPanel painelContato = new DadoContatoPanel();
		tabbedPane.addTab("CONTATOS", null, painelContato, null);

		JPanel painelFiscal = new DadoFiscalPanel();
		tabbedPane.addTab("DADOS FISCAIS", null, painelFiscal, null);

		add(tabbedPane, "cell 0 0 4 1,grow");
		JPanel criarBotoes = criarBotoes();
		add(criarBotoes, "cell 3 1 4 1, growx");
		setSize(690, 480);
	}

	private JPanel criarBotoes() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 5, align right", "[][]", "[]"));

		btnSalvar = criarBotao("Salvar", "/com/br/yat/gerenciador/image/salvar_24.png");
		// btnSalvar = criarBotao("Salvar", "/image/salvar_24.png");
		panel.add(btnSalvar, "h 35!, w 120!");

		return panel;
	}

	private JButton criarBotao(String texto, String caminhoIcone) {
		JButton botao;
		URL url = getClass().getResource(caminhoIcone);
		if (url != null) {
			botao = new JButton(texto, new ImageIcon(url));
		} else {
			botao = new JButton(texto);
			System.err.println("Icone não encontrado: " + caminhoIcone);
		}
		botao.setFont(new Font("Tahoma", Font.BOLD, 12));
		botao.setHorizontalTextPosition(SwingConstants.RIGHT);
		botao.setVerticalTextPosition(SwingConstants.CENTER);
		botao.setIconTextGap(8);
		return botao;
	}

//	private void aoClicarSalvar(ActionEvent e) {
//		String tipo = String.valueOf(cbTipoDoc.getSelectedItem());
//		if ("SELECIONE".equals(tipo)) {
//			JOptionPane.showMessageDialog(this, "SELECIONE O TIPO DE DOCUMENTO (CNPJ OU CPF).");
//			cbTipoDoc.requestFocusInWindow();
//			return;
//		}
//		String cep = ftxtCep.getText().replaceAll("\\D", "");
//		if (cep.length() == 8) {
//			exibirErro(ftxtCep, "CEP INVÁLIDO. USE 8 DÍGITOS.");
//			return;
//		}
//
//		preencherEnderecoPorCep();
//		validarDocumento();
//		validarInscricaoEstadual();
//		validarInscricaoMunicipal();
//		validarFundacao();
//		validarTelefoneCelular();
//		validarTelefoneFixo();
//
//		if (hasErroVisual()) {
//			JOptionPane.showMessageDialog(this, "CORRIGE OS CAMPOS DESTACADOS EM VERMELHO ANTES DE SALVAR.",
//					"CAMPOS INVÁLIDOS", JOptionPane.WARNING_MESSAGE);
//			return;
//		}
//
//		JOptionPane.showMessageDialog(this, "CONFIGURAÇÂO SALVA COM SUCESSO!\nA APLICAÇÂO SERÁ REINICIADA.", "SUCESSO",
//				JOptionPane.INFORMATION_MESSAGE);
//	}

}
