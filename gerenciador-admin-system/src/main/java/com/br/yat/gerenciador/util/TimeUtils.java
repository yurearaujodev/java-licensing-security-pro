package com.br.yat.gerenciador.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

	private static final DateTimeFormatter FORMATTER_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	/**
	 * Formata uma data para o padrão brasileiro (dd/MM/yyyy HH:mm:ss) Útil para
	 * exibir o limite de bloqueio: "Bloqueado até 08/02/2026 16:30:00"
	 */
	public static String formatarDataHora(LocalDateTime dataHora) {
		if (dataHora == null)
			return "";
		return dataHora.format(FORMATTER_BR);
	}

	public static LocalDateTime parseDataHora(String dataStr) {
	    if (dataStr == null || dataStr.trim().isEmpty()) return null;
	    try {
	        // Tenta o formato completo primeiro
	        return LocalDateTime.parse(dataStr, FORMATTER_BR);
	    } catch (Exception e) {
	        try {
	            // Tenta um formato sem segundos como fallback
	            DateTimeFormatter fallback = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	            return LocalDateTime.parse(dataStr, fallback);
	        } catch (Exception ex) {
	            return null; // Se não for data válida, retorna null
	        }
	    }
	}

	public static String formatarTempoDecorrido(LocalDateTime ultimoLogin) {
		if (ultimoLogin == null)
			return "NUNCA ACESSOU";

		LocalDateTime agora = LocalDateTime.now();
		long minutos = ChronoUnit.MINUTES.between(ultimoLogin, agora);
		long horas = ChronoUnit.HOURS.between(ultimoLogin, agora);
		long dias = ChronoUnit.DAYS.between(ultimoLogin, agora);

		if (minutos < 1)
			return "AGORA MESMO";
		if (minutos < 60)
			return "HÁ " + minutos + " MINUTOS ATRÁS";
		if (horas < 24)
			return "HÁ " + horas + " HORAS ATRÁS";
		return "HÁ " + dias + " DIAS ATRÁS";
	}
}
