package com.br.yat.gerenciador.util;

import java.util.Set;

public class Diferenca<T> {

    private final Set<T> adicionados;
    private final Set<T> removidos;

    public Diferenca(Set<T> adicionados, Set<T> removidos) {
        this.adicionados = Set.copyOf(adicionados);
        this.removidos = Set.copyOf(removidos);
    }

    public Set<T> getAdicionados() {
        return adicionados;
    }

    public Set<T> getRemovidos() {
        return removidos;
    }

    public boolean temAlteracao() {
        return !adicionados.isEmpty() || !removidos.isEmpty();
    }
}