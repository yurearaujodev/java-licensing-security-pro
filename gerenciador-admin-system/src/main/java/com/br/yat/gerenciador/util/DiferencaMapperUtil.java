package com.br.yat.gerenciador.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DiferencaMapperUtil {

    private DiferencaMapperUtil() {}

    public static <T> Diferenca<String> calcular(
            List<T> antes,
            List<T> depois,
            Function<T, String> mapper) {

        Set<String> setAntes = (antes == null ? List.<T>of() : antes)
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> setDepois = (depois == null ? List.<T>of() : depois)
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return DiferencaUtil.calcular(setAntes, setDepois);
    }
}