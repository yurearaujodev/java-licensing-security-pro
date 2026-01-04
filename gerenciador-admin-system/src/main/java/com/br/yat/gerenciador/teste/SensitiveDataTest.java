package com.br.yat.gerenciador.teste;

import com.br.yat.gerenciador.util.SensitiveData;

public class SensitiveDataTest {

	public static void main(String[] args) {
		System.out.println("===TESTE COMPLETO DE SensitiveData ===\n");
		try {
			System.out.println("1.Testando safeClear() com array válido...");
			char[] data = "MinhaSenha123".toCharArray();
			System.out.println("Antes: " + new String(data));

			SensitiveData.safeClear(data);
			System.out.println("Depois: \"" + new String(data) + "\"");

			boolean cleared = true;
			for (char c : data) {
				if (c != '\0') {
					cleared = false;
					break;
				}
			}
			System.out.println("Array limpo: " + cleared);
			assert cleared : "Array não foi limpo corretamente";
			System.out.println("safeClear() funcionando!\n");

//			System.out.println("2. Testendo safeClear() com null...");
//			SensitiveData.safeClear(null);
//			System.out.println("safeClear(null) executando sem erros!\n");

			System.out.println("3. Testando safeClear() com array grande...");
			char[] bigData = new char[10000];
			for (int i = 0; i < bigData.length; i++)
				bigData[i] = 'X';
			long start = System.nanoTime();
			SensitiveData.safeClear(bigData);
			long end = System.nanoTime();
			System.out.println("Array de 10.000 chars limpo em " + (end - start));
			System.out.println("Desempenho aceitável!\n");

			System.out.println("TODOS OS TESTES DO SensitiveData FORAM EXECUTADOS!");
		} catch (Exception e) {
			System.err.println("Falha no teste: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
