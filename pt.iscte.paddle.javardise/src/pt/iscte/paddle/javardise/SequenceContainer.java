package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

public interface SequenceContainer {

	SequenceWidget getBody();
	
	default Composite getParent() {
		return getBody().getParent();
	}

	boolean setFocus();
}
