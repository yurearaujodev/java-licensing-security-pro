package com.br.yat.gerenciador.util;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
/**
 * Classe utilitária para gerenciamento de itens de menu da aplicação.
 * <p>
 * Responsável por:
 * <ul>
 * <li>Registrar itens de menu associados a chaves enumeradas ({@link MenuChave}).</li>
 * <li>Controlar habilitação e desabilitação de itens.</li>
 * <li>Desabilitar todos os itens registrados de forma centralizada.</li>
 * <li>Recuperar itens de menu a partir de sua chave </li>
 * </ul>
 * </p>
 * 
 * <p>Não deve ser instanciada.</p>
 */
public final class MenuRegistry {

	private static final Map<MenuChave, JMenuItem> REGISTRY = new EnumMap<>(MenuChave.class);

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private MenuRegistry() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Registra um item de menu associado a uma chave.
	 * <p>
	 * O item é desabilitado por padrão ao ser registrado.
	 * </p>
	 * 
	 * @param key chave do menu
	 * @param item item de menu a ser registrado
	 */
	public static void register(MenuChave key, JMenuItem item) {
		item.setEnabled(false);
		REGISTRY.put(key, item);
	}

	/**
	 * Habilita um item de menu previamente registrado.
	 * 
	 * @param key chave do menu
	 */
	public static void enable(MenuChave key) {
		JMenuItem item = REGISTRY.get(key);
		if (item != null) {
			item.setEnabled(true);
		}
	}

	/**
	 * Desabilita todos os itens de menu registrados.
	 */
	public static void disableAll() {
		REGISTRY.values().forEach(i -> i.setEnabled(false));
	}
	
	/**
	 * Retorna o item de menu associado a uma chave.
	 * 
	 * @param key chave do menu
	 * @return item de menu registrado ou {@code null} se não existir
	 */
	public static JMenuItem getItem(MenuChave key) {
		return REGISTRY.get(key);
	}

}
