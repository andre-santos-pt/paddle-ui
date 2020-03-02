package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IExpression;

public class BracketsWidget extends EditorWidget implements Expression  {
	private ExpressionWidget expression;
	private Token left;
	private Token right;
	
	public BracketsWidget(Composite parent, Creator f) {
		super(parent);
		left = new Token(this, "(");
		expression = new ExpressionWidget(this, f, null);
		right = new Token(this, ")");
	}

	@Override
	public boolean setFocus() {
		return expression.setFocus();
	}

	@Override
	public Expression copyTo(Composite parent) {
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

	@Override
	public void toCode(StringBuffer buffer) {
		left.toCode(buffer);
		expression.toCode(buffer);
		right.toCode(buffer);
	}
}
