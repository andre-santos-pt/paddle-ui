package pt.iscte.paddle.javasde;

import java.util.function.Function;

import pt.iscte.paddle.javasde.Constants.DeleteListener;

public class AssignmentWidget extends EditorWidget {

	private final ExpressionWidget id;
	private final ExpressionWidget expression;
	private boolean statement;
	private boolean array;
	
	AssignmentWidget(SequenceWidget parent, String id, String expression, boolean statement, boolean array) {
		super(parent);
		this.statement = statement;
		this.array = array;
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
		id.toCode(buffer);
		buffer.append(" = ");
		expression.toCode(buffer);
		buffer.append(";");
	}

}
