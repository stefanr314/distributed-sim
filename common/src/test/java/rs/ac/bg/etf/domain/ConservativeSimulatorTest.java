package rs.ac.bg.etf.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventBufferReceiveException;
import rs.ac.bg.etf.domain.netlist.LogicalNetlist;
import rs.ac.bg.etf.domain.netlist.Netlist;
import rs.ac.bg.etf.domain.ports.SimBuffer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConservativeSimulatorTest {

	@Mock
	SimBuffer<Boolean> buffer;

	LogicalNetlist emptyNetlist;

	@BeforeEach
	void setUp() {
		emptyNetlist = LogicalNetlist.create();
	}

	@Test
	void constructorSeedsExternalChannelClocksAtZero() {
		InputPortKey key = new InputPortKey(new ComponentId("G1"), 0);
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of(key));

		// untouched channel seeded at 0, not MAX_VALUE - so target 100 is not yet reached
		assertThat(sim.isTerminated()).isFalse();
	}

	@Test
	void externalSeedOfEventQueuePreventsTermination() {
		InputPortKey key = new InputPortKey(new ComponentId("G1"), 0);
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of(key));
		Event<Boolean> seed = new Event<>(new ComponentId("G1"), 0, true, 5L);

		sim.seed(List.of(seed));

		assertThat(sim.isTerminated()).isFalse();
	}

	@Test
	void consecutiveSeedCallsThrowsException() {
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of());

		Event<Boolean> seed = new Event<>(new ComponentId("G1"), 0, true, 5L);

		sim.seed(List.of(seed));


		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> sim.seed(List.of(seed)))
				.withMessage("Simulator already initialized.");
	}

	@Test
	void isTerminatedTrueWithNoExternalChannelsAndEmptyQueue() {
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of());

		assertThat(sim.isTerminated()).isTrue();
	}

	@Test
	void safeToProceedComparesAgainstMinimumChannelClock() {
		InputPortKey a = new InputPortKey(new ComponentId("A"), 0);
		InputPortKey b = new InputPortKey(new ComponentId("B"), 0);
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of(a, b));

		sim.onMessageArrived(new Event<>(new ComponentId("A"), 0, null, 10L));
		sim.onMessageArrived(new Event<>(new ComponentId("B"), 0, null, 30L));

		assertThat(sim.safeToProceed(new Event<>(new ComponentId("X"), 0, true, 10L))).isTrue();
		assertThat(sim.safeToProceed(new Event<>(new ComponentId("X"), 0, true, 11L))).isFalse();
	}

	@Test
	void onMessageArrivedDoesNotQueueNullMessages() {
		InputPortKey key = new InputPortKey(new ComponentId("G1"), 0);
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of(key));

		sim.onMessageArrived(new Event<>(new ComponentId("G1"), 0, null, 5L));

		assertThat(sim.queue()).isEmpty();
	}

	@Test
	void onMessageArrivedQueuesRealValuedEvents() {
		InputPortKey key = new InputPortKey(new ComponentId("G1"), 0);
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of(key));

		Event<Boolean> real = new Event<>(new ComponentId("G1"), 0, true, 5L);
		sim.onMessageArrived(real);

		assertThat(sim.queue()).containsExactly(real);
	}

	@Test
	void onMessageArrivedRejectsUnknownChannel() {
		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of());

		Event<Boolean> stray = new Event<>(new ComponentId("GHOST"), 0, true, 5L);

		assertThatExceptionOfType(MisroutedEventBufferReceiveException.class)
				.isThrownBy(() -> sim.onMessageArrived(stray));
	}

	@SuppressWarnings("unchecked") // mocking a generic type - Mockito can't preserve <Boolean> at the raw-type call
	@Test
	void noValueMessageProducedSendsNullOnlyToExternalConnections() {
		ComponentId sourceId = new ComponentId("G1");
		Connection external = new Connection(sourceId, new ComponentId("REMOTE"), 0, 0);

		Component<Boolean> component = mock(Component.class);
		when(component.delay()).thenReturn(3L);
		when(component.outgoingConnections()).thenReturn(List.of(external));

		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, emptyNetlist, 100L, Set.of());

		sim.noValueMessageProduced(new Event<>(sourceId, 0, true, 10L), component);

		verify(buffer).send(argThat(sent ->
				sent.destinationComponent().equals(new ComponentId("REMOTE"))
						&& sent.value() == null
						&& sent.atDiscreteTimeMoment() == 13L)); // 10 + delay(3)
	}

	@SuppressWarnings("unchecked")
	@Test
	void noValueMessageProducedDoesNotSendNullMessagesInternally() {
		ComponentId sourceId = new ComponentId("G1");
		ComponentId targetId = new ComponentId("INTERNAL");
		Connection external = new Connection(sourceId, targetId, 0, 0);

		Component<Boolean> component = mock(Component.class);
		when(component.delay()).thenReturn(3L);
		when(component.outgoingConnections()).thenReturn(List.of(external));

		Component<Boolean> targetComponent = mock(Component.class);

		Netlist<Boolean> netlistDouble = mock(Netlist.class);
		when(netlistDouble.components()).thenReturn(Map.of(sourceId, component, targetId, targetComponent));

		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, netlistDouble, 100L, Set.of());

		sim.noValueMessageProduced(new Event<>(sourceId, 0, true, 10L), component);

		verify(buffer, never()).send(new Event<>(targetId, 0, null, 13L));
	}

	@SuppressWarnings("unchecked")
	@Test
	void declareEndSendsFinalNullEventsToEveryExternalConnection() {
		ComponentId sourceId = new ComponentId("N1");
		Connection external = new Connection(sourceId, new ComponentId("REMOTE"), 0, 0);

		Component<Boolean> component = mock(Component.class);
		when(component.outgoingConnections()).thenReturn(List.of(external));

		Netlist<Boolean> netlistDouble = mock(Netlist.class);
		when(netlistDouble.components()).thenReturn(Map.of(sourceId, component));

		ConservativeSimulator<Boolean> sim =
				new ConservativeSimulator<>(buffer, netlistDouble, 50L, Set.of());

		sim.declareEnd();

		verify(buffer).send(argThat(sent ->
				sent.destinationComponent().equals(new ComponentId("REMOTE"))
						&& sent.value() == null
						&& sent.atDiscreteTimeMoment() == 50L));
	}
}