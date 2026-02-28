package com.br.yat.gerenciador.util;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;

public class AuditDiffUtil {

	public static <T> Map<String, Object[]> calcularDiff(T antes, T depois) {

		Map<String, Object[]> diff = new HashMap<>();

		if (antes == null || depois == null) {
			return diff;
		}

		try {

			Class<?> clazz = antes.getClass();

			if (clazz.isRecord()) {

				for (RecordComponent component : clazz.getRecordComponents()) {

					Method accessor = component.getAccessor();

					Object valorAntes = accessor.invoke(antes);
					Object valorDepois = accessor.invoke(depois);

					if (!equalsSafe(valorAntes, valorDepois)) {
						diff.put(component.getName(), new Object[] { valorAntes, valorDepois });
					}
				}

			} else {
				Method[] methods = clazz.getDeclaredMethods();

				for (Method m : methods) {
					if (m.getParameterCount() == 0 && m.getName().startsWith("get")
							&& !m.getName().equals("getClass")) {

						String nomeCampo = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);

						Object valorAntes = m.invoke(antes);
						Object valorDepois = m.invoke(depois);

						if (!equalsSafe(valorAntes, valorDepois)) {
							diff.put(nomeCampo, new Object[] { valorAntes, valorDepois });
						}
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException("Erro ao calcular diff", e);
		}

		return diff;
	}

	private static boolean equalsSafe(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}
