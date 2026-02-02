package com.br.yat.gerenciador.controller;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.view.PermissaoConsultaView;

public class PermissaoConsultaController extends BaseController {
    private final PermissaoConsultaView view;
    private final UsuarioService service;

    public PermissaoConsultaController(PermissaoConsultaView view, UsuarioService service) {
        this.view = view;
        this.service = service;
        registrarAcoes();
    }

    private void registrarAcoes() {
        view.getCbPermissoes().addActionListener(e -> carregarUsuarios());
        view.getBtnFechar().addActionListener(e -> view.dispose());
    }

    private void carregarUsuarios() {
        MenuChave selecionada = (MenuChave) view.getCbPermissoes().getSelectedItem();
        if (selecionada == null) return;

        runAsync(null, () -> {
           return service.listarUsuariosPorPermissao(selecionada);
        }, lista -> view.getTableModel().setDados(lista));
    }
}
