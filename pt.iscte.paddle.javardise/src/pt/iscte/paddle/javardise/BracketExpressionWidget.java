package pt.iscte.paddle.javardise;

import java.util.function.Consumer;

import org.eclipse.swt.events.KeyListener;
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
		
		KeyListener delListener = right.addDeleteListener(() -> {
			Expression e = this.expression.copyTo(getParent());
			Expression p = (Expression) getParent();
			if(p.isSubstitutable())
				((SubstitutableExpression) p).substitute(this, e);
			e.setFocus();
		});
		left.addKeyListener(delListener);
		expression.addKeyListener(delListener);
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
	public void accept(Consumer<String> visitor) {
		expression.accept(visitor);
	}
}
