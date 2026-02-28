package com.br.yat.gerenciador.domain.event;

public class ErrorEvents {

	public static class ErroSistema extends DomainEvent {
		private final String tipo;
		private final String acao;
		private final String entidade;
		private final String mensagem;

		public ErroSistema(String tipo, String acao, String entidade, String mensagem) {
			this.tipo = tipo;
			this.acao = acao;
			this.entidade = entidade;
			this.mensagem = mensagem;
		}

		public String getTipo() {
			return tipo;
		}

		public String getAcao() {
			return acao;
		}

		public String getEntidade() {
			return entidade;
		}

		public String getMensagem() {
			return mensagem;
		}
	}
}