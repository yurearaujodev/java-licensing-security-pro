package com.br.yat.gerenciador.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
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
