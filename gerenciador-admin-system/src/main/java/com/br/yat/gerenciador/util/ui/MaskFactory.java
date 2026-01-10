package com.br.yat.gerenciador.util.ui;
/**
 * Classe utilitária para definição de máscaras de formatação.
 * <p>
 * Responsável apenas por fonecer os padrões de máscara (ex.: CPF, CNPJ, Telefone).
 * </p>
 * 
 * <p>Não deve ser instanciada.</p>
 */

import java.util.HashMap;
import java.util.Map;

public final class MaskFactory {
	/**
	 * Construtor privado para evitar instanciação.
	 */
	private MaskFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}
	/**
	 * Retorna um mapa com todas as máscaras disponíveis.
	 * 
	 * @return mapa de máscaras (chave = nome, valor = padrão)
	 */
	public static Map<String, String> createMask(){
		Map<String, String> map = new HashMap<>();
		map.put("CPF", "###.###.###-##");
		map.put("CNPJ", "##.###.###/####-##");
		map.put("FIXO", "(##) ####-####");
		map.put("CELULAR", "(##) #####-####");
		map.put("WHATSAPP", "(##) #####-####");
		map.put("FUNDACAO", "##/##/####");
		map.put("CAPITAL", "#,##0.00");
		map.put("CEP", "#####-###");
		map.put("BANCO", "###");
		return map;
	}
	/**
	 * 
	 * @return
	 */
	public static String getCpfMask() {
		return "###.###.###-##";
	}

}
