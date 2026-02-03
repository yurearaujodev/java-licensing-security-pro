package com.br.yat.gerenciador.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.ComboBoxFactory;
import com.br.yat.gerenciador.view.factory.DesktopFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;

import net.miginfocom.swing.MigLayout;

public class UsuarioView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtNome;
	private JTextField txtEmail;
	private JTextField txtEmpresa;
	private Integer idEmpresa;
	private JPasswordField txtSenha;
	private JComboBox<StatusUsuario> cbStatus;
	private JButton btnSalvar;
	private JButton btnNovo;
	private JButton btnCancelar;
	private JCheckBox chkMaster;

	private final Map<MenuChave, JCheckBox> permissoes = new LinkedHashMap<>();

	private final Map<String, JCheckBox> chkTodosPorCategoria = new LinkedHashMap<>();

	private final Map<String, List<MenuChave>> gruposPermissoes = new LinkedHashMap<>();

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

		panel.add(LabelFactory.createLabel("SENHA: "), "cell 0 1, alignx trailing");
		txtSenha = FieldFactory.createPasswordField(20);
		panel.add(txtSenha, "cell 1 1,growx,h 25!");

		panel.add(LabelFactory.createLabel("STATUS: "), "cell 2 1, alignx trailing");
		cbStatus = ComboBoxFactory.createEnumComboBox(StatusUsuario.class);
		panel.add(cbStatus, "cell 3 1,growx,h 25!");

		panel.add(LabelFactory.createLabel("EMPRESA: "), "cell 0 3, alignx trailing");
		txtEmpresa = FieldFactory.createTextField(20);
		txtEmpresa.setEditable(false);
		panel.add(txtEmpresa, "cell 1 3 3 1,growx,h 25!");
		
		panel.add(LabelFactory.createLabel("MASTER: "), "cell 0 2, alignx trailing");
		chkMaster = ButtonFactory.createCheckBox("ESTE USUÁRIO É ADMINISTRADOR MASTER");
		panel.add(chkMaster, "cell 1 2 3 1, growx");
	}

	private JScrollPane criarPermissoes() {
        JPanel container = new JPanel(new MigLayout("wrap 1", "[grow]", "[]10[]"));
        container.setBorder(BorderFactory.createTitledBorder("Permissões do Usuário"));
        // Removido o MenuChaveGrouper daqui!
        
        // Atribuímos o container a uma variável para podermos adicionar os grupos depois
        this.pnlPermissoesContainer = container; 
        
        return DesktopFactory.createScroll(container);
    }
    
    // Variável necessária para adicionar os grupos dinamicamente
    private JPanel pnlPermissoesContainer;

    // NOVO MÉTODO: O Controller chamará este método passando os dados
    public void construirGradePermissoes(Map<String, List<MenuChave>> grupos) {
        gruposPermissoes.clear();
        gruposPermissoes.putAll(grupos);
        pnlPermissoesContainer.removeAll(); // Limpa se houver algo

        for (var entry : gruposPermissoes.entrySet()) {
            String categoria = entry.getKey();
            List<MenuChave> chaves = entry.getValue();

            JPanel categoriaPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]5[]"));
            categoriaPanel.setBorder(BorderFactory.createTitledBorder(categoria));

            JCheckBox chkTodos = ButtonFactory.createCheckBox("MARCAR TODOS");
            chkTodosPorCategoria.put(categoria, chkTodos);
            categoriaPanel.add(chkTodos, "span, growx");

            for (MenuChave chave : chaves) {
                JCheckBox cb = ButtonFactory.createCheckBox(formatarTexto(chave.name()));
                cb.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
                permissoes.put(chave, cb);
                categoriaPanel.add(cb, "wrap, growx");
            }
            pnlPermissoesContainer.add(categoriaPanel, "growx, wrap");
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

	public char[] getSenha() {
		return txtSenha.getPassword();
	}

	public void setSenha(char[] password) {
		txtSenha.setText("");
	}

	public void setEmpresa(Integer id, String nome) {
		this.idEmpresa = id;
		this.txtEmpresa.setText(nome);
	}

	public Integer getEmpresa() {
		return idEmpresa;
	}

	public StatusUsuario getStatus() {
		return (StatusUsuario)cbStatus.getSelectedItem();
	}

	public void setStatus(StatusUsuario status) {
		cbStatus.setSelectedItem(status);
	}

	public Map<MenuChave, JCheckBox> getPermissoes() {
		return permissoes;
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
		return txtSenha;
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
	}

	public JCheckBox getChkMaster() {
	    return chkMaster;
	}

	public void limpar() {
		idEmpresa = null;
		txtNome.setText("");
		txtEmail.setText("");
		txtSenha.setText("");
		cbStatus.setSelectedIndex(0);
		txtEmpresa.setText("");
		
		chkMaster.setSelected(false);
	    chkMaster.setEnabled(false);
		permissoes.values().forEach(cb -> cb.setSelected(false));
		chkTodosPorCategoria.values().forEach(cb -> cb.setSelected(false));
	}

	public void desativarAtivar(boolean ativa) {
		txtNome.setEnabled(ativa);
		txtEmail.setEnabled(ativa);
		txtSenha.setEnabled(ativa);
		cbStatus.setEnabled(ativa);
		permissoes.values().forEach(cb -> cb.setEnabled(ativa));
		chkTodosPorCategoria.values().forEach(cb -> cb.setEnabled(ativa));
	}

	// Substitua o marcarPermissoes antigo por este:
	public void setPermissaoSelecionada(MenuChave chave, boolean selecionada) {
		JCheckBox chk = permissoes.get(chave);
		if (chk != null) {
			chk.setSelected(selecionada);
		}
	}

	// Mantenha este para resetar a tela antes de começar
	public void desmarcarTodasPermissoes() {
		permissoes.values().forEach(chk -> chk.setSelected(false));
		chkTodosPorCategoria.values().forEach(chk -> chk.setSelected(false));
	}

	// Adicione este para atualizar o "Marcar Todos" de uma categoria específica
	public void atualizarStatusMarcarTodos(String categoria, boolean selecionado) {
		JCheckBox chk = chkTodosPorCategoria.get(categoria);
		if (chk != null) {
			chk.setSelected(selecionado);
		}
	}
	
	public void marcarTodosDaCategoria(String categoria, boolean selecionado) {
	    List<MenuChave> chaves = gruposPermissoes.get(categoria);
	    if (chaves != null) {
	        for (MenuChave chave : chaves) {
	            setPermissaoSelecionada(chave, selecionado);
	        }
	    }
	}
	
	public void bloquearStatus(boolean editavel) {
	    // Substitua 'comboStatus' pelo nome real do seu componente de status
	    this.cbStatus.setEnabled(editavel);
	}

	// Método para bloquear todos os checkboxes de permissões
	public void bloquearGradePermissoes(boolean editavel) {
	    getPermissoes().values().forEach(chk -> chk.setEnabled(editavel));
	    getChkTodosPorCategoria().values().forEach(chk -> chk.setEnabled(editavel));
	}
}
