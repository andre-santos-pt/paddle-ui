package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

interface SequenceContainer {

	SequenceWidget getBody();
	
	default Composite getParent() {
		return getBody().getParent();
	}

	boolean setFocus();
}
