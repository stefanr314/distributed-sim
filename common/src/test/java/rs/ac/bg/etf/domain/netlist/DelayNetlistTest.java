package rs.ac.bg.etf.domain.netlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.component.DelayOnlyComponent;

import static org.assertj.core.api.Assertions.assertThat;

class DelayNetlistTest {

	private DelayNetlist<String> netlist;

	@BeforeEach
	void setUp() {
		netlist = DelayNetlist.create();
	}

	@Test
	void createComponentBuildsDelayOnlyComponentWithSinglePorts() {
		netlist.addComponent(new String[]{"D1", "7"});

		Component<String> component = netlist.components().get(new ComponentId("D1"));

		assertThat(component).isInstanceOf(DelayOnlyComponent.class);
		assertThat(component.delay()).isEqualTo(7L);
		assertThat(component.inputPort().numberOfPorts()).isEqualTo(1);
		assertThat(component.outputPort().numberOfPorts()).isEqualTo(1);
	}
}