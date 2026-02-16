package com.br.yat.gerenciador.view;

import javax.swing.*;

import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.*;

public class PerfilView extends BaseCadastroView {
	private static final long serialVersionUID = 1L;
	private final JTextField txtNome = FieldFactory.createTextField(30);
	private final JTextField txtDescricao = FieldFactory.createTextField(30);

	public PerfilView() {
		super("CADASTRO DE PERFIL DE ACESSO", false);
		montarLayoutPrincipal(montarPainelIdentificacao());
		setSize(800, 600);
	}

	private JPanel montarPainelIdentificacao() {
		JPanel panel = PanelFactory.createPanel("gapx 10", "[right][grow,fill]", "[]");
		panel.setBorder(BorderFactory.createTitledBorder("Identificação"));
		panel.add(LabelFactory.createLabel("NOME DO PERFIL: "));
		panel.add(txtNome, "h 25!");
		panel.add(LabelFactory.createLabel("DESCRIÇÃO: "));
		panel.add(txtDescricao, "h 25!");
		return panel;
	}

	@Override
	public void limpar() {
		txtNome.setText("");
		txtDescricao.setText("");
		limparPermissoes();
	}

	@Override
	public void setCamposHabilitados(boolean habilitado) {
		txtNome.setEnabled(habilitado);
		txtDescricao.setEnabled(habilitado);
		setPermissoesHabilitadas(habilitado);
	}

	public String getNome() {
		return txtNome.getText();
	}

	public void setNome(String n) {
		txtNome.setText(n);
	}

	public String getDescricao() {
		return txtDescricao.getText();
	}

	public void setDescricao(String d) {
		txtDescricao.setText(d);
	}

	public void setEdicaoNomeHabilitada(boolean h) {
		txtNome.setEnabled(h);
		txtNome.setEditable(h);
	}

	public JTextField getTxtNome() {
		return txtNome;
	}

	@Override
	public void setTextoBotaoSalvar(String texto) {
		this.btnSalvar.setText(texto);
		if ("ALTERAR".equals(texto)) {
			this.btnSalvar.setIcon(IconFactory.cancelar());
		} else {
			this.btnSalvar.setIcon(IconFactory.salvar());
		}
	}
}