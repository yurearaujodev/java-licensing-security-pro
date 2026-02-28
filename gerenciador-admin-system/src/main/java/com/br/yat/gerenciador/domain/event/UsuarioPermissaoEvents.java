package com.br.yat.gerenciador.domain.event;

import java.util.List;
import java.util.Optional;

import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.dto.UsuarioPermissaoAuditRecord;
import com.br.yat.gerenciador.util.AuditCollectionDiffUtil;
import com.br.yat.gerenciador.util.CollectionDiffResult;

public class UsuarioPermissaoEvents {

	public static class UsuarioAlterado extends DomainEvent {

		private final Integer idUsuario;
		private final List<UsuarioPermissaoAuditRecord> antes;
		private final List<UsuarioPermissaoAuditRecord> depois;
		private final CollectionDiffResult<UsuarioPermissaoAuditRecord, Integer> diff;

		public UsuarioAlterado(Usuario usuario, List<UsuarioPermissao> antes, List<UsuarioPermissao> depois) {

			this.idUsuario = usuario.getIdUsuario();

			this.antes = Optional.ofNullable(antes).orElse(List.of()).stream().map(UsuarioPermissaoAuditRecord::from)
					.toList();

			this.depois = Optional.ofNullable(depois).orElse(List.of()).stream().map(UsuarioPermissaoAuditRecord::from)
					.toList();

			this.diff = AuditCollectionDiffUtil.calcularDiffCollection(this.antes, this.depois,
					UsuarioPermissaoAuditRecord::idPermissao);
		}

		public Integer getIdUsuario() {
			return idUsuario;
		}

		public List<UsuarioPermissaoAuditRecord> getAntes() {
			return antes;
		}

		public List<UsuarioPermissaoAuditRecord> getDepois() {
			return depois;
		}

		public CollectionDiffResult<UsuarioPermissaoAuditRecord, Integer> getDiff() {
			return diff;
		}
	}

	public static class PerfilAlterado extends DomainEvent {

	    private final Integer idPerfil;
	    private final List<String> antes;
	    private final List<String> depois;
	    private final CollectionDiffResult<String, String> diff;

	    public PerfilAlterado(Integer idPerfil, List<Permissao> antes, List<Permissao> depois) {
	        this.idPerfil = idPerfil;

	        this.antes = Optional.ofNullable(antes).orElse(List.of()).stream()
	                .map(p -> p.getChave() + ":" + p.getTipo())
	                .toList();

	        this.depois = Optional.ofNullable(depois).orElse(List.of()).stream()
	                .map(p -> p.getChave() + ":" + p.getTipo())
	                .toList();

	        this.diff = AuditCollectionDiffUtil.calcularDiffCollection(this.antes, this.depois, s -> s);
	    }

	    public Integer getIdPerfil() {
	        return idPerfil;
	    }

	    public List<String> getAntes() {
	        return antes;
	    }

	    public List<String> getDepois() {
	        return depois;
	    }

	    public CollectionDiffResult<String, String> getDiff() {
	        return diff;
	    }
	}
}