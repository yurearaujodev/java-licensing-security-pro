package com.br.yat.gerenciador.view.tables;

import java.util.Map;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.view.factory.FormatterUtils;
import com.br.yat.gerenciador.view.factory.MaskFactory;

public class EmpresaTableModel extends BaseTableModel<Empresa> {

	private static final long serialVersionUID = 1L;
	private final String[] colunas = { "ID", "RAZÃO SOCIAL", "CNPJ/CPF", "CIDADE/UF" };
	private static final Map<String, String> DOC_MASKS = MaskFactory.createMask();

	@Override
	public int getColumnCount() {
		return colunas.length;
	}

	@Override
	public String getColumnName(int column) {
		return colunas[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Empresa e = dados.get(rowIndex);
		String tipo = e.getTipoDocEmpresa().name();
		String valorFormatado = DOC_MASKS.containsKey(tipo)
				? FormatterUtils.formatValueWithMask(e.getDocumentoEmpresa(), DOC_MASKS.get(tipo))
				: e.getDocumentoEmpresa();
		return switch (columnIndex) {
		case 0 -> e.getIdEmpresa();
		case 1 -> e.getRazaoSocialEmpresa();
		case 2 -> valorFormatado;
		case 3 -> (e.getEndereco() != null)
				? e.getEndereco().getCidadeEndereco() + " / " + e.getEndereco().getEstadoEndereco()
				: "SEM ENDEREÇO";
		default -> null;
		};
	}

}
