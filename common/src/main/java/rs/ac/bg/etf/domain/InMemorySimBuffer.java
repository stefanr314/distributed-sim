package rs.ac.bg.etf.domain;

import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.UnknownRoutingDestinationException;
import rs.ac.bg.etf.domain.ports.SimBuffer;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * Class {@code InMemorySimBuffer<V>} is the {@link SimBuffer} implementation used when sender and
 * receiver share the same JVM — one workstation running multiple partitions as separate threads.
 * {@link #send(Event)} looks up the destination's inbox in the shared {@link SimpleWorkstationRouting}
 * table and writes directly to it; no serialization or network I/O involved.
 * <p>
 * <b>Thread confinement:</b> every method of a given instance is called exclusively by its owning
 * simulator's own thread — {@link #receive()} from {@link Simulator#simulate()}, {@link #send(Event)}
 * from {@code dispatch}, both on that same thread. This class itself therefore needs no internal
 * synchronization. All actual cross-thread interaction happens one level down, inside
 * already-thread-safe collaborators — the shared {@link SimpleWorkstationRouting} and each component's
 * individual {@link BlockingQueue} inbox — never through this class's own state.
 * <p>
 * Safe publication of both fields is guaranteed by them being {@code final} and fully assigned before
 * the constructor returns, provided this instance is handed to its owning thread only after
 * construction completes.
 *
 * @param <V> the value type carried by events on this buffer
 * @author stefanr
 * @since 1.0
 */
public class InMemorySimBuffer<V extends Serializable> implements SimBuffer<V> {
	private final SimpleWorkstationRouting<V> routing;
	private final BlockingQueue<Event<V>> inbox;

	/**
	 * @param ownInbox this simulator's own inbox, drained by {@link #receive()}
	 * @param routing  workstation-wide routing table, shared with every other simulator here, used by
	 *                 {@link #send(Event)} to find other components' inboxes
	 */
	public InMemorySimBuffer(BlockingQueue<Event<V>> ownInbox, SimpleWorkstationRouting<V> routing) {
		this.routing = routing;
		this.inbox = ownInbox;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnknownRoutingDestinationException if {@code event}'s destination is not currently
	 *                                            registered — signals a partition-assignment mistake
	 *                                            upstream rather than silently dropping the event
	 */
	@Override
	public void send(Event<V> event) {
		Objects.requireNonNull(event);

		BlockingQueue<Event<V>> inboxOfComponent = routing.inboxAtComponent(event.destinationComponent());

		if (inboxOfComponent == null)
			throw new UnknownRoutingDestinationException(event.destinationComponent());

		boolean regularBehaviour = inboxOfComponent.offer(event);
		if (!regularBehaviour)
			throw new AssertionError("Unexpected behaviour. Out of memory error is more likely" +
					" to occur.");

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Event<V> receive() throws InterruptedException {
		return inbox.take();
	}
}