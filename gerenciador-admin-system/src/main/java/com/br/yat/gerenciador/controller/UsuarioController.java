	package com.br.yat.gerenciador.controller;
	
	import java.awt.Color;
	import java.util.Arrays;
	import java.util.LinkedHashMap;
	import java.util.List;
	import java.util.Map;
	import java.util.Optional;
	import java.util.ArrayList;
	
	import javax.swing.JCheckBox;
	import javax.swing.SwingUtilities;
	
	import com.br.yat.gerenciador.exception.ValidationException;
	import com.br.yat.gerenciador.model.Empresa;
	import com.br.yat.gerenciador.model.Perfil;
	import com.br.yat.gerenciador.model.Sessao;
	import com.br.yat.gerenciador.model.Usuario;
	import com.br.yat.gerenciador.model.enums.MenuChave;
	import com.br.yat.gerenciador.model.enums.StatusUsuario;
	import com.br.yat.gerenciador.model.enums.TipoPermissao;
	import com.br.yat.gerenciador.model.enums.ValidationErrorType;
	import com.br.yat.gerenciador.security.SensitiveData;
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
		private final PerfilService perfilService;
		private Usuario usuarioAtual;
		private RefreshCallback refreshCallback;
	
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
	
			registrarAcoes();
			carregarEmpresaPadrao();
			carregarListaPerfis(null);
	
			view.getBtnNovo().setEnabled(true);
			view.getBtnSalvar().setEnabled(false);
			configurarMarcarTodosPorCategoria();
			aplicarRestricoesDoExecutor();
			aplicarPermissoesDaTela();
		}
	
		private void registrarAcoes() {
			view.getBtnSalvar().addActionListener(e -> salvarUsuario());
			view.getBtnCancelar().addActionListener(e -> {
				view.getBtnNovo().setEnabled(true);
				view.getBtnSalvar().setEnabled(false);
				view.doDefaultCloseAction();
			});
			view.getBtnNovo().addActionListener(e -> novoUsuario());
	
			// Monitor de força da senha usando o JPasswordField exposto pelo getter da View
			view.getCampoSenhaNova().getDocument()
					.addDocumentListener(ValidationUtils.createDocumentListener(view.getCampoSenhaNova(), () -> {
						char[] senhaChars = view.getSenhaNova();
						try {
							int forca = ValidationUtils.calcularForcaSenha(senhaChars);
							view.getBarraForcaSenha().setValue(forca);
							atualizarFeedbackForcaSenha(forca);
						} finally {
							SensitiveData.safeClear(senhaChars);
						}
					}));
		}
	
		private void atualizarFeedbackForcaSenha(int forca) {
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
		}
		
		private void sincronizarCategoria(MenuChave chaveAlterada) {
	
		    for (String categoria : view.getPainelPermissoes().getCategorias()) {
	
		        List<MenuChave> chaves = view.getPainelPermissoes()
		                                     .getChavesDaCategoria(categoria);
	
		        if (chaves == null || !chaves.contains(chaveAlterada)) {
		            continue;
		        }
	
		        boolean todosMarcados = true;
	
		        for (MenuChave chave : chaves) {
	
		            for (TipoPermissao tipo : TipoPermissao.values()) {
	
		                JCheckBox chk = view.getPainelPermissoes()
		                                    .getCheckBox(chave, tipo);
	
		                if (chk != null && chk.isEnabled() && !chk.isSelected()) {
		                    todosMarcados = false;
		                    break;
		                }
		            }
		        }
	
		        JCheckBox chkCategoria =
		                view.getPainelPermissoes()
		                    .getCheckBoxCategoria(categoria);
	
		        if (chkCategoria != null) {
		            chkCategoria.setSelected(todosMarcados);
		        }
		    }
		}
	
	
	
		private void salvarUsuario() {
		    if (view.getStatus() == StatusUsuario.SELECIONE) {
		        DialogFactory.aviso(view, "SELECIONE UM STATUS VÁLIDO!");
		        return;
		    }

		    boolean isNovo = (usuarioAtual == null || usuarioAtual.getIdUsuario() == null);
		    if (isNovo) usuarioAtual = new Usuario();

		    usuarioAtual.setNome(view.getNome());
		    usuarioAtual.setEmail(view.getEmail());
		    usuarioAtual.setStatus(view.getStatus());
		    usuarioAtual.setMaster(view.isMaster());

		    Empresa emp = new Empresa();
		    emp.setIdEmpresa(view.getIdEmpresa());
		    usuarioAtual.setEmpresa(emp);

		    Perfil perfilSel = view.getPerfilSelecionado();
		    if (perfilSel == null) {
		        DialogFactory.aviso(view, "POR FAVOR, SELECIONE UM PERFIL!");
		        return;
		    }
		    usuarioAtual.setPerfil(perfilSel);

		    char[] senha = view.getSenhaNova();
		    char[] senhaConfirmar = view.getConfirmarSenha();

		    if (senha.length > 0) {
		        if (!Arrays.equals(senha, senhaConfirmar)) {
		            DialogFactory.erro(view, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
		            SensitiveData.safeClear(senha);
		            SensitiveData.safeClear(senhaConfirmar);
		            view.limparSenhas();
		            return;
		        }

		        int forca = ValidationUtils.calcularForcaSenha(senha);
		        if (forca < 2) { // mínima força = média
		            DialogFactory.aviso(view, "A SENHA DEVE TER FORÇA MÍNIMA MÉDIA.");
		            SensitiveData.safeClear(senha);
		            SensitiveData.safeClear(senhaConfirmar);
		            view.limparSenhas();
		            return;
		        }

		        usuarioAtual.setSenhaHash(senha);
		        usuarioAtual.setConfirmarSenha(senhaConfirmar);
		        usuarioAtual.setSenhaAntiga(view.getSenhaAntiga());
		    } else if (isNovo) {
		        DialogFactory.erro(view, "A SENHA É OBRIGATÓRIA PARA NOVOS USUÁRIOS.");
		        return;
		    }

		    Map<MenuChave, List<String>> permissoes = coletarPermissoesMarcadas();
		    Map<MenuChave, String> datasExpiracao = coletarDatasExpiracao();

		    runAsync(SwingUtilities.getWindowAncestor(view), () -> {
		        service.salvarUsuario(usuarioAtual, permissoes, datasExpiracao, Sessao.getUsuario());
		        return true;
		    }, sucesso -> {
		        try {
		            DialogFactory.informacao(view,
		                    isNovo ? "USUÁRIO SALVO COM SUCESSO!" : "ALTERAÇÃO REALIZADA COM SUCESSO!");
		            if (isNovo) {
		                view.limpar();
		                view.getBtnNovo().setEnabled(true);
		                view.getBtnSalvar().setEnabled(false);
		            } else {
		                view.doDefaultCloseAction();
		            }
		        } finally {
		            view.limparSenhas();
		        }
		        if (refreshCallback != null) refreshCallback.onSaveSuccess();
		    });
		}

	
		private Map<MenuChave, List<String>> coletarPermissoesMarcadas() {
	
			Map<MenuChave, List<String>> selecionadas = new LinkedHashMap<>();
	
			for (MenuChave chave : view.getChavesAtivas()) {
	
				List<String> tipos = new ArrayList<>();
	
				for (TipoPermissao tipo : TipoPermissao.values()) {
	
					if (view.isPermissaoSelecionada(chave, tipo)) {
						tipos.add(tipo.name());
					}
				}
	
				if (!tipos.isEmpty()) {
					selecionadas.put(chave, tipos);
				}
			}
	
			return selecionadas;
		}
	
		private Map<MenuChave, String> coletarDatasExpiracao() {
			Map<MenuChave, String> filtradas = new LinkedHashMap<>();
			for (MenuChave chave : MenuChave.values()) {
				// PnlPermissoes expõe getDataExpiracao(MenuChave)
				String data = view.getDataExpiracao(chave);
				if (data != null && !data.replaceAll("[^0-9]", "").trim().isEmpty()) {
					filtradas.put(chave, data);
				}
			}
			return filtradas;
		}
	
		public void carregarUsuarioParaEdicao(Usuario usuario) {
			this.usuarioAtual = usuario;
			view.limpar();
			view.setNome(usuario.getNome());
			view.setEmail(usuario.getEmail());
			view.setStatus(usuario.getStatus());
			view.setMaster(usuario.isMaster());
	
			if (usuario.getEmpresa() != null) {
				view.setEmpresa(usuario.getEmpresa().getIdEmpresa(), usuario.getEmpresa().getRazaoSocialEmpresa());
			}
	
			view.getBtnNovo().setEnabled(false);
			view.getBtnSalvar().setEnabled(true);
	
			carregarListaPerfis(() -> {
				if (usuario.getPerfil() != null)
					view.setPerfil(usuario.getPerfil());
			});
	
			runAsync(SwingUtilities.getWindowAncestor(view),
			        () -> service.carregarPermissoesEspeciais(usuario.getIdUsuario()),
			        permissoesEspeciais -> {
			            if (permissoesEspeciais != null) {
			                permissoesEspeciais.forEach(p -> {
			                    try {
			                        MenuChave chave = MenuChave.valueOf(p.getChave());
			                        TipoPermissao tipo = TipoPermissao.valueOf(p.getTipo());
			                        view.setPermissao(chave, tipo, true);
			                    } catch (IllegalArgumentException ex) {
			                        System.err.println("Permissão inválida do DB: " + p.getChave() + "/" + p.getTipo());
			                    }
			                });
			            }
			            for (MenuChave chave : view.getChavesAtivas()) sincronizarCategoria(chave);
			            view.setPermissoesHabilitadas(!usuario.isMaster());
			        });

		}
		
					
	
		public void prepararComoMaster() {
			view.setMaster(true);
			view.getStatus(); // Apenas exemplo, setaremos ativo abaixo
			view.setStatus(StatusUsuario.ATIVO);
	
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				Empresa emp = new EmpresaService().buscarFornecedoraParaSetup();
				if (emp == null)
					throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "CADASTRE A EMPRESA ANTES!");
				Perfil perf = perfilService.buscarOuCriarPerfilMaster();
				return new Object[] { emp, perf };
			}, result -> {
				Empresa emp = (Empresa) result[0];
				Perfil perf = (Perfil) result[1];
				view.setEmpresa(emp.getIdEmpresa(), emp.getRazaoSocialEmpresa());
				view.setPerfil(perf);
				marcarTodasPermissoesNaGrade();
			});
		}
	
		private void configurarMarcarTodosPorCategoria() {
		    for (String categoria : view.getPainelPermissoes().getCategorias()) {
		        JCheckBox chkCategoria = view.getPainelPermissoes().getCheckBoxCategoria(categoria);
		        if (chkCategoria == null) continue;

		        // Remove todos os listeners antigos para evitar múltiplas chamadas
		        for (java.awt.event.ActionListener l : chkCategoria.getActionListeners()) {
		            chkCategoria.removeActionListener(l);
		        }

		        // Listener único com flag de bloqueio
		        chkCategoria.addActionListener(new java.awt.event.ActionListener() {
		            private boolean bloqueado = false;

		            @Override
		            public void actionPerformed(java.awt.event.ActionEvent e) {
		                if (bloqueado) return;
		                bloqueado = true;
		                try {
		                    boolean marcado = chkCategoria.isSelected();
		                    List<MenuChave> chaves = view.getPainelPermissoes().getChavesDaCategoria(categoria);
		                    if (chaves == null) return;

		                    for (MenuChave chave : chaves) {
		                        for (TipoPermissao tipo : TipoPermissao.values()) {
		                            JCheckBox chk = view.getPainelPermissoes().getCheckBox(chave, tipo);
		                            if (chk != null && chk.isEnabled()) chk.setSelected(marcado);
		                        }
		                        sincronizarCategoria(chave);
		                    }
		                } finally {
		                    bloqueado = false;
		                }
		            }
		        });
		    }
		}

	
		private void configurarMarcarTodosPorCategoriaCallback(java.awt.event.ActionEvent e) {
		    JCheckBox chkCategoria = (JCheckBox) e.getSource();
		    String categoria = (String) chkCategoria.getClientProperty("categoria"); // ou passar de outra forma
		    boolean marcado = chkCategoria.isSelected();
		    List<MenuChave> chaves = view.getPainelPermissoes().getChavesDaCategoria(categoria);
		    if (chaves == null) return;

		    for (MenuChave chave : chaves) {
		        for (TipoPermissao tipo : TipoPermissao.values()) {
		            JCheckBox chk = view.getPainelPermissoes().getCheckBox(chave, tipo);
		            if (chk != null && chk.isEnabled()) chk.setSelected(marcado);
		        }
		        sincronizarCategoria(chave);
		    }
		}

		
		
		private void marcarTodasPermissoesNaGrade() {
		    for (MenuChave chave : MenuChave.values()) {
		        for (TipoPermissao tipo : TipoPermissao.values()) {
		            view.setPermissao(chave, tipo, true);
		        }
		    }
		}
	
		private void aplicarRestricoesDoExecutor() {
	
		    Usuario executor = Sessao.getUsuario();
	
		    if (executor == null || executor.isMaster()) {
		        return;
		    }
	
		    runAsyncSilent(
		        SwingUtilities.getWindowAncestor(view),
		        () -> service.carregarPermissoesDetalhadas(executor.getIdUsuario()),
		        minhasPermissoes -> {
	
		            for (MenuChave chave : view.getChavesAtivas()) {
	
		                for (TipoPermissao tipo : TipoPermissao.values()) {
	
		                    boolean permitido = minhasPermissoes.stream()
		                            .anyMatch(p ->
		                                    p.getChave().equals(chave.name()) &&
		                                    p.getTipo().equals(tipo.name())
		                            );
	
		                    JCheckBox chk = view.getPainelPermissoes()
		                                        .getCheckBox(chave, tipo);
	
		                    if (chk != null) {
		                        chk.setEnabled(permitido);
	
		                        if (!permitido) {
		                            chk.setSelected(false);
		                            chk.setToolTipText("Você não possui essa permissão.");
		                        } else {
		                            chk.setToolTipText(null);
		                        }
		                    }
		                }
		            }
		        }
		    );
		}
	
	
		public void novoUsuario() {
			this.usuarioAtual = null;
			view.limpar();
			view.getBtnNovo().setEnabled(false);
			view.getBtnSalvar().setEnabled(true);
			carregarEmpresaPadrao();
	
			runAsync(SwingUtilities.getWindowAncestor(view), () -> service.buscarMasterUnico(), masterExistente -> {
				boolean podeSerMaster = masterExistente == null;
				view.setMasterHabilitado(podeSerMaster);
				if (podeSerMaster) {
					view.setMaster(true);
				}
			});
	
		}
	
		private void carregarListaPerfis(Runnable callback) {
			runAsync(SwingUtilities.getWindowAncestor(view),
			         () -> perfilService.listarTodos(),
			         perfis -> {
			             view.carregarCombosPerfil(perfis);
			             if (perfis != null && !perfis.isEmpty()) {
			                 if (callback == null)
			                     view.setPerfil(perfis.get(0));
			                 else
			                     callback.run();
			             } else {
			                 System.err.println("Aviso: Nenhum perfil encontrado para carregar.");
			             }
			         });


		}
	
		private void carregarEmpresaPadrao() {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> service.buscarEmpresaFornecedora(), emp -> {
				if (emp != null)
					view.setEmpresa(emp.getIdEmpresa(), emp.getRazaoSocialEmpresa());
			});
		}
	
		private void aplicarPermissoesDaTela() {
	
			Usuario logado = Sessao.getUsuario();
			if (logado == null || logado.isMaster())
				return;
	
			runAsync(SwingUtilities.getWindowAncestor(view),
	
					// 🔹 Executa em thread virtual
					() -> service.listarPermissoesAtivasPorMenu(logado.getIdUsuario(), MenuChave.CADASTROS_USUARIO),
	
					// 🔹 Volta para EDT automaticamente
					permissoes -> {
	
						if (!aplicarRestricoesVisuais(permissoes, view.getBtnNovo(), null, null)) {
							DialogFactory.aviso(view, "SEM PERMISSÃO DE LEITURA.");
							view.doDefaultCloseAction();
							return;
						}
	
						view.getBtnSalvar().setVisible(permissoes.contains("WRITE"));
	
					});
		}
	
		public void setRefreshCallback(RefreshCallback callback) {
			this.refreshCallback = callback;
		}
	}