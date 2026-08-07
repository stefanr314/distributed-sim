package rs.ac.bg.etf.domain;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.netlist.LogicalNetlist;
import rs.ac.bg.etf.domain.netlist.Pair;
import rs.ac.bg.etf.domain.ports.SimBuffer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Two partitions on two real threads, wired {@code N1 -> N2} across an in-memory {@link SimBuffer}.
 * {@code N1} is driven by two seed events; every event {@code N1} emits lands at
 * {@code seedTime + N1_DELAY}, so the value observable on {@code N2} after the run is decided purely
 * by which of those emissions fall inside {@code [0, simulationEndTime]}.
 */
class ConservativeSimulatorIT {

	private static final String N1 = "N1";
	private static final String N2 = "N2";
	private static final long N1_DELAY = 5L;
	private static final long N2_DELAY = 10L;

	/**
	 * Each case is {@code (simulationEndTime, expected value on N2 once the run completes)}.
	 * <ul>
	 *   <li>{@code 7}  — N1's first emission lands at 8, past the end time, so nothing ever reaches N2</li>
	 *   <li>{@code 8}  — first emission (value {@code false}) arrives, N2 inverts it to {@code true}</li>
	 *   <li>{@code 14} — second emission lands at 15 and is still cut off, so the first value stands</li>
	 *   <li>{@code 15} — second emission (value {@code true}) arrives, N2 inverts it to {@code false}</li>
	 *   <li>{@code 20} — nothing further is produced, so the value is unchanged from 15</li>
	 * </ul>
	 */
	static Stream<Arguments> endTimeToExpectedValue() {
		return Stream.of(
				Arguments.of(7L, null),
				Arguments.of(8L, Boolean.TRUE),
				Arguments.of(14L, Boolean.TRUE),
				Arguments.of(15L, Boolean.FALSE),
				Arguments.of(20L, Boolean.FALSE)
		);
	}

	@ParameterizedTest(name = "simulationEndTime={0} leaves N2 at {1}")
	@MethodSource("endTimeToExpectedValue")
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void simulationHonoursItsEndTime(long simulationEndTime, Boolean expectedOnN2) throws InterruptedException {
		LogicalNetlist downstream = runTwoPartitions(simulationEndTime);

		assertThat(downstream.getState())
				.extracting(Pair::id, Pair::value)
				.containsExactly(tuple(new ComponentId(N2), expectedOnN2));
	}

	private <V extends Serializable> Runnable runMe(Simulator<V> simulator) {
		return () -> {
			try {
				simulator.simulate();
			} catch (InterruptedException ignore) {
				Thread.currentThread().interrupt();
			}
		};
	}

	/**
	 * Builds and runs the two-partition scenario to completion.
	 *
	 * @param simulationEndTime end time handed to both simulators
	 * @return the downstream netlist, whose single component holds the value observed after the run
	 */
	private LogicalNetlist runTwoPartitions(long simulationEndTime) throws InterruptedException {
		ComponentId id1 = new ComponentId(N1);
		ComponentId id2 = new ComponentId(N2);

		LogicalNetlist netlist1 = LogicalNetlist.create();
		LogicalNetlist netlist2 = LogicalNetlist.create();
		netlist1.addComponent(new String[]{"NOT", N1, Long.toString(N1_DELAY)});
		netlist2.addComponent(new String[]{"NOT", N2, Long.toString(N2_DELAY)});
		netlist1.components().get(id1).attachOutgoingConnection(new Connection(id1, id2, 0, 0));

		BlockingQueue<Event<Boolean>> inbox1 = new LinkedBlockingQueue<>();
		BlockingQueue<Event<Boolean>> inbox2 = new LinkedBlockingQueue<>();

		Map<ComponentId, BlockingQueue<Event<Boolean>>> routingMap = new HashMap<>();
		routingMap.put(id1, inbox1);
		routingMap.put(id2, inbox2);

		SimpleWorkstationRouting<Boolean> routing = new SimpleWorkstationRouting<>(routingMap);

		ConservativeSimulator<Boolean> simulator1 = new ConservativeSimulator<>(
				new InMemorySimBuffer<>(inbox1, routing), netlist1, simulationEndTime, Set.of());
		ConservativeSimulator<Boolean> simulator2 = new ConservativeSimulator<>(
				new InMemorySimBuffer<>(inbox2, routing), netlist2, simulationEndTime,
				Set.of(new InputPortKey(id2, 0)));

		simulator1.seed(List.of(
				new Event<>(id1, 0, true, 3L),
				new Event<>(id1, 0, false, 10L)
		));

		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(runMe(simulator1));
		executor.submit(runMe(simulator2));
		executor.shutdown();

		boolean finished;

		finished = executor.awaitTermination(7, TimeUnit.SECONDS);


		if (!finished) executor.shutdownNow();

		assertThat(finished)
				.as("simulators did not terminate — deadlock at simulationEndTime=%d", simulationEndTime)
				.isTrue();

		return netlist2;
	}
}