package rs.ac.bg.etf.domain.component;

import org.junit.Assert;
import org.junit.Test;

public class ComponentTest {

	@Test
	public void testEquals() {
		// arrange
		ComponentId cid1 = new ComponentId("C1");
		ComponentId cid2 = new ComponentId("C2");
		ComponentPort in = ComponentPort.fromNumber(2);
		ComponentPort out = ComponentPort.singlePort();

		Component<Boolean> andGate = new LogicalComponent(cid1, in, out, 2, true);
		Component<Boolean> orGate = new LogicalComponent(cid2, in, out, 2, true);
		Component<Boolean> andGate2 = new LogicalComponent(cid1, in, out, 2, true);
		Component<Boolean> andGate3 = new LogicalComponent(cid1, in, out, 2, true);

		// assert
		Assert.assertNotEquals(andGate, orGate);
		Assert.assertNotEquals(orGate, andGate);

		// symmetry
		Assert.assertEquals(andGate, andGate2);
		Assert.assertEquals(andGate2, andGate);

		// transitive
		Assert.assertEquals(andGate, andGate2);
		Assert.assertEquals(andGate2, andGate3);
		Assert.assertEquals(andGate, andGate3);

	}

	private static class LogicalComponent extends Component<Boolean> {

		protected LogicalComponent(ComponentId componentId, ComponentPort inputPort, ComponentPort outputPort,
		                           long delay, boolean value) {
			super(componentId, inputPort, outputPort, delay, value);
		}

		@Override
		public Component<Boolean> withValue(Boolean newValue) {
			return null;
		}
	}
}