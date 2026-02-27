package com.br.yat.gerenciador.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class DiferencaUtil {

    private DiferencaUtil() {
    }

    public static <T> Diferenca<T> calcular(Collection<T> antes, Collection<T> depois) {

        Set<T> setAntes = new HashSet<>(antes);
        Set<T> setDepois = new HashSet<>(depois);

        Set<T> adicionados = new HashSet<>(setDepois);
        adicionados.removeAll(setAntes);

        Set<T> removidos = new HashSet<>(setAntes);
        removidos.removeAll(setDepois);

        return new Diferenca<>(adicionados, removidos);
    }
}