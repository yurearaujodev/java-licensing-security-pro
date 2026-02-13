package com.br.yat.gerenciador.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.*;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.*;
import net.miginfocom.swing.MigLayout;

public class UsuarioView extends JInternalFrame {

	private static final long serialVersionUID = 1L;

	// ===============================
	// COMPONENTES DE DADOS
	// ===============================
	private final JTextField txtNome;
	private final JTextField txtEmail;
	private final JTextField txtEmpresa;

	private final JPasswordField txtSenhaNova;
	private final JPasswordField txtSenhaAntiga;
	private final JPasswordField txtConfirmarSenha;

	private final JProgressBar barraForcaSenha;

	private final JComboBox<StatusUsuario> cbStatus;
	private final JComboBox<Perfil> cbPerfil;

	private final JCheckBox chkMaster;

	private Integer idEmpresa;

	// ===============================
	// COMPONENTE DE PERMISSÕES
	// ===============================
	private final PainelPermissoes pnlPermissoes;

	// ===============================
	// BOTÕES
	// ===============================
	private final JButton btnSalvar;
	private final JButton btnNovo;
	private final JButton btnCancelar;

	// ===============================
	// CONSTRUTOR
	// ===============================
	public UsuarioView() {
		super("Cadastro de Usuário", true, true, true, true);

		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[][grow][]"));

		// Instancia componentes
		txtNome = FieldFactory.createTextField(20);
		txtEmail = FieldFactory.createTextField(20);
		txtSenhaAntiga = FieldFactory.createPasswordField(20);
		txtSenhaNova = FieldFactory.createPasswordField(20);
		txtConfirmarSenha = FieldFactory.createPasswordField(20);
		txtEmpresa = FieldFactory.createTextField(20);
		txtEmpresa.setEnabled(false);

		cbStatus = ComboBoxFactory.createEnumComboBox(StatusUsuario.class);
		cbPerfil = new JComboBox<>();

		chkMaster = ButtonFactory.createCheckBox("ESTE USUÁRIO É ADMINISTRADOR MASTER");
		barraForcaSenha = DesktopFactory.createPasswordStrengthBar();

		pnlPermissoes = new PainelPermissoes(true);

		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		btnCancelar = ButtonFactory.createPrimaryButton("CANCELAR", IconFactory.cancelar());
		btnSalvar = ButtonFactory.createPrimaryButton("SALVAR", IconFactory.salvar());

		montarLayout();

		setSize(850, 650);
	}

	// ===============================
	// MONTAGEM DA TELA
	// ===============================
	private void montarLayout() {
		add(montarPainelDadosBasicos(), "cell 0 0, growx");
		add(DesktopFactory.createScroll(pnlPermissoes), "cell 0 1, grow");
		add(montarPainelBotoes(), "cell 0 2, right");
	}

	private JPanel montarPainelDadosBasicos() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][]");

		panel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));

		panel.add(LabelFactory.createLabel("USUÁRIO: "), "cell 0 0");
		panel.add(txtNome, "cell 1 0");

		panel.add(LabelFactory.createLabel("E-MAIL: "), "cell 2 0");
		panel.add(txtEmail, "cell 3 0");

		panel.add(LabelFactory.createLabel("SENHA ANTIGA: "), "cell 0 1");
		panel.add(txtSenhaAntiga, "cell 1 1");

		panel.add(LabelFactory.createLabel("STATUS: "), "cell 2 1");
		panel.add(cbStatus, "cell 3 1");

		panel.add(LabelFactory.createLabel("NOVA SENHA: "), "cell 0 2");
		panel.add(txtSenhaNova, "cell 1 2");
		panel.add(barraForcaSenha, "cell 1 2, growx, h 15!, gaptop 20");

		panel.add(LabelFactory.createLabel("CONFIRMAR: "), "cell 2 2");
		panel.add(txtConfirmarSenha, "cell 3 2");

		panel.add(LabelFactory.createLabel("PERFIL: "), "cell 0 3");
		panel.add(cbPerfil, "cell 1 3");

		panel.add(LabelFactory.createLabel("MASTER: "), "cell 2 3");
		panel.add(chkMaster, "cell 3 3");

		panel.add(LabelFactory.createLabel("EMPRESA: "), "cell 0 4");
		panel.add(txtEmpresa, "cell 1 4 3 1");

		return panel;
	}

	private JPanel montarPainelBotoes() {
		JPanel panel = new JPanel(new MigLayout("insets 5", "[left][grow][right]", "[]"));

		panel.add(btnNovo, "w 140!, h 35!");
		panel.add(btnCancelar, "gapleft push, w 140!, h 35!");
		panel.add(btnSalvar, "w 140!, h 35!");

		return panel;
	}

	// ===============================
	// API DE PERMISSÕES (ENCAPSULADA)
	// ===============================
	public void construirGradePermissoes(Map<String, List<MenuChave>> grupos) {
		pnlPermissoes.construirGrade(grupos);
	}

	public void setPermissao(MenuChave chave, TipoPermissao tipo, boolean valor) {
		pnlPermissoes.setPermissao(chave, tipo, valor);
	}

	public boolean isPermissaoSelecionada(MenuChave chave, TipoPermissao tipo) {
		return pnlPermissoes.isPermissaoSelecionada(chave, tipo);
	}

	public void setPermissoesHabilitadas(boolean habilitado) {
		pnlPermissoes.setHabilitado(habilitado);
	}

	public void setMasterHabilitado(boolean habilitado) {
		chkMaster.setEnabled(habilitado);
	}

	public void limparPermissoes() {
		pnlPermissoes.resetarSelecoes(); // Agora usamos o novo método
	}

	public void aplicarRestricaoPermissao(MenuChave chave, TipoPermissao tipo, boolean permitido) {
		pnlPermissoes.aplicarRestricaoPermissao(chave, tipo, permitido);
	}

	public void setDataExpiracao(MenuChave chave, java.time.LocalDateTime data) {
		pnlPermissoes.setDataExpiracao(chave, data);
	}

	// ===============================
	// API DE CATEGORIAS (NOVO)
	// ===============================

	public Set<String> getCategoriasPermissoes() {
		return pnlPermissoes.getCategorias();
	}

	public List<MenuChave> getChavesDaCategoria(String categoria) {
		return pnlPermissoes.getChavesDaCategoria(categoria);
	}

	public void marcarCategoria(String categoria, boolean marcar) {
		pnlPermissoes.marcarCategoria(categoria, marcar);
	}

	public boolean isCategoriaTotalmenteMarcada(String categoria) {
		return pnlPermissoes.isCategoriaTotalmenteMarcada(categoria);
	}

	public void setCategoriaMarcada(String categoria, boolean marcada) {
		pnlPermissoes.setCategoriaMarcada(categoria, marcada);
	}

	public void addListenerCategoria(String categoria, Consumer<Boolean> listener) {
		pnlPermissoes.addListenerCategoria(categoria, listener);
	}

	public void limpar() {
		txtNome.setText("");
		txtEmail.setText("");
		limparSenhas();
		cbStatus.setSelectedIndex(0);
		cbPerfil.setSelectedItem(null);
		chkMaster.setSelected(false);
		barraForcaSenha.setValue(0);
		limparPermissoes(); // Apenas reseta os checks, não apaga a grade da tela
	}

	// ===============================
	// API DE DADOS
	// ===============================
	public String getNome() {
		return txtNome.getText();
	}

	public void setNome(String nome) {
		txtNome.setText(nome);
	}

	public String getEmail() {
		return txtEmail.getText();
	}

	public void setEmail(String email) {
		txtEmail.setText(email);
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

	public void limparSenhas() {
		txtSenhaNova.setText("");
		txtSenhaAntiga.setText("");
		txtConfirmarSenha.setText("");
		barraForcaSenha.setValue(0);
	}

	public StatusUsuario getStatus() {
		return (StatusUsuario) cbStatus.getSelectedItem();
	}

	public void setStatus(StatusUsuario status) {
		cbStatus.setSelectedItem(status);
	}

	public boolean isMaster() {
		return chkMaster.isSelected();
	}

	public void setMaster(boolean master) {
		chkMaster.setSelected(master);
	}

	public void setEmpresa(Integer id, String nome) {
		this.idEmpresa = id;
		this.txtEmpresa.setText(nome);
	}

	public Integer getIdEmpresa() {
		return idEmpresa;
	}

	public void carregarCombosPerfil(List<Perfil> lista) {
		cbPerfil.removeAllItems();
		if (lista != null) {
			lista.forEach(cbPerfil::addItem);
		}
	}

	public Perfil getPerfilSelecionado() {
		return (Perfil) cbPerfil.getSelectedItem();
	}

	public String getDataExpiracao(MenuChave chave) {
		return pnlPermissoes.getDataExpiracao(chave);
	}

	public void setPerfil(Perfil perfil) {
		cbPerfil.setSelectedItem(perfil);
	}

	public void setPerfilHabilitado(boolean habilitado) {
		cbPerfil.setEnabled(habilitado);
	}

	public Set<MenuChave> getChavesAtivas() {
		return pnlPermissoes.getChavesConstruidas();
	}

	// ===============================
	// GETTERS DE AÇÃO (Controller)
	// ===============================
	public JButton getBtnSalvar() {
		return btnSalvar;
	}

	public JButton getBtnNovo() {
		return btnNovo;
	}

	public JButton getBtnCancelar() {
		return btnCancelar;
	}

	public JPasswordField getCampoSenhaNova() {
		return txtSenhaNova;
	}

	public JProgressBar getBarraForcaSenha() {
		return barraForcaSenha;
	}
}
