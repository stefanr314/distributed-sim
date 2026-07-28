package rs.ac.bg.etf.domain.netlist;

import rs.ac.bg.etf.domain.component.Component;

public class LogicalNetlist extends Netlist<Boolean> {
	//FIXME
	@Override
	protected Component<Boolean> createComponent(String[] data) {
		if (data == null) throw new AssertionError("FIX THIS");
		//prepare data
		String logicalGateType = data[0];
		// prepare the component's gate body

		//convert string to enum of logical gate types

		//switch logical gate types and return the concrete type
		return null;
	}
}