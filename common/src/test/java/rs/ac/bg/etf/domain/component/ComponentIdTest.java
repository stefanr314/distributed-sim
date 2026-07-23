package rs.ac.bg.etf.domain.component;

import org.junit.Assert;
import org.junit.Test;

public class ComponentIdTest {

	@Test
	public void testEquals() {
		// Act
		String clientId = "C1";
		ComponentId c1 = new ComponentId(clientId);
		ComponentId c2 = new ComponentId(clientId);
		ComponentId c3 = new ComponentId(clientId);

		//Assert
		Assert.assertEquals(c1, c2);
		Assert.assertEquals(c2, c1);

		Assert.assertEquals(c1, c2);
		Assert.assertEquals(c2, c3);
		Assert.assertEquals(c1, c3);
	}

	@Test
	public void testHashCode() {
		String clientId = "C1";
		ComponentId c1 = new ComponentId(clientId);
		ComponentId c2 = new ComponentId(clientId);

		Assert.assertEquals(c1, c2);
		Assert.assertEquals(c1.hashCode(), c2.hashCode());
	}

}