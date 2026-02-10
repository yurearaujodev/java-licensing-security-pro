package com.br.yat.gerenciador.controller;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuChaveGrouper;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.view.UsuarioView;

public class UsuarioController extends BaseController {
	private final UsuarioView view;
	private final UsuarioService service;
	private Usuario usuarioAtual;
	private RefreshCallback refreshCallback;
	private final PerfilService perfilService;

	public UsuarioController(UsuarioView view, UsuarioService service, PerfilService perfilService) {
		this.view = view;
		this.service = service;
		this.perfilService = perfilService;
		inicializar();
	}

	private void inicializar() {
		Usuario logado = Sessao.getUsuario();

		Map<String, List<MenuChave>> grupos = Optional.ofNullable(logado)
				.map(u -> u.isMaster() ? MenuChaveGrouper.groupByCategoria()
						: MenuChaveGrouper.groupByCategoriaFiltrado(
								Optional.ofNullable(Sessao.getPermissoes()).orElse(List.of())))

				.orElse(MenuChaveGrouper.groupByCategoria());

		view.construirGradePermissoes(grupos);
		aplicarRestricoesDoExecutor();
		registrarAcoes();
		carregarEmpresaPadrao();
		carregarListaPerfis();
		view.desativarAtivar(false);
		view.getBtnNovo().setEnabled(true);
		view.getBtnSalvar().setEnabled(false);
		aplicarPermissoesDaTela();
	}

	public void setRefreshCallback(RefreshCallback callback) {
		this.refreshCallback = callback;
	}

	private void registrarAcoes() {
		view.getChkTodosPorCategoria().forEach((categoria, chk) -> {
			chk.addActionListener(e -> {
				List<MenuChave> chaves = view.getGruposPermissoes().get(categoria);
				boolean marcar = chk.isSelected();
				if (chaves != null) {
					chaves.forEach(chave -> {
						view.setPermissaoSelecionada(chave, "READ", marcar);
						view.setPermissaoSelecionada(chave, "WRITE", marcar);
						view.setPermissaoSelecionada(chave, "DELETE", marcar);
					});
				}
			});
		});

		view.getBtnSalvar().addActionListener(e -> salvarUsuario());
		view.getBtnCancelar().addActionListener(e -> view.doDefaultCloseAction());
		view.getBtnNovo().addActionListener(e -> novoUsuario());
		view.getTxtSenha().getDocument()
				.addDocumentListener(ValidationUtils.createDocumentListener(view.getTxtSenha(), () -> {
					String senha = new String(view.getTxtSenha().getPassword());
					int forca = ValidationUtils.calcularForcaSenha(senha);

					view.getBarraForcaSenha().setValue(forca);
					switch (forca) {
					case 0, 1 -> {
						view.getBarraForcaSenha().setForeground(Color.RED);
						view.getBarraForcaSenha().setString("Fraca");
					}
					case 2 -> {
						view.getBarraForcaSenha().setForeground(Color.ORANGE);
						view.getBarraForcaSenha().setString("Média");
					}
					case 3, 4 -> {
						view.getBarraForcaSenha().setForeground(new Color(0, 128, 0));
						view.getBarraForcaSenha().setString("Forte");
					}
					}
				}));
		view.getCbPerfil().addActionListener(e -> {
			Perfil selecionado = (Perfil) view.getCbPerfil().getSelectedItem();
			if (selecionado != null) {
				// Importante: Atualiza o ID interno da view para o getPerfil() não vir nulo
				view.setPerfil(selecionado);

				runAsync(SwingUtilities.getWindowAncestor(view),
						() -> perfilService.listarPermissoesDoPerfil(selecionado.getIdPerfil()), permissoesDoPerfil -> {
							view.desmarcarTodasPermissoes();
							for (Permissao p : permissoesDoPerfil) {
								try {
									view.setPermissaoSelecionada(MenuChave.valueOf(p.getChave()), p.getTipo(), true);
								} catch (Exception ex) {
									/* Chave obsoleta */ }
							}
							atualizarTodosCheckboxesCategoria();
						});
			}
		});
		view.getPermissoesGranulares().forEach((chave, mapaTipos) -> {
			mapaTipos.values().forEach(chk -> {
				chk.addActionListener(e -> {
					// Sempre que um individual mudar, reavaliamos o "Marcar Todos" daquela
					// categoria
					atualizarTodosCheckboxesCategoria();
				});
			});
		});
	}

	private void atualizarTodosCheckboxesCategoria() {
		view.getGruposPermissoes().forEach((cat, listaChaves) -> {
			boolean todasMarcadas = listaChaves.stream().allMatch(chave -> {
				var mapaTipos = view.getPermissoesGranulares().get(chave);
				return mapaTipos != null && mapaTipos.values().stream().allMatch(chk -> chk.isSelected());
			});
			view.atualizarStatusMarcarTodos(cat, todasMarcadas);
		});
	}
	
	private void aplicarRestricoesDoExecutor() {
	    Usuario executor = Sessao.getUsuario();
	    if (executor == null || executor.isMaster()) return; 

	    // Buscamos as permissões detalhadas do executor para saber quando as DELE expiram
	    runAsyncSilent(SwingUtilities.getWindowAncestor(view), 
	        () -> service.carregarPermissoesDetalhadas(executor.getIdUsuario()), 
	        minhasPermissoes -> {
	            
	            view.getPermissoesGranulares().forEach((chave, mapaTipos) -> {
	                // Se o executor não tem essa permissão, bloqueia o checkbox
	                boolean temAcesso = minhasPermissoes.stream()
	                        .anyMatch(p -> p.getChave().equals(chave.name()));

	                mapaTipos.forEach((tipo, chk) -> {
	                    if (!temAcesso) {
	                        chk.setEnabled(false);
	                        chk.setToolTipText("Você não possui este privilégio.");
	                    }
	                });

	                // DICA: Se a view tiver o campo de data, você pode setar o limite visual aqui
	                // view.setDataMaximaPermissao(chave, dataExpiraExecutor);
	            });
	        }
	    );
	}
	
//	private void aplicarRestricoesDoExecutor() {
//	    Usuario executor = Sessao.getUsuario();
//	    if (executor == null || executor.isMaster()) return; 
//
//	    // Permissões que o executor atual possui (Chaves de Menu)
//	    List<MenuChave> minhasChaves = Sessao.getPermissoes();
//
//	    view.getPermissoesGranulares().forEach((chave, mapaTipos) -> {
//	        // Se o executor não tem sequer acesso a esse Menu, ele não pode dar nenhuma permissão dele
//	        boolean temAcessoAoMenu = minhasChaves.contains(chave);
//	        
//	        mapaTipos.forEach((tipo, chk) -> {
//	            if (!temAcessoAoMenu) {
//	                chk.setEnabled(false);
//	                chk.setSelected(false);
//	                chk.setToolTipText("Você não possui privilégios sobre este menu.");
//	            }
//	            // Aqui você poderia adicionar lógica extra: 
//	            // ex: se o tipo for "DELETE" e o executor não for admin...
//	        });
//	    });
//	}

	private void carregarListaPerfis() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> perfilService.listarTodos(), perfis -> {
			// 1. A View limpa e popula o combo
			view.carregarCombosPerfil(perfis);

			if (perfis != null && !perfis.isEmpty()) {
				view.setPerfil(perfis.get(0));
			}
		});
	}

	public void novoUsuario() {
		this.usuarioAtual = null;
		view.limpar();
		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
		view.desativarAtivar(true);
		view.getTxtSenhaAntiga().setEnabled(false);
		view.setTextoBotaoSalvar("SALVAR");
		carregarEmpresaPadrao();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			boolean naoExisteMaster = service.buscarMasterUnico() == null;
			return naoExisteMaster; // Agora o Java sabe que T é Boolean
		}, podeSerMaster -> {
			view.getChkMaster().setEnabled(podeSerMaster);
			if (podeSerMaster) {
				view.setMaster(true);
			}
		});
	}

	public void carregarUsuarioParaEdicao(Usuario usuario) {
		this.usuarioAtual = usuario;
		view.limpar();
		view.setNome(usuario.getNome());
		view.setEmail(usuario.getEmail());
		view.setStatus(usuario.getStatus());
		view.setMaster(usuario.isMaster());

		view.setTextoBotaoSalvar("ALTERAR");
		if (usuario.getEmpresa() != null) {
			view.setEmpresa(usuario.getEmpresa().getIdEmpresa(), usuario.getEmpresa().getRazaoSocialEmpresa());
		}
		view.desativarAtivar(true);
		view.setMaster(usuario.isMaster());
		view.getChkMaster().setEnabled(false);
		view.getBtnNovo().setEnabled(false);
		view.getBtnSalvar().setEnabled(true);
		view.getTxtSenhaAntiga().setEnabled(true);
		view.bloquearStatus(!usuario.isMaster());
		if (usuario.getPerfil() != null) {
			view.setPerfil(usuario.getPerfil()); // Você já tem esse método na View!
		}
		// Carrega as permissões detalhadas (R, W, D) de forma assíncrona
		runAsync(SwingUtilities.getWindowAncestor(view),
				() -> service.carregarPermissoesDetalhadas(usuario.getIdUsuario()), permissoesDoBanco -> {
					view.desmarcarTodasPermissoes();
					permissoesDoBanco.forEach(p -> {
						try {
							view.setPermissaoSelecionada(MenuChave.valueOf(p.getChave()), p.getTipo(), true);
						} catch (IllegalArgumentException ex) {
							// Caso a chave no banco não exista mais no Enum
						}
					});

					// Double Validation / Regra de interface: bloqueia se o logado não puder editar
					// ou se o alvo for Master
					boolean podeEditar = service.podeEditarPermissoes(Sessao.getUsuario()) && !usuario.isMaster();
					view.bloquearGradePermissoes(podeEditar);

					// Atualiza os "Marcar Todos" baseado no que veio do banco
					view.getGruposPermissoes().forEach((cat, listaChaves) -> {
						boolean todasMarcadas = listaChaves.stream().allMatch(chave -> {
							var mapaTipos = view.getPermissoesGranulares().get(chave);
							return mapaTipos != null && mapaTipos.values().stream().allMatch(chk -> chk.isSelected());
						});
						view.atualizarStatusMarcarTodos(cat, todasMarcadas);
					});
				});
	}

	private void salvarUsuario() {
		if (view.getStatus() == StatusUsuario.SELECIONE) {
	        DialogFactory.aviso(view, "SELECIONE UM STATUS VÁLIDO!");
	        return;
	    }
		boolean isNovo = (usuarioAtual == null || usuarioAtual.getIdUsuario() == null);

		if (isNovo)
			usuarioAtual = new Usuario();

		usuarioAtual.setNome(view.getNome());
		usuarioAtual.setEmail(view.getEmail());
		char[] senha = view.getSenhaNova();
		char[] senhaAntiga = view.getSenhaAntiga();
		char[] senhaConfirmar = view.getConfirmarSenha();

		if (senha != null && senha.length > 0) {
			if (senhaConfirmar == null || !Arrays.equals(senha, senhaConfirmar)) {
				DialogFactory.erro(view, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
				return;
			}

			usuarioAtual.setSenhaHash(senha);
			usuarioAtual.setConfirmarSenha(senhaConfirmar);
			usuarioAtual.setSenhaAntiga(senhaAntiga);
		}
	

		usuarioAtual.setStatus(view.getStatus());

		usuarioAtual.setMaster(view.isMaster());

		Empresa emp = new Empresa();
		emp.setIdEmpresa(view.getEmpresa());
		usuarioAtual.setEmpresa(emp);
		Integer idPerfilSelecionado = view.getPerfil();
		if (idPerfilSelecionado == null) {
			DialogFactory.aviso(view, "POR FAVOR, SELECIONE UM PERFIL!");
			return;
		}
		Perfil perfilParaSalvar = new Perfil();
		perfilParaSalvar.setIdPerfil(idPerfilSelecionado);
		usuarioAtual.setPerfil(perfilParaSalvar);

		Map<MenuChave, List<String>> permissoes = coletarPermissoesMarcadas();
		Map<MenuChave, String> datasBrutas = view.getDatasExpiracaoTexto();
		Map<MenuChave, String> datasFiltradas = new LinkedHashMap<>();
		
		datasBrutas.forEach((chave, texto) -> {
	        // Se o campo só tiver máscara (ex: 0-9), ignoramos
	        String apenasNumeros = texto.replaceAll("[^0-9]", "").trim();
	        if (!apenasNumeros.isEmpty()) {
	            datasFiltradas.put(chave, texto);
	        }
	    });
		if (isNovo && (senha == null || senha.length == 0)) {
			DialogFactory.erro(view, "A SENHA É OBRIGATÓRIA PARA NOVOS USUÁRIOS.");
			return;
		}
		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			service.salvarUsuario(usuarioAtual, permissoes,datasFiltradas, Sessao.getUsuario());
			return true;
		}, sucesso -> {
			DialogFactory.informacao(view, isNovo ? "USUÁRIO SALVO COM SUCESSO!" : "ALTERAÇÃO REALIZADA COM SUCESSO!");
			if (isNovo) {
				view.getBtnNovo().setEnabled(true);
				view.desativarAtivar(false);
				view.limpar();
				view.getBtnSalvar().setEnabled(false);
			} else {
				SwingUtilities.invokeLater(() -> view.doDefaultCloseAction());
			}

			if (refreshCallback != null)
				refreshCallback.onSaveSuccess();
		});
	}
	
	private void aplicarPermissoesDaTela() {
		Usuario logado = Sessao.getUsuario();
		// 1. SE NÃO HÁ NINGUÉM LOGADO, É PRIMEIRO ACESSO: Liberamos a tela e saímos.
		if (logado == null) {
	        view.getBtnNovo().setVisible(true);
	        view.getBtnSalvar().setVisible(true);
	        return; 
	    }
		
	    // Bypass para Master: ele sempre pode ver e escrever
	    if (logado != null && logado.isMaster()) {
	        view.getBtnNovo().setVisible(true);
	        view.getBtnSalvar().setVisible(true);
	        return;
	    }
	    // Buscamos as permissões específicas que o logado tem para ESTA tela de usuários
	    List<String> minhasPermissoes = service.listarPermissoesAtivasPorMenu(
	        Sessao.getUsuario().getIdUsuario(), 
	        MenuChave.CADASTROS_USUARIO
	    );

	    // Usa o método da sua BaseController
	    boolean podeVer = aplicarRestricoesVisuais(
	        minhasPermissoes, 
	        view.getBtnNovo(), 
	        null, // No seu caso, o Salvar faz o papel de editar
	        null 
	    );
	    
	    // O botão salvar segue a lógica de escrita
	    view.getBtnSalvar().setVisible(minhasPermissoes.contains("WRITE"));

	    if (!podeVer) {
	        DialogFactory.aviso(view, "VOCÊ NÃO TEM PERMISSÃO DE LEITURA NESTA TELA.");
	        view.doDefaultCloseAction();
	    }
	}

	public void prepararComoMaster() {
		view.setMaster(true);
		view.getChkMaster().setEnabled(false);
		view.setStatus(StatusUsuario.ATIVO);
		view.bloquearStatus(true);
		view.setTitle("CADASTRO INICIAL - ADMINISTRADOR MASTER");

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			// USANDO O NOVO MÉTODO QUE NÃO EXIGE EXECUTOR
			EmpresaService empresaService = new EmpresaService();
			Empresa emp = empresaService.buscarFornecedoraParaSetup();

			if (emp == null) {
	            throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "CADASTRE A EMPRESA ANTES DO USUÁRIO!");
	        }
			// Perfil master (a service de perfil deve permitir null no executor
			// ou ter lógica similar se for o primeiro)
			Perfil perf = perfilService.buscarOuCriarPerfilMaster();

			return new Object[] { emp, perf };
		}, result -> {
			Empresa emp = (Empresa) result[0];
			Perfil perf = (Perfil) result[1];

			// Chama os novos métodos que criamos na View
			view.setEmpresa(emp.getIdEmpresa(), emp.getRazaoSocialEmpresa());
			view.setPerfil(perf);
			
			marcarTodasPermissoesNaGrade();
		});
	}

	private void marcarTodasPermissoesNaGrade() {
		view.getPermissoesGranulares().values().forEach(mapaTipos -> {
			mapaTipos.values().forEach(chk -> chk.setSelected(true));
		});
		view.getChkTodosPorCategoria().values().forEach(chk -> chk.setSelected(true));
	}

	private Map<MenuChave, List<String>> coletarPermissoesMarcadas() {
		Map<MenuChave, List<String>> selecionadas = new LinkedHashMap<>();

		view.getPermissoesGranulares().forEach((chave, tiposMap) -> {
			List<String> tiposAtivos = tiposMap.entrySet().stream().filter(e -> e.getValue().isSelected())
					.map(Map.Entry::getKey).toList();

			if (!tiposAtivos.isEmpty()) {
				selecionadas.put(chave, tiposAtivos);
			}
		});
		return selecionadas;
	}

	private void carregarEmpresaPadrao() {
		runAsync(SwingUtilities.getWindowAncestor(view), () -> service.buscarEmpresaFornecedora(), empresa -> {
			if (empresa != null) {
				SwingUtilities.invokeLater(() -> {
					view.setEmpresa(empresa.getIdEmpresa(), empresa.getRazaoSocialEmpresa());
				});
			}
		});
	}

}
