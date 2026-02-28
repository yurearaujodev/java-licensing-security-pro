package com.br.yat.gerenciador.domain.event;

import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.dto.UsuarioAuditRecord;
import com.br.yat.gerenciador.util.AuditDiffUtil;
import java.util.Collections;
import java.util.Map;

public class UsuarioEvents {

	public static class Criado extends DomainEvent {
		private final UsuarioAuditRecord usuario;

		public Criado(Usuario u) {
			this.usuario = UsuarioAuditRecord.fromUsuario(u);
		}

		public UsuarioAuditRecord getUsuario() {
			return usuario;
		}
	}

	public static class Alterado extends DomainEvent {
		private final UsuarioAuditRecord antes;
		private final UsuarioAuditRecord depois;
		private final Map<String, Object[]> diff;

		public Alterado(Usuario uAntes, Usuario uDepois) {
			this.antes = UsuarioAuditRecord.fromUsuario(uAntes);
			this.depois = UsuarioAuditRecord.fromUsuario(uDepois);
			this.diff = AuditDiffUtil.calcularDiff(this.antes, this.depois);
		}

		public UsuarioAuditRecord getAntes() {
			return antes;
		}

		public UsuarioAuditRecord getDepois() {
			return depois;
		}

		public Map<String, Object[]> getDiff() {
			return Collections.unmodifiableMap(diff);
		}
	}

	public static class Excluido extends DomainEvent {
		private final UsuarioAuditRecord antes;

		public Excluido(Usuario u) {
			this.antes = UsuarioAuditRecord.fromUsuario(u);
		}

		public UsuarioAuditRecord getAntes() {
			return antes;
		}
	}

	public static class Restaurado extends DomainEvent {
		private final UsuarioAuditRecord depois;

		public Restaurado(Usuario u) {
			this.depois = UsuarioAuditRecord.fromUsuario(u);
		}

		public UsuarioAuditRecord getDepois() {
			return depois;
		}
	}

	public static class SenhaAlterada extends DomainEvent {
		private final UsuarioAuditRecord usuario;

		public SenhaAlterada(Usuario u) {
			this.usuario = UsuarioAuditRecord.fromUsuario(u);
		}

		public UsuarioAuditRecord getUsuario() {
			return usuario;
		}
	}

	public static class StatusAlterado extends DomainEvent {
		private final UsuarioAuditRecord antes;
		private final UsuarioAuditRecord depois;

		public StatusAlterado(Usuario uAntes, Usuario uDepois) {
			this.antes = UsuarioAuditRecord.fromUsuario(uAntes);
			this.depois = UsuarioAuditRecord.fromUsuario(uDepois);
		}

		public UsuarioAuditRecord getAntes() {
			return antes;
		}

		public UsuarioAuditRecord getDepois() {
			return depois;
		}
	}
}