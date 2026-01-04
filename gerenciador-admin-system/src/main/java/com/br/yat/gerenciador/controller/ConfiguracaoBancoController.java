package com.br.yat.gerenciador.controller;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.service.DatabaseSetupService;
import com.br.yat.gerenciador.util.CryptoException;

public class ConfiguracaoBancoController {

	private static final Logger logger = LoggerFactory.getLogger(ConfiguracaoBancoController.class);

	public String salvarConfiguracao(String url, String user, char[] password) {
		try {
			validarEntrada(url, user, password);

			DatabaseSetupService.saveDatabaseConfigConfiguration(url, user, password);

			return "Configuração do banco salva com sucesso.";
		} catch (IllegalArgumentException e) {
			return e.getMessage();

		} catch (CryptoException e) {
			logger.error("Erro ao salvar configuração do banco", e);
			return "Erro ao salvar configuração do banco. Verifique os dados e tente novamente.";

		} catch (Exception e) {
			logger.error("Erro inesperado ao salvar configuração do banco", e);
			return "Erro inesperado.Consulte o log do sistema.";
		}
	}

	private void validarEntrada(String url, String user, char[] password) {
		Objects.requireNonNull(url, "URL do banco é obrigatória.");
		Objects.requireNonNull(user, "Usuário do banco é obrigatório.");
		Objects.requireNonNull(password, "Senha do banco é obrigatória.");

		if (url.isBlank()) {
			throw new IllegalArgumentException("URL do banco não pode ser vazia.");
		}
		if (!url.startsWith("jdbc:")) {
			throw new IllegalArgumentException("URL do banco inválida. Deve iniciar com 'jdbc:'.");
		}

		if (user.isBlank()) {
			throw new IllegalArgumentException("Usuário do banco não pode ser vazia.");
		}
		if (password.length == 0) {
			throw new IllegalArgumentException("Senha do banco não pode ser vazia.");
		}

	}

}
