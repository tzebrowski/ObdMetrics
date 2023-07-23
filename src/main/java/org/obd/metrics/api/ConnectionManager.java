package org.obd.metrics.api;

import org.obd.metrics.api.model.ErrorsPolicy;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.context.Service;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ConnectionManager extends LifecycleAdapter implements AutoCloseable, Service {

	private final AdapterConnection connection;
	private final ErrorsPolicy errorsPolicy;
	private volatile int numberOfReconnectRetries = 0;

	@Override
	public void onInternalError(String message, Throwable e) {
		log.error("Received onInternalError event. Reason: {}", message);

		if (errorsPolicy.isReconnectEnabled() && numberOfReconnectRetries < errorsPolicy.getNumberOfRetries()) {
			reconnect();
		} else {
			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onError(message, e);
			});

			Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
				p.onError(new Exception(message));
				p.onNext(Reply.builder().command(new QuitCommand()).build());
			});
		}
	}

	@SneakyThrows
	@Override
	public void close() {
		getConnector().close();
	}

	@SneakyThrows
	void init() {
		Context.instance().register(Connector.class, Connector.builder().connection(connection).build());
	}

	Connector getConnector() {
		return Context.instance().forceResolve(Connector.class);
	}

	boolean isReconnectAllowed() {
		return errorsPolicy.isReconnectEnabled() && numberOfReconnectRetries < errorsPolicy.getNumberOfRetries();
	}

	void resetFaultCounter() {
		numberOfReconnectRetries = 0;
	}

	private void reconnect() {
		try {
			numberOfReconnectRetries++;
			final String msg = "Connector is faulty. Reconnecting.....";
			log.error(msg);

			final Context context = Context.instance();
			Connector connector = context.forceResolve(Connector.class);
			connector.close();
			connector = Connector.builder().connection(connection).build();
			context.register(Connector.class, connector);
		} catch (Throwable e) {
			log.error("Failed to reconnect. ", e);
			Subscription.notifyOnInternalError("Failed to reconnect. ");
		}
	}
}
