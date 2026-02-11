package com.br.yat.gerenciador.controller;

import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.UsuarioViewTrocaSenha;

public class TrocaSenhaObrigatoriaController extends BaseController {

    private final UsuarioViewTrocaSenha view;
    private final AutenticacaoService authService;
    private final Usuario usuarioAlvo;
    private final Runnable onSuccess;

    public TrocaSenhaObrigatoriaController(UsuarioViewTrocaSenha view, Usuario usuario,AutenticacaoService authService, Runnable onSuccess) {
        this.view = view;
        this.usuarioAlvo = usuario;
        this.onSuccess = onSuccess;
        this.authService = authService;
        init();
    }

    private void init() {
        view.getBtnSalvar().addActionListener(e -> salvar());
    }

    private void salvar() {
        char[] nova = view.getNovaSenha();
        char[] confirma = view.getConfirmaSenha();

        runAsync(view, () -> {
            // Double Validation: A service valida complexidade e igualdade
            authService.alterarSenhaObrigatoria(usuarioAlvo.getIdUsuario(), nova, confirma);
            return null;
        }, result -> {
            DialogFactory.informacao(view, "Senha atualizada com sucesso!");
            view.dispose();
            onSuccess.run(); // Executa o finalizarLogin lá do LoginController
        });
    }
}
