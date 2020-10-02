package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.javardise.Expression.SubstitutableExpression;

public class InfixExpressionWidget<T> extends EditorWidget<T> implements SubstitutableExpression {
	private Expression left;
	private Expression right;
	private TokenWidget op;

	public InfixExpressionWidget(Composite parent, T element, String operator, Expression.Creator left) {
		this(parent, element, operator, left, p -> new SimpleExpressionWidget(p, ""));
	}

	public InfixExpressionWidget(Composite parent, T element, String operator, Expression.Creator left, Expression.Creator right) {
		super(parent, element);
		this.left = new ExpressionWidget(this, left, null);
		this.op = new TokenWidget(this, operator);
		this.right = new ExpressionWidget(this, right, null);
		setup();
	}
	

	private void setup() {
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		KeyListener delListener = op.addDeleteListener(() -> {
			Expression e = this.left.copyTo(getParent());
			Expression p = (Expression) getParent();
			if(p.isSubstitutable())
				((SubstitutableExpression) p).substitute(this, e);
			e.setFocus();
		});
		this.right.addKeyListener(delListener);

		Menu menu = op.getMenu();
		new MenuItem(menu, SWT.SEPARATOR);
	}

	@Override
	public boolean setFocus() {
		return left.setFocus();
	}

	public void focusRight() {
		right.setFocus();
	}

	@Override
	public void toCode(StringBuffer buffer) {
		left.toCode(buffer);
		buffer.append(' ');
		op.toCode(buffer);
		buffer.append(' ');
		right.toCode(buffer);
	}

	@Override
	public void setData(Object data) {
		left.setData(data);
		right.setData(data);
	}


	@Override
	public InfixExpressionWidget<T> copyTo(Composite parent) {
		return new InfixExpressionWidget<T>(parent, element, op.getText(), p -> left.copyTo(p));
	}

	@Override
	public void substitute(Expression current, Expression newExpression) {
		if(left == current) {
			left.dispose();
			left = newExpression;
		}
	}
}
