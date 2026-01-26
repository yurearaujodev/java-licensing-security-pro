package com.br.yat.gerenciador.controller;

@FunctionalInterface
public interface TaskWithResult<T> {
	T execute()throws Exception;
}
