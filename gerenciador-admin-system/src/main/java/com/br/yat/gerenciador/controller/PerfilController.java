package com.br.yat.gerenciador.controller;

import java.util.*;
import javax.swing.SwingUtilities;
import com.br.yat.gerenciador.model.*;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.util.*;
import com.br.yat.gerenciador.view.PerfilView;

public class PerfilController extends BaseController {
    private final PerfilView view;
    private final PerfilService service;
    private Perfil perfilAtual;
    private RefreshCallback refreshCallback;

    public PerfilController(PerfilView view, PerfilService service) {
        this.view = view;
        this.service = service;
        inicializar();
    }

    private void inicializar() {
        // Carrega todas as chaves de menu disponíveis para configurar o perfil
        view.construirGradePermissoes(MenuChaveGrouper.groupByCategoria());
        registrarAcoes();
        view.getBtnSalvar().setEnabled(false);
    }

    private void registrarAcoes() {
        view.getBtnNovo().addActionListener(e -> novoPerfil());
        view.getBtnSalvar().addActionListener(e -> salvar());
        view.getBtnCancelar().addActionListener(e -> view.dispose());

        // Lógica de "Marcar Todos" por categoria
        view.getChkTodosPorCategoria().forEach((cat, chk) -> {
            chk.addActionListener(e -> {
                List<MenuChave> chaves = view.getGruposPermissoes().get(cat);
                chaves.forEach(c -> {
                    view.setPermissao(c, "READ", chk.isSelected());
                    view.setPermissao(c, "WRITE", chk.isSelected());
                    view.setPermissao(c, "DELETE", chk.isSelected());
                });
            });
        });
    }

    public void carregarParaEdicao(Perfil perfil) {
        this.perfilAtual = perfil;
        
        // Trava o nome se for o perfil MASTER
        boolean isMaster = "MASTER".equalsIgnoreCase(perfil.getNome());
        view.setEdicaoNomeHabilitada(!isMaster);
        
        runAsync(SwingUtilities.getWindowAncestor(view),
            () -> service.listarPermissoesDoPerfil(perfil.getIdPerfil()),
            permissoes -> {
                view.limpar(); // Limpa as checkboxes
                view.setNome(perfil.getNome()); // Seta o nome após o limpar
                
                // Marca as checkboxes das permissões que o perfil já possui
                permissoes.forEach(p -> {
                    try {
                        view.setPermissao(MenuChave.valueOf(p.getChave()), p.getTipo(), true);
                    } catch (IllegalArgumentException e) {
                        // Ignora chaves que podem ter sido removidas do Enum
                    }
                });
                view.getBtnSalvar().setEnabled(true);
            }
        );
    }

    private void salvar() {
        if (view.getNome().trim().isEmpty()) {
            DialogFactory.aviso(view, "NOME DO PERFIL É OBRIGATÓRIO!");
            return;
        }

        if (perfilAtual == null) perfilAtual = new Perfil();
        perfilAtual.setNome(view.getNome());

        Map<MenuChave, List<String>> permissoes = coletarPermissoes();

        // Pegamos o usuário logado para atender ao requisito da sua Service
        Usuario logado = Sessao.getUsuario();

        runAsync(SwingUtilities.getWindowAncestor(view),
            () -> { 
                // Agora passando o executor (Sessao.getUsuario()) como pede a sua Service
                service.salvarPerfil(perfilAtual, permissoes, logado); 
                return true; 
            },
            sucesso -> {
                DialogFactory.informacao(view, "PERFIL SALVO COM SUCESSO!");
                novoPerfil(); // Limpa a tela para o próximo
                
                if (refreshCallback != null) refreshCallback.onSaveSuccess();
            }
        );
    }

    private Map<MenuChave, List<String>> coletarPermissoes() {
        Map<MenuChave, List<String>> mapa = new LinkedHashMap<>();
        view.getPermissoesGranulares().forEach((chave, tipos) -> {
            List<String> ativos = tipos.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey).toList();
            if (!ativos.isEmpty()) mapa.put(chave, ativos);
        });
        return mapa;
    }
    
    public void setRefreshCallback(RefreshCallback refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    private void novoPerfil() {
        perfilAtual = null;
        view.limpar();
        view.setEdicaoNomeHabilitada(true); 
        view.getBtnSalvar().setEnabled(true);
    }
}