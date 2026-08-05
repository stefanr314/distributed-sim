package rs.ac.bg.etf.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.netlist.LogicalNetlist;
import rs.ac.bg.etf.domain.netlist.Pair;
import rs.ac.bg.etf.domain.ports.SimBuffer;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Proves the conservative synchronization mechanism end to end across two real, independently running
 * threads: {@code N1} (partition 1) sends a real event to {@code N2} (partition 2) over a shared,
 * in-memory {@link SimBuffer}, and partition 1's final null message (sent once it locally terminates)
 * is what eventually releases partition 2 from waiting on its only external channel.
 */
class ConservativeSimulatorIT {

	private <V> Runnable runMe(Simulator<V> simulator) {
		return () -> {
			try {
				simulator.simulate();
			} catch (InterruptedException ignore) {
				Thread.currentThread().interrupt();
			}
		};
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void twoSimulatorsExchangeValuesOverBuffer() {
		ComponentId id1 = new ComponentId("N1");
		ComponentId id2 = new ComponentId("N2");

		LogicalNetlist netlist1 = LogicalNetlist.create();
		LogicalNetlist netlist2 = LogicalNetlist.create();

		netlist1.addComponent(new String[]{"NOT", "N1", "1", "5"});
		netlist2.addComponent(new String[]{"NOT", "N2", "1", "10"});

		Component<Boolean> component1 = netlist1.components().get(id1);
		component1.attachOutgoingConnection(new Connection(id1, id2, 0, 0));

		BlockingQueue<Event<Boolean>> inbox1 = new LinkedBlockingQueue<>();
		BlockingQueue<Event<Boolean>> inbox2 = new LinkedBlockingQueue<>();

		Map<ComponentId, BlockingQueue<Event<Boolean>>> routingMap = new HashMap<>();
		routingMap.put(id1, inbox1);
		routingMap.put(id2, inbox2);

		SimpleWorkstationRouting<Boolean> workstationRouting = new SimpleWorkstationRouting<>(routingMap);

		SimBuffer<Boolean> simBuffer1 = new InMemorySimBuffer<>(inbox1, workstationRouting);
		SimBuffer<Boolean> simBuffer2 = new InMemorySimBuffer<>(inbox2, workstationRouting);

		long simulationEndTime = 20L;
		InputPortKey externalConnectionOnComponent2 = new InputPortKey(id2, 0);

		ConservativeSimulator<Boolean> simulator1 = new ConservativeSimulator<>(simBuffer1, netlist1,
				simulationEndTime, Set.of());
		ConservativeSimulator<Boolean> simulator2 = new ConservativeSimulator<>(simBuffer2, netlist2,
				simulationEndTime, Set.of(externalConnectionOnComponent2));

		simulator1.seed(List.of(new Event<>(id1, 0, true, 3L)));


		ExecutorService executorService = Executors.newFixedThreadPool(2);
		executorService.submit(runMe(simulator1));
		executorService.submit(runMe(simulator2));

		executorService.shutdown();

		try {
			boolean finished = executorService.awaitTermination(7, TimeUnit.SECONDS);
			if (!finished) {
				executorService.shutdownNow();
			}

			assertThat(finished).isTrue();
		} catch (InterruptedException ack) {
			Thread.currentThread().interrupt();
			executorService.shutdown();
		}

		assertThat(netlist2.getState())
				.extracting(Pair::id, Pair::value)
				.containsExactly(tuple(new ComponentId("N2"), Optional.of(true)));
	}
}