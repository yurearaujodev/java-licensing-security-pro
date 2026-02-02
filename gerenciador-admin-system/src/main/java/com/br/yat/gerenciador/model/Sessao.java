package com.br.yat.gerenciador.model;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;

import java.util.List;

public class Sessao {
	private static Usuario usuarioLogado;
	private static List<MenuChave> permissoesAtivas;

	private Sessao() {
	} // Evita instanciação

	public static void login(Usuario usuario, List<MenuChave> permissoes) {
	    usuarioLogado = usuario;
	    permissoesAtivas = permissoes;

	    // 1. Limpa o estado visual anterior (Segurança: ninguém herda menu de ninguém)
	    MenuRegistry.disableAll();

	    // 2. Lógica Master (Bypass)
	    if (usuario.isMaster()) {
	        // Habilita absolutamente tudo que estiver registrado no Enum
	        for (MenuChave total : MenuChave.values()) {
	            MenuRegistry.enable(total);
	        }
	        return; 
	    }

	    // 3. Lógica Usuário Comum
	    if (permissoes != null) {
	        for (MenuChave p : permissoes) {
	            MenuRegistry.enable(p);
	        }
	    }
	}

	public static void logout() {
		usuarioLogado = null;
		permissoesAtivas = null;
		MenuRegistry.disableAll();
	}

	public static Usuario getUsuario() {
		return usuarioLogado;
	}

	public static List<MenuChave> getPermissoes() {
		return permissoesAtivas;
	}
}
