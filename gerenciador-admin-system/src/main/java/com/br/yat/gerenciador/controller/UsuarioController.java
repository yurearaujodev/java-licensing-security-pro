package com.br.yat.gerenciador.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuChaveGrouper;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;

public class UsuarioController extends BaseController {
	private final UsuarioView view;
	private final UsuarioService service;
	private Usuario usuarioAtual; // Armazena o usuário que está sendo editado

	public UsuarioController(UsuarioView view) {
        this.view = view;
        this.service = new UsuarioService();
        
        // 1. O Controller busca os grupos (Lógica de Negócio/Configuração)
        Map<String, List<MenuChave>> grupos = MenuChaveGrouper.groupByCategoria();
        
        // 2. O Controller manda a View se desenhar com esses dados
        this.view.construirGradePermissoes(grupos);
        
        // 3. Registra as ações após a View estar montada
        registrarAcoes();
        carregarEmpresaPadrao();
    }

    private void registrarAcoes() {
        // Agora que a View construiu os componentes via construirGradePermissoes, 
        // os listeners abaixo funcionarão corretamente.
        view.getChkTodosPorCategoria().forEach((categoria, chkTodos) -> {
            chkTodos.addActionListener(e -> {
                view.marcarTodosDaCategoria(categoria, chkTodos.isSelected());
            });
        });

        view.getBtnNovo().addActionListener(e -> novoUsuario());
        view.getBtnCancelar().addActionListener(e -> cancelar());
        view.getBtnSalvar().addActionListener(e -> salvarUsuario());
    }
	// Adicione estes métodos à sua UsuarioController existente

	// Mude de private para public para a ViewFactory enxergar
	public void vincularAcoesConsulta(UsuarioConsultaView consultaView) {
	    // Ação de Buscar
	    consultaView.getBtnPesquisar().addActionListener(e -> pesquisarUsuarios(consultaView));

	    // Ação de Novo (limpa o cadastro e traz para frente)
	    consultaView.getBtnNovo().addActionListener(e -> {
	        novoUsuario();
	        // Se a view de cadastro estiver no desktop, trazemos ela para o foco
	        DesktopUtils.showFrame(consultaView.getDesktopPane(), view);
	        view.toFront();
	    });

	    // Ação de Editar
	    consultaView.getBtnEditar().addActionListener(e -> {
	        Usuario sel = consultaView.getSelecionado();
	        if (sel != null) {
	            carregarUsuarioParaEdicao(sel);
	            DesktopUtils.showFrame(consultaView.getDesktopPane(), view);
	            view.toFront();
	        } else {
	            DialogFactory.aviso(consultaView, "SELECIONE UM USUÁRIO NA TABELA.");
	        }
	    });
	}

	private void pesquisarUsuarios(UsuarioConsultaView consultaView) {
	    String termo = consultaView.getTextoPesquisa();
	    // Seguindo o seu padrão runAsync
	    runAsync(null, () -> {
	        return service.listarUsuarios(termo); 
	    }, lista -> {
	        consultaView.setDados(lista);
	    });
	}
	
	private void novoUsuario() {
	    view.limpar();
	    this.usuarioAtual = null; // IMPORTANTE garantir isso para o próximo save ser um INSERT
	    view.desativarAtivar(true);
	    carregarEmpresaPadrao();
	}

	private void cancelar() {
		view.limpar();
		view.desativarAtivar(false);
	}
	
	public void carregarUsuarioParaEdicao(Usuario usuario) {
	    this.usuarioAtual = usuario;
	    view.limpar();
	    
	    view.setNome(usuario.getNome());
	    view.setEmail(usuario.getEmail());
	    view.setStatus(usuario.getStatus());
	    if (usuario.getIdEmpresa() != null) {
	        view.setEmpresa(usuario.getIdEmpresa().getIdEmpresa(), 
	                         usuario.getIdEmpresa().getRazaoSocialEmpresa());
	    }
	    view.desativarAtivar(true);

	    runAsync(null, () -> {
	        return service.carregarPermissoesAtivas(usuario.getIdUsuario());
	    }, chavesDoBanco -> {
	        // O CONTROLLER agora executa a lógica
	        view.desmarcarTodasPermissoes();
	        
	        // 1. Marca as permissões individuais
	        for (MenuChave chave : chavesDoBanco) {
	            view.setPermissaoSelecionada(chave, true);
	        }

	        // 2. Lógica visual: Verifica se deve marcar o "Todos" de cada categoria
	        Map<String, List<MenuChave>> grupos = view.getGruposPermissoes();
	        for (Map.Entry<String, List<MenuChave>> entry : grupos.entrySet()) {
	            String categoria = entry.getKey();
	            List<MenuChave> chavesDaCategoria = entry.getValue();
	            
	            // Se todas as chaves dessa categoria estão na lista que veio do banco...
	            boolean todasMarcadas = chavesDoBanco.containsAll(chavesDaCategoria);
	            view.atualizarStatusMarcarTodos(categoria, todasMarcadas);
	        }
	    });
	}

	public void salvarUsuario() {
	    if (usuarioAtual == null) usuarioAtual = new Usuario();
	    
	    usuarioAtual.setNome(view.getNome());
	    usuarioAtual.setEmail(view.getEmail());
	    usuarioAtual.setSenhaHash(view.getSenha());
	    usuarioAtual.setStatus(view.getStatus());
	    
	    Empresa emp = new Empresa();
	    emp.setIdEmpresa(view.getEmpresa()); 
	    usuarioAtual.setIdEmpresa(emp);
	    List<MenuChave> permissoes = coletarPermissoesMarcadas();
	    Usuario logado = Sessao.getUsuario();

	    // Correção: SwingUtilities busca o JFrame pai da View para o LoadingDialog
	    runAsync(SwingUtilities.getWindowAncestor(view), () -> {
	        service.salvarUsuarioCompleto(usuarioAtual, permissoes, logado);
	        return Boolean.TRUE; // Correção: Retorno explícito de Boolean
	        
	    }, sucesso -> {
	        DialogFactory.informacao(view, "USUÁRIO SALVO COM SUCESSO!");
	        if (Sessao.getUsuario()==null) {
				view.dispose();
			}else {
				cancelar();
			}	        
	    });
	}

    private List<MenuChave> coletarPermissoesMarcadas() {
        List<MenuChave> marcadas = new ArrayList<>();
        view.getPermissoes().forEach((chave, chk) -> {
            if (chk.isSelected()) marcadas.add(chave);
        });
        return marcadas;
    }
    private void carregarEmpresaPadrao() {
        // A Controller apenas coordena a chamada
        runAsyncSilent(null, () -> {
            return service.buscarEmpresaFornecedora(); // Chama a Service aqui!
        }, empresa -> {
            if (empresa != null) {
                // idEmpresaSelecionada é o campo que você deve criar na View (conforme dica anterior)
                view.setEmpresa(empresa.getIdEmpresa(), empresa.getRazaoSocialEmpresa());
            }
        });
    }
}
