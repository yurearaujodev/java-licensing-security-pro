package com.br.yat.gerenciador.teste;

import java.security.KeyPair;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.crypto.SecretKey;

import com.br.yat.gerenciador.config.ConnectionFactory;
import com.br.yat.gerenciador.security.AESUtils;
import com.br.yat.gerenciador.security.RSAUtils;
import com.br.yat.gerenciador.security.SecureKeyManager;



public class TesteCompleto {

	public static void main(String[] args) {
		System.out.println("Iniciando testes completos do sistema...\n");

		try {

			testAesCrypto();
			
			testRsaSignature();
			
			System.out.println("\n[TESTE}] Geração de chaves para cliente (abriar janela)...");
			KeyPair clienteKeys = SecureKeyManager.generateAndSaveKeys("12345678900");
			
			String licenseData = "cliente=12345678900;validade=2026-12-31;modulos=caixa,relatorios";
			
			byte[] signature = RSAUtils.sign(licenseData, clienteKeys.getPrivate());
			System.out.println("Licenca assinada com sucesso!");
			
			boolean valid = RSAUtils.verify(licenseData, signature, clienteKeys.getPublic());
			System.out.println("Assinatura válida: "+valid);
			if (!valid) {
				throw new RuntimeException("Falha na verificação da asssinatura!");
			}
			
			testDataBaseConnection();

			System.out.println("Todos os testes foram concluidos com sucesso!");
			System.out.println("Arquivos verificados na pasta: config/");

		} catch (Exception e) {
			System.err.println("Falha critica durante os testes:");
			e.printStackTrace();
			System.exit(1);
		}

	}

	private static void testAesCrypto() {
		System.out.println("\n [TESTE] AES-GCM:");
		SecretKey key = AESUtils.generateKey(256);
		String original = "Dados secretos do sistema!";
		byte[] encrypted = AESUtils.encrypt(original, key);
		String decrypted = AESUtils.decryptToString(encrypted, key);
		assert original.equals(decrypted) : "Falha na criptografia AES";
		System.out.println("Original: " + original);
		System.out.println("Decifrado: " + decrypted);
		System.out.println("AES-GCM funcionando.");

	}
	
	
	private static void testRsaSignature() {
		System.out.println("\n[TESTE] RSA-PSS:");
		KeyPair kp = RSAUtils.generateKeyPair(2048);
		String data = "Assinatura de teste";
		byte[] sig = RSAUtils.sign(data, kp.getPrivate());
		boolean valid = RSAUtils.verify(data, sig, kp.getPublic());
		assert valid: "Falha na assinatura RSA!";
		System.out.println("Dados: "+data);
		System.out.println("Assinatura válida:"+ valid);
		System.out.println("RSA-PSS funcionando");	
		
	}
	
	private static void testDataBaseConnection() {
		System.out.println("\n[TESTE] Conexão com banco de dados");
		
		try {
			var status = ConnectionFactory.checkStatus();
			if (!status.available) {
				System.out.println("Banco indisponivel: "+ status.message);
				System.out.println("certifique-se de ter configurado o banco via configuracaobancoview");
				return;
			}
			try(Connection conn = ConnectionFactory.getConnection()){
				DatabaseMetaData meta = conn.getMetaData();
				System.out.println("Conectado a: "+meta.getURL());
				System.out.println("Driver: "+meta.getDriverName());
				System.out.println("Conexao mysql funcionando.");
				
			}
			
		} catch (Exception e) {
			System.err.println("Falha na conexao: "+e.getMessage());
			e.printStackTrace();
		}
		
		
		
		
		
		
	}

}
