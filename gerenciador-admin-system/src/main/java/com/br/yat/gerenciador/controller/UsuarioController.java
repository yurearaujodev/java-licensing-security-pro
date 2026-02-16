package com.br.yat.gerenciador.controller;

import java.awt.Color;
import java.util.*;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.*;
import com.br.yat.gerenciador.model.enums.*;
import com.br.yat.gerenciador.service.*;
import com.br.yat.gerenciador.util.*;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.security.SensitiveData;

public class UsuarioController extends BaseCadastroController<UsuarioView> {

	private final UsuarioService service;
	private final PerfilService perfilService;
	private Usuario usuarioAtual;
	private RefreshCallback refreshCallback;

	public UsuarioController(UsuarioView view, UsuarioService service, PerfilService perfilService) {
		super(view);
		this.service = service;
		this.perfilService = perfilService;
		inicializar();
	}

	private void inicializar() {
		Usuario logado = Sessao.getUsuario();
		boolean bancoVazio = !service.existeUsuarioMaster();
		Map<String, List<MenuChave>> grupos = (bancoVazio || (logado != null && logado.isMaster()))
				? MenuChaveGrouper.groupByCategoria()
				: MenuChaveGrouper
						.groupByCategoriaFiltrado(Optional.ofNullable(Sessao.getPermissoes()).orElse(List.of()));

		view.construirGradePermissoes(grupos);
		configurarAcoesBase(this::salvarUsuario, this::novoUsuario);
		configurarMonitorSenha();
		carregarDadosIniciais();
		aplicarRestricoesDoExecutor();
		aplicarPermissoesDaTela();
	}

	private void carregarDadosIniciais() {
		carregarEmpresaPadrao();
		carregarListaPerfis(null);
		view.setCamposHabilitados(false);
		view.getBtnNovo().setEnabled(true);
		view.getBtnSalvar().setEnabled(false);
	}

	private void configurarMonitorSenha() {

		view.adicionarListenerSenhaNova(ValidationUtils.createDocumentListener(() -> {

			char[] senha = view.getSenhaNova();
			int forca = ValidationUtils.calcularForcaSenha(senha);

			Color cor = switch (forca) {
			case 0, 1 -> Color.RED;
			case 2 -> Color.ORANGE;
			default -> new Color(0, 128, 0);
			};

			String texto = switch (forca) {
			case 0, 1 -> "FRACA";
			case 2 -> "MÉDIA";
			default -> "FORTE";
			};

			view.atualizarForcaSenha(forca, cor, texto);
		}));
	}

	private void salvarUsuario() {
		if (!validarStatus())
			return;

		boolean isNovo = (usuarioAtual == null || usuarioAtual.getIdUsuario() == null);
		if (isNovo)
			usuarioAtual = new Usuario();

		preencherDadosBasicos();

		char[] senha = view.getSenhaNova();
		char[] senhaConfirmar = view.getConfirmarSenha();
		char[] senhaAntiga = view.getSenhaAntiga();

		if (!processarSenha(senha, senhaConfirmar, isNovo))
			return;

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			try {
				service.salvarUsuario(usuarioAtual, coletarPermissoesMarcadas(), coletarDatasExpiracao(),
						Sessao.getUsuario());
				return true;
			} finally {
				limparSenhas(senha, senhaConfirmar, senhaAntiga);
			}
		}, sucesso -> {
			DialogFactory.informacao(view, isNovo ? "USUÁRIO SALVO!" : "ALTERAÇÃO REALIZADA!");
			view.limparSenhas();
			view.setCamposHabilitados(false);
			if (isNovo)
				novoUsuario();
			else
				view.doDefaultCloseAction();
			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();

		});

	}

	private void limparSenhas(char[] senha, char[] confirmar, char[] antiga) {
		SensitiveData.safeClear(senha);
		SensitiveData.safeClear(confirmar);
		SensitiveData.safeClear(antiga);
		view.limparSenhas();
	}

	private boolean validarStatus() {
		if (view.getStatus() == StatusUsuario.SELECIONE) {
			DialogFactory.aviso(view, "SELECIONE UM STATUS VÁLIDO!");
			return false;
		}
		return true;
	}

	private void preencherDadosBasicos() {
		usuarioAtual.setNome(view.getNome());
		usuarioAtual.setEmail(view.getEmail());
		usuarioAtual.setStatus(view.getStatus());
		usuarioAtual.setMaster(view.isMaster());

		Empresa emp = new Empresa();
		emp.setIdEmpresa(view.getIdEmpresa());
		usuarioAtual.setEmpresa(emp);
		usuarioAtual.setPerfil(view.getPerfilSelecionado());
	}

	private boolean processarSenha(char[] senha, char[] confirmar, boolean isNovo) {
		if (senha.length == 0) {
			if (isNovo) {
				DialogFactory.erro(view, "A SENHA É OBRIGATÓRIA PARA NOVOS USUÁRIOS.");
				return false;
			}
			return true;
		}

		if (!validarSenha(senha, confirmar)) {
			return false;
		}

		usuarioAtual.setSenhaHash(senha);
		usuarioAtual.setConfirmarSenha(confirmar);
		usuarioAtual.setSenhaAntiga(view.getSenhaAntiga());
		return true;
	}

	private boolean validarSenha(char[] senha, char[] confirmar) {
		if (!Arrays.equals(senha, confirmar)) {
			DialogFactory.erro(view, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			return false;
		}
		int forca = ValidationUtils.calcularForcaSenha(senha);
		if (forca < 2) {
			DialogFactory.aviso(view, "A SENHA DEVE TER FORÇA MÍNIMA MÉDIA.");
			return false;
		}
		return true;
	}

	private Map<MenuChave, String> coletarDatasExpiracao() {
		Map<MenuChave, String> datas = new LinkedHashMap<>();
		view.getChavesAtivas().forEach(c -> {
			String d = view.getDataExpiracao(c);
			if (!d.isEmpty())
				datas.put(c, d);
		});
		return datas;
	}

	public void carregarUsuarioParaEdicao(Usuario u) {
		this.usuarioAtual = u;
		view.limpar();
		view.setNome(u.getNome());
		view.setEmail(u.getEmail());
		view.setStatus(u.getStatus());
		view.setMaster(u.isMaster());
		if (u.getEmpresa() != null)
			view.setEmpresa(u.getEmpresa().getIdEmpresa(), u.getEmpresa().getRazaoSocialEmpresa());

		view.entrarModoEdicao(u.isMaster());
		view.setPerfilHabilitado(!u.isMaster());
		view.setMasterHabilitado(false);
		view.setSenhaAntigaHabilitado(true);
		carregarListaPerfis(() -> {
			if (u.getPerfil() != null)
				view.setPerfil(u.getPerfil());
		});
		atualizarEstadoInterface();
		if (u.isMaster()) {
			view.limparPermissoes(); // Garante que a grade fique limpa
			view.setPermissoesHabilitadas(false);
			view.getBtnSalvar().setEnabled(true);
			// Não chama a service para carregar permissões aqui!
		} else {

			runAsync(SwingUtilities.getWindowAncestor(view),
					() -> service.carregarDadosPermissoesEdicao(u.getIdUsuario()), lista -> {
						lista.forEach(dto -> {
							view.setPermissao(MenuChave.valueOf(dto.permissao().getChave()),
									TipoPermissao.valueOf(dto.permissao().getTipo()), true);
							if (dto.getExpiraEm() != null)
								view.setDataExpiracao(MenuChave.valueOf(dto.permissao().getChave()), dto.getExpiraEm());
						});
						atualizarEstadoCheckBoxesCategoria();
						view.getBtnSalvar().setEnabled(true);
					});
		}
	}

	private void aplicarRestricoesDoExecutor() {
		Usuario executor = Sessao.getUsuario();
		if (executor == null || executor.isMaster())
			return;

		runAsyncSilent(SwingUtilities.getWindowAncestor(view),
				() -> service.carregarPermissoesDetalhadas(executor.getIdUsuario()), minhas -> {
					view.getChavesAtivas().forEach(chave -> {
						for (TipoPermissao t : TIPOS) {
							boolean ok = minhas.stream()
									.anyMatch(p -> p.getChave().equals(chave.name()) && p.getTipo().equals(t.name()));
							view.aplicarRestricaoPermissao(chave, t, ok);
						}
					});
					atualizarEstadoCheckBoxesCategoria();
				});
	}

	public void novoUsuario() {
		this.usuarioAtual = null;
		view.limpar();
		view.setMaster(false);
		view.setMasterHabilitado(false);
		view.setPerfilHabilitado(true);
		view.setPermissoesHabilitadas(true);
		carregarEmpresaPadrao();
		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
		view.setCamposHabilitados(true);
		atualizarEstadoInterface();
		view.setSenhaAntigaHabilitado(false);

		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.buscarMasterUnico(), masterExistente -> {
			if (masterExistente == null) {
				view.setMaster(true);
				view.setMasterHabilitado(true);
				view.setPermissoesHabilitadas(false);
			} else {
				view.setMaster(false);
				view.setMasterHabilitado(false);
			}
		});
	}

	private void carregarListaPerfis(Runnable cb) {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			List<Perfil> todos = perfilService.listarTodos();
			Usuario logado = Sessao.getUsuario();

			if (logado != null && !logado.isMaster()) {
				return todos.stream().filter(p -> !"MASTER".equalsIgnoreCase(p.getNome())).toList();
			}
			return todos;
		}, p -> {
			view.carregarCombosPerfil(p);
			if (cb != null)
				cb.run();
		});
	}

	private void carregarEmpresaPadrao() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.buscarEmpresaFornecedora(), e -> {
			if (e != null)
				view.setEmpresa(e.getIdEmpresa(), e.getRazaoSocialEmpresa());
		});
	}

	private void aplicarPermissoesDaTela() {
		Usuario logado = Sessao.getUsuario();
		if (logado == null || logado.isMaster())
			return;
		runAsync(SwingUtilities.getWindowAncestor(view), () -> service
				.listarPermissoesAtivasPorMenu(logado.getIdUsuario(), MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES),
				p -> {
					if (!aplicarRestricoesVisuais(p, view.getBtnNovo(), null, null))
						view.doDefaultCloseAction();
					view.getBtnSalvar().setVisible(p.contains(TipoPermissao.WRITE.name()));
				});
	}

//	public void prepararComoMaster() {
//		view.setMaster(true);
//		view.setStatus(StatusUsuario.ATIVO);
//
//		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
//			Empresa emp = new EmpresaService().buscarFornecedoraParaSetup();
//			if (emp == null)
//				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "CADASTRE A EMPRESA ANTES!");
//			Perfil perf = perfilService.buscarOuCriarPerfilMaster();
//			return new Object[] { emp, perf };
//		}, result -> {
//			view.setEmpresa(((Empresa) result[0]).getIdEmpresa(), ((Empresa) result[0]).getRazaoSocialEmpresa());
//			// carregarListaPerfis(()->{});
//			view.setPerfil((Perfil) result[1]);
//			marcarTodasPermissoesNaGrade();
//			view.setPermissoesHabilitadas(false);
//		});
//	}
//	
	public void prepararComoMaster() {
		view.setMaster(true);
		view.setStatus(StatusUsuario.ATIVO);

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			Empresa emp = new EmpresaService().buscarFornecedoraParaSetup();
			if (emp == null)
				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "CADASTRE A EMPRESA ANTES!");

			// 1. Cria ou busca o perfil no banco
			Perfil perf = perfilService.buscarOuCriarPerfilMaster();
			return new Object[] { emp, perf };
		}, result -> {
			Empresa emp = (Empresa) result[0];
			Perfil perf = (Perfil) result[1];

			view.setEmpresa(emp.getIdEmpresa(), emp.getRazaoSocialEmpresa());

			// 2. FORÇA a recarga da lista de perfis para garantir que o novo perfil apareça
			// no Combo
			carregarListaPerfis(() -> {
				// 3. Só agora seleciona o perfil, depois que ele já está carregado no ComboBox
				view.setPerfil(perf);
			});
			view.limparPermissoes();
			view.setCamposHabilitados(true); // Ativa Nome, Email, Senhas
			view.getBtnNovo().setEnabled(false); // Desativa o botão NOVO
			view.getBtnSalvar().setEnabled(true); // Ativa o botão SALVAR

			// marcarTodasPermissoesNaGrade(); // Marca tudo
			view.setPermissoesHabilitadas(false); // Trava os checks (Master não edita permissão manual)
			view.setMasterHabilitado(false);
			view.setPerfilHabilitado(false);
			view.setStatusHabilitado(false);
			view.setSenhaAntigaHabilitado(false);
			atualizarEstadoInterface();
		});
	}

	private void atualizarEstadoInterface() {
		boolean isNovo = (usuarioAtual == null || usuarioAtual.getIdUsuario() == null);
		view.setTextoBotaoSalvar(isNovo ? "SALVAR" : "ALTERAR");
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}
}
