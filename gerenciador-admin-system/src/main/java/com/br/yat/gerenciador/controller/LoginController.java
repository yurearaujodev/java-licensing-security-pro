package com.br.yat.gerenciador.controller;

import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.dto.LoginDTO;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.service.LogSistemaService;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class LoginController extends BaseController {
    private final UsuarioViewLogin view;
    private final UsuarioService userService;
    private final AutenticacaoService authService;
  //  private Timer monitorInatividade;

    public LoginController(UsuarioViewLogin view,AutenticacaoService authService,UsuarioService userService) {
        this.view = view;
        this.userService = userService;
        this.authService = authService;
        registrarAcoes();
        SwingUtilities.invokeLater(view::focarEmail);
    }

    private void registrarAcoes() {
        view.getBtnEntrar().addActionListener(e -> {
            String email = view.getEmail();
            char[] senha = view.getSenha();

            // Limpa o campo na tela por segurança, mas temos a referência na variável 'senha'
            view.limpar();

            runAsync(SwingUtilities.getWindowAncestor(view), () -> {
                // 1. Autentica
                Usuario user = authService.autenticar(email, senha);
                
                // 2. Carrega permissões
                List<MenuChave> permissoes = userService.carregarPermissoesAtivas(user.getIdUsuario());
                return new LoginDTO(user, permissoes);
            }, data -> {
                Usuario user = data.user();
                
                // --- INTERCEPTAÇÃO PARA TROCA DE SENHA OBRIGATÓRIA ---
                if (user.isForcarResetSenha()) {
                    // Abrimos a tela de troca. Só chamamos o finalizarLogin se ele completar a troca.
                    ViewFactory.abrirTrocaSenhaObrigatoria(user, () -> {
                        finalizarLogin(data);
                    });
                    view.dispose(); 
                    return;
                }

                // Fluxo normal de login
                finalizarLogin(data);
            });
            view.limpar();
        });

        view.getBtnEsqueciSenha().addActionListener(e -> {
            DialogFactory.informacao(view,
                    "POR FAVOR, ENTRE EM CONTATO COM O ADMINISTRADOR DO SISTEMA PARA SOLICITAR O RESET DE SUA SENHA.");
        });
    }

    /**
     * Centraliza a finalização do login (Sessão, Logs e Menu).
     * Usado tanto no login direto quanto após a troca de senha obrigatória.
     */
    private void finalizarLogin(LoginDTO data) {
    	ParametroSistemaService ps = new ParametroSistemaService();
    	int tempoSessao = ps.getInt(ParametroChave.TEMPO_SESSAO_MIN, 30);
        
        Sessao.login(data.user(), data.permissoes(), tempoSessao);
        if (ViewFactory.getMainController() != null) {
            ViewFactory.getMainController().iniciarMonitorSessao();
        }
        // Double Validation: Verificação de privilégio para tarefas de infra
        if (UsuarioPolicy.isPrivilegiado(data.user())) {
            runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
                new LogSistemaService().executarLimpezaAutomatica(data.user());
                return null;
            }, result -> { });
        }
   //     iniciarMonitorInatividade();
        ViewFactory.atualizarAcoesMenuPrincipal();
        // Garante o fechamento da tela de login se ela ainda existir
        if (view != null) {
            view.dispose();
        }
    }
//    private void iniciarMonitorInatividade() {
//        Timer timer = new Timer(60000, e -> {
//            // Double Validation: Checa se a sessão expirou conforme o parâmetro do banco
//            if (Sessao.isExpirada()) {
//                ((Timer)e.getSource()).stop();
//                
//                SwingUtilities.invokeLater(() -> {
//                    DialogFactory.aviso(null, "Sua sessão expirou por inatividade. Por favor, faça login novamente.");
//                    
//                    // Recupera a instância da MainController via Factory para deslogar
//                    if (ViewFactory.getMainController() != null) {
//                        ViewFactory.getMainController().executarLogout(false);
//                    }
//                });
//            }
//        });
//        timer.start();
//    }
}