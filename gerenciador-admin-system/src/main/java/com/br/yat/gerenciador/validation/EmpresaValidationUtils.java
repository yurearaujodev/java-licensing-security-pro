package com.br.yat.gerenciador.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.RegimeTributario;
import com.br.yat.gerenciador.model.enums.TipoContato;
import com.br.yat.gerenciador.model.enums.TipoDocumento;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.ValidationUtils;

public final class EmpresaValidationUtils {

	private EmpresaValidationUtils() {
		throw new AssertionError("");
	}

	public static void validarCep(String valor) {
		if (ValidationUtils.isEmpty(valor) || !valor.matches("\\d{8}")) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "CEP INVÁLIDO. USE 8 DÍGITOS.");
		}
	}

	public static void validarLogradouro(String valor) {
		if (ValidationUtils.isEmpty(valor)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "LOGRADOURO É OBRIGATÓRIO.");
		}
	}

	public static void validarBairro(String valor) {
		if (ValidationUtils.isEmpty(valor)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "BAIRRO É OBRIGATÓRIO.");
		}
	}

	public static void validarCidade(String valor) {
		if (ValidationUtils.isEmpty(valor)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "CIDADE É OBRIGATÓRIO.");
		}
	}

	public static void validarEstado(String valor) {
		if (ValidationUtils.isEmpty(valor)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "ESTADO É OBRIGATÓRIO.");
		}
	}

	public static void validarPais(String valor) {
		if (ValidationUtils.isEmpty(valor)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "PAIS É OBRIGATÓRIO.");
		}
	}

	public static void validarEndereco(Endereco e) {
		validarCep(e.getCepEndereco());
		validarLogradouro(e.getLogradouroEndereco());
		validarBairro(e.getBairroEndereco());
		validarCidade(e.getCidadeEndereco());
		validarEstado(e.getEstadoEndereco());
		validarPais(e.getPaisEndereco());
	}

	public static void validarDocumento(TipoDocumento tipo, String documento) {
		if (tipo == TipoDocumento.SELECIONE) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "SELECIONE UM TIPO DOCUMENTO.");
		}
		if (ValidationUtils.isEmpty(documento)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "DOCUMENTO NÃO PODE SER VAZIO.");
		}
		if (!DocumentValidator.isValidaCpfCnpj(documento)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "DOCUMENTO INVÁLIDO.");
		}
	}

	public static void validarRazaoSocial(String texto) {
		if (ValidationUtils.isEmpty(texto)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "RAZÃO SOCIAL É OBRIGATÓRIA.");
		}
	}

	public static void validarCapital(BigDecimal valor) {
		if (valor != null && valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "CAPITAL SOCIAL DEVE SER MAIOR QUE ZERO.");
		}

	}

	public static void validarFundacao(LocalDate data) {
		if (data != null) {
			String fundacaoStr = ValidationUtils.formatDate(data);
			if (!FormatValidator.isValidFoundationDate(fundacaoStr)) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"DATA DE FUNDAÇÃO INVÁLIDA OU FUTURA.");
			}
		}
	}

	public static void validarInscricaoEstadual(String valor) {
		if (ValidationUtils.isEmpty(valor) || !DocumentValidator.isValidInscricaoEstadual(valor)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"INSCRIÇÃO ESTADUAL INVÁLIDA. DIGITE O NÚMERO OU 'ISENTO'");
		}
	}

	public static void validarInscricaoMunicipal(String valor) {
		if (ValidationUtils.isEmpty(valor) || !DocumentValidator.isValidInscricaoMunicipal(valor)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"INSCRIÇÃO MUNICIPAL INVÁLIDA.. DIGITE O NÚMERO OU 'ISENTO'");
		}
	}

	public static void validarEmpresa(Empresa e) {
		validarDocumento(e.getTipoDocEmpresa(), e.getDocumentoEmpresa());
		validarRazaoSocial(e.getRazaoSocialEmpresa());
		validarCapital(e.getCapitalEmpresa());
		validarFundacao(e.getFundacaoEmpresa());
		validarInscricaoEstadual(e.getInscEst());
		validarInscricaoMunicipal(e.getInscMun());
	}

	public static void validarContato(TipoContato tipo, String valor) {
		if (tipo == null || tipo == TipoContato.SELECIONE) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "SELECIONE UMA OPÇÃO.");
		}
		if (ValidationUtils.isEmpty(valor)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "O CAMPO NÃO PODE ESTAR VAZIO.");
		}

		boolean valido = switch (tipo) {
		case FIXO, CELULAR, WHATSAPP -> FormatValidator.isValidPhoneNumberBR(valor);
		case EMAIL -> valor.matches("^[\\w._%+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
		case SITE, REDESOCIAL -> true;
		default -> true;
		};

		if (!valido) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, tipo + " INVÁLIDO.");
		}
	}

	public static void validarContatos(List<Contato> contatos) {
		if (contatos == null || contatos.isEmpty()) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"AO MENOS UM CONTATO É OBRIGATÓRIO.");
		}

		for (Contato c : contatos) {
			validarContato(c.getTipoContato(), c.getValorContato());
		}
	}

	public static void validarCnae(Cnae cnae) {
		if (cnae == null || cnae == Cnae.SELECIONE) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O CAMPO CNAE É OBRIGATÓRIO PARA FORNECEDORA.");
		}
	}

	public static void validarNaturezaJuridica(NaturezaJuridica natureza) {
		if (natureza == null || natureza == NaturezaJuridica.SELECIONE) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O CAMPO NATUREZA JURÍDICA É OBRIGATÓRIO PARA FORNECEDORA.");
		}
	}

	public static void validarRegimeTributario(RegimeTributario regime, int crt) {
		if (regime == null || regime == RegimeTributario.SELECIONE) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O CAMPO REGIME TRIBUTÁRIO É OBRIGATÓRIO PARA FORNECEDORA.");
		}

		if (crt <= 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"O CÓDIGO DE REGIME TRIBUTÁRIO (CRT) NÃO FOI GERADO CORRETAMENTE.");
		}
	}

	public static void validarEmpresaFiscal(Empresa e) {
		if (e == null) {
//			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
//					"OS DADOS FISCAIS DA EMPRESA NÃO FORAM INFORMADOS.");
			return;
		}
		validarCnae(e.getCnaeEmpresa());
		validarNaturezaJuridica(e.getNaturezaJuriEmpresa());
		validarRegimeTributario(e.getRegimeTribEmpresa(), e.getCrtEmpresa());
	}

	public static void validarNomeRepresentante(String nome) {
		if (ValidationUtils.isEmpty(nome)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"NOME DO REPRESENTANTE É OBRIGATÓRIO.");
		}
	}

	public static void validarCpfRepresentante(String cpf) {
		if (ValidationUtils.isEmpty(cpf)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"CPF DO REPRESENTANTE É OBRIGATÓRIO.");
		}
		if (!DocumentValidator.isValidaCPF(cpf)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "CPF INVÁLIDO.");
		}

	}

	public static void validarTelefoneRepresentante(String telefone) {
		if (ValidationUtils.isEmpty(telefone)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"TELEFONE DO REPRESENTANTE É OBRIGATÓRIO.");
		}
		if (!FormatValidator.isValidPhoneNumberBR(telefone)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "TELEFONE INVÁLIDO.");

		}
	}

	public static void validarRepresentantes(List<Representante> representantes) {
		if (representantes == null || representantes.isEmpty()) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"AO MENOS UM REPRESENTANTE DEVE SER CADASTRADO.");
		}

		for (Representante r : representantes) {
			validarNomeRepresentante(r.getNomeRepresentante());
			validarCpfRepresentante(r.getCpfRepresentante());
			validarTelefoneRepresentante(r.getTelefoneRepresentante());
		}
	}

	public static void validarCodigoBanco(int codigo) {
		if (codigo != 0) {
			if (codigo < 1 || codigo > 999) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"CÓDIGO DO BANCO DEVE ESTAR ENTRE 001 E 999.");
			}
		}
	}

	public static void validarNomeBanco(String banco) {
		if (ValidationUtils.isEmpty(banco)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NOME DO BANCO É OBRIGATÓRIO.");
		}
	}

	public static void validarAgencia(String agencia) {
		if (ValidationUtils.isEmpty(agencia)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "AGÊNCIA É OBRIGATÓRIO.");
		}
	}

	public static void validarConta(String conta) {
		if (ValidationUtils.isEmpty(conta)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NÚMERO DA CONTA É OBRIGATÓRIO.");
		}
	}

	public static void validarBancos(List<Banco> bancos) {
		if (bancos == null || bancos.isEmpty()) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"AO MENOS UM BANCO DEVE SER CADASTRADO.");
		}

		for (Banco b : bancos) {
			validarCodigoBanco(b.getIdBanco());
			validarNomeBanco(b.getNomeBanco());
			validarAgencia(b.getAgenciaBanco());
			validarConta(b.getContaBanco());
		}
	}

	public static void validarRamoAtividade(String ramo) {
		if (ValidationUtils.isEmpty(ramo)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"RAMO DE ATIVIDADE É OBRIGATÓRIO.");
		}
	}

	public static void validarNumeroFuncionarios(int numero) {
		if (numero < 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "FUNCIONÁRIOS É OBRIGATÓRIO.");
		}
	}

	public static void validarComplementar(Complementar c) {
		if (c == null) {
//			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
//			"OS DADOS FISCAIS DA EMPRESA NÃO FORAM INFORMADOS.");
			return;
		}
		validarRamoAtividade(c.getRamoAtividadeComplementar());
		validarNumeroFuncionarios(c.getNumFuncionariosComplementar());
	}
	
	public static void validarDocumentos(List<Documento>doc) {
		if (doc == null || doc.isEmpty()) {
			return;
		}
		for (Documento d : doc) {
			if (ValidationUtils.isEmpty(d.getTipoDocumento())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
						"TIPO DE DOCUMENTO INVÁLIDO NA LISTA.");
			}
			if (ValidationUtils.isEmpty(d.getArquivoDocumento())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
						"CAMINHO DO ARQUIVO INVÁLIDO.");
			}
		}
	}
}
