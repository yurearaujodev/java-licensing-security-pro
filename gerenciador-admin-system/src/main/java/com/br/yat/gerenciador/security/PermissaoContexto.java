package com.br.yat.gerenciador.security;

import java.util.EnumSet;
import java.util.Set;

import com.br.yat.gerenciador.model.enums.TipoPermissao;

public final class PermissaoContexto {

	private final boolean master;
	private final Set<TipoPermissao> permissoes;

	private PermissaoContexto(boolean master, Set<TipoPermissao> permissoes) {
		this.master = master;
		this.permissoes = permissoes;
	}

	public static PermissaoContexto master() {
		return new PermissaoContexto(true, EnumSet.allOf(TipoPermissao.class));
	}

	public static PermissaoContexto comum(Set<TipoPermissao> permissoes) {
		return new PermissaoContexto(false,
				permissoes == null ? EnumSet.noneOf(TipoPermissao.class) : EnumSet.copyOf(permissoes));
	}

	public static PermissaoContexto semPermissao() {
		return new PermissaoContexto(false, EnumSet.noneOf(TipoPermissao.class));
	}

	public boolean isMaster() {
		return master;
	}

	public boolean temRead() {
		return master || permissoes.contains(TipoPermissao.READ);
	}

	public boolean temWrite() {
		return master || permissoes.contains(TipoPermissao.WRITE);
	}

	public boolean temDelete() {
		return master || permissoes.contains(TipoPermissao.DELETE);
	}
}