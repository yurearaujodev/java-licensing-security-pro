package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.AuditLogHelper;

public abstract class BaseService {

	/**
	 * Validação centralizada de permissões granulares.
	 */
	protected void validarAcesso(Connection conn, Usuario executor, MenuChave chave, String tipoOperacao)
			throws SQLException {
		// 1. Validação da Sessão (Camada de Aplicação)
		// Checa se o Timer de inatividade ou o logout já invalidaram a sessão em
		// memória
		if (executor != null && Sessao.isExpirada()) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"SUA SESSÃO EXPIROU POR INATIVIDADE. OPERAÇÃO BLOQUEADA.");
		}

		// 2. Setup inicial (Executor nulo)
		if (executor == null) {
			EmpresaDao dao = new EmpresaDao(conn);
			if (dao.buscarPorFornecedora() != null) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "SESSÃO INVÁLIDA.");
			}
			return;
		}

		// 3. Validação de Estado (Camada de Dados - Double Check)
		// Verifica se o usuário não foi desativado ou excluído enquanto ele estava
		// logado
		UsuarioDao uDao = new UsuarioDao(conn);
		Usuario atualNoBanco = uDao.searchById(executor.getIdUsuario());

		if (atualNoBanco == null
				|| atualNoBanco.getStatus() != com.br.yat.gerenciador.model.enums.StatusUsuario.ATIVO) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"USUÁRIO INATIVO OU REMOVIDO. ACESSO IMEDIATAMENTE REVOGADO.");
		}

		// --- FIM DA DOUBLE VALIDATION ---

		// 4. Super usuário
		if (executor.isMaster())
			return;

		// 5. Validação Granular (Perfil + Permissões Diretas)
		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
		Integer idPerfil = (executor.getPerfil() != null) ? executor.getPerfil().getIdPerfil() : 0;

		boolean temPermissao = upDao.usuarioPossuiAcessoCompleto(executor.getIdUsuario(), idPerfil, chave.name(),
				tipoOperacao);

		if (!temPermissao) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"ACESSO NEGADO: VOCÊ NÃO TEM PERMISSÃO DE " + tipoOperacao + " NESTA TELA.");
		}
	}

	/**
	 * Centraliza a gravação de erros usando o seu AuditLogHelper.
	 * 
	 * @param tipo     Ex: "ERRO", "SISTEMA"
	 * @param acao     Ex: "SALVAR_EMPRESA"
	 * @param entidade Ex: "empresa"
	 * @param e        A exceção capturada
	 */
	protected void registrarLogErro(String tipo, String acao, String entidade, Exception e) {
		try (Connection connLog = ConnectionFactory.getConnection()) {
			String msgErro = (e.getMessage() != null) ? e.getMessage() : e.toString();

			// Chama o SEU método: gerarLogErro(tipo, acao, entidade, erro)
			var log = AuditLogHelper.gerarLogErro(tipo, acao, entidade, msgErro);

			new LogSistemaDao(connLog).save(log);
		} catch (Exception ex) {
			// Log do erro da tentativa de log (fail-safe)
			System.err.println("Falha crítica ao gravar log de erro: " + ex.getMessage());
		}
	}

	protected void registrarLogSucesso(Connection conn, String tipo, String acao, String entidade, Integer idRef,
			Object antes, Object depois) {
		try {
			LogSistemaDao logDao = new LogSistemaDao(conn);

			var log = AuditLogHelper.gerarLogSucesso(tipo, acao, entidade, idRef, antes, depois);

			logDao.save(log);

		} catch (Exception e) {
			// Fail-safe: nunca quebrar fluxo principal por erro de log
			System.err.println("Falha ao registrar log de sucesso: " + e.getMessage());
		}
	}

}