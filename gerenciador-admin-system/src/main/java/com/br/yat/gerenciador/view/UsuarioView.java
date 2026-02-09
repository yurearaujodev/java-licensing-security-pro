package com.br.yat.gerenciador.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.ComboBoxFactory;
import com.br.yat.gerenciador.view.factory.DesktopFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.FormatterUtils;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.MaskFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;

import net.miginfocom.swing.MigLayout;

public class UsuarioView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtNome;
	private JTextField txtEmail;
	private JTextField txtEmpresa;
	private Integer idEmpresa;
	private Integer idPerfil;
	private JPasswordField txtSenhaNova;
	private JPasswordField txtSenhaAntiga;
	private JPasswordField txtConfirmarSenha;
	private JProgressBar barraForcaSenha;

	private JComboBox<StatusUsuario> cbStatus;
	private JButton btnSalvar;
	private JButton btnNovo;
	private JButton btnCancelar;
	private JCheckBox chkMaster;
	private JComboBox<Perfil> cbPerfil;

	private final Map<String, JCheckBox> chkTodosPorCategoria = new LinkedHashMap<>();

	private final Map<String, List<MenuChave>> gruposPermissoes = new LinkedHashMap<>();

	private final Map<MenuChave, Map<String, JCheckBox>> permissoesGranulares = new LinkedHashMap<>();

	private final Map<MenuChave, JTextField> datasExpiracao = new LinkedHashMap<>();
	
	public UsuarioView() {
		super("Cadastro de Usuário", true, true, true, true);

		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[][grow][]"));

		montarTela();
		add(criarPermissoes(), "cell 0 1, grow");
		add(criarBotoes(), "cell 0 2, right");
		setSize(800, 600);
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]", "[][][]");
		panel.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));
		montarCampos(panel);
		add(panel, "cell 0 0,grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("USUÁRIO: "), "cell 0 0, alignx trailing");
		txtNome = FieldFactory.createTextField(20);
		panel.add(txtNome, "cell 1 0,growx,h 25!");

	

		panel.add(LabelFactory.createLabel("E-MAIL: "), "cell 2 0, alignx trailing");
		txtEmail = FieldFactory.createTextField(20);
		panel.add(txtEmail, "cell 3 0,growx,h 25!");

		panel.add(LabelFactory.createLabel("SENHA ANTIGA: "), "cell 0 1, alignx trailing");
		txtSenhaAntiga = FieldFactory.createPasswordField(20);
		panel.add(txtSenhaAntiga, "cell 1 1,growx,h 25!");

		panel.add(LabelFactory.createLabel("STATUS: "), "cell 2 1, alignx trailing");
		cbStatus = ComboBoxFactory.createEnumComboBox(StatusUsuario.class);
		panel.add(cbStatus, "cell 3 1,growx,h 25!");

		panel.add(LabelFactory.createLabel("NOVA SENHA: "), "cell 0 2, alignx trailing");
		txtSenhaNova = FieldFactory.createPasswordField(20);
		panel.add(txtSenhaNova, "cell 1 2,growx,h 25!");

		barraForcaSenha = DesktopFactory.createPasswordStrengthBar();
		panel.add(barraForcaSenha, "cell 1 2,growx,h 15!, wrap");

		panel.add(LabelFactory.createLabel("CONFIRMAR SENHA: "), "cell 2 2, alignx trailing");
		txtConfirmarSenha = FieldFactory.createPasswordField(20);
		panel.add(txtConfirmarSenha, "cell 3 2,growx,h 25!");

		panel.add(LabelFactory.createLabel("MASTER: "), "cell 2 3, alignx trailing");
		chkMaster = ButtonFactory.createCheckBox("ESTE USUÁRIO É ADMINISTRADOR MASTER");
		panel.add(chkMaster, "cell 3 3 3 1, growx");

		panel.add(LabelFactory.createLabel("EMPRESA: "), "cell 0 4, alignx trailing");
		txtEmpresa = FieldFactory.createTextField(20);
		txtEmpresa.setEnabled(false);
		panel.add(txtEmpresa, "cell 1 4 3 1,growx,h 25!");
		
		panel.add(LabelFactory.createLabel("PERFIL: "), "cell 0 3, alignx trailing");
		cbPerfil = new JComboBox<>(); // Aqui você vai carregar os perfis do banco
		panel.add(cbPerfil, "cell 1 3, growx, h 25!");

	}

	private JScrollPane criarPermissoes() {
		JPanel container = new JPanel(new MigLayout("wrap 1", "[grow]", "[]10[]"));
		container.setBorder(BorderFactory.createTitledBorder("Permissões do Usuário"));

		this.pnlPermissoesContainer = container;

		return DesktopFactory.createScroll(container);
	}

	private JPanel pnlPermissoesContainer;

//	public void construirGradePermissoes(Map<String, List<MenuChave>> grupos) {
//		gruposPermissoes.clear();
//		gruposPermissoes.putAll(grupos);
//		pnlPermissoesContainer.removeAll();
//		permissoesGranulares.clear();
//
//		for (var entry : gruposPermissoes.entrySet()) {
//			String categoria = entry.getKey();
//			List<MenuChave> chaves = entry.getValue();
//
//			JPanel categoriaPanel = new JPanel(new MigLayout("wrap 4", "[grow,fill][center][center][center]", "[]5[]"));
//			categoriaPanel.setBorder(BorderFactory.createTitledBorder(categoria));
//
//			JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR TODOS DA CATEGORIA");
//			chkTodosPorCategoria.put(categoria, chkTodos);
//			categoriaPanel.add(chkTodos, "span 4, left, wrap");
//
//			// Cabeçalho da tabela
//			categoriaPanel.add(LabelFactory.createLabel("MENU"), "growx");
//			categoriaPanel.add(LabelFactory.createLabel("VER (R)"), "w 60!");
//			categoriaPanel.add(LabelFactory.createLabel("EDITAR (W)"), "w 60!");
//			categoriaPanel.add(LabelFactory.createLabel("EXCLUIR (D)"), "w 60!");
//			categoriaPanel.add(LabelFactory.createLabel("EXPIRA EM"), "w 100!");
//			
//			for (MenuChave chave : chaves) {
//				categoriaPanel.add(LabelFactory.createLabel(formatarTexto(chave.name())), "growx");
//
//				Map<String, JCheckBox> tiposMap = new LinkedHashMap<>();
//
//				JCheckBox chkRead = new JCheckBox();
//				JCheckBox chkWrite = new JCheckBox();
//				JCheckBox chkDelete = new JCheckBox();
//
//				categoriaPanel.add(chkRead);
//				categoriaPanel.add(chkWrite);
//				categoriaPanel.add(chkDelete);
//
//				tiposMap.put("READ", chkRead);
//				tiposMap.put("WRITE", chkWrite);
//				tiposMap.put("DELETE", chkDelete);
//
//				permissoesGranulares.put(chave, tiposMap);
//				
//				JTextField txtData = FieldFactory.createTextField(10);
//		        txtData.setToolTipText("dd/mm/aaaa hh:mm");
//		        datasExpiracao.put(chave, txtData);
//		        categoriaPanel.add(txtData, "w 100!");
//			}
//			pnlPermissoesContainer.add(categoriaPanel, "growx, wrap");
//		}
//		pnlPermissoesContainer.revalidate();
//		pnlPermissoesContainer.repaint();
//	}

	public void construirGradePermissoes(Map<String, List<MenuChave>> grupos) {
	    gruposPermissoes.clear();
	    gruposPermissoes.putAll(grupos);
	    pnlPermissoesContainer.removeAll();
	    permissoesGranulares.clear();
	    datasExpiracao.clear();

	    for (var entry : gruposPermissoes.entrySet()) {
	        String categoria = entry.getKey();
	        List<MenuChave> chaves = entry.getValue();

	        // Layout com larguras preferenciais: Menu[grow], R[50], W[50], D[50], Data[120]
	        JPanel categoriaPanel = new JPanel(new MigLayout("fillx, insets 10", "[grow,fill][50!][50!][50!][120!]", "[]5[]"));
	        categoriaPanel.setBorder(BorderFactory.createTitledBorder(categoria));

	        JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR TODOS DESTA CATEGORIA");
	        chkTodosPorCategoria.put(categoria, chkTodos);
	        categoriaPanel.add(chkTodos, "span 5, left, gapy 5 10, wrap");

	        // Cabeçalho estilizado
	        categoriaPanel.add(LabelFactory.createLabel("MÓDULO / MENU"), "center");
	        categoriaPanel.add(LabelFactory.createLabel("VER"), "center");
	        categoriaPanel.add(LabelFactory.createLabel("ADD"), "center");
	        categoriaPanel.add(LabelFactory.createLabel("DEL"), "center");
	        categoriaPanel.add(LabelFactory.createLabel("EXPIRA EM (OPCIONAL)"), "center, wrap");

	        for (MenuChave chave : chaves) {
	            categoriaPanel.add(LabelFactory.createLabel(formatarTexto(chave.name())), "");

	            Map<String, JCheckBox> tiposMap = new LinkedHashMap<>();
	            JCheckBox chkRead = new JCheckBox();
	            JCheckBox chkWrite = new JCheckBox();
	            JCheckBox chkDelete = new JCheckBox();

	            categoriaPanel.add(chkRead, "center");
	            categoriaPanel.add(chkWrite, "center");
	            categoriaPanel.add(chkDelete, "center");

	            tiposMap.put("READ", chkRead);
	            tiposMap.put("WRITE", chkWrite);
	            tiposMap.put("DELETE", chkDelete);
	            permissoesGranulares.put(chave, tiposMap);
	            
	            JFormattedTextField txtData = FieldFactory.createFormattedField();
	            String mask = MaskFactory.createMask().get("DATA_HORA");
	            FormatterUtils.applyDateMask(txtData, mask);
	            txtData.setToolTipText("Ex: 31/12/2026 23:59");
	            datasExpiracao.put(chave, txtData);
	            categoriaPanel.add(txtData, "growx, h 20!, wrap");
	        }
	        pnlPermissoesContainer.add(categoriaPanel, "growx, wrap 10");
	    }
	    pnlPermissoesContainer.revalidate();
	    pnlPermissoesContainer.repaint();
	}
	
	private JPanel criarBotoes() {
		JPanel panel = PanelFactory.createPanel("insets 5", "[left][grow][right]", "[]");

		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		panel.add(btnNovo, "cell 0 0,h 35!,w 140!");

		btnCancelar = ButtonFactory.createPrimaryButton("CANCELAR", IconFactory.cancelar());
		panel.add(btnCancelar, "cell 2 0 2 1,split 2,alignx center,w 140!, h 35!");

		btnSalvar = ButtonFactory.createPrimaryButton("SALVAR", IconFactory.salvar());
		panel.add(btnSalvar, "w 140!, h 35!, alignx center");
		return panel;
	}
	
	public Map<MenuChave, String> getDatasExpiracaoTexto() {
	    Map<MenuChave, String> dados = new LinkedHashMap<>();
	    datasExpiracao.forEach((chave, txtField) -> {
	        dados.put(chave, txtField.getText());
	    });
	    return dados;
	}
	
	private String formatarTexto(String texto) {
		return texto.replace("_", " ").toUpperCase();
	}

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

	public void setEmpresa(Integer id, String nome) {
		this.idEmpresa = id;
		this.txtEmpresa.setText(nome);
	}

	public Integer getEmpresa() {
		return idEmpresa;
	}
	
	public void setPerfil(Perfil perfil) {
	    if (perfil == null) return;
	    
	    this.idPerfil = perfil.getIdPerfil();
	    cbPerfil.removeAllItems();
	    cbPerfil.addItem(perfil);
	    cbPerfil.setSelectedItem(perfil);
	}

	public Integer getPerfil() {
	    return idPerfil;
	}

//	public Perfil getPerfilSelecionado() {
//		return (Perfil) cbPerfil.getSelectedItem();
//	}

	public JComboBox<Perfil> getCbPerfil() {
		return cbPerfil;
	}

	public void carregarCombosPerfil(List<Perfil> perfis) {
		cbPerfil.removeAllItems();
		for (Perfil p : perfis) {
			cbPerfil.addItem(p);
		}
	}

//	public void setPerfil(Integer idPerfil) {
//		for (int i = 0; i < cbPerfil.getItemCount(); i++) {
//			Perfil p = cbPerfil.getItemAt(i);
//			if (p.getIdPerfil().equals(idPerfil)) {
//				cbPerfil.setSelectedIndex(i);
//				break;
//			}
//		}
//	}

	public StatusUsuario getStatus() {
		return (StatusUsuario) cbStatus.getSelectedItem();
	}

	public void setStatus(StatusUsuario status) {
		cbStatus.setSelectedItem(status);
	}

	public Map<MenuChave, Map<String, JCheckBox>> getPermissoesGranulares() {
		return permissoesGranulares;
	}

	public Map<String, JCheckBox> getChkTodosPorCategoria() {
		return chkTodosPorCategoria;
	}

	public Map<String, List<MenuChave>> getGruposPermissoes() {
		return gruposPermissoes;
	}

	public JTextField getTxtNome() {
		return txtNome;
	}

	public JTextField getTxtEmail() {
		return txtEmail;
	}

	public JTextField getTxtEmpresa() {
		return txtEmpresa;
	}

	public JPasswordField getTxtSenha() {
		return txtSenhaNova;
	}

	public JPasswordField getTxtSenhaAntiga() {
		return txtSenhaAntiga;
	}

	public JComboBox<StatusUsuario> getCbStatus() {
		return cbStatus;
	}

	public JButton getBtnSalvar() {
		return btnSalvar;
	}

	public void setTextoBotaoSalvar(String texto) {
		btnSalvar.setText(texto);
	}

	public JButton getBtnNovo() {
		return btnNovo;
	}

	public JButton getBtnCancelar() {
		return btnCancelar;
	}

	public boolean isMaster() {
		return chkMaster.isSelected();
	}

	public void setMaster(boolean master) {
		chkMaster.setSelected(master);
		chkMaster.setEnabled(false);
	}

	public JCheckBox getChkMaster() {
		return chkMaster;
	}

	public JProgressBar getBarraForcaSenha() {
		return barraForcaSenha;
	}

	public void limpar() {
		idEmpresa = null;
		txtNome.setText("");
		txtEmail.setText("");
		txtSenhaNova.setText("");
		txtSenhaAntiga.setText("");
		txtConfirmarSenha.setText("");
		cbStatus.setSelectedIndex(0);
		txtEmpresa.setText("");

		chkMaster.setSelected(false);
		chkMaster.setEnabled(false);

		permissoesGranulares.values().forEach(map -> map.values().forEach(chk -> chk.setSelected(false)));
		chkTodosPorCategoria.values().forEach(cb -> cb.setSelected(false));

	}

	public void desativarAtivar(boolean ativa) {
		txtNome.setEnabled(ativa);
		txtEmail.setEnabled(ativa);
		txtSenhaNova.setEnabled(ativa);
		txtSenhaAntiga.setEnabled(ativa);
		txtConfirmarSenha.setEnabled(ativa);
		cbStatus.setEnabled(ativa);
		chkMaster.setEnabled(ativa);

		permissoesGranulares.values().forEach(map -> map.values().forEach(chk -> chk.setEnabled(ativa)));
		chkTodosPorCategoria.values().forEach(cb -> cb.setEnabled(ativa));
	}

	public void setPermissaoSelecionada(MenuChave chave, String tipo, boolean selecionada) {
		Map<String, JCheckBox> tiposMap = permissoesGranulares.get(chave);
		if (tiposMap != null) {
			JCheckBox chk = tiposMap.get(tipo); // tipo aqui é "READ", "WRITE" ou "DELETE"
			if (chk != null) {
				chk.setSelected(selecionada);
			}
		}
	}

	public void desmarcarTodasPermissoes() {
		// Limpa as granulares (READ, WRITE, DELETE)
		permissoesGranulares.values().forEach(map -> map.values().forEach(chk -> chk.setSelected(false)));
		// Limpa os seletores de categoria
		chkTodosPorCategoria.values().forEach(chk -> chk.setSelected(false));
	}

	public void atualizarStatusMarcarTodos(String categoria, boolean selecionado) {
		JCheckBox chk = chkTodosPorCategoria.get(categoria);
		if (chk != null) {
			chk.setSelected(selecionado);
		}
	}

	public void bloquearStatus(boolean editavel) {
		this.cbStatus.setEnabled(editavel);
	}

	public void bloquearGradePermissoes(boolean editavel) {
		// Primeiro, entramos em cada MenuChave do Map principal
		getPermissoesGranulares().values().forEach(tiposMap -> {
			// Agora, para cada mapa de tipos (READ, WRITE, DELETE), desativamos os
			// checkboxes
			tiposMap.values().forEach(chk -> chk.setEnabled(editavel));
		});

		// Desativa também os seletores de "Marcar Todos" por categoria
		getChkTodosPorCategoria().values().forEach(chk -> chk.setEnabled(editavel));
	}

}
