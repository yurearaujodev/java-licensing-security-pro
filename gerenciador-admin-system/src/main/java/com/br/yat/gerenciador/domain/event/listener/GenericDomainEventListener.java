package com.br.yat.gerenciador.domain.event.listener;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.br.yat.gerenciador.domain.event.DomainEvent;
import com.br.yat.gerenciador.domain.event.DomainEventListener;

public abstract class GenericDomainEventListener implements DomainEventListener<DomainEvent> {
	private static final Logger logger = LogManager.getLogger(GenericDomainEventListener.class);

	private final Map<Class<? extends DomainEvent>, ThrowingEventConsumer> handlers = new HashMap<>();

	@FunctionalInterface
	private interface ThrowingEventConsumer {
		void accept(DomainEvent event, Connection conn) throws Exception;
	}

	@FunctionalInterface
	public interface EventHandler<T extends DomainEvent> {
		void handle(T event, Connection conn) throws Exception;
	}

	public <T extends DomainEvent> void registerHandler(Class<T> type, EventHandler<T> handler) {

		handlers.put(type, (event, conn) -> handler.handle(type.cast(event), conn));
	}

	@Override
	public Class<DomainEvent> getEventType() {
		return DomainEvent.class;
	}

	@Override
	public void onEvent(DomainEvent event, Connection conn) {

		boolean anyMatched = false;

		for (var entry : handlers.entrySet()) {

			Class<? extends DomainEvent> type = entry.getKey();

			if (type.isAssignableFrom(event.getClass())) {

				anyMatched = true;

				try {
					entry.getValue().accept(event, conn);
				} catch (Exception e) {
					logger.error("Erro no handler {} para evento {}", type.getSimpleName(),
							event.getClass().getSimpleName(), e);
				}
			}
		}

		if (!anyMatched) {
			onUnknownEvent(event, conn);
		}
	}

	protected void onUnknownEvent(DomainEvent event, Connection conn) {
		logger.warn("Nenhum handler para {}", event.getClass().getSimpleName());
	}

}