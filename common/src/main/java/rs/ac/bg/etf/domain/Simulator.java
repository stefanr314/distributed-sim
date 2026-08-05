package rs.ac.bg.etf.domain;

import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.netlist.Netlist;
import rs.ac.bg.etf.domain.ports.SimBuffer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Class {@code Simulator<V>} is the abstract base for a single Logical Process (LP) — one thread
 * executing exactly one partition of the overall {@link Netlist}, exchanging events with other
 * simulators exclusively through a {@link SimBuffer}.
 * <p>
 * Template Method: {@link #simulate()} and {@link #dispatch(Event)} implement the behaviour shared by
 * every synchronization strategy (draining the local queue in timestamp order, routing produced events
 * either back locally or out through the buffer, blocking on {@link SimBuffer#receive()} when nothing
 * is safe to process yet). Concrete subclasses — {@link ConservativeSimulator}, eventually an optimistic
 * counterpart — supply only the points where strategies genuinely differ: whether an event is currently
 * safe to process, what to do when a message arrives, what to do when a component produces no output,
 * and how termination and shutdown are decided.
 * <p>
 * Not thread-safe by itself, and does not need to be: {@link #queue()} is touched exclusively by this
 * simulator's own thread inside {@link #simulate()}. Concurrent access from other simulators is confined
 * entirely to the {@link SimBuffer} implementation.
 *
 * @param <V> the value type carried by events processed by this simulator
 * @author stefanr
 * @since 1.0
 */
public abstract class Simulator<V> {
	private final SimBuffer<V> buffer;
	private final Netlist<V> netlist;
	private final PriorityQueue<Event<V>> queue = new PriorityQueue<>(
			Comparator.comparingLong(Event::atDiscreteTimeMoment)
	);
	private final long simulationEndTime;
	private long localClock = Long.MIN_VALUE;

	protected Simulator(SimBuffer<V> buffer, Netlist<V> netlist, long simulationEndTime) {
		this.buffer = buffer;
		this.netlist = netlist;
		this.simulationEndTime = simulationEndTime;
	}

	protected void setLocalClock(long localClock) {
		this.localClock = localClock;
	}

	protected SimBuffer<V> buffer() {
		return buffer;
	}

	public Netlist<V> netlist() {
		return netlist;
	}

	protected PriorityQueue<Event<V>> queue() {
		return queue;
	}

	public long simulationEndTime() {
		return simulationEndTime;
	}

	/**
	 * Drains {@link #queue()} in timestamp order until {@link #isTerminated()}. On each iteration, the
	 * queue head is dispatched if {@link #safeToProceed(Event)} allows it; otherwise this thread blocks
	 * on {@link SimBuffer#receive()} until something arrives, handled via {@link #onMessageArrived(Event)},
	 * before re-checking the head. Calls {@link #declareEnd()} once terminated.
	 *
	 * @throws InterruptedException - required to delegate this exception risen from blocking
	 *                              {@link SimBuffer#receive()} to appropriate orchestrating logic that can detect and handle interrupt
	 */
	public void simulate() throws InterruptedException {
		System.out.println("Simulation starting on thread " + Thread.currentThread().getName());

		while (!isTerminated()) {
			Event<V> event = queue.peek();

			if (event == null || !safeToProceed(event)) {
				Event<V> incoming = buffer.receive();
				onMessageArrived(incoming);
				continue;
			}

			queue.poll();

			dispatch(event);
		}
		declareEnd();
	}


	/**
	 * Dispatch the event to appropriate component and send the outcome of component's execution down the line.
	 * Executes the component addressed by {@code event} and routes whatever it produces: real output
	 * events are dispatched via {@link #route(Event)} (locally requeued or sent out through the buffer),
	 * while an empty result is delegated to {@link #noValueMessageProduced(Event, Component)} for
	 * subclass-specific handling (e.g. synthesizing a null message). Event can travel three different
	 * roads:
	 * <p>
	 *      <ul>
	 * 	      <li>either back to the same simulator i.e. priority queue on simulator/thread</li>
	 * 	      <li>either on same workstation, but on different thread/simulator</li>
	 * 	      <li>or over the network using message passing i.e. network stack</li>
	 * 	 </ul>
	 * </p>
	 *
	 * <p>
	 * This behaviour is delegated to the appropriate buffer.
	 * </p>
	 *
	 * @param event {@code Event<V>} - event to be proceeded holding value V; value is permitted to be null, in order
	 *              to ease the NULL message sending
	 */
	private void dispatch(Event<V> event) {
		this.localClock = event.atDiscreteTimeMoment();
		Component<V> receiver = netlist.components().get(event.destinationComponent());

		//reach the netlist and perform te execute on the appropriate component
		List<Event<V>> produced = receiver.execute(event);

		if (produced.isEmpty())
			noValueMessageProduced(event, receiver);
		else
			for (Event<V> product : produced) {
				route(product);
			}
	}

	public void seed(List<Event<V>> seedEvents) {
		Objects.requireNonNull(seedEvents);

		if (!this.queue.isEmpty())
			throw new IllegalStateException("Simulator already initialized.");

		List<Event<V>> seed = List.copyOf(seedEvents);

		this.queue.addAll(seed);
	}

	/**
	 * @param id id to check
	 * @return {@code true} if {@code id} identifies a component within this simulator's own partition
	 */
	protected boolean componentOfSimulator(ComponentId id) {
		return netlist.components().containsKey(id);
	}

	/**
	 * Routes a produced event to its destination: added to the local {@link #queue()} if the destination
	 * is within this partition, otherwise handed to {@link #buffer()} for external delivery.
	 *
	 * @param product an event produced by a component's {@code execute}, to be delivered somewhere
	 */
	private void route(Event<V> product) {
		if (componentOfSimulator(product.destinationComponent())) {
			queue.add(product);
		} else {
			buffer.send(product);
		}
	}

	/**
	 * Called when a component's {@code execute} produces no output — it is still waiting on other
	 * inputs. Conservative implementations use this to synthesize and send null messages downstream;
	 * optimistic implementations have no use for it.
	 *
	 * @param triggerEvent the event that was just processed with no resulting output
	 * @param receiver     the component that processed it
	 */
	protected abstract void noValueMessageProduced(Event<V> triggerEvent, Component<V> receiver);

	/**
	 * @return {@code true} if {@code event} may be safely dispatched right now
	 */
	protected abstract boolean safeToProceed(Event<V> event);

	/**
	 * Called with whatever {@link SimBuffer#receive()} returned, to update subclass-specific state.
	 */
	protected abstract void onMessageArrived(Event<V> event);

	/**
	 * @return {@code true} if this simulator has nothing left to safely process, ever
	 */
	protected abstract boolean isTerminated();

	/**
	 * Called once {@link #isTerminated()}, to perform any subclass-specific shutdown notification.
	 */
	protected abstract void declareEnd();
}