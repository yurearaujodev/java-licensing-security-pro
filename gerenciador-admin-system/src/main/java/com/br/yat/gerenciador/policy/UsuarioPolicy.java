package com.br.yat.gerenciador.policy;

import java.util.List;

import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;

public final class UsuarioPolicy {

	private UsuarioPolicy() {
	}

	public static boolean isPrivilegiado(Usuario u) {
		return u != null && u.isMaster();
	}

	public static boolean ignoraValidacaoPermissao(Usuario u) {
		return isPrivilegiado(u);
	}

	public static List<MenuChave> permissoesCompletas() {
		return List.of(MenuChave.values());
	}

	public static boolean podeExcluir(Usuario executor, Usuario alvo, int nivelExecutor, int nivelAlvo) {
		if (executor == null || alvo == null)
			return false;

		if (executor.getIdUsuario().equals(alvo.getIdUsuario()))
			return false;

		if (isPrivilegiado(executor))
			return true;

		if (isPrivilegiado(alvo))
			return false;

		return nivelExecutor > nivelAlvo;
	}

	public static boolean podeEditarPermissoes(Usuario executor) {
		return isPrivilegiado(executor);
	}

	public static boolean podeAlterarStatusPrivilegiado(Usuario executor) {
		return isPrivilegiado(executor);
	}

	public static boolean podeAlterar(Usuario executor, Usuario alvo, int nivelExecutor, int nivelAlvo) {
		if (executor == null || alvo == null)
			return false;

		if (isPrivilegiado(executor))
			return true;

		if (isPrivilegiado(alvo))
			return false;

		return nivelExecutor > nivelAlvo;
	}

	public static boolean podeAlterarStatusMaster(Usuario usuario) {
		if (usuario == null)
			return false;

		// Apenas master existente mantém status ativo; outros não podem ser master
		return usuario.getStatus() == StatusUsuario.ATIVO && isPrivilegiado(usuario);
	}

}