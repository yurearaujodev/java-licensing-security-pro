package com.br.yat.gerenciador.security;

import java.sql.Connection;

import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;

public class SecurityService {

	public void validarAcesso(Connection conn, Usuario executor, MenuChave chave, TipoPermissao tipoOperacao) {

		if (isBootstrap(conn, executor)) {
			return;
		}

		validarSessao(executor);
		validarUsuarioAtivo(conn, executor);
		validarPermissao(conn, executor, chave, tipoOperacao);
	}

	private boolean isBootstrap(Connection conn, Usuario executor) {

		if (executor != null) {
			return false;
		}

		EmpresaDao dao = new EmpresaDao(conn);

		if (dao.buscarPorFornecedora() != null) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Sessão inválida.");
		}

		return true;
	}

	private void validarSessao(Usuario executor) {

		if (executor == null) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Usuário não autenticado.");
		}

		if (Sessao.isExpirada()) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Sessão expirada.");
		}
	}

	private void validarUsuarioAtivo(Connection conn, Usuario executor) {
		UsuarioDao usuarioDao = new UsuarioDao(conn);
		Usuario usuarioBanco = usuarioDao.searchById(executor.getIdUsuario());

		if (usuarioBanco == null || usuarioBanco.getStatus() != StatusUsuario.ATIVO) {

			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Usuário inativo ou removido.");
		}
	}

	private void validarPermissao(Connection conn, Usuario executor, MenuChave chave, TipoPermissao tipoOperacao) {

		if (chave == null || tipoOperacao == null) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "Operação inválida.");
		}

		if (executor.isMaster())
			return;

		UsuarioPermissaoDao permissaoDao = new UsuarioPermissaoDao(conn);

		Integer idPerfil = executor.getPerfil() != null ? executor.getPerfil().getIdPerfil() : 0;

		boolean permitido = permissaoDao.usuarioPossuiAcessoCompleto(executor.getIdUsuario(), idPerfil, chave.name(),
				tipoOperacao.name());

		if (!permitido) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"Acesso negado para operação " + tipoOperacao);
		}
	}
}