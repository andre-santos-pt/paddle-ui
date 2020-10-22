package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.Id;
import pt.iscte.paddle.javardise.TokenWidget;
import pt.iscte.paddle.model.IVariableAssignment;

public class IncrementationWidget extends EditorWidget {
	private final Id id; // TODO to complex
	
	IncrementationWidget(Composite parent, IVariableAssignment assignment) {
		super(parent, assignment);
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		this.id = new Id(this, assignment.getTarget().getId());
		String op = assignment.is(Flag.INC.name()) ? "++" : "--";
		TokenWidget w = new TokenWidget(this, op);
		KeyListener delListener = w.addDeleteListener(() -> assignment.remove());
		id.addKeyListener(delListener);
		new FixedToken(this, ";");
	}
	
	@Override
	public boolean setFocus() {
		return id.setFocus();
	}
	
}
