package com.br.yat.gerenciador.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AuditCollectionDiffUtil {

    public static <T, K> CollectionDiffResult<T, K> calcularDiffCollection(
            Collection<T> antes,
            Collection<T> depois,
            Function<T, K> idExtractor) {

        if (antes == null || depois == null) {
            return new CollectionDiffResult<>(
                    List.of(),
                    List.of(),
                    Map.of()
            );
        }

        Map<K, T> mapAntes = antes.stream()
                .collect(Collectors.toMap(
                        idExtractor,
                        Function.identity(),
                        (a, b) -> b
                ));

        Map<K, T> mapDepois = depois.stream()
                .collect(Collectors.toMap(
                        idExtractor,
                        Function.identity(),
                        (a, b) -> b
                ));

        Set<K> todosIds = new HashSet<>();
        todosIds.addAll(mapAntes.keySet());
        todosIds.addAll(mapDepois.keySet());

        List<T> adicionados = new ArrayList<>();
        List<T> removidos = new ArrayList<>();
        Map<K, Map<String, Object[]>> alterados = new LinkedHashMap<>();

        for (K id : todosIds) {

            T objAntes = mapAntes.get(id);
            T objDepois = mapDepois.get(id);

            if (objAntes == null) {
                adicionados.add(objDepois);
                continue;
            }

            if (objDepois == null) {
                removidos.add(objAntes);
                continue;
            }

            Map<String, Object[]> diffInterno =
                    AuditDiffUtil.calcularDiff(objAntes, objDepois);

            if (!diffInterno.isEmpty()) {
                alterados.put(id, diffInterno);
            }
        }

        return new CollectionDiffResult<>(
                List.copyOf(adicionados),
                List.copyOf(removidos),
                Map.copyOf(alterados)
        );
    }
}