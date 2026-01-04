package com.br.yat.gerenciador.util.validation;

import java.util.regex.Pattern;

import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
/**
 * Classe utilitária para validação de documentos brasileiros.
 * <p>
 * Fornece métodos para validar CPF, CNPJ, inscrição estadual e inscrição municipal.
 * </p>
 * 
 * <p>
 * Esta classe utiliza a biblioteca <b>Stella</b> ({@code br.com.caelum.stella.validation})
 * para validação de CPF e CNPJ,e expressões regulares para inscrição estadual e municipal.
 * </p>
 * 
 * <p>Não deve ser instaciada.</p>
 */
public final class DocumentValidator {
	/**
	 * Validador de CPF da biblioteca Stella.
	 */
	private static final CPFValidator CPF_VALIDATOR = new CPFValidator();
	/**
	 * Validador de CNPJ da bibioteca Stella.
	 */
	private static final CNPJValidator CNPJ_VALIDATOR = new CNPJValidator();
	/**
	 * Padrão para inscrição estadual (ISENTO ou 9 a 14 dígitos).
	 */
	private static final Pattern INSCRICAO_ESTADUAL_PATTERN = Pattern.compile("(?i)^(ISENTO|\\d{9,14})$");
	/**
	 * Padrão para inscrição municipal (ISENTO ou 7 a 15 dígitos).
	 */
	private static final Pattern INSCRICAO_MUNICIPAL_PATTERN = Pattern.compile("(?i)^(ISENTO|\\d{7,15})$");

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private DocumentValidator() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Valida um CPF utilizando a biblioteca Stella.
	 * 
	 * @param cpf número de CPF em formato de string
	 * @return {@code true} se o CPF for válido, {@code false} caso contrário
	 */
	public static boolean isValidaCPF(String cpf) {
		if (cpf == null || cpf.isBlank()) {
			return false;
		}
		try {
			CPF_VALIDATOR.assertValid(cpf);
			return true;
		} catch (InvalidStateException | IllegalArgumentException e) {
			return false;
		}
	}
	
	/**
	 * Valida um CNPJ utilizando a biblioteca Stella.
	 * 
	 * @param cnpj número de CNPJ em formato de string
	 * @return {@code true} se o CNPJ for válido, {@code false} caso contrário
	 */
	public static boolean isValidaCNPJ(String cnpj) {
		if (cnpj == null || cnpj.isBlank()) {
			return false;
		}
		try {
			CNPJ_VALIDATOR.assertValid(cnpj);
			return true;
		} catch (InvalidStateException | IllegalArgumentException e) {
			return false;
		}
	}
	
	/**
	 * Valida se um documento é CPF ou CNPJ.
	 * <p>
	 * Usa a biblioteca Stella para validação após remover caracteres não numéricos.
	 * </>
	 * 
	 * @param document número de documento em formato de string
	 * @return {@code true} se for um CPF ou CNPJ válido, {@code false} caso contrário.
	 */
	public static boolean isValidaCpfCnpj(String document) {
		if (document == null) {
			return false;
		}

		String clean = document.replaceAll("\\D", "");
		return clean.length() == 11 && isValidaCPF(clean) || clean.length() == 14 && isValidaCNPJ(clean);
	}

	/**
	 * Valida incrição estadual.
	 * <p>
	 * Usa expressão regular para aceitar valores "ISENTO" ou números entre 9 e 14 dígitos.
	 * </p>
	 * 
	 * @param insc inscrição estadual em formato string 
	 * @return {@code true} se for válida ou estiver vazia, {@code false} caso contrário
	 */
	public static boolean isValidInscricaoEstadual(String insc) {
		return insc == null || insc.isBlank() || INSCRICAO_ESTADUAL_PATTERN.matcher(insc.trim()).matches();
	}

	/**
	 * Valida incrição municipal.
	 * <p>
	 * Usa expressão regular para aceitar valores "ISENTO" ou números entre 7 e 15 dígitos.
	 * </p>
	 * 
	 * @param insc inscrição municipal em formato string 
	 * @return {@code true} se for válida ou estiver vazia, {@code false} caso contrário
	 */
	public static boolean isValidInscricaoMunicipal(String insc) {
		return insc == null || insc.isBlank() || INSCRICAO_MUNICIPAL_PATTERN.matcher(insc.trim()).matches();
	}
}
