package pt.iscte.paddle.javardise;
import java.util.function.Consumer;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Expression.SubstitutableExpression;

public class ExpressionWidget extends EditorWidget implements SubstitutableExpression {
	private Expression expression;

	public ExpressionWidget(Composite parent, Expression.Creator f, Object e) {
		super(parent, e);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		expression = f.apply(this);
	}

	@Override
	public void substitute(Expression current, Expression newExpression) {
		current.dispose();
		this.expression = newExpression;
		this.expression.requestLayout();
		requestLayout();
	}

	@Override
	public void setData(Object data) {
		expression.setData(data);
	}

	@Override
	public boolean setFocus() {
		return expression.setFocus();
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		expression.addKeyListener(listener);
	}

	@Override
	public Expression copyTo(Composite parent) {
		return expression.copyTo(parent);
	}	

	public boolean isEmpty() {
		return expression instanceof SimpleExpressionWidget && ((SimpleExpressionWidget) expression).isEmpty();
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	@Override
	public void accept(Consumer<String> visitor) {
		expression.accept(visitor);
	}
}
