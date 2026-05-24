package com.br.yat.gerenciador.service;

import java.awt.AWTEvent;
import java.awt.Toolkit;

import javax.swing.JInternalFrame;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class SessaoService {

    private static final Logger logger =
            LoggerFactory.getLogger(SessaoService.class);

    private final MenuPrincipal view;

    private Timer monitorInatividade;

    private boolean processandoLogout = false;

    public SessaoService(MenuPrincipal view) {
        this.view = view;
    }

    public void configurarMonitorGlobal() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (Sessao.getUsuario() != null) {
                Sessao.registrarAtividade();
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    public void iniciarMonitorSessao() {

        pararMonitorSessao();

        monitorInatividade = new Timer(60000, e -> {

            if (Sessao.getUsuario() != null) {

                if (Sessao.isExpirada()) {

                    logger.warn(
                            "SESSÃO EXPIRADA PARA O USUÁRIO: {}",
                            Sessao.getUsuario().getNome());

                    pararMonitorSessao();

                    executarLogout(false);

                    DialogFactory.aviso(
                            null,
                            "SUA SESSÃO EXPIROU POR INATIVIDADE.");
                }
            }
        });

        monitorInatividade.start();

        logger.info("MONITOR DE INATIVIDADE INICIADO.");
    }

    public void pararMonitorSessao() {

        if (monitorInatividade != null) {
            monitorInatividade.stop();
        }
    }

    public void executarLogout(boolean pedirConfirmacao) {

        if (processandoLogout) {
            return;
        }

        if (pedirConfirmacao &&
                !DialogFactory.confirmacao(
                        view,
                        "DESEJA REALMENTE SAIR?")) {

            return;
        }

        processandoLogout = true;

        try {

            pararMonitorSessao();

            Sessao.logout();

            view.setNomeUsuario("SESSÃO ENCERRADA");
            view.setTempoAcesso("");

            for (JInternalFrame frame :
                    view.getDesktopPane().getAllFrames()) {

                frame.dispose();
            }

            exibirLogin();

            logger.info(
                    pedirConfirmacao
                            ? "LOGOUT MANUAL REALIZADO."
                            : "SESSÃO EXPIRADA POR INATIVIDADE.");

        } finally {

            processandoLogout = false;
        }
    }

    public void forcarLogoutExpiracao() {

        Sessao.logout();

        for (JInternalFrame frame :
                view.getDesktopPane().getAllFrames()) {

            frame.dispose();
        }

        MenuRegistry.disableAll();

        view.setNomeUsuario("SESSÃO EXPIRADA");

        view.setTempoAcesso("");

        exibirLogin();

        logger.info(
                "SESSÃO FINALIZADA POR EXPIRAÇÃO DE INATIVIDADE.");
    }

    public void exibirLogin() {

        MenuRegistry.disableAll();

        UsuarioViewLogin login =
                ViewFactory.createLoginView();

        DesktopUtils.showFrame(
                view.getDesktopPane(),
                login);
    }
}