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
		System.out.println("Usuário logado: " + usuario.getNome());
	    System.out.println("Permissões recebidas: " + permissoes); // VEJA SE APARECE ALGO AQUI
		// Aplica as permissões nos menus físicos
		MenuRegistry.disableAll();
		if (permissoes != null) {
			for (MenuChave p : permissoes) {
				System.out.println("Habilitando menu: " + p.name());
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
