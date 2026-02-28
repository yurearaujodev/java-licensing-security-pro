package com.br.yat.gerenciador.domain.event;

import java.sql.Connection;

public interface DomainEventListener<T extends DomainEvent> {

    Class<T> getEventType();

    void onEvent(T event, Connection conn) throws Exception;
}