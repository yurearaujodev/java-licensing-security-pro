package com.br.yat.gerenciador.model;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;

import java.util.List;

public class Sessao {
    private static Usuario usuarioLogado;
    private static List<MenuChave> permissoesAtivas;
    private static long ultimaAtividadeMillis; 
    private static int tempoExpiracaoMinutos;

    private Sessao() {}

    public static void login(Usuario usuario, List<MenuChave> permissoes, int minutosLimite) {
        usuarioLogado = usuario;
        permissoesAtivas = permissoes;
        
        tempoExpiracaoMinutos = (minutosLimite > 0) ? minutosLimite : 30;
        
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
        // Usa o tempo do sistema em milissegundos
        ultimaAtividadeMillis = System.currentTimeMillis();
    }

    public static boolean isExpirada() {
        if (usuarioLogado == null) return true;

        long agora = System.currentTimeMillis();
        long limiteMillis = (long) tempoExpiracaoMinutos * 60 * 1000;
        
        // Se a diferença entre agora e a última atividade for maior que o limite
        return (agora - ultimaAtividadeMillis) > limiteMillis;
    }

    public static void logout() {
        usuarioLogado = null;
        permissoesAtivas = null;
        ultimaAtividadeMillis = 0;
        MenuRegistry.disableAll();
    }

	public static Usuario getUsuario() {
		return usuarioLogado;
	}

	public static List<MenuChave> getPermissoes() {
		return permissoesAtivas;
	}

	public static long getUltimaAtividadeMillis() {
	    return ultimaAtividadeMillis;
	}

	public static int getTempoExpiracaoMinutos() {
		return tempoExpiracaoMinutos;
	}
	
	
}
