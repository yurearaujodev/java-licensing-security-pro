package com.br.yat.gerenciador.view;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentListener;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.view.factory.*;

public class UsuarioView extends BaseCadastroView {
	private static final long serialVersionUID = 1L;

	private final JTextField txtNome = FieldFactory.createTextField(20);
	private final JTextField txtEmail = FieldFactory.createTextField(20);
	private final JTextField txtEmpresa = FieldFactory.createTextField(20);
	private final JPasswordField txtSenhaNova = FieldFactory.createPasswordField(20);
	private final JPasswordField txtSenhaAntiga = FieldFactory.createPasswordField(20);
	private final JPasswordField txtConfirmarSenha = FieldFactory.createPasswordField(20);
	private final JProgressBar barraForcaSenha = DesktopFactory.createPasswordStrengthBar();
	private final JComboBox<StatusUsuario> cbStatus = ComboBoxFactory.createEnumComboBox(StatusUsuario.class);
	private final JComboBox<Perfil> cbPerfil = new JComboBox<>();
	private final JCheckBox chkMaster = ButtonFactory.createCheckBox("ESTE USUÁRIO É ADMINISTRADOR MASTER");
	private Integer idEmpresa;

	public UsuarioView() {
		super("Cadastro de Usuário", true);
		txtEmpresa.setEnabled(false);
		chkMaster.setEnabled(false);
		montarLayoutPrincipal(montarPainelDadosBasicos());
		setSize(850, 650);
	}

	private JPanel montarPainelDadosBasicos() {
	    JPanel p = PanelFactory.createPanel("gapx 10, gapy 10", 
	                                        "[right][grow,fill][right][grow,fill]", 
	                                        "[][][][][]");
	    p.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));

	    // Linha 0
	    p.add(LabelFactory.createLabel("USUÁRIO: "), "cell 0 0, alignx trailing");
	    p.add(txtNome, "cell 1 0, growx, h 25!");
	    p.add(LabelFactory.createLabel("E-MAIL: "), "cell 2 0, alignx trailing");
	    p.add(txtEmail, "cell 3 0, growx, h 25!");

	    // Linha 1
	    p.add(LabelFactory.createLabel("SENHA ANTIGA: "), "cell 0 1, alignx trailing");
	    p.add(txtSenhaAntiga, "cell 1 1, growx, h 25!");
	    p.add(LabelFactory.createLabel("STATUS: "), "cell 2 1, alignx trailing");
	    p.add(cbStatus, "cell 3 1, growx, h 25!");

	    // Linha 2
	    p.add(LabelFactory.createLabel("NOVA SENHA: "), "cell 0 2, alignx trailing");
	    p.add(txtSenhaNova, "cell 1 2, growx, h 25!");
	    p.add(barraForcaSenha, "cell 1 2, growx, h 15!, wrap");
	    p.add(LabelFactory.createLabel("CONFIRMAR SENHA: "), "cell 2 2, alignx trailing");
	    p.add(txtConfirmarSenha, "cell 3 2, growx, h 25!");

	    // Linha 3
	    p.add(LabelFactory.createLabel("PERFIL: "), "cell 0 3, alignx trailing");
	    p.add(cbPerfil, "cell 1 3, growx, h 25!");
	    p.add(LabelFactory.createLabel("MASTER: "), "cell 2 3, alignx trailing");
	    p.add(chkMaster, "cell 3 3, growx");

	    // Linha 4
	    p.add(LabelFactory.createLabel("EMPRESA: "), "cell 0 4, alignx trailing");
	    p.add(txtEmpresa, "cell 1 4 3 1, growx, h 25!");

	    return p;
	}

	// Métodos específicos de Usuário (Expiração)
	public String getDataExpiracao(MenuChave c) {
		return pnlPermissoes.getDataExpiracao(c);
	}

	public void setDataExpiracao(MenuChave c, LocalDateTime d) {
		pnlPermissoes.setDataExpiracao(c, d);
	}

	@Override
	public void limpar() {
		txtNome.setText("");
		txtEmail.setText("");
		limparSenhas();
		cbStatus.setSelectedIndex(0);
		cbPerfil.setSelectedItem(null);
		chkMaster.setSelected(false);
		limparPermissoes();
	}

	public void limparSenhas() {
		txtSenhaNova.setText("");
		txtSenhaAntiga.setText("");
		txtConfirmarSenha.setText("");
		barraForcaSenha.setValue(0);
	}
	
	@Override
	public void setCamposHabilitados(boolean habilitado) {
	    txtNome.setEnabled(habilitado);
	    txtEmail.setEnabled(habilitado);
	    txtSenhaNova.setEnabled(habilitado);
	    txtSenhaAntiga.setEnabled(habilitado);
	    txtConfirmarSenha.setEnabled(habilitado);
	    cbStatus.setEnabled(habilitado);
	    cbPerfil.setEnabled(habilitado);
	    chkMaster.setEnabled(habilitado); 
	    
	    setPermissoesHabilitadas(habilitado);
	}

	// Getters/Setters necessários para a Controller
	public String getNome() {
		return txtNome.getText();
	}

	public void setNome(String n) {
		txtNome.setText(n);
	}

	public String getEmail() {
		return txtEmail.getText();
	}

	public void setEmail(String e) {
		txtEmail.setText(e);
	}

	public char[] getSenhaNova() {
		return txtSenhaNova.getPassword();
	}

	public char[] getSenhaAntiga() {
		return txtSenhaAntiga.getPassword();
	}

	public char[] getConfirmarSenha() {
		return txtConfirmarSenha.getPassword();
	}

	public StatusUsuario getStatus() {
		return (StatusUsuario) cbStatus.getSelectedItem();
	}

	public void setStatus(StatusUsuario s) {
		cbStatus.setSelectedItem(s);
	}

	public boolean isMaster() {
		return chkMaster.isSelected();
	}

	public void setMaster(boolean m) {
		chkMaster.setSelected(m);
	}

	public void setMasterHabilitado(boolean h) {
		chkMaster.setEnabled(h);
	}

	public void setEmpresa(Integer id, String n) {
		this.idEmpresa = id;
		this.txtEmpresa.setText(n);
	}

	public Integer getIdEmpresa() {
		return idEmpresa;
	}

	public void carregarCombosPerfil(List<Perfil> l) {
		cbPerfil.removeAllItems();
		if (l != null)
			l.forEach(cbPerfil::addItem);
	}

	public Perfil getPerfilSelecionado() {
		return (Perfil) cbPerfil.getSelectedItem();
	}

	public void setPerfil(Perfil p) {
		cbPerfil.setSelectedItem(p);
	}

	public void setPerfilHabilitado(boolean h) {
		cbPerfil.setEnabled(h);
	}

	public void adicionarListenerSenhaNova(DocumentListener listener) {
	    txtSenhaNova.getDocument().addDocumentListener(listener);
	}

	public void atualizarForcaSenha(int forca, Color cor, String texto) {
	    barraForcaSenha.setValue(forca);
	    barraForcaSenha.setForeground(cor);
	    barraForcaSenha.setString(texto);
	}
	
	public void configurarEstadoEdicao(boolean isMaster) {
	    setCamposHabilitados(true);
	    getBtnNovo().setEnabled(false);
	    getBtnSalvar().setEnabled(true);
	    setMasterHabilitado(false);
	    setPerfilHabilitado(!isMaster);
	    setPermissoesHabilitadas(!isMaster);
	}

	
}
