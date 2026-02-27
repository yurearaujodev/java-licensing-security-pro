package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.usuario.PerfilDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.Diferenca;
import com.br.yat.gerenciador.util.DiferencaMapperUtil;

public class PerfilService extends BaseService {

	private static final MenuChave CHAVE_ACESSO = MenuChave.CONFIGURACAO_PERMISSAO;
	private static final String PERFIL_MASTER = "MASTER";
	private final UsuarioPermissaoService usuarioPermissaoService;
	private final AuditLogService auditLogService;
	private DaoFactory daoFactory;

	public PerfilService(UsuarioPermissaoService usuarioPermissaoService, DaoFactory daoFactory,
			AuditLogService auditLogService) {
		this.usuarioPermissaoService = usuarioPermissaoService;
		this.daoFactory = daoFactory;
		this.auditLogService = auditLogService;
	}

	public void salvarPerfil(Perfil perfil, Map<MenuChave, List<String>> permissoes, Usuario executor) {

		validarRestricoesSistema(perfil);

		Map<MenuChave, List<String>> permissoesPreparadas = prepararPermissoes(permissoes);

		try {

			executeInTransactionVoid(conn -> {

				boolean isNovo = isNovoPerfil(perfil);

				validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.WRITE);

				PerfilDao perfilDao = daoFactory.createPerfilDao(conn);

				Perfil estadoAnterior = obterPerfilExistente(perfil, perfilDao);

				validarRegrasPersistencia(perfilDao, perfil);

				if (isNovo) {
					inserirPerfil(perfilDao, perfil, conn);
				} else {
					validarImutabilidadeMaster(estadoAnterior, perfil);
					atualizarPerfil(perfilDao, perfil, estadoAnterior, conn);
				}

				usuarioPermissaoService.sincronizarPermissoesPerfil(conn, perfil.getIdPerfil(), permissoesPreparadas,
						executor);

			});

		} catch (ValidationException ve) {
			throw ve;

		} catch (RuntimeException e) {
			auditLogService.registrarErro("ERRO", "SALVAR_PERFIL", "perfil", e);
			throw e;
		}
	}

	private Map<MenuChave, List<String>> prepararPermissoes(Map<MenuChave, List<String>> permissoes) {

		return permissoes != null ? new HashMap<>(permissoes) : new HashMap<>();
	}

	private void validarRegrasPersistencia(PerfilDao dao, Perfil perfil) {

		dao.buscarPorNome(perfil.getNome()).ifPresent(existente -> {

			boolean mesmoRegistro = perfil.getIdPerfil() != null
					&& existente.getIdPerfil().equals(perfil.getIdPerfil());

			if (!mesmoRegistro) {
				throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
						"JÁ EXISTE UM PERFIL COM ESTE NOME.");
			}
		});
	}

	private void inserirPerfil(PerfilDao dao, Perfil perfil, Connection conn) {

		int id = dao.save(perfil);
		perfil.setIdPerfil(id);

		auditLogService.registrarSucesso(conn, "SEGURANCA", "CRIAR_PERFIL", "perfil", id, null, perfil);
	}

	private void atualizarPerfil(PerfilDao dao, Perfil perfil, Perfil estadoAnterior, Connection conn) {

		if (estadoAnterior == null) {
			throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
					"PERFIL NÃO ENCONTRADO PARA ATUALIZAÇÃO.");
		}

		dao.update(perfil);

		Perfil depois = dao.searchById(perfil.getIdPerfil());

		registrarAlteracaoComDiff(conn, "SEGURANCA", "ALTERAR_PERFIL", perfil.getIdPerfil(), estadoAnterior, depois);
	}

	private void validarImutabilidadeMaster(Perfil anterior, Perfil atual) {

		if (anterior == null)
			return;

		if (isMaster(anterior) && !anterior.getNome().equalsIgnoreCase(atual.getNome())) {

			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "O NOME DO PERFIL MASTER É IMUTÁVEL.");
		}
	}

	private void validarRestricoesSistema(Perfil perfil) {

		if (perfil.getNome() != null && PERFIL_MASTER.equalsIgnoreCase(perfil.getNome()) && isNovoPerfil(perfil)) {

			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"O PERFIL MASTER SÓ PODE SER CRIADO PELO SISTEMA.");
		}

	}

	private Perfil obterPerfilExistente(Perfil perfil, PerfilDao dao) {
		if (perfil.getIdPerfil() == null || perfil.getIdPerfil() == 0)
			return null;

		Perfil existente = dao.searchById(perfil.getIdPerfil());

		if (existente == null) {
			throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "PERFIL NÃO ENCONTRADO.");
		}

		return existente;
	}

	private boolean isNovoPerfil(Perfil perfil) {
		return perfil.getIdPerfil() == null || perfil.getIdPerfil() == 0;
	}

	private boolean isMaster(Perfil perfil) {
		return perfil != null && PERFIL_MASTER.equalsIgnoreCase(perfil.getNome());
	}

	public boolean isPerfilMaster(Perfil perfil) {
		return perfil != null && PERFIL_MASTER.equalsIgnoreCase(perfil.getNome());
	}

	public void excluirPerfil(int idPerfil, Usuario executor) {
		try {
			executeInTransactionVoid(conn -> {
				validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.DELETE);

				PerfilDao dao = daoFactory.createPerfilDao(conn);

				Perfil perfil = dao.searchById(idPerfil);

				if (perfil == null) {
					throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "PERFIL NÃO ENCONTRADO.");
				}

				if (PERFIL_MASTER.equalsIgnoreCase(perfil.getNome())) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"O PERFIL MASTER NÃO PODE SER EXCLUÍDO OU DESATIVADO.");
				}

				Perfil antes = dao.searchById(idPerfil); // estado antes
				dao.softDeleteById(idPerfil);
				Perfil depois = dao.searchByIdIncluindoExcluidos(idPerfil); // estado depois (soft deleted)

				registrarAlteracaoComDiff(conn, "SEGURANCA", "EXCLUIR_PERFIL", idPerfil, antes, depois);
			});

		} catch (RuntimeException e) {
			auditLogService.registrarErro("ERRO", "EXCLUIR_PERFIL", "perfil", e);
			throw e;
		}
	}

	public List<Perfil> listarExcluidos(Usuario executor) {
		return execute(conn -> {
			validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.DELETE);
			PerfilDao dao = daoFactory.createPerfilDao(conn);
			return dao.listarExcluidos();
		});
	}

	public List<Perfil> listarPerfisVisiveis(String termo, boolean verExcluidos, Usuario usuarioLogado) {

		List<Perfil> lista = verExcluidos ? listarExcluidos(usuarioLogado) : listarTodos();

		if (usuarioLogado == null || !usuarioLogado.isMaster()) {
			lista = lista.stream().filter(p -> !isMaster(p)).collect(Collectors.toList());
		}

		if (termo != null && !termo.isBlank()) {
			String termoLower = termo.toLowerCase();
			lista = lista.stream().filter(p -> p.getNome().toLowerCase().contains(termoLower))
					.collect(Collectors.toList());
		}

		return lista;
	}

	public void restaurarPerfil(int idPerfil, Usuario executor) {

		try {
			executeInTransactionVoid(conn -> {

				validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.DELETE);

				PerfilDao dao = daoFactory.createPerfilDao(conn);

				Perfil perfil = dao.searchByIdIncluindoExcluidos(idPerfil);

				if (perfil == null) {
					throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "PERFIL NÃO ENCONTRADO.");
				}

				if (isMaster(perfil)) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"O PERFIL MASTER NÃO PODE SER RESTAURADO MANUALMENTE.");
				}

				Perfil antes = dao.searchByIdIncluindoExcluidos(idPerfil);
				dao.restaurar(idPerfil);
				Perfil depois = dao.searchById(idPerfil);

				registrarAlteracaoComDiff(conn, "SEGURANCA", "RESTAURAR_PERFIL", idPerfil, antes, depois);

			});

		} catch (RuntimeException e) {
			auditLogService.registrarErro("ERRO", "RESTAURAR_PERFIL", "perfil", e);
			throw e;
		}
	}

	public List<Perfil> listarTodos() {
		return execute(conn -> {
			PerfilDao dao = daoFactory.createPerfilDao(conn);
			return dao.listAll();
		});
	}

	public List<Permissao> listarPermissoesDoPerfil(int idPerfil) {
		return execute(conn -> {
			PerfilPermissoesDao dao = daoFactory.createPerfilPermissoesDao(conn);
			return dao.listarPermissoesPorPerfil(idPerfil);
		});
	}

	private String mapearPerfilParaDiff(Perfil p) {
		if (p == null)
			return "";

		return p.getNome() + "|" + (p.getDescricao() != null ? p.getDescricao() : "");
	}

	private void registrarAlteracaoComDiff(Connection conn, String tipo, String acao, Integer id, Perfil anterior,
			Perfil depois) {

		Diferenca<String> diff = DiferencaMapperUtil.calcular(List.of(anterior), List.of(depois),
				this::mapearPerfilParaDiff);

		if (diff.temAlteracao()) {
			auditLogService.registrarSucesso(conn, tipo, acao, "perfil", id, Map.of("antes", anterior),
					Map.of("depois", depois));
		}
	}
}