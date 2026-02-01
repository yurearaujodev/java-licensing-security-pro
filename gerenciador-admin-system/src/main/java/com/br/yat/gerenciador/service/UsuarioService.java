package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoMenuDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.validation.UsuarioValidationUtils;

public class UsuarioService {

	public void salvarUsuarioCompleto(Usuario usuario, List<MenuChave> chavesSelecionadas, Usuario executor) {

		// 1. PRIMEIRA VALIDAÇÃO (Estatística/Campos - Antes de abrir conexão)
		validarDados(usuario, chavesSelecionadas);

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);

			try {
				UsuarioDao usuarioDao = new UsuarioDao(conn);
				UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
				PermissaoDao permissaoDao = new PermissaoDao(conn);

				// 2. SEGUNDA VALIDAÇÃO (Regras de Negócio - Depende do Banco)
				validarDuplicidadeEmail(usuarioDao, usuario);

				// Processamento de Senha (BCrypt)
				if (usuario.getSenhaHash() != null && usuario.getSenhaHash().length > 0) {
					String hash = PasswordUtils.hashPassword(usuario.getSenhaHash());
					usuario.setSenhaHashString(hash); // Corrigido para o novo método da Model
				}

				// Salva ou Atualiza o Usuário
				if (usuario.getIdUsuario() != null && usuario.getIdUsuario() > 0) {
					usuarioDao.update(usuario);
				} else {
					int idGerado = usuarioDao.save(usuario);
					usuario.setIdUsuario(idGerado);
				}

				// Sincroniza Permissões
				sincronizarPermissoes(upDao, permissaoDao, usuario, chavesSelecionadas, executor);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	/**
	 * Autentica o usuário para o Login
	 */
	public Usuario autenticar(String email, char[] senhaPura) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			Usuario user = dao.buscarPorEmail(email);

			if (user == null) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO NÃO ENCONTRADO.");
			}
			if ("BLOQUEADO".equals(user.getStatus())) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"CONTA BLOQUEADA POR EXCESSO DE TENTATIVAS. CONTATE O ADMIN.");
			}
			if ("INATIVO".equals(user.getStatus())) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ESTE USUÁRIO ESTÁ INATIVO.");
			}

			boolean senhaValida = PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString());
			if (!senhaValida) {
				dao.incrementarTentativasFalhas(email);
				if (user.getTentativasFalhas() + 1 >= 5) {
					dao.bloquearUsuario(user.getIdUsuario());
					throw new ValidationException(ValidationErrorType.INVALID_FIELD,
							"LIMITE ATINGIDO. SUA CONTA FOI BLOQUEADA POR SEGURANÇA.");
				}
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"SENHA INCORRETA. TENTATIVA " + (user.getTentativasFalhas() + 1) + " DE 5.");
			}
			// Se chegou aqui, login deu certo
			dao.atualizarUltimoLogin(user.getIdUsuario()); // Reseta falhas e marca hora
			return user;
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		} finally {
			SensitiveData.safeClear(senhaPura);
		}
	}
	
	public List<Usuario> listarUsuarios(String termo) {
	    try (Connection conn = ConnectionFactory.getConnection()) {
	        UsuarioDao dao = new UsuarioDao(conn);
	        // Se o termo estiver vazio, listamos todos, caso contrário, filtramos
	        if (termo == null || termo.trim().isEmpty()) {
	            return dao.listAll();
	        } else {
	            return dao.listarPorNomeOuEmail(termo);
	        }
	    } catch (SQLException e) {
	        throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR USUÁRIOS", e);
	    }
	}

	/**
	 * Carrega as permissões para a Sessão
	 */
	public List<MenuChave> carregarPermissoesAtivas(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new UsuarioPermissaoDao(conn).buscarChavesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void validarDados(Usuario usuario, List<MenuChave> chaves) {
		UsuarioValidationUtils.validarUsuario(usuario);
		if (chaves == null || chaves.isEmpty()) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O USUÁRIO DEVE TER AO MENOS UMA PERMISSÃO.");
		}
	}

	private void validarDuplicidadeEmail(UsuarioDao dao, Usuario u) {
		Usuario existente = dao.buscarPorEmail(u.getEmail());
		if (existente != null && !existente.getIdUsuario().equals(u.getIdUsuario())) {
			throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
					"ESTE E-MAIL JÁ ESTÁ CADASTRADO NO SISTEMA.");
		}
	}

	private void sincronizarPermissoes(UsuarioPermissaoDao upDao, PermissaoDao pDao, Usuario usuario,
			List<MenuChave> chaves, Usuario executor) {

		validarDados(usuario, chaves);
		upDao.disableAllFromUser(usuario.getIdUsuario());

		for (MenuChave chave : chaves) {
			var permissaoBanco = pDao.findByChave(chave.name());

// Se a permissão não existe, criamos o ecossistema completo para ela
			if (permissaoBanco == null) {
// 1. Criar na tabela 'permissoes'
				permissaoBanco = new Permissao();
				permissaoBanco.setChave(chave.name());
				permissaoBanco.setTipo("MENU");
				String categoria = extrairCategoria(chave.name());
				permissaoBanco.setCategoria(categoria);

				int idPermissao = pDao.save(permissaoBanco);
				permissaoBanco.setIdPermissoes(idPermissao);

// 2. Criar na tabela 'menu_sistema'
				MenuSistemaDao menuDao = new MenuSistemaDao(upDao.getConnection());
				int idMenu = menuDao.save(chave.name(), categoria);

// 3. Criar o vínculo na 'permissao_menu'
				PermissaoMenuDao pmDao = new PermissaoMenuDao(upDao.getConnection());
				pmDao.vincular(idPermissao, idMenu);
			}

// 4. Finalmente, vincula ao usuário (Tabela usuario_permissoes)
			UsuarioPermissao up = new UsuarioPermissao();
			up.setIdUsuario(usuario.getIdUsuario());
			up.setIdPermissoes(permissaoBanco.getIdPermissoes());
			up.setAtiva(true);
			up.setUsuarioConcedeu(executor != null ? executor : usuario);

			upDao.saveOrUpdate(up);
		}
	}

//Método auxiliar para não deixar a categoria vazia no banco
	private String extrairCategoria(String nomeEnum) {
		if (nomeEnum.contains("_")) {
			return nomeEnum.split("_")[0]; // Pega ex: "CADASTROS", "DASHBOARD"
		}
		return "GERAL";
	}

	public Empresa buscarEmpresaFornecedora() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			EmpresaDao dao = new EmpresaDao(conn);
			return dao.buscarPorFornecedora();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO BUSCAR EMPRESA: " + e.getMessage(), e);
		}
	}
}