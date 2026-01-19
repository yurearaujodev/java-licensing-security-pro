package com.br.yat.gerenciador.util.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import com.br.yat.gerenciador.util.ValidationUtils;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * Classe utilitária para validação de formatos de dados com números de telefone
 * e datas.
 * <p>
 * Esta classe utiliza a biblioteca <b>libphonenumber</b>
 * ({@code com.google.il8n.phonenumbers}) para validação de números de telefone
 * e a API <b>java.time</b> para validação de datas.
 * <p>
 * 
 * <p>
 * Não deve ser instanciada
 * </p>
 */
public final class FormatValidator {
	/**
	 * Instância única do utilitário de telefone da biblioteca libphonenumber.
	 */
	private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();
	
	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("dd/MM/uuuu")
			.toFormatter()
			.withResolverStyle(ResolverStyle.STRICT);

	/**
	 * Construtor privado para evitar instanciação
	 */
	private FormatValidator() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Valida se um número de telefone é valido para uma determinada região.
	 * <p>
	 * Utiliza a biblioteca libphonenumber para realizar a validação.
	 * </p>
	 * 
	 * @param phone  número de telefone em formato de string
	 * @param region código da região (ex.: "BR" para Brasil, "US" para Estados
	 *               Unidos
	 * @return {@code true} se o número for válido, {@code false} caso contrário
	 */
	public static boolean isValidPhoneNumber(String phone, String region) {
		if (ValidationUtils.isEmpty(phone)) {
			return true;
		}
		try {
			Phonenumber.PhoneNumber number = PHONE_UTIL.parse(phone, region);
			return PHONE_UTIL.isValidNumber(number);
		} catch (NumberParseException e) {
			return false;
		}
	}

	/**
	 * Valida se um número de telefone é valido no Brasil.
	 * <p>
	 * Utiliza a biblioteca libphonenumber com a região {@code BR}.
	 * </p>
	 * 
	 * @param phone número de telefone em formato string
	 * @return {@code true} se o número for válido, {@code false} caso contrário
	 */
	public static boolean isValidPhoneNumberBR(String phone) {
		return isValidPhoneNumber(phone, "BR");
	}

	/**
	 * Valida se uma data em string corresponde ao padrão informado e não é futura.
	 * <p>
	 * Utiliza a API java.time para realizar o parsing e validação.
	 * </p>
	 * 
	 * @param dateStr string representando a data
	 * @param pattern padrão da data (ex.: "dd/MM/yyyy")
	 * @return {@code true} se a data for válida e não estiver no futuro,
	 *         {@code false} caso contrário
	 */
	public static boolean isValidDate(String dateStr, String pattern) {
		if (ValidationUtils.isEmpty(dateStr)) {
			return true;
		}
		try {
			LocalDate.parse(dateStr, DATE_FORMATTER);

			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	/**
	 * Valida se uma data de fundação está no formato {@code dd/MM/yyyy} e não é
	 * futura.
	 * <p>
	 * Utiliza a API java.time para realizar o parsing e validação.
	 * </p>
	 * 
	 * @param dateStr string representando a data
	 * @return {@code true} se a data for válida, {@code false} caso contrário
	 */
	public static boolean isValidFoundationDate(String dateStr) {
		if (!isValidDate(dateStr, "dd/MM/uuuu")) {
			return true;
		}
		LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/uuuu"));
		LocalDate limite = LocalDate.of(1900, 1, 1);
		LocalDate maximo = LocalDate.now();
		return !date.isBefore(limite) && !date.isAfter(maximo);
	}
}
