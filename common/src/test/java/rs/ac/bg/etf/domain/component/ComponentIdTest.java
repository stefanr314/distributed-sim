package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ComponentIdTest {

	@Test
	public void testEquals() {
		// Act
		String clientId = "C1";
		ComponentId c1 = new ComponentId(clientId);
		ComponentId c2 = new ComponentId(clientId);
		ComponentId c3 = new ComponentId(clientId);

		//Assert
		assertThat(c1).isEqualTo(c2);
		assertThat(c2).isEqualTo(c1);

		assertThat(c1).isEqualTo(c2);
		assertThat(c2).isEqualTo(c3);
		assertThat(c3).isEqualTo(c1);
	}

	@Test
	public void testHashCode() {
		String clientId = "C1";
		ComponentId c1 = new ComponentId(clientId);
		ComponentId c2 = new ComponentId(clientId);

		assertThat(c1).isEqualTo(c2);
		assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
	}

}