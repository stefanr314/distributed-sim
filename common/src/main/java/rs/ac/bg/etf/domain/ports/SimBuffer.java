package rs.ac.bg.etf.domain.ports;

import rs.ac.bg.etf.domain.Simulator;
import rs.ac.bg.etf.domain.event.Event;

/**
 * Interface {@code SimBuffer<V>} is the sole channel of communication a {@link Simulator} has with the
 * outside world — other simulators, whether on the same workstation or across the network. A concrete
 * implementation is responsible for physically delivering events to their destination component,
 * wherever it lives; {@link Simulator} itself never distinguishes network delivery from any other
 * transport.
 * <p>
 * {@code null}-valued events (null messages, used by conservative synchronization to advance a
 * downstream simulator's known channel time without an actual computed value) travel through this
 * interface identically to real events — implementations must not special-case or drop them.
 *
 * @param <V> the value type carried by events on this buffer
 * @author stefanr
 * @since 1.0
 */
public interface SimBuffer<V> {

	/**
	 * Delivers {@code event} to its {@link Event#destinationComponent()}.
	 *
	 * @param event the event to deliver, possibly carrying a {@code null} value (a NULL MESSAGE)
	 */
	void send(Event<V> event);

	/**
	 * Blocks until an event addressed to this buffer's owning simulator becomes available. Called by
	 * {@link Simulator#simulate()} exactly when the local queue offers nothing safe to process, so the
	 * simulator's thread yields instead of busy-waiting.
	 *
	 * @return the next event delivered to this buffer, once one arrives
	 * @throws InterruptedException - this is a blocking method so special care is required for orchestrating
	 *                              interrupt event.
	 */
	Event<V> receive() throws InterruptedException;
}