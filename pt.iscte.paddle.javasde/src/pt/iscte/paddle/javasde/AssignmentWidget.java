package pt.iscte.paddle.javasde;

import java.util.function.Function;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import pt.iscte.paddle.javasde.Constants.DeleteListener;

public class AssignmentWidget extends EditorWidget {

	private final ExpressionWidget id;
	private final ExpressionWidget expression;

	AssignmentWidget(SequenceWidget parent, String id, String expression, boolean statement, boolean array) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		Function<EditorWidget, Expression> f = 
				e -> array ? new ArrayElementExpression(this, id, "expression") : new SimpleExpressionWidget(e, id, false);
		this.id = new ExpressionWidget(this, f);
		this.id.addKeyListener(deleteListener);
		if(this.id.expression instanceof TextWidget)
			Constants.addInsertLine((TextWidget) this.id.expression);
		new FixedToken(this, "=");
		this.expression = new ExpressionWidget(this, "expression");
		this.expression.addKeyListener(deleteListener);
		if(statement)
			new FixedToken(this, ";");		
	}
	
	public boolean setFocus() {
		return id.setFocus();
	}
	
	public void focusExpression() {
		expression.setFocus();
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		buffer.append(id).append(" = ");
		expression.toCode(buffer);
	}

}
