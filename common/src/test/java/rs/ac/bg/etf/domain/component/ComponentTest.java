package rs.ac.bg.etf.domain.component;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.event.Event;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ComponentTest {

	@Test
	public void testEquals() {
		// arrange
		ComponentId cid1 = new ComponentId("C1");
		ComponentId cid2 = new ComponentId("C2");
		ComponentPort<Boolean> in = ComponentPort.fromNumber(2);
		ComponentPort<Boolean> out = ComponentPort.singlePort();

		Component<Boolean> andGate = new LogicalComponent(cid1, in, out, 2);
		Component<Boolean> orGate = new LogicalComponent(cid2, in, out, 2);
		Component<Boolean> andGate2 = new LogicalComponent(cid1, in, out, 2);
		Component<Boolean> andGate3 = new LogicalComponent(cid1, in, out, 2);

		// assert
		assertThat(andGate).isNotEqualTo(orGate);
		assertThat(orGate).isNotEqualTo(andGate);

		// symmetry
		assertThat(andGate).isEqualTo(andGate2);
		assertThat(andGate2).isEqualTo(andGate);

		// transitive
		assertThat(andGate).isEqualTo(andGate2);
		assertThat(andGate2).isEqualTo(andGate3);
		assertThat(andGate).isEqualTo(andGate3);

	}

	private static class LogicalComponent extends Component<Boolean> {

		protected LogicalComponent(ComponentId componentId, ComponentPort<Boolean> inputPort,
		                           ComponentPort<Boolean> outputPort,
		                           long delay) {
			super(componentId, inputPort, outputPort, delay);
		}

		@Contract(pure = true)
		@Override
		public @NotNull List<Event<Boolean>> execute(Event<Boolean> msg) {
			return List.of();
		}

	}
}