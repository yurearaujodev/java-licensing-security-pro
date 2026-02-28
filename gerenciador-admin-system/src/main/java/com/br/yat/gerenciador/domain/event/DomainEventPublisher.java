package com.br.yat.gerenciador.domain.event;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DomainEventPublisher {
	private static final Logger logger = LogManager.getLogger(DomainEventPublisher.class);

	private final Map<Class<? extends DomainEvent>, List<DomainEventListener<? extends DomainEvent>>> listeners = new ConcurrentHashMap<>();

	public <T extends DomainEvent> void register(DomainEventListener<T> listener) {
		listeners.computeIfAbsent(listener.getEventType(), k -> new CopyOnWriteArrayList<>()).add(listener);
	}

	public void publish(DomainEvent event, Connection conn) {

		Set<DomainEventListener<?>> alreadyCalled = new HashSet<>();

		Class<?> eventClass = event.getClass();

		while (eventClass != null && DomainEvent.class.isAssignableFrom(eventClass)) {

			notifyListenersForType(eventClass, event, conn, alreadyCalled);

			for (Class<?> iface : eventClass.getInterfaces()) {
				notifyListenersForType(iface, event, conn, alreadyCalled);
			}

			eventClass = eventClass.getSuperclass();
		}
	}

	private void notifyListenersForType(Class<?> type, DomainEvent event, Connection conn,
			Set<DomainEventListener<?>> alreadyCalled) {

		List<DomainEventListener<? extends DomainEvent>> eventListeners = listeners.get(type);

		if (eventListeners == null)
			return;

		for (DomainEventListener<? extends DomainEvent> listener : eventListeners) {

			if (!alreadyCalled.contains(listener)) {
				dispatch(listener, event, conn);
				alreadyCalled.add(listener);
			}
		}
	}

	private <T extends DomainEvent> void dispatch(DomainEventListener<T> listener, DomainEvent event, Connection conn) {
		try {
			if (listener.getEventType().isInstance(event)) {
				listener.onEvent(listener.getEventType().cast(event), conn);
			}
		} catch (Exception e) {
			logger.error("Erro ao despachar evento {}", event.getClass().getSimpleName(), e);
		}
	}
}