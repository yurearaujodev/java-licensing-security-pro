package com.br.yat.gerenciador.teste;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;

import com.br.yat.gerenciador.security.RSAUtils;
import com.br.yat.gerenciador.security.SecureKeyManager;


public class SecureKeyManagerTest {

	private static final String DOCUMENTO = "12345678900";

	public static void main(String[] args) {
		System.out.println("===Teste de SecureKeyManager===");

		try {

			KeyPair keyPair = SecureKeyManager.generateAndSaveKeys(DOCUMENTO);

			Path dir = Paths.get("config/licencas", DOCUMENTO);
			Path masterKeyFile = dir.resolve("master.key");
			Path pubKeyFile = dir.resolve("rsa_public.pem");
			Path privKeyFile = dir.resolve("rsa_private.pem");

			System.out.println("Diretorio das chaves: " + dir.toAbsolutePath());
			System.out.println("master.key existe? " + Files.exists(masterKeyFile));
			System.out.println("rsa_public.pem existe? " + Files.exists(pubKeyFile));
			System.out.println("rsa_private.pem existe? " + Files.exists(privKeyFile));

			String licenseData = "cliente=" + DOCUMENTO + ";validade=2026-12-31;modulos=caixa,relatorios";
			byte[] signature = RSAUtils.sign(licenseData, keyPair.getPrivate());
			System.out.println("licença assinada com sucesso!");

			boolean valid = RSAUtils.verify(licenseData, signature, keyPair.getPublic());
			System.out.println("Assinatura válida: " + valid);

			if (valid) {
				System.out.println("SecureKeyManager funcionando corretamente!");
			} else {
				System.out.println("Falha na assinatura/verificação RSA!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
