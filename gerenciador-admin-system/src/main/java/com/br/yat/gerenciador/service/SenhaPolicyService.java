package com.br.yat.gerenciador.service;

import java.util.Arrays;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;

/**
 * Políticas de senha: complexidade, confirmação e geração de hash.
 */
public final class SenhaPolicyService {

	private static final String CARACTERES_ESPECIAIS = "!@#$%^&*(),.?\":{}|<>";

	private final ParametroSistemaService parametroService;

	public SenhaPolicyService(ParametroSistemaService parametroService) {
		this.parametroService = parametroService;
	}

	public String gerarHashSeguro(char[] senha) {
		validarComplexidade(senha);
		return PasswordUtils.hashPassword(copiarSenha(senha));
	}

	public void validarComplexidade(char[] senha) {
		int min = parametroService.getInt(ParametroChave.SENHA_MIN_TAMANHO, 6);

		if (senha == null || senha.length < min) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"A SENHA DEVE TER NO MÍNIMO " + min + " CARACTERES.");
		}

		boolean maiuscula = false;
		boolean numero = false;
		boolean especial = false;

		for (char c : senha) {
			if (Character.isUpperCase(c)) {
				maiuscula = true;
			} else if (Character.isDigit(c)) {
				numero = true;
			} else if (CARACTERES_ESPECIAIS.indexOf(c) >= 0) {
				especial = true;
			}
		}

		if (!(maiuscula && numero && especial)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"SENHA FRACA: REQUER LETRA MAIÚSCULA, NÚMERO E CARACTERE ESPECIAL.");
		}
	}

	public void validarConfirmacaoSenha(char[] senha, char[] confirmacao) {
		if (confirmacao == null || senha == null || senha.length != confirmacao.length) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
		}

		if (!charArraysIguaisEmTempoConstante(senha, confirmacao)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
		}
	}

	public void validarIgualdadeSenhas(char[] senha, char[] confirmacao) {
		if (senha == null || confirmacao == null || senha.length != confirmacao.length) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "AS SENHAS DIGITADAS NÃO CONFEREM.");
		}

		if (!charArraysIguaisEmTempoConstante(senha, confirmacao)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "AS SENHAS DIGITADAS NÃO CONFEREM.");
		}
	}

	public boolean charArraysIguaisEmTempoConstante(char[] esquerda, char[] direita) {
		int diff = esquerda.length ^ direita.length;

		for (int i = 0; i < Math.min(esquerda.length, direita.length); i++) {
			diff |= esquerda[i] ^ direita[i];
		}

		return diff == 0;
	}

	public char[] copiarSenha(char[] senha) {
		return senha == null ? null : Arrays.copyOf(senha, senha.length);
	}

	public void limparSenhas(char[]... senhas) {
		for (char[] senha : senhas) {
			SensitiveData.safeClear(senha);
		}
	}
}
