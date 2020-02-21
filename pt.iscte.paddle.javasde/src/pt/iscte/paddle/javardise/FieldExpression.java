package pt.iscte.paddle.javardise;

import pt.iscte.paddle.model.IExpression;

public class FieldExpression extends EditorWidget implements Expression {

	private Id id;
	private ExpressionWidget expression;

	public FieldExpression(EditorWidget parent, String varId) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_DOT);
		id = new Id(this, varId, false);
		new FixedToken(this, ".");
		expression = new ExpressionWidget(this, "field");		
	}

	@Override
	public boolean setFocus() {
		id.setFocus();
		return true;
	}
	

	public void focusExpression() {
		id.setForeground(Constants.FONT_COLOR);
		expression.setFocus();
	}

	@Override
	public Expression copyTo(EditorWidget parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IExpression toModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
