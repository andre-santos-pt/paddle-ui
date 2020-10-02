package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

public class BracketExpressionWidget extends EditorWidget implements Expression  {
	private ExpressionWidget expression;
	private TokenWidget left;
	private TokenWidget right;
	
	public BracketExpressionWidget(Composite parent, Creator f, String open, String close) {
		super(parent);
		left = new TokenWidget(this, open);
		expression = new ExpressionWidget(this, f, null);
		right = new TokenWidget(this, close);
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
	public void toCode(StringBuffer buffer) {
		left.toCode(buffer);
		expression.toCode(buffer);
		right.toCode(buffer);
	}
}
