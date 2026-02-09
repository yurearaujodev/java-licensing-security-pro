package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.ParametroSistemaDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ServiceOperationException;
import com.br.yat.gerenciador.model.ParametroSistema;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.ServiceErrorType;

public class ParametroSistemaService extends BaseService {

	private static final MenuChave CHAVE_MENU = MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA;

	// -------------------- MÉTODOS PÚBLICOS --------------------
	public int getInt(ParametroChave chave, int valorPadrao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new Interno(conn).getInt(chave, valorPadrao);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "Erro ao carregar parâmetro do sistema",
					e);
		}
	}

	public boolean getBoolean(ParametroChave chave, boolean valorPadrao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new Interno(conn).getBoolean(chave, valorPadrao);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "Erro ao carregar parâmetro do sistema",
					e);
		}
	}

	public String getString(ParametroChave chave, String valorPadrao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new Interno(conn).getString(chave, valorPadrao);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "Erro ao carregar parâmetro do sistema",
					e);
		}
	}

	public void salvarOuAtualizar(List<ParametroSistema> parametros, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_MENU, "WRITE");
			try {
				ConnectionFactory.beginTransaction(conn);
				Interno interno = new Interno(conn);

				for (ParametroSistema p : parametros) {
					interno.salvarOuAtualizar(p);
				}

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "SALVAR_PARAMETROS", "parametro_sistema", e);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO DE INFRAESTRUTURA AO SALVAR PARÂMETROS", e);
		}
	}

	// -------------------- CLASSE INTERNA --------------------
	private static class Interno {

		private final ParametroSistemaDao dao;

		private Interno(Connection conn) {
			this.dao = new ParametroSistemaDao(conn);
		}

		// --- GET INT ---
		private int getInt(ParametroChave chave, int valorPadrao) {
			return dao.getParametro(chave.getChaveBanco()).map(p -> parseInt(p.getValor(), chave)).orElse(valorPadrao);
		}

		// --- GET BOOLEAN ---
		private boolean getBoolean(ParametroChave chave, boolean valorPadrao) {
			return dao.getParametro(chave.getChaveBanco()).map(p -> parseBoolean(p.getValor(), chave))
					.orElse(valorPadrao);
		}

		// --- GET STRING ---
		private String getString(ParametroChave chave, String valorPadrao) {
			return dao.getParametro(chave.getChaveBanco()).map(ParametroSistema::getValor).orElse(valorPadrao);
		}

		// --- SALVAR OU ATUALIZAR ---
		private void salvarOuAtualizar(ParametroSistema parametro) {
			validarParametro(parametro);
			dao.salvarOuAtualizar(parametro);
		}

		// --- PARSE INT COM TRATAMENTO ---
		private int parseInt(String valor, ParametroChave chave) {
			try {
				return Integer.parseInt(valor);
			} catch (NumberFormatException e) {
				throw new ServiceOperationException(ServiceErrorType.BUSINESS_RULE_VIOTATION,
						"Parâmetro inválido (int): " + chave.name(), e);
			}
		}

		// --- PARSE BOOLEAN COM TRATAMENTO ---
		private boolean parseBoolean(String valor, ParametroChave chave) {
			if (valor == null) {
				return false;
			}
			return valor.equalsIgnoreCase("true") || valor.equals("1");
		}

		// --- VALIDAÇÃO BÁSICA ---
		private void validarParametro(ParametroSistema p) {
			if (p == null) {
				throw new ServiceOperationException(ServiceErrorType.BUSINESS_RULE_VIOTATION,
						"Parâmetro não pode ser nulo");
			}
			if (p.getChave() == null || p.getChave().isBlank()) {
				throw new ServiceOperationException(ServiceErrorType.BUSINESS_RULE_VIOTATION,
						"Chave do parâmetro é obrigatória");
			}
			if (p.getValor() == null) {
				throw new ServiceOperationException(ServiceErrorType.BUSINESS_RULE_VIOTATION,
						"O valor do parâmetro [" + p.getChave() + "] não pode ser nulo.");
			}
		}
	}
}
