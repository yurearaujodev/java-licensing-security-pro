package com.br.yat.gerenciador.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.service.DatabaseSetupService;
import com.br.yat.gerenciador.validation.DatabaseValidationUtils;

public class ConfiguracaoBancoController {

	private static final Logger logger = LoggerFactory.getLogger(ConfiguracaoBancoController.class);

	public String salvarConfiguracao(String url, String user, char[] password) {
		try {
			DatabaseValidationUtils.validarUrl(url);
			DatabaseValidationUtils.validarUsuario(user);
			DatabaseValidationUtils.validarSenha(password);

			DatabaseSetupService.saveDatabaseConfigConfiguration(url, user, password);

			return "CONFIGURAÇÃO SALVA COM SUCESSO.";
		} catch (ValidationException e) {
			return e.getMessage();
		} catch (Exception e) {
			logger.error("ERRO INESPERADO: "+ e.getMessage());
			return "Erro inesperado.Consulte o log do sistema.";
		}
	}

}
