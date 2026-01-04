package com.br.yat.gerenciador.util.security;

import java.util.Objects;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.util.SensitiveData;
/**
 * Classe utilitária para manipulação segura de senhas.
 * <p>
 * Utiliza a biblioteca <b>jBCrypt</b> ({@code org.mindrot.jbcrypt.BCrypt}) para
 * realizar o hash e a verificação de senhas.
 * Também integra com {@link SensitiveData} para limpar dados sensíveis da memória
 * após o uso, e utiliza <b>SLF4J,</b> para logging.
 * </p>
 * 
 * <p>
 * As senhas são criptografadas com salt e um número configurável de rounds (padrão : 12)
 * </p>
 * 
 * <p>
 * Não deve ser instanciada
 * </p>
 */
public final class PasswordUtils {

	private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);
	private static final int LOG_ROUNDS = 12;

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private PasswordUtils() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Gera o hash de uma senha utilizando BCrypt
	 * <p>
	 * O array de caracteres da senha é limpo da memória após o processamento.
	 * </p>
	 * 
	 * @param plainPassword senha em texto puro como array de caracteres (não pode ser nula ou vazia)
	 * @return string contendo o hash da senha
	 * @throws NullPointerException se {@code plainPassword} for nula
	 * @throws IllegalArgumentException se {@code plainPassword} for vazia
	 */
	public static String hashPassword(char[] plainPassword) {
		Objects.requireNonNull(plainPassword);

		if (plainPassword.length == 0) {
			throw new IllegalArgumentException("SENHA NÃO PODE SER VAZIA.");
		}
		try {
			return BCrypt.hashpw(new String(plainPassword), BCrypt.gensalt(LOG_ROUNDS));
		} finally {
			SensitiveData.safeClear(plainPassword);
		}
	}

	/**
	 * Verifica se uma senha em texto puro corresponde ao hash armazenado.
	 * <p>
	 * O array de caracteres da senha é limpo da memória após o processamento.
	 * </p>
	 * 
	 * @param plainPassword senha em texto puro como array de caracteres
	 * @param hashedPassword hash da senha armazenada
	 * @return {@code true} se a senha corresponder, {@code false} caso contrário
	 */
	public static boolean verifyPassword(char[] plainPassword, String hashedPassword) {
		if (plainPassword == null || plainPassword.length == 0 || hashedPassword == null || hashedPassword.isBlank()) {
			return false;
		}
		try {
			return BCrypt.checkpw(new String(plainPassword), hashedPassword);
		} catch (IllegalArgumentException e) {
			logger.warn("Hash Bcrypt INVÁLIDO OU CORROMPIDO.", e);
			return false;
		} finally {
			SensitiveData.safeClear(plainPassword);
		}
	}

}
