package pt.iscte.paddle.javasde;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javasde.Constants.DeleteListener;

public class IncrementationWidget extends EditorWidget {
	private final Id id;
	
	IncrementationWidget(Composite parent, String id, boolean increment) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id = new Id(this, id, false);
		this.id.addKeyListener(deleteListener);
		Constants.addInsertLine(this.id);
		
		new FixedToken(this, "++");
		new FixedToken(this, ";");
	}
	
	@Override
	public boolean setFocus() {
		return id.setFocus();
	}
	
	
	@Override
	public String toString() {
		return this.id.getText() + "++;";
	}
	
}
