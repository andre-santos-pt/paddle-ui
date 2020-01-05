package pt.iscte.paddle.javasde;

import org.eclipse.swt.widgets.Composite;

interface SequenceContainer {

	SequenceWidget getBody();
	
	default Composite getParent() {
		return getBody().getParent();
	}

	boolean setFocus();
}
