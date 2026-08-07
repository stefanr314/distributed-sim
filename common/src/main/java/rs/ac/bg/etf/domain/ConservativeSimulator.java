package rs.ac.bg.etf.domain;

import org.jetbrains.annotations.NotNull;
import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventBufferReceiveException;
import rs.ac.bg.etf.domain.netlist.Netlist;
import rs.ac.bg.etf.domain.ports.SimBuffer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.min;

/**
 * Class {@code ConservativeSimulator} implements conservative (Chandy-Misra-Bryant style) distributed
 * synchronization: an event is only dispatched once it is guaranteed that no earlier-timestamped event
 * could still arrive on any external input channel. That guarantee is tracked per {@link InputPortKey}
 * in {@link #externalChannelClocks} — the earliest possible time a message could still arrive on that
 * channel, updated on every real or null message received via {@link #onMessageArrived(Event)}.
 *
 * @param <V> the value type carried by events processed by this simulator
 * @author stefanr
 * @since 1.0
 */
public class ConservativeSimulator<V extends Serializable> extends Simulator<V> {
	private final Map<InputPortKey, Long> externalChannelClocks;

	/**
	 * @param buffer              delivery channel to other simulators
	 * @param netlist             this partition's own components and internal connections
	 * @param simulationEndTime   target logical time this simulation must reach, declared by client
	 * @param externalConnections every {@link InputPortKey} within this partition that is fed by a
	 *                            component outside it — determined by the server when this partition
	 *                            was assigned. Each is seeded with a channel clock of {@code 0}.
	 */
	public ConservativeSimulator(SimBuffer<V> buffer, Netlist<V> netlist, long simulationEndTime,
	                             Set<InputPortKey> externalConnections) {
		super(buffer, netlist, simulationEndTime);
		this.externalChannelClocks = (externalize(externalConnections));
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Synthesizes a null message — {@code triggerEvent.atDiscreteTimeMoment() + receiver.delay()} — and
	 * sends it to every one of {@code receiver}'s outgoing connections that targets a component outside
	 * this partition, so downstream simulators can advance their channel clock even though this
	 * component had nothing real to report yet.
	 */
	@Override
	protected void noValueMessageProduced(Event<V> triggerEvent, Component<V> receiver) {
		Objects.requireNonNull(triggerEvent);
		Objects.requireNonNull(receiver);

		long nullTime = triggerEvent.atDiscreteTimeMoment() + receiver.delay();

		//send it to all the outgoing connections from receiver which are targeting components not in simulator's
		// netlist
		sendNullFromComponentWithClockValue(receiver, nullTime);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Safe if {@code event}'s timestamp does not exceed the minimum known channel clock across every
	 * external input channel — the earliest time at which any external source could still send
	 * something.
	 */
	@Override
	protected boolean safeToProceed(Event<V> event) {
		Objects.requireNonNull(event);

		return event.atDiscreteTimeMoment() <= currentSafeTime();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Updates the channel clock for {@code event}'s {@link InputPortKey} to its timestamp — valid for
	 * both real and null messages, since either establishes a lower bound on that channel's next
	 * message. Only real-valued events are additionally queued for processing.
	 *
	 * <p>
	 * Instead of only putting the value in external channel clocks, firstly anticipate whether the event is
	 * carrying older timestamp which break the invariant of constantly increasing time moments. On this basis
	 * the distributed simulation rests.
	 * </p>
	 *
	 * @throws MisroutedEventBufferReceiveException if {@code event} targets a port this simulator was
	 *                                              never told to expect external input on
	 */
	@Override
	protected void onMessageArrived(Event<V> event) {
		Objects.requireNonNull(event);

		// receive the event and update external clock times always
		ComponentId forComponent = event.destinationComponent();
		int atPort = event.atPort();
		InputPortKey inputPortKey = new InputPortKey(forComponent, atPort);
		if (!externalChannelClocks.containsKey(inputPortKey))
			throw new MisroutedEventBufferReceiveException(inputPortKey);

		externalChannelClocks.merge(inputPortKey, event.atDiscreteTimeMoment(), Math::max);

		//if value not null add it to the queue
		if (event.value() != null)
			queue().add(event);

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sends a final null message, timestamped at {@link #simulationEndTime()}, to every external
	 * outgoing connection of every component in this partition — releasing any neighbouring simulator
	 * that may still be waiting on this partition's channel clock. After this the channel to external simulator is
	 * considered as saturated.
	 */
	@Override
	protected void declareEnd() {
		setLocalClock(simulationEndTime());

		long finalTime = simulationEndTime();

		// broadcast null end time messages; inner null messages are not gonna be sent, check the method declaration
		for (Component<V> component : netlist().components().values()) {
			sendNullFromComponentWithClockValue(component, finalTime);
		}
	}

	@Override
	protected void reactToBlocking() {
		var currentSafe = currentSafeTime();

		if (currentSafe == Long.MAX_VALUE || currentSafe >= simulationEndTime()) return;

		for (Component<V> component : netlist().components().values()) {
			sendNullFromComponentWithClockValue(component, currentSafe + component.delay());
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Terminated once the local queue is empty and the current safe time has reached
	 * {@link #simulationEndTime()} — the latter is trivially satisfied when this partition has no
	 * external channels at all (a single-workstation simulation).
	 */
	@Override
	protected boolean isTerminated() {
		long safeTime = currentSafeTime();
		return queue().isEmpty() && safeTime >= simulationEndTime();
	}

	private @NotNull Map<InputPortKey, Long> externalize(Set<InputPortKey> externalConnections) {
		Objects.requireNonNull(externalConnections, "External clocks can not be null");

		var externalClocks = new HashMap<InputPortKey, Long>();
		for (InputPortKey external : externalConnections) {
			externalClocks.put(external, 0L);
		}

		return externalClocks;
	}

	/**
	 * Help method for sending NULL messages. The use of {@code component.outgoingConnections()} is key here, since
	 * netlist doesn't hold the external connections i.e. they are prohibited. Only send NULL messages to the outside
	 * world, there is no need for inner communication with this type of messages.
	 *
	 * @param component  - a sender component
	 * @param clockValue - value of clock/discrete time moment to be sent
	 */
	private void sendNullFromComponentWithClockValue(Component<V> component, long clockValue) {
		for (Connection outgoing : component.outgoingConnections()) {
			ComponentId target = outgoing.target();
			if (!componentOfSimulator(target)) {
				buffer().send(new Event<>(target, outgoing.toPort(), null, clockValue));
			}
		}
	}

	private long currentSafeTime() {
		return externalChannelClocks.isEmpty() ? Long.MAX_VALUE : min(externalChannelClocks.values());
	}
}