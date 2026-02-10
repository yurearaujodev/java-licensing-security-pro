package com.br.yat.gerenciador.model;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;

import java.time.LocalDateTime;
import java.util.List;

public class Sessao {
	private static Usuario usuarioLogado;
    private static List<MenuChave> permissoesAtivas;
    private static LocalDateTime ultimaAtividade;
    private static int tempoExpiracaoMinutos; // Buscado do ParametroChave.TEMPO_SESSAO_MIN

    private Sessao() {}

    public static void login(Usuario usuario, List<MenuChave> permissoes, int minutosLimite) {
        usuarioLogado = usuario;
        permissoesAtivas = permissoes;
        tempoExpiracaoMinutos = minutosLimite;
        registrarAtividade();

        MenuRegistry.disableAll();
        if (usuario.isMaster()) {
            for (MenuChave total : MenuChave.values()) {
                MenuRegistry.enable(total);
            }
            return; 
        }

        if (permissoes != null) {
            permissoes.forEach(MenuRegistry::enable);
        }
    }

    public static void registrarAtividade() {
        ultimaAtividade = LocalDateTime.now();
    }

    public static boolean isExpirada() {
        if (usuarioLogado == null) return true;
        return LocalDateTime.now().isAfter(ultimaAtividade.plusMinutes(tempoExpiracaoMinutos));
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

	public static LocalDateTime getUltimaAtividade() {
		return ultimaAtividade;
	}

	public static int getTempoExpiracaoMinutos() {
		return tempoExpiracaoMinutos;
	}
	
	
}
