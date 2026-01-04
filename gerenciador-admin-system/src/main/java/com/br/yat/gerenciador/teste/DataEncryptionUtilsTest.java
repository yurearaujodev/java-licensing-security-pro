package com.br.yat.gerenciador.teste;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.br.yat.gerenciador.util.crypto.DataEncryptionUtils;

public class DataEncryptionUtilsTest {
	
	private static final String IDENTIFIER = "teste_cliente";
	
	public static void main(String[] args) {
		
		System.out.println("===Teste de dataEncryptionutils===");
		
		try {
			
		//	SecretKey key = DataEncryptionUtils.generateAndSaveKey(IDENTIFIER);
			DataEncryptionUtils.generateAndSaveKey(IDENTIFIER);
			Path keyFile = Paths.get("secure-data",IDENTIFIER+".key");
			System.out.println("Arquivo existe? "+ Files.exists(keyFile));
			System.out.println("Chave aes salva em: "+keyFile);
			System.out.println("Arquivo existe? "+ Files.exists(keyFile));
			
			String dadosOriginais = "Dados secretos do cliente!";
			DataEncryptionUtils.encryptAndSave(IDENTIFIER, dadosOriginais);
			Path dataFile = Paths.get("secure_data",IDENTIFIER+".dat");
			System.out.println("Dados criptografados salvos em: "+dataFile);
			System.out.println("Arquivos existe? " + Files.exists(dataFile));
			
			String dadosDecifrados = DataEncryptionUtils.decryptAndLoad(IDENTIFIER);
			System.out.println("Dados originais: "+dadosOriginais);
			System.out.println("Dados decifrados: "+dadosDecifrados);
			
			if (dadosOriginais.equals(dadosDecifrados)) {
				System.out.println("Criptografia e descriptografia funcionaram corretamente!");
			}else {
				System.out.println("Falha na criptografia/descriptografia!");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
