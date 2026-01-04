package com.br.yat.gerenciador.teste;

import java.security.KeyPair;
import java.util.Base64;

import com.br.yat.gerenciador.util.crypto.RSAUtils;

public class RSAUtilsTest {

	public static void main(String[] args) {
		System.out.println("=== TESTE COMPLETO DE RSAUtils ===\n");

		try {

			System.out.println("1. Testando generateKeyPair()...");
			KeyPair kp = RSAUtils.generateKeyPair(2048);
			System.out.println("Chave pública: " + kp.getPublic().getAlgorithm() + " ("
					+ kp.getPublic().getEncoded().length + " bytes)");
			System.out.println("Chave privada: " + kp.getPrivate().getAlgorithm() + " ("
					+ kp.getPrivate().getEncoded().length + " bytes)");
			assert "RSA".equals(kp.getPublic().getAlgorithm()) : "Algoritmo inválido";
			System.out.println(" generateKeyPair() funcionando!\n");

			System.out.println("2. Testando chave pequena...");
			try {
				RSAUtils.generateKeyPair(1024);
				assert false : "Deveria ter falhado";
			} catch (Exception e) {
				System.out.println("Exceção esperada: " + e.getMessage());
			}
			System.out.println();

			System.out.println("3. Testando sign/verify...");
			String data = "Licença válida para Empresa XYZ até 2025-12-31";
			System.out.println("Dado original: " + data);

			byte[] signature = RSAUtils.sign(data, kp.getPrivate());
			String signatureB64 = Base64.getEncoder().encodeToString(signature);
			System.out.println("Assinatura (Base64): " + signatureB64);

			boolean valid = RSAUtils.verify(data, signature, kp.getPublic());
			System.out.println("Verificação: " + (valid ? "VÁLIDA" : "INVÁLIDA"));
			assert valid : "Falha na Verifição";
			System.out.println("sign/verify funcionando!\n");

			System.out.println("4. Testando sign/verify com byte[]...");
			byte[] rawData = "Dados binários".getBytes();
			byte[] rawSignature = RSAUtils.sign(rawData, kp.getPrivate());
			boolean rawValid = RSAUtils.verify(rawData, rawSignature, kp.getPublic());
			System.out.println("Verificação de byte[]: " + (rawValid ? "VÁLIDA" : "INVÁLIDA"));
			assert rawValid : "Falha na verificação de byte[]";
			System.out.println("sign/verify com byte[] funcionando!\n");

			System.out.println("5. Testando dados adulterados...");
			String tampered = "Licença MODIFICADA";
			boolean tamperedValid = RSAUtils.verify(tampered, signature, kp.getPublic());
			System.out.println("Verificação de dados adulterados: " + (tamperedValid ? "VÁLIDA" : "INVÁLIDA"));
			assert !tamperedValid : "Verificação deveria falhar";
			System.out.println("Detecção de adulteração funcionando!\n");

			System.out.println("6. Testando validação de nulos...");
			try {
				RSAUtils.sign("teste", null);
				assert false : "Deveria ter falhado";
			} catch (NullPointerException e) {
				System.out.println("Exceção esperada para chave privada nula");
			}
			try {
				RSAUtils.verify("teste", new byte[0], null);
				assert false : "Deveria ter falhado";

			} catch (NullPointerException e) {
				System.out.println("Exceção esperada para chave pública nula");
			}

			System.out.println("TODOS OS TESTES DO RSAUtils FORAM EXECUTADOS!");
		} catch (Exception e) {
			System.err.println("Falha no teste: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
