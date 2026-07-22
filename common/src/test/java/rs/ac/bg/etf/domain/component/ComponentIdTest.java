package rs.ac.bg.etf.domain.component;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

public class ComponentIdTest {

	@Test
	public void nextIdentity() {
		// Act
		ComponentId c1 = ComponentId.nextIdentity();
		ComponentId c2 = ComponentId.nextIdentity();

		//Assert
		Assert.assertNotEquals(c1, c2);
	}
	
}