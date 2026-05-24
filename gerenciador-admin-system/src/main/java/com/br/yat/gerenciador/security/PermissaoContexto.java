package com.br.yat.gerenciador.security;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.TipoPermissao;

public final class PermissaoContexto {

	private final boolean master;

	private final EnumSet<TipoPermissao> permissoes;

	private PermissaoContexto(boolean master, EnumSet<TipoPermissao> permissoes) {

		this.master = master;

		this.permissoes = permissoes.clone();
	}

	public static PermissaoContexto master() {

		return new PermissaoContexto(true, EnumSet.allOf(TipoPermissao.class));
	}

	public static PermissaoContexto comum(Set<TipoPermissao> permissoes) {

		EnumSet<TipoPermissao> perms = (permissoes == null || permissoes.isEmpty())
				? EnumSet.noneOf(TipoPermissao.class)
				: EnumSet.copyOf(permissoes);

		return new PermissaoContexto(false, perms);
	}

	public boolean podeResetarSenha(boolean temSelecao, boolean modoLixeira) {

		return temSelecao && !modoLixeira && temDelete();
	}

	public static PermissaoContexto semPermissao() {

		return new PermissaoContexto(false, EnumSet.noneOf(TipoPermissao.class));
	}

	public boolean isMaster() {

		return master;
	}

	public boolean tem(TipoPermissao permissao) {

		Objects.requireNonNull(permissao, "Permissão não pode ser null.");

		return master || permissoes.contains(permissao);
	}

	public boolean temRead() {

		return tem(TipoPermissao.READ);
	}

	public boolean temWrite() {

		return tem(TipoPermissao.WRITE);
	}

	public boolean temDelete() {

		return tem(TipoPermissao.DELETE);
	}

	public boolean podeExcluirPerfil(Perfil perfil, boolean modoLixeira) {

		if (perfil == null) {
			return false;
		}

		if (!temDelete()) {
			return false;
		}

		if (!modoLixeira && "MASTER".equalsIgnoreCase(perfil.getNome())) {
			return false;
		}

		return true;
	}

	public boolean podeExcluirUsuario(Usuario usuario, boolean modoLixeira) {

		if (usuario == null) {
			return false;
		}

		if (!temDelete()) {
			return false;
		}

		if (!modoLixeira && usuario.isMaster()) {
			return false;
		}

		return true;
	}

	public boolean podeEditar(boolean temSelecao, boolean modoLixeira) {

		return temSelecao && !modoLixeira && temWrite();
	}

	public boolean podeCriar(boolean modoLixeira) {

		return !modoLixeira && temWrite();
	}

	public Set<TipoPermissao> getPermissoes() {

		return Collections.unmodifiableSet(permissoes);
	}

	@Override
	public String toString() {

		return "PermissaoContexto [master=" + master + ", permissoes=" + permissoes + "]";
	}
}