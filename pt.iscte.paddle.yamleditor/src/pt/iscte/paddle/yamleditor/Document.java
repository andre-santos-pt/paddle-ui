package pt.iscte.paddle.yamleditor;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.FixedToken;

public class Document extends EditorWidget {
	private FixedToken header;

	public Document(Composite parent) {
		super(parent);
		header = new FixedToken(parent, "---");
	}

	

}
