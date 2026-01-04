package com.br.yat.gerenciador.view;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.PorteEmpresa;
import com.br.yat.gerenciador.util.CepUtils;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.validation.DocumentValidator;
import com.br.yat.gerenciador.util.validation.FormatValidator;

import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JFormattedTextField;

import javax.swing.JTabbedPane;

public class EmpresaView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Border BORDA_PADRAO = UIManager.getBorder("TextField.border");
	private static final Border BORDA_ERRO = BorderFactory.createLineBorder(Color.RED, 2);

	private JTextField txtFantasia;
	private JTextField txtRazao;
	private JTextField txtCodigo;
	private JTextField txtEmail;
	private JTextField txtInscEst;
	private JTextField txtInscMun;

	private JTextField txtBairro;
	private JTextField txtLogradouro;
	private JTextField txtNumero;
	private JTextField txtCidade;
	private JTextField txtComplemento;
	private JTextField txtEstado;

	private JFormattedTextField ftxtFundacao;
	private JFormattedTextField ftxtDocumento;
	private JFormattedTextField ftxtTelCel;
	private JFormattedTextField ftxtTelFixo;
	private JFormattedTextField ftxtCapital;
	private JFormattedTextField ftxtCep;

	private JComboBox<String> cbTipoDoc;
	private JComboBox<String> cbSituacao;
	private JComboBox<String> cbCadastro;
	private JComboBox<Cnae> cbCnae;
	private JComboBox<NaturezaJuridica> cbNatJuri;
	private JComboBox<PorteEmpresa> cbPortEmp;

	private JButton btnSalvar;

	private final Map<String, String> mascaras;

	public EmpresaView() {
		this.mascaras = criarMascaras();
		setLayout(new MigLayout("gapx 15, gapy 15", "[right][grow,fill][right][grow,fill]", "10[25]10[25]"));

		JTabbedPane tabbedPane = DesktopFactory.createTabbedPane();

		JPanel painelEmpresa = criarPainelEmpresa();
		tabbedPane.addTab("DADOS PRINCIPAIS", null, painelEmpresa, null);

		JPanel painelEndereco = criarAbaEndereco();
		tabbedPane.addTab("ENDEREÇO", null, painelEndereco, null);

		JPanel painelContato = criarAbaContato();
		tabbedPane.addTab("CONTATOS", null, painelContato, null);

		add(tabbedPane, "cell 0 0 4 1,grow");
		JPanel criarBotoes = criarBotoes();
		add(criarBotoes, "cell 3 1 4 1, growx");
		configurarEventos();
	}

	private Map<String, String> criarMascaras() {
		Map<String, String> map = new HashMap<>();
		map.put("CPF", "###.###.###-##");
		map.put("CNPJ", "##.###.###/####-##");
		map.put("FIXO", "(##) ####-####");
		map.put("CELULAR", "(##) #####-####");
		map.put("FUNDACAO", "##/##/####");
		map.put("CAPITAL", "#,##0.00");
		map.put("CEP", "#####-###");
		return map;
	}

	private JPanel criarPainelEmpresa() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("gapx 15, gapy 15", "[right][grow,fill][right][grow,fill]",
				"5[25]5[25]5[25]5[25]5[25]5[25]5[25]5[25]5[25]5[25]"));

		panel.add(criarLabel("CÓDIGO :"), "cell 0 0,alignx trailing");
		txtCodigo = criarCampoTexto(false);
		panel.add(txtCodigo, "cell 1 0,growx, h 25!,wmax 120");

		panel.add(criarLabel("TIPO DE CADASTRO :"), "cell 0 1,alignx trailing");
		cbCadastro = new JComboBox<>();
		cbCadastro.setModel(new DefaultComboBoxModel<>(new String[] { "SELECIONE", "CLIENTE", "FORNECEDORA" }));
		cbCadastro.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbCadastro, "cell 1 1,growx, h 25!");

		panel.add(criarLabel("DT. FUNDAÇÂO :"), "cell 2 1,alignx trailing");
		ftxtFundacao = new JFormattedTextField();
		FormatterUtils.applyDateMask(ftxtFundacao, mascaras.get("FUNDACAO"));
		panel.add(ftxtFundacao, "cell 3 1,growx, h 25!");

		panel.add(criarLabel("TIPO DOC. :"), "cell 0 2,alignx trailing");
		cbTipoDoc = new JComboBox<>();
		cbTipoDoc.setModel(new DefaultComboBoxModel<>(new String[] { "SELECIONE", "CNPJ", "CPF" }));
		cbTipoDoc.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbTipoDoc, "cell 1 2,growx, h 25!");

		panel.add(criarLabel("DOCUMENTO :"), "cell 2 2,alignx trailing");
		ftxtDocumento = new JFormattedTextField();
		ftxtDocumento.setEditable(false);
		ftxtDocumento.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(ftxtDocumento, "cell 3 2,growx, h 25!");

		panel.add(criarLabel("RAZÃO SOCIAL :"), "cell 0 3,alignx trailing");
		txtRazao = criarCampoTexto(true);
		panel.add(txtRazao, "cell 1 3 3 1,growx, h 25!");

		panel.add(criarLabel("NOME FANTASIA :"), "cell 0 4,alignx trailing");
		txtFantasia = criarCampoTexto(true);
		panel.add(txtFantasia, "cell 1 4 3 1,growx, h 25!");

		panel.add(criarLabel("INSC. ESTADUAL :"), "cell 0 5,alignx trailing");
		txtInscEst = criarCampoTexto(true);
		panel.add(txtInscEst, "cell 1 5,growx, h 25!");

		panel.add(criarLabel("INSC. MUNICIPAL :"), "cell 2 5,alignx trailing");
		txtInscMun = criarCampoTexto(true);
		panel.add(txtInscMun, "cell 3 5,growx, h 25!");

		panel.add(criarLabel("SITUAÇÂO CADASTRAL :"), "cell 0 6,alignx trailing");
		cbSituacao = new JComboBox<>();
		cbSituacao.setModel(new DefaultComboBoxModel<>(new String[] { "SELECIONE", "ATIVA", "INATIVA", "SUSPENSA" }));
		cbSituacao.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbSituacao, "cell 1 6,growx, h 25!");

		panel.add(criarLabel("CAPITAL SOCIAL :"), "cell 2 6,alignx trailing");
		ftxtCapital = new JFormattedTextField();
		ftxtCapital.setFont(new Font("Tahoma", Font.PLAIN, 12));
		FormatterUtils.applyCapitalMask(ftxtCapital, mascaras.get("CAPITAL"));
		panel.add(ftxtCapital, "cell 3 6,growx, h 25!");

		JLabel lblCnae = criarLabel("CNAE :");
		lblCnae.setToolTipText("CLASSIFICAÇÃO NACIONAL DE ATIVADADE ECONÔMICAS");
		panel.add(lblCnae, "cell 0 7,alignx trailing");
		cbCnae = new JComboBox<>(Cnae.values());
		cbCnae.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbCnae, "cell 1 7 3 1,growx, h 25!");

		JLabel lblNat = criarLabel("NATUREZA JURÍD. :");
		lblNat.setToolTipText("NATUREZA JURÍDICA");
		panel.add(lblNat, "cell 0 8,alignx trailing");
		cbNatJuri = new JComboBox<>(NaturezaJuridica.values());
		cbNatJuri.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbNatJuri, "cell 1 8 3 1,growx, h 25!");

		JLabel lblPorte = criarLabel("PORT. DA EMPRESA :");
		lblPorte.setToolTipText("NATUREZA JURÍDICA");
		panel.add(lblPorte, "cell 0 9,alignx trailing");
		cbPortEmp = new JComboBox<>(PorteEmpresa.values());
		cbPortEmp.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbPortEmp, "cell 1 9 3 1,growx, h 25!");

		return panel;
	}

	private JPanel criarAbaEndereco() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("gapx 15, gapy 15", "[right][grow,fill][right][grow,fill][right][grow,fill]",
				"10[25]10[25]10[25]10[25]"));

		panel.add(criarLabel("CEP :"), "cell 0 0,alignx trailing");

		ftxtCep = new JFormattedTextField();
		ftxtCep.setFont(new Font("Tahoma", Font.PLAIN, 12));
		FormatterUtils.applyPostalCodeMask(ftxtCep, mascaras.get("CEP"));
		panel.add(ftxtCep, "cell 1 0,growx, h 25!,wmin 150,wmax 600");

		panel.add(criarLabel("BAIRRO: "), "cell 2 0,alignx trailing");
		txtBairro = criarCampoTexto(true);
		panel.add(txtBairro, "cell 3 0,growx, h 25!");

		panel.add(criarLabel("NÚMERO :"), "cell 4 0,alignx trailing");
		txtNumero = criarCampoTexto(true);
		panel.add(txtNumero, "cell 5 0,growx, h 25!");

		panel.add(criarLabel("LOGRADOURO :"), "cell 0 1,alignx trailing");
		txtLogradouro = criarCampoTexto(true);
		panel.add(txtLogradouro, "cell 1 1 3 1,growx, h 25!");

		panel.add(criarLabel("CIDADE :"), "cell 4 1,alignx trailing");
		txtCidade = criarCampoTexto(true);
		panel.add(txtCidade, "cell 5 1,growx, h 25!");

		panel.add(criarLabel("COMPLEMENTO :"), "cell 0 2,alignx trailing");
		txtComplemento = criarCampoTexto(true);
		panel.add(txtComplemento, "cell 1 2 3 1,growx, h 25!");

		panel.add(criarLabel("ESTADO :"), "cell 4 2,alignx trailing");
		txtEstado = criarCampoTexto(true);
		panel.add(txtEstado, "cell 5 2,growx, h 25!");
		return panel;
	}

	private JPanel criarBotoes() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 5, align right", "[][]", "[]"));

		btnSalvar = criarBotao("Salvar", "/com/br/yat/gerenciador/image/salvar_24.png");
		// btnSalvar = criarBotao("Salvar", "/image/salvar_24.png");
		panel.add(btnSalvar, "h 35!, w 120!");

		return panel;
	}

	private JPanel criarAbaContato() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("gapx 15, gapy 15", "[right][grow,fill][right][grow,fill][right][grow,fill]",
				"10[25]10[25]10[25]10[25]"));

		panel.add(criarLabel("E-MAIL :"), "cell 0 4,alignx trailing");
		txtEmail = criarCampoTexto(true);
		panel.add(txtEmail, "cell 1 4 3 1,growx, h 25!,wmin 250, wmax 850");

		panel.add(criarLabel("TEL. FIXO :"), "cell 4 5,alignx trailing");
		ftxtTelFixo = new JFormattedTextField();
		FormatterUtils.applyPhoneMask(ftxtTelFixo, mascaras.get("FIXO"));
		panel.add(ftxtTelFixo, "cell 5 5,h 25!,wmin 120,wmax 500");

		panel.add(criarLabel("TEL. CELULAR :"), "cell 4 4,alignx trailing");
		ftxtTelCel = new JFormattedTextField();
		FormatterUtils.applyPhoneMask(ftxtTelCel, mascaras.get("CELULAR"));
		panel.add(ftxtTelCel, "cell 5 4, h 25!,wmin 120,wmax 400");

		return panel;
	}

	private JLabel criarLabel(String texto) {
		JLabel label = new JLabel(texto);
		label.setFont(new Font("Tahoma", Font.BOLD, 12));
		return label;
	}

	private JTextField criarCampoTexto(boolean editavel) {
		JTextField field = new JTextField();
		field.setFont(new Font("Tahoma", Font.PLAIN, 12));
		field.setEditable(editavel);
		field.setColumns(10);
		return field;
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

	private void configurarEventos() {
		cbTipoDoc.addActionListener(e -> aplicarMascaraDocumento());

		ftxtDocumento.addFocusListener(createValidationListener(ftxtDocumento, this::validarDocumento));
		txtInscEst.addFocusListener(createValidationListener(txtInscEst, this::validarInscricaoEstadual));
		txtInscMun.addFocusListener(createValidationListener(txtInscMun, this::validarInscricaoMunicipal));
		ftxtTelCel.addFocusListener(createValidationListener(ftxtTelCel, this::validarTelefoneCelular));
		ftxtTelFixo.addFocusListener(createValidationListener(ftxtTelFixo, this::validarTelefoneFixo));
		ftxtCapital.addFocusListener(createValidationListener(ftxtCapital, this::validarCapitalSocial));
		ftxtFundacao.addFocusListener(createValidationListener(ftxtFundacao, this::validarFundacao));

		ftxtCep.addActionListener(e -> preencherEnderecoPorCep());
		btnSalvar.addActionListener(e -> aoClicarSalvar(e));
	}

	private FocusAdapter createValidationListener(JTextComponent campo, Runnable validator) {
		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				removerDestaque(campo);
			}

			@Override
			public void focusLost(FocusEvent e) {
				validator.run();
			}
		};
	}

	private void aplicarMascaraDocumento() {
		String selecionado = String.valueOf(cbTipoDoc.getSelectedItem());

		if ("SELECIONE".equals(selecionado)) {
			ftxtDocumento.setFormatterFactory(null);
			ftxtDocumento.setValue(null);
			ftxtDocumento.setText("");
			ftxtDocumento.setEditable(false);
			cbTipoDoc.requestFocusInWindow();
			return;
		}

		ftxtDocumento.setValue(null);
		ftxtDocumento.setText("");
		ftxtDocumento.setEditable(true);

		String mascara = mascaras.get(selecionado);
		if (mascara != null) {
			FormatterUtils.applyDocumentMask(ftxtDocumento, mascara);
		}
	}

	private void validarDocumento() {
		String doc = ftxtDocumento.getText();
		String tipo = String.valueOf(cbTipoDoc.getSelectedItem());

		if ("SELECIONE".equals(tipo)) {
			return;
		}

		if (doc == null || doc.isBlank()) {
			exibirErro(ftxtDocumento, "Documento é obrigatório.");
			return;
		}
		String soDigitos = doc.replaceAll("\\D", "");
		boolean completo = ("CPF".equals(tipo) && soDigitos.length() == 11)
				|| ("CNPJ".equals(tipo) && soDigitos.length() == 14);

		if (completo && !DocumentValidator.isValidaCpfCnpj(soDigitos)) {
			exibirErro(ftxtDocumento, "DOCUMENTO INVÁLIDO. VERIFIQUE CPF OU CNPJ.");
			return;
		}
		removerDestaque(ftxtDocumento);
	}

	private void validarTelefoneCelular() {
		if (!FormatValidator.isValidPhoneNumberBR(ftxtTelCel.getText())) {
			exibirErro(ftxtTelCel, "TELEFONE CELULAR INVÁLIDO.");
			return;
		}
		removerDestaque(ftxtTelCel);
	}

	private void validarTelefoneFixo() {
		if (!FormatValidator.isValidPhoneNumberBR(ftxtTelFixo.getText())) {
			exibirErro(ftxtTelFixo, "TELEFONE FIXO INVÁLIDO.");
			return;
		}
		removerDestaque(ftxtTelFixo);
	}

	private void validarInscricaoMunicipal() {
		if (!DocumentValidator.isValidInscricaoMunicipal(txtInscMun.getText())) {
			exibirErro(txtInscMun, "INSCRIÇÃO MUNICIPAL INVÁLIDA. USE 7 A 15 DÍGITOS OU ISENTO.");
			return;
		}
		removerDestaque(txtInscMun);
	}

	private void validarInscricaoEstadual() {
		if (!DocumentValidator.isValidInscricaoEstadual(txtInscEst.getText())) {
			exibirErro(txtInscEst, "INSCRIÇÃO ESTADUAL INVÁLIDA. USE 9 A 14 DÍGITOS OU ISENTO.");
			return;
		}
		removerDestaque(txtInscEst);
	}

	private void validarCapitalSocial() {
		String valor = ftxtCapital.getText();
		if (valor == null || valor.isBlank()) {
			removerDestaque(ftxtCapital);
			return;
		}
		try {
			String limpo = valor.replaceAll("[^\\d,]", "");
			if (limpo.isBlank()) {
				removerDestaque(ftxtCapital);
				return;
			}
			double capital = Double.parseDouble(limpo.replace(',', '.'));
			if (capital <= 0) {
				exibirErro(ftxtCapital, "CAPITAL SOCIAL DEVE SER MAIOR QUE ZERO.");
				return;
			}
		} catch (NumberFormatException e) {
			exibirErro(ftxtCapital, "FORMATO DE CAPITAL SOCIAL INVÁLIDO.");
			return;
		}
		removerDestaque(ftxtCapital);
	}

	private void validarFundacao() {
		String data = ftxtFundacao.getText();
		
		if (data == null || data.isBlank()) {
			exibirErro(ftxtFundacao, "DATA DE FUNDAÇÂO É OBRIGATÓRIA.");
			return;
		}
		
		String soDigitos = data.replaceAll("\\D", "");
		if (soDigitos.length() != 8) {
			exibirErro(ftxtFundacao, "DATA INCOMPLETA. USE O FORMATO dd/mm/aaaa.");
			return;
		}

		String formatada = soDigitos.substring(0, 2) + "/" + 
		soDigitos.substring(2, 4) + "/"
				+ soDigitos.substring(4, 8);

		if (!FormatValidator.isValidFoundationDate(formatada)) {
			exibirErro(ftxtFundacao, "DATA DE FUNDAÇÂO INVÁLIDA OU FUTURA.");
			return;
		}
		removerDestaque(ftxtFundacao);
	}

	private void preencherEnderecoPorCep() {
		String cep = ftxtCep.getText();
		
		CepUtils.searchCep(cep).thenAccept(optEndereco -> {
			EventQueue.invokeLater(() -> {
				if (optEndereco.isPresent()) {
					var e = optEndereco.get();
					txtLogradouro.setText(e.getLogradouroEndereco().toUpperCase());
					txtBairro.setText(e.getBairroEndereco().toUpperCase());
					txtCidade.setText(e.getCidadeEndereco().toUpperCase());
					txtEstado.setText(e.getEstadoEndereco().toUpperCase());
					// txtComplemento.setText(endereco.getPaisEndereco());
				} else {
					DialogFactory.aviso(this, "CEP não Encontrado.");
					ftxtCep.requestFocusInWindow();
				}
			});
		});

	}

	private void exibirErro(JTextComponent campo, String mensagem) {
		campo.setBorder(BORDA_ERRO);
		JOptionPane.showMessageDialog(this, mensagem, "CAMPO INVÁlido", JOptionPane.WARNING_MESSAGE);
		campo.requestFocusInWindow();
	}

	private void removerDestaque(JTextComponent campo) {
		campo.setBorder(BORDA_PADRAO);
	}

	private void aoClicarSalvar(ActionEvent e) {
		String tipo = String.valueOf(cbTipoDoc.getSelectedItem());
		if ("SELECIONE".equals(tipo)) {
			JOptionPane.showMessageDialog(this, "SELECIONE O TIPO DE DOCUMENTO (CNPJ OU CPF).");
			cbTipoDoc.requestFocusInWindow();
			return;
		}
		String cep = ftxtCep.getText().replaceAll("\\D", "");
		if (cep.length() == 8) {
			exibirErro(ftxtCep, "CEP INVÁLIDO. USE 8 DÍGITOS.");
			return;
		}

		preencherEnderecoPorCep();
		validarDocumento();
		validarInscricaoEstadual();
		validarInscricaoMunicipal();
		validarFundacao();
		validarTelefoneCelular();
		validarTelefoneFixo();

		if (hasErroVisual()) {
			JOptionPane.showMessageDialog(this, "CORRIGE OS CAMPOS DESTACADOS EM VERMELHO ANTES DE SALVAR.",
					"CAMPOS INVÁLIDOS", JOptionPane.WARNING_MESSAGE);
			return;
		}

		JOptionPane.showMessageDialog(this, "CONFIGURAÇÂO SALVA COM SUCESSO!\nA APLICAÇÂO SERÁ REINICIADA.", "SUCESSO",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private boolean hasErroVisual() {
		return (ftxtDocumento.getBorder() == BORDA_ERRO) || (txtInscEst.getBorder() == BORDA_ERRO)
				|| (txtInscMun.getBorder() == BORDA_ERRO) || (ftxtFundacao.getBorder() == BORDA_ERRO)
				|| (ftxtTelCel.getBorder() == BORDA_ERRO) || (ftxtTelFixo.getBorder() == BORDA_ERRO)
				|| (ftxtCapital.getBorder() == BORDA_ERRO) || (ftxtCep.getBorder() == BORDA_ERRO);
	}

}
