package com.br.yat.gerenciador.util;

import java.util.List;
import java.util.Map;

public record CollectionDiffResult<T, K>(
        List<T> adicionados,
        List<T> removidos,
        Map<K, Map<String, Object[]>> alterados
) {

    public boolean isEmpty() {
        return adicionados.isEmpty()
                && removidos.isEmpty()
                && alterados.isEmpty();
    }
}