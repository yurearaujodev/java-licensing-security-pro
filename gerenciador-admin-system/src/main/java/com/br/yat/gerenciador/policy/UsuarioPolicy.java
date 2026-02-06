package com.br.yat.gerenciador.policy;

import java.util.List;

import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;

public final class UsuarioPolicy {

	private UsuarioPolicy() {
	}

	/** Quem é considerado usuário privilegiado */
	public static boolean isPrivilegiado(Usuario u) {
		return u != null && u.isMaster();
		// amanhã pode virar: u.isAdmin() || u.isMaster()
	}

	/** Usuário privilegiado pode tudo */
	public static boolean ignoraValidacaoPermissao(Usuario u) {
		return isPrivilegiado(u);
	}

	/** Usuário privilegiado sempre tem todas as permissões */
	public static List<MenuChave> permissoesCompletas() {
		return List.of(MenuChave.values());
	}

	/** Pode excluir outro usuário? */
	public static boolean podeExcluir(Usuario executor, Usuario alvo) {
		if (executor == null || alvo == null)
			return false;
		if (executor.getIdUsuario().equals(alvo.getIdUsuario()))
			return false;
		if (isPrivilegiado(alvo))
			return false;
		return isPrivilegiado(executor);
	}

	/** Pode editar permissões? */
	public static boolean podeEditarPermissoes(Usuario executor) {
		return isPrivilegiado(executor);
	}

	/** Pode alterar status de usuário privilegiado? */
	public static boolean podeAlterarStatusPrivilegiado(Usuario executor) {
		return isPrivilegiado(executor);
	}

}
