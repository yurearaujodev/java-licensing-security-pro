package com.br.yat.gerenciador.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.model.Sessao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogHelper {

	private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, typeOfSrc,
                            context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                            context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            // A MÁGICA ACONTECE AQUI:
            .setExclusionStrategies(new SensitiveDataExclusionStrategy())
            .setPrettyPrinting()
            .create();

	private static String objetoParaJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj; // Se já for string/resumo, não serializa
        try {
            return gson.toJson(obj);
        } catch (Exception e) {
            return "Erro ao serializar: " + e.getMessage();
        }
    }
	
	private static String getNetworkInfo() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName() + " (" + 
                   java.net.InetAddress.getLocalHost().getHostAddress() + ")";
        } catch (Exception e) {
            return "IP_DESCONHECIDO";
        }
    }
	
	public static LogSistema gerarLogSucesso(String tipo, String acao, String entidade, Integer id, Object anterior, Object novo) {
        LogSistema log = inicializarBase();
        log.setTipo(tipo);
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setIdEntidade(id);
        log.setSucesso(true);

        if (anterior != null) log.setDadosAnteriores(objetoParaJson(anterior));
        if (novo != null) log.setDadosNovos(objetoParaJson(novo));

        return log;
    }

    public static LogSistema gerarLogErro(String tipo, String acao, String entidade, String erro) {
        LogSistema log = inicializarBase();
        log.setTipo(tipo);
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setSucesso(false);
        log.setMensagemErro(erro);
        return log;
    }

    // Centraliza a criação básica para evitar repetição
    private static LogSistema inicializarBase() {
        LogSistema log = new LogSistema();
        log.setDataHora(LocalDateTime.now());
        log.setUsuario(Sessao.getUsuario());
        log.setIpOrigem(getNetworkInfo());
        return log;
    }

}
