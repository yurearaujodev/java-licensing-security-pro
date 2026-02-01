package com.br.yat.gerenciador.controller;

import java.util.List;
import javax.swing.SwingUtilities;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class LoginController extends BaseController {
    private final UsuarioViewLogin view;
    private final UsuarioService service;

    public LoginController(UsuarioViewLogin view) {
        this.view = view;
        this.service = new UsuarioService(); 
        registrarAcoes();
    }

    private void registrarAcoes() {
        view.getBtnEntrar().addActionListener(e -> {
            String email = view.getEmail();
            char[] senha = view.getSenha();

            // Roda o processo pesado (BCrypt/Banco) na Virtual Thread da BaseController
            runAsync(SwingUtilities.getWindowAncestor(view), () -> {
                Usuario user = service.autenticar(email, senha);
                List<MenuChave> permissoes = service.carregarPermissoesAtivas(user.getIdUsuario());
                return new LoginData(user, permissoes);
            }, data -> {
                // Sucesso: popula a sessão e fecha a janela de login
                Sessao.login(data.user(), data.permissoes());
                ViewFactory.atualizarAcoesMenuPrincipal();
                view.dispose();
            });
        });
    }

    private record LoginData(Usuario user, List<MenuChave> permissoes) {}
}
