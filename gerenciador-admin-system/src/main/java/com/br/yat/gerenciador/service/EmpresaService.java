package com.br.yat.gerenciador.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.br.yat.gerenciador.dao.BancoDao;
import com.br.yat.gerenciador.dao.ComplementarDao;
import com.br.yat.gerenciador.dao.ContatoDao;
import com.br.yat.gerenciador.dao.DocumentoDao;
import com.br.yat.gerenciador.dao.EmpresaDao;
import com.br.yat.gerenciador.dao.EnderecoDao;
import com.br.yat.gerenciador.dao.RepresentanteDao;
import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.EmpresaDTO;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.model.enums.CryptoErrorType;
import com.br.yat.gerenciador.util.CryptoException;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.database.ConnectionFactory;
import com.br.yat.gerenciador.util.validation.DocumentValidator;
import com.br.yat.gerenciador.util.validation.FormatValidator;

public class EmpresaService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");

	public void salvarEmpresaCompleta(Endereco endereco, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementar,
			List<Documento> documentos) throws Exception {
		
		validarEndereco(endereco);
		validarEmpresa(empresa);
		validarContatos(contatos);

		boolean isFornecedora = "FORNECEDORA".equals(empresa.getTipoEmpresa());
		if (isFornecedora) {
			validarEmpresaComplementar(empresa);
			validarRepresentante(representantes);
			validarBanco(bancos);
			validarComplementarIndividual(complementar);
			validarDocumentos(documentos);
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);
			try {
				EnderecoDao endDao = new EnderecoDao(conn);
				if (endereco.getIdEndereco()>0) {
					endDao.update(endereco);
				}else {
					endereco=endDao.save(endereco);
				}
				
				empresa.setEndereco(endereco);
				EmpresaDao empDao = new EmpresaDao(conn);
				if (empresa.getIdEmpresa()>0) {
					empresa = empDao.update(empresa);
				} else {
					empresa = empDao.save(empresa);
				}
				
				salvarFornecedora(conn, empresa, contatos, representantes, bancos, complementar, documentos,
						isFornecedora);
				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw new Exception("FALHA AO SALVAR O CADASTRO COMPLETO: " + e.getMessage());
			}

		} catch (SQLException e) {
			throw new CryptoException(CryptoErrorType.INTERNAL_ERROR, "ERRO DE CONEXÃO" + e.getMessage());
		}
	}
	
	public EmpresaDTO carregarForncedoraCompleta()throws Exception{
		try(Connection conn = ConnectionFactory.getConnection()){
			EmpresaDao empDao = new EmpresaDao(conn);
			
			Empresa emp = empDao.buscarPorFornecedora();
			if(emp==null)return null;
			
			int id = emp.getIdEmpresa();
			
			List<Contato> contatos = new ContatoDao(conn).listarPorEmpresa(id);
			List<Representante> reps = new RepresentanteDao(conn).listarPorEmpresa(id);
			List<Banco> bancos = new BancoDao(conn).listarPorEmpresa(id);
			Complementar comp = new ComplementarDao(conn).buscarPorEmpresa(id);
			List<Documento> docs = new DocumentoDao(conn).listarPorEmpresa(id);
			
			return new EmpresaDTO(emp,contatos,reps,bancos,comp,docs);
		}
	}
	
	public EmpresaDTO carregarClienteCompleto(int id)throws Exception{
		try(Connection conn = ConnectionFactory.getConnection()){
			EmpresaDao empDao = new EmpresaDao(conn);
			Empresa emp = empDao.searchById(id);
			if(emp==null)return null;
			
			List<Contato> contatos = new ContatoDao(conn).listarPorEmpresa(id);
			return new EmpresaDTO(emp, contatos, null, null, null, null);
		}
	}
	
	public List<Empresa> listarClientesParaTabela()throws Exception{
		try(Connection conn = ConnectionFactory.getConnection()){
			return new EmpresaDao(conn).listarTodosClientes();
		}
	}
	
	public List<Empresa> filtrarClientes(String termo)throws Exception{
		try(Connection conn = ConnectionFactory.getConnection()){
			return new EmpresaDao(conn).filtrarClientes(termo);
		}
	}

	public void validarEmpresa(Empresa empresa) {
		if (empresa.getDocumentoEmpresa() != null) {
			if (ValidationUtils.isEmpty(empresa.getDocumentoEmpresa())) {
				throw new IllegalArgumentException("DOCUMENTO NÃO PODE SER VAZIO.");
			}
			if (!DocumentValidator.isValidaCpfCnpj(empresa.getDocumentoEmpresa())) {
				throw new IllegalArgumentException("DOCUMENTO INVÁLIDO OU INCOMPLETO");
			}
		}

		if (empresa.getRazaoSocialEmpresa() != null) {
			if (ValidationUtils.isEmpty(empresa.getRazaoSocialEmpresa())) {
				throw new IllegalArgumentException("RAZÃO SOCIAL É OBRIGATÓRIA.");
			}
		}
		if (empresa.getCapitalEmpresa() != null) {
			if (empresa.getCapitalEmpresa().compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("CAPITAL SOCIAL DEVE SER MAIOR QUE ZERO.");
			}
		}

		if (empresa.getFundacaoEmpresa() != null) {
			String fundacaoStr = empresa.getFundacaoEmpresa().format(DATE_FORMATTER);
			if (!FormatValidator.isValidFoundationDate(fundacaoStr)) {
				throw new IllegalArgumentException("DATA DE FUNDAÇÃO INVÁLIDA OU FUTURA.");
			}
		}

		if (!ValidationUtils.isEmpty(empresa.getInscEst())
				&& !DocumentValidator.isValidInscricaoEstadual(empresa.getInscEst())) {
			throw new IllegalArgumentException("INSCRIÇÃO ESTADUAL INVÁLIDA.");
		}

		if (!ValidationUtils.isEmpty(empresa.getInscMun())
				&& !DocumentValidator.isValidInscricaoMunicipal(empresa.getInscMun())) {
			throw new IllegalArgumentException("INSCRIÇÃO MUNICIPAL INVÁLIDA.");
		}
	}

	public void validarRepresentante(Representante r) {
		if (r == null)
			return;

		if (r.getNomeRepresentante() != null && ValidationUtils.isEmpty(r.getNomeRepresentante())) {
			throw new IllegalArgumentException("NOME DO REPRESENTANTE É OBRIGATÓRIO.");
		}

		if (r.getCpfRepresentante() != null) {
			if (!DocumentValidator.isValidaCPF(r.getCpfRepresentante())) {
				throw new IllegalArgumentException("CPF INVÁLIDO.");
			}
		}

		if (!ValidationUtils.isEmpty(r.getTelefoneRepresentante())) {
			if (!FormatValidator.isValidPhoneNumberBR(r.getTelefoneRepresentante())) {
				throw new IllegalArgumentException("TELEFONE INVÁLIDO.");
			}
		}

	}

	public void validarRepresentante(List<Representante> representantes) {
		if (representantes == null || representantes.isEmpty())
			return;
		for (Representante r : representantes) {
			if (ValidationUtils.isEmpty(r.getNomeRepresentante())) {
				throw new IllegalArgumentException("NOME OBRIGATÓRIO.");
			}

			if (ValidationUtils.isEmpty(r.getCpfRepresentante())) {
				throw new IllegalArgumentException("CPF OBRIGATÓRIO.");
			}

			validarRepresentante(r);
		}
	}

	public void validarEmpresaComplementar(Empresa empresa) {
		if (empresa == null)
			return;

		if (empresa.getCnaeEmpresa() != null) {
			if (ValidationUtils.isEmpty(empresa.getCnaeEmpresa())
					|| "SELECIONE UMA OPÇÂO".equals(empresa.getCnaeEmpresa())) {
				throw new IllegalArgumentException("O CAMPO CNAE É OBRIGATÓRIO PRA FORNECEDORA.");
			}
		}

		if (empresa.getRegimeTribEmpresa() != null) {
			if (ValidationUtils.isEmpty(empresa.getRegimeTribEmpresa())
					|| "SELECIONE UMA OPÇÃO".equals(empresa.getRegimeTribEmpresa())) {
				throw new IllegalArgumentException("O CAMPO REGIME TRIBUTÁRIO É OBRIGATÓRIO PRA FORNECEDORA.");
			}

			if (empresa.getCrtEmpresa() <= 0) {
				throw new IllegalArgumentException("O CÓDIGO DE REGIME TRIBUTÁRIO (CRT) NÃO FOI GERADO CORRETAMENTE.");
			}
		}

	}

	public void validarEndereco(Endereco endereco) {
		if (endereco.getCepEndereco() != null) {
			if (ValidationUtils.isEmpty(endereco.getCepEndereco()) || !endereco.getCepEndereco().matches("\\d{8}")) {
				throw new IllegalArgumentException("CEP INVÁLIDO. USE 8 DÍGITOS.");
			}
		}

		if (endereco.getLogradouroEndereco() != null && ValidationUtils.isEmpty(endereco.getLogradouroEndereco())) {
			throw new IllegalArgumentException("LOGRADOURO É OBRIGATÓRIO.");
		}
		if (endereco.getCidadeEndereco() != null && ValidationUtils.isEmpty(endereco.getCidadeEndereco())) {
			throw new IllegalArgumentException("CIDADE É OBRIGATÓRIA.");
		}
		if (endereco.getEstadoEndereco() != null && ValidationUtils.isEmpty(endereco.getEstadoEndereco())) {
			throw new IllegalArgumentException("ESTADO É OBRIGATÓRIO.");
		}
		if (endereco.getPaisEndereco() != null && ValidationUtils.isEmpty(endereco.getPaisEndereco())) {
			throw new IllegalArgumentException("PAÍS É OBRIGATÓRIO.");
		}
	}

	public void validarContatos(List<Contato> contatos) {
		if (contatos == null || contatos.isEmpty()) {
			throw new IllegalArgumentException("AO MENOS UM CONTATO É OBRIGATÓRIO.");
		}

		for (Contato c : contatos) {
			validarContatoIndividual(c);

			if (ValidationUtils.isEmpty(c.getValorContato())) {
				throw new IllegalArgumentException("VALOR DO TIPO " + c.getTipoContato() + " NÃO PODE SER VAZIO.");
			}

		}

	}

	public void validarContatoIndividual(Contato contato) {
		if (contato == null)
			return;

		if (contato.getValorContato() != null && ValidationUtils.isEmpty(contato.getValorContato())) {
			throw new IllegalArgumentException("O CAMPO NÃO PODE ESTAR VAZIO.");
		}

		if (contato.getValorContato() != null) {

			boolean valido = switch (contato.getTipoContato()) {
			case "FIXO", "CELULAR", "WHATSAPP" -> FormatValidator.isValidPhoneNumberBR(contato.getValorContato());
			case "E-MAIL" -> contato.getValorContato().matches("^[\\w._%+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
			case "SITE", "REDE SOCIAL" -> true;
			default -> true;
			};

			if (!valido) {
				throw new IllegalArgumentException(contato.getTipoContato() + "INVÁLIDO.");
			}
		}
	}

	public void validarBanco(Banco b) {
		if (b == null)
			return;

		if (b.getCodBanco() != 0) {
			if (b.getCodBanco() < 1 || b.getCodBanco() > 999) {
				throw new IllegalArgumentException("CÓDIGO DO BANCO DEVE ESTAR ENTRE 001 E 999.");
			}
		}
		if (b.getNomeBanco() != null && ValidationUtils.isEmpty(b.getNomeBanco())) {
			throw new IllegalArgumentException("NOME DO BANCO É OBRIGATÓRIO.");
		}
	}

	public void validarBanco(List<Banco> bancos) {
		if (bancos == null || bancos.isEmpty()) {
			throw new IllegalArgumentException("AO MENOS UM BANCO DEVE SER CADASTRADO.");
		}
		for (Banco b : bancos) {
			if (ValidationUtils.isEmpty(b.getNomeBanco())) {
				throw new IllegalArgumentException("NOME DO BANCO NA LISTA NÃO PODE ESTAR VAZIO.");
			}

			validarBanco(b);
		}
	}

	public void validarComplementarIndividual(Complementar comp) {
		if (comp == null)
			return;

		if (comp.getRamoAtividadeComplementar() != null
				&& ValidationUtils.isEmpty(comp.getRamoAtividadeComplementar())) {
			throw new IllegalArgumentException("RAMO DE ATIVIDADE É OBRIGATÓRIO.");
		}
	}

	public void validarDocumentos(List<Documento> doc) {
		if (doc == null || doc.isEmpty()) {
			return;
		}
		for (Documento d : doc) {
			if (ValidationUtils.isEmpty(d.getTipoDocumento())) {
				throw new IllegalArgumentException("TIPO DE DOCUMENTO INVÁLIDO NA LISTA.");
			}
			if (ValidationUtils.isEmpty(d.getArquivoDocumento())) {
				throw new IllegalArgumentException("CAMINHO DO ARQUIVO INVÁLIDO.");
			}
		}
	}

	private void salvarFornecedora(Connection conn, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementares,
			List<Documento> documentos, boolean isFornecedora) throws Exception {

		int id = empresa.getIdEmpresa();
		
		ContatoDao contDao = new ContatoDao(conn);
		contDao.deleteByEmpresa(id);
		if (contatos != null) {
			for (Contato c : contatos) {
				c.setEmpresa(empresa);
				contDao.save(c);
			}
		}

		if (isFornecedora) {
			
			RepresentanteDao repDao = new RepresentanteDao(conn);
			repDao.deleteByEmpresa(id);
			if (representantes != null) {
				for (Representante r : representantes) {
					r.setEmpresa(empresa);
					repDao.save(r);
				}
			}
			BancoDao banDao = new BancoDao(conn);
			banDao.deleteByEmpresa(id);
			if (bancos != null) {
				for (Banco b : bancos) {
					b.setEmpresa(empresa);
					banDao.save(b);
				}
			}
			if (complementares != null) {
				complementares.setEmpresa(empresa);
				ComplementarDao compDao = new ComplementarDao(conn);
				if (complementares.getIdComplementar()>0) {
					compDao.update(complementares);
				} else {
					compDao.save(complementares);
				}
			}

			DocumentoDao docDao = new DocumentoDao(conn);
			docDao.deleteByEmpresa(id);
			if (documentos != null) {
				for (Documento d : documentos) {
					d.setEmpresa(empresa);
					docDao.save(d);
				}
			}

		}
	}

}
