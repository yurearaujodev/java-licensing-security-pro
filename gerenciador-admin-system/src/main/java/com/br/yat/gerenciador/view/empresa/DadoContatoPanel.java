package com.br.yat.gerenciador.view.empresa;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.br.yat.gerenciador.model.enums.TipoContato;
import com.br.yat.gerenciador.util.ui.ButtonFactory;
import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;

import net.miginfocom.swing.MigLayout;

public class DadoContatoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JFormattedTextField ftxtContato;
	private JComboBox<TipoContato> cbTipoContato;
	private JTable tabela;
	private JButton adicionar;
	private JButton remover;

	public DadoContatoPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));
		montarTela();

	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill]", "[][][][grow]");
		montarCampos(panel);
		add(panel, "grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("TIPO CONTATO: "), "cell 0 0, alignx trailing");
		cbTipoContato = ComboBoxFactory.createEnumComboBox(TipoContato.class);
		panel.add(cbTipoContato, "cell 1 0 2 1,growx,h 25!");

		ftxtContato = FieldFactory.createFormattedField();
		ftxtContato.setEnabled(false);
		panel.add(ftxtContato,"cell 0 1 2 1,growx, h 25!");

		adicionar = ButtonFactory.createPrimaryButton("Adicionar", null);
		panel.add(adicionar, "cell 0 2 2 1,split 2,alignx right,w 120!, h 25!");
		remover = ButtonFactory.createPrimaryButton("Remover", null);
		panel.add(remover, "w 120!, h 25!");

		tabela = TableFactory.createDefaultTable(new String[] { "TIPO", "VALOR" });
		configurarColunas(tabela);
		panel.add(TableFactory.createTableScrolling(tabela), "cell 0 3 2 1,growx,hmin 150,pushy");
	}

	private void configurarColunas(JTable tabela) {
		tabela.getColumnModel().getColumn(0).setPreferredWidth(180);
		tabela.getColumnModel().getColumn(0).setMinWidth(180);
		tabela.getColumnModel().getColumn(0).setMaxWidth(180);

		tabela.getColumnModel().getColumn(1).setPreferredWidth(462);
		tabela.getColumnModel().getColumn(1).setMinWidth(462);
		tabela.getColumnModel().getColumn(1).setMaxWidth(462);
	}
	
	public TipoContato getTipoContato() {
		return (TipoContato)cbTipoContato.getSelectedItem();
	}
	
	public void setTipoContato(TipoContato tipo) {
		cbTipoContato.setSelectedItem(tipo);
	}
	
	public String getContato() {
		return ftxtContato.getText();
	}
	
	public void setContato(String contato) {
		ftxtContato.setText(contato);
	}	

	public JComboBox<TipoContato> getCbTipoContato() {
		return cbTipoContato;
	}

	public JFormattedTextField getFtxtContato() {
		return ftxtContato;
	}

	public JTable getTabela() {
		return tabela;
	}

	public JButton getAdicionar() {
		return adicionar;
	}

	public JButton getRemover() {
		return remover;
	}
	
	public void limpar() {
		cbTipoContato.setSelectedIndex(0);
		ftxtContato.setText("");
	}
	
	public void desativarAtivar(boolean ativa) {
		cbTipoContato.setEnabled(ativa);
		adicionar.setEnabled(ativa);
		remover.setEnabled(ativa);
	}
}
