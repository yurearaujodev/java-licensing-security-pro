package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Endereco;

public class EmpresaDao extends GenericDao<Empresa> {

	private final String SELECT_COMPLETO = "SELECT emp.*,end.* FROM " + tableName + " emp "
			+ "INNER JOIN endereco end ON emp.id_endereco = end.id_endereco ";

	public EmpresaDao(Connection conn) throws SQLException {
		super(conn, "empresa", "id_empresa");
	}

	public Empresa save(Empresa emp) {
		String sql = "INSERT INTO " + tableName + "(tipo,nome_fantasia,razao_social,tipo_documento,documento,"
				+ "insc_estadual,contribuinte_icms,insc_municipal,data_fundacao,cnae,porte_empresa,"
				+ "natureza_juridica,crt,regime_tributario,capital_social,situacao_cadastral,"
				+ "criado_em,atualizado_em,id_endereco) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW(),?)";

		int id = executeUpdate(sql, emp.getTipoEmpresa(), emp.getFantasiaEmpresa(), emp.getRazaoSocialEmpresa(),
				emp.getTipoDocEmpresa(), emp.getDocumentoEmpresa(), emp.getInscEst(), emp.getContribuinteIcmsEmpresa(),
				emp.getInscMun(), emp.getFundacaoEmpresa(), emp.getCnaeEmpresa(), emp.getPorteEmpresa(),
				emp.getNaturezaJuriEmpresa(), emp.getCrtEmpresa(), emp.getRegimeTribEmpresa(), emp.getCapitalEmpresa(),
				emp.getSituacaoEmpresa(), emp.getEndereco().getIdEndereco());
		return searchById(id);
	}

	public Empresa update(Empresa emp) {
		String sql = "UPDATE " + tableName + " SET tipo=?,nome_fantasia=?,razao_social=?,tipo_documento=?,documento=?,"
				+ "insc_estadual=?,contribuinte_icms=?,insc_municipal=?,data_fundacao=?,cnae=?,porte_empresa=?,"
				+ "natureza_juridica=?,crt=?,regime_tributario=?,capital_social=?,situacao_cadastral=?,"
				+ "atualizado_em=NOW() WHERE " + pkName + "=?";
		executeUpdate(sql, emp.getTipoEmpresa(), emp.getFantasiaEmpresa(), emp.getRazaoSocialEmpresa(),
				emp.getTipoDocEmpresa(), emp.getDocumentoEmpresa(), emp.getInscEst(), emp.getContribuinteIcmsEmpresa(),
				emp.getInscMun(), emp.getFundacaoEmpresa(), emp.getCnaeEmpresa(), emp.getPorteEmpresa(),
				emp.getNaturezaJuriEmpresa(), emp.getCrtEmpresa(), emp.getRegimeTribEmpresa(), emp.getCapitalEmpresa(),
				emp.getSituacaoEmpresa(), emp.getIdEmpresa());
		return searchById(emp.getIdEmpresa());
	}

	public void delete(int id) {
		executeUpdate("DELETE FROM " + tableName + " WHERE " + pkName + " =?", id);
	}

	@Override
	public Empresa searchById(int id) {
		String sql = SELECT_COMPLETO + " WHERE emp." + pkName + " = ?";
		List<Empresa> lista = executeQuery(sql, id);
		return lista.isEmpty() ? null : lista.get(0);
	}

	public List<Empresa> listAll() {
		return executeQuery(SELECT_COMPLETO);
	}

	public Empresa buscarPorFornecedora() {
		var sql = SELECT_COMPLETO + " WHERE emp.tipo = 'FORNECEDORA' LIMIT 1";
		List<Empresa> resultados = executeQuery(sql);
		return resultados.isEmpty() ? null : resultados.get(0);
	}

	public List<Empresa> filtrarClientes(String termo) {
		var sql = SELECT_COMPLETO + " WHERE emp.tipo = 'CLIENTE' AND (emp.razao_social LIKE ? OR emp.documento LIKE ?)";
		var busca = "%" + termo + "%";
		return executeQuery(sql, busca, busca);
	}

	public List<Empresa> listarTodosClientes() {
		var sql = SELECT_COMPLETO + " WHERE emp.tipo = 'CLIENTE'";
		return executeQuery(sql);
	}

	@Override
	protected Empresa mapResultSetToEntity(ResultSet rs) throws SQLException {
		Empresa emp = new Empresa();
		emp.setIdEmpresa(rs.getInt(pkName));
		emp.setTipoEmpresa(rs.getString("tipo"));
		emp.setFantasiaEmpresa(rs.getString("nome_fantasia"));
		emp.setRazaoSocialEmpresa(rs.getString("razao_social"));
		emp.setTipoDocEmpresa(rs.getString("tipo_documento"));
		emp.setDocumentoEmpresa(rs.getString("documento"));
		emp.setInscEst(rs.getString("insc_estadual"));
		emp.setContribuinteIcmsEmpresa(rs.getString("contribuinte_icms"));
		emp.setInscMun(rs.getString("insc_municipal"));
		emp.setFundacaoEmpresa(rs.getObject("data_fundacao", LocalDate.class));
		emp.setCnaeEmpresa(rs.getString("cnae"));
		emp.setPorteEmpresa(rs.getString("porte_empresa"));
		emp.setNaturezaJuriEmpresa(rs.getString("natureza_juridica"));
		emp.setCrtEmpresa(rs.getInt("crt"));
		emp.setRegimeTribEmpresa(rs.getString("regime_tributario"));
		emp.setCapitalEmpresa(rs.getBigDecimal("capital_social"));
		emp.setSituacaoEmpresa(rs.getString("situacao_cadastral"));

		Endereco end = new Endereco();
		end.setIdEndereco(rs.getInt("id_endereco"));
		end.setCepEndereco(rs.getString("cep"));
		end.setLogradouroEndereco(rs.getString("logradouro"));
		end.setComplementoEndereco(rs.getString("complemento"));
		end.setBairroEndereco(rs.getString("bairro"));
		end.setNumeroEndereco(rs.getString("numero"));
		end.setCidadeEndereco(rs.getString("cidade"));
		end.setEstadoEndereco(rs.getString("estado"));
		end.setPaisEndereco(rs.getString("pais"));
		emp.setEndereco(end);
		return emp;
	}

}
