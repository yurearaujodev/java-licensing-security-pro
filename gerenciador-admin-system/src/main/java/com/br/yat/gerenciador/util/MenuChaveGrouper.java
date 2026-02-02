package com.br.yat.gerenciador.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.model.enums.MenuChave;

public final class MenuChaveGrouper {
	private MenuChaveGrouper() {
	}

	public static Map<String, List<MenuChave>> groupByCategoria() {
		return Arrays.stream(MenuChave.values()).collect(
				Collectors.groupingBy(MenuChaveGrouper::extrairCategoria, LinkedHashMap::new, Collectors.toList()));
	}
	
	public static Map<String, List<MenuChave>> groupByCategoriaFiltrado(List<MenuChave> permitidas) {
        return permitidas.stream().collect(
                Collectors.groupingBy(MenuChaveGrouper::extrairCategoria, LinkedHashMap::new, Collectors.toList()));
    }

	private static String extrairCategoria(MenuChave chave) {
		String nome = chave.name();
		int idx = nome.indexOf('_');
		return idx > 0 ? nome.substring(0, idx) : "OUTROS";
	}
}
