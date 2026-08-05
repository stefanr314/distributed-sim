package rs.ac.bg.etf.domain.component;

import org.jetbrains.annotations.NotNull;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.ComputeNullInputValueException;
import rs.ac.bg.etf.domain.exceptions.InvalidSizeOfInputValues;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class {@code LogicalComponent} is the common base for all Boolean-valued combinational logic gates
 * (AND, OR, NOT, ...). It implements the routing and event-scheduling behaviour shared by every gate
 * via the Template Method pattern: {@link #execute(Event)} handles unpacking the incoming event,
 * updating input port state, waiting for all inputs to arrive, and scheduling outgoing events, while
 * each concrete gate only supplies its own truth table via {@link #computeValues(List)}.
 * <p>
 * Subclasses are expected to remain purely combinational — {@link #computeValues(List)} should be a
 * pure function of its input list, with no side effects beyond what {@link #execute(Event)} already
 * performs on their behalf.
 *
 * @author stefanr
 * @since 1.0
 */
public abstract class LogicalComponent extends Component<Boolean> {
	protected LogicalComponent(ComponentId componentId, ComponentPort<Boolean> inputPort, ComponentPort<Boolean> outputPort, long delay) {
		super(componentId, inputPort, outputPort, delay);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Updates the value on the input port addressed by {@code msg}, then checks whether every input
	 * port has received at least one value. If not, returns an empty list — the gate is still waiting
	 * on other inputs and produces no output yet.
	 * <p>
	 * Once all inputs have arrived at least once, the gate recomputes its output using the
	 * <em>latest known value</em> on every input — not just the one carried by {@code msg}. This means
	 * a gate re-evaluates on every subsequent input change as well, using whichever values were most
	 * recently set on the ports that did not change in this particular event. Input port state is
	 * intentionally never reset between invocations.
	 *
	 * @param msg the incoming event carrying a new value for one of this gate's input ports
	 * @return a list of events — one per outgoing connection — addressed to downstream components,
	 * or an empty list if not all inputs have arrived yet, or if this gate has no outgoing
	 * connections
	 * @throws MisroutedEventException if {@code msg} is addressed to a different component than this one
	 */
	@Override
	public List<Event<Boolean>> execute(@NotNull Event<Boolean> msg) {
		if (!msg.destinationComponent().equals(componentId())) throw new MisroutedEventException(componentId(),
				msg.destinationComponent());
		inputPort().setValueAtPort(msg.value(), msg.atPort());

		//send data to other ports if ready, if not return empty list; just forward data
		if (!inputPort().allPortValuesSet()) return List.of();

		//set value on output port i.e. this component's value
		Boolean calculatedValue = computeValues(inputValues());
		outputPort().setValueAtPort(calculatedValue, 0);

		long timeMoment = msg.atDiscreteTimeMoment() + delay();

		List<Event<Boolean>> listOfEvents = new ArrayList<>();

		for (Connection connection : outgoingConnections()) {

			Event<Boolean> event = new Event<>(connection.target(), connection.toPort(), calculatedValue, timeMoment);

			listOfEvents.add(event);
		}

		return listOfEvents;
	}

	/**
	 * Guard clause verifying that {@code inputs} has exactly {@code expected} elements. Intended to be
	 * called from within {@link #computeValues(List)} implementations to validate the arity of the
	 * gate before applying its truth table. Not enforced by {@link #execute(Event)} itself, since the
	 * input list it builds is already guaranteed to match this gate's configured input port count —
	 * this check exists so {@link #computeValues(List)} remains safe to call directly and in isolation,
	 * e.g. from unit tests. Currently, no implementations change the default method visibility so the check is never
	 * triggered from client's code.
	 *
	 * <p>With the proper construction of components this method becomes deprecated and redundant.</p>
	 *
	 * @param expected number of input values this gate's truth table requires
	 * @param inputs   the input values to validate
	 * @throws InvalidSizeOfInputValues if {@code inputs.size() != expected}
	 */
	@Deprecated
	protected void ensureExpectedNumberOfInputValues(int expected, @NotNull List<Boolean> inputs) {
		if (inputs.size() != expected) throw new InvalidSizeOfInputValues(expected, inputs.size());

	}

	/**
	 * Guard clause verifying that none of the values in {@code inputs} is {@code null}. Intended to be
	 * called from within {@link #computeValues(List)} implementations, for the same reason as
	 * {@link #ensureExpectedNumberOfInputValues(int, List)} — {@link #execute(Event)} only invokes
	 * {@link #computeValues(List)} once every input port is confirmed set, so this check is redundant
	 * on that path but guards {@link #computeValues(List)} when called on its own. Currently, no implementations
	 * change the default method visibility so the check is never triggered from client's code.
	 *
	 * @param inputs the input values to validate
	 * @throws ComputeNullInputValueException if any element of {@code inputs} is {@code null}
	 */
	protected void ensureAllInputValuesNotNull(@NotNull List<Boolean> inputs) {
		if (inputs.stream().anyMatch(Objects::isNull)) throw new ComputeNullInputValueException();
	}

	/**
	 * Computes this gate's output value from its current input values, according to its specific
	 * truth table (AND, OR, NOT, ...). Implementations should validate {@code inputs} using
	 * {@link #ensureAllInputValuesNotNull(List)} and {@link #ensureExpectedNumberOfInputValues(int, List)}
	 * before applying their logic.
	 *
	 * @param inputs current values of every input port, in port-index order
	 * @return the Boolean output value produced by this gate's logic function
	 */
	protected abstract Boolean computeValues(List<Boolean> inputs);
}