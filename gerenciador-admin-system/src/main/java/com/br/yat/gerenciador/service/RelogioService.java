package com.br.yat.gerenciador.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.Timer;

public class RelogioService {

    private final JLabel lblHora;
    private Timer timer;

    public RelogioService(JLabel lblHora) {
        this.lblHora = lblHora;
    }

    public void iniciar() {
        parar();

        timer = new Timer(1000, e -> atualizarHora());
        timer.start();
    }

    public void parar() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void atualizarHora() {
        LocalDateTime agora = LocalDateTime.now();

        DateTimeFormatter formatador =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm:ss",
                        Locale.getDefault());

        lblHora.setText(agora.format(formatador));
    }
}