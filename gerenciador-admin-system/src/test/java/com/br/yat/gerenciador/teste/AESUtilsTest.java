package com.br.yat.gerenciador.teste;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.br.yat.gerenciador.exception.CryptoException;
import com.br.yat.gerenciador.security.AESUtils;

public class AESUtilsTest {

	public static void main(String[] args) {
		System.out.println("=== TESTE DE AESUTILS===\n");

		try {

			System.out.println("1. Testadando generateKeys()...");
			int[] validSizes = { 128, 192, 256 };
			for (int size : validSizes) {
				SecretKey key = AESUtils.generateKey(256);
				System.out.println("Tamanho " + size + " bits: " + key.getAlgorithm());
				assert "AES".equals(key.getAlgorithm()) : "Algoritmo inválido";
			}
			System.out.println("generateKey() funcionando!\n");

			System.out.println("2. Testando chave inválida...");
			try {
				AESUtils.generateKey(123);
				assert false : "Deveria ter falhado";
			} catch (Exception e) {
				System.out.println("Excecao esperada: " + e.getMessage());
			}
			System.out.println();

			System.out.println("3. Testando encrypt/decrypt...");
			SecretKey key = AESUtils.generateKey(256);
			String original = "Dados secretos da licença!";
			System.out.println("Dados original: " + original);

			byte[] encrypted = AESUtils.encrypt(original, key);
			String encryptedB64 = Base64.getEncoder().encodeToString(encrypted);
			System.out.println("Dado criptografado (Base64): " + encryptedB64);

			String decrypted = AESUtils.decryptToString(encrypted, key);
			System.out.println("Dado descriptografado: " + decrypted);

			assert original.equals(decrypted) : "Falha na criptografia/descriptografia";
			System.out.println("encrypt/decrypt funcionando!\n");

			System.out.println("4. Testando encypt/decrypt com byte[]...");
			byte[] rawData = "Dados binários".getBytes();
			byte[] encryptedRaw = AESUtils.encrypt(rawData, key);
			byte[] decryptedRaw = AESUtils.decrypt(encryptedRaw, key);
			String result = new String(decryptedRaw);
			System.out.println("Dado binário original: " + new String(rawData));
			System.out.println("Dado binário recuperdado: " + result);
			assert Arrays.equals(rawData, decryptedRaw) : "Falha em byte[]";
			System.out.println(" encrypt/decrypt com byte[] funcionando");

			System.out.println("5. Testando dados corrompidos...");
			encrypted[20] ^= 1;
			try {
				AESUtils.decrypt(encrypted, key);
				assert false : "Deveria ter falhado";
			} catch (CryptoException e) {
				System.out.println("Falha correta ao descriptografar dados corrompidos!");
			}

			System.out.println("\n TODOS OS TESTES DO AESUTILS PASSARAM!");
		} catch (Exception e) {
			System.err.println("Falha no teste: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
