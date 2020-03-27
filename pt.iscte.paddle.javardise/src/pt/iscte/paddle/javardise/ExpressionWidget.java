package pt.iscte.paddle.javardise;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IProgramElement;

public class ExpressionWidget extends EditorWidget implements Expression {
	Expression expression;

	public ExpressionWidget(Composite parent, Expression.Creator f, IProgramElement e) {
		super(parent, e);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		expression = f.apply(this);
	}

	public void set(String expression) {
		this.expression.dispose();
		this.expression = new SimpleExpressionWidget(this, expression);
		this.expression.requestLayout();
	}

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

	@Override
	public void toCode(StringBuffer buffer) {
		expression.toCode(buffer);
	}

	public boolean isEmpty() {
		return expression instanceof SimpleExpressionWidget && ((SimpleExpressionWidget) expression).isEmpty();
	}

	@Override
	public IExpression toModel() {
		return expression.toModel();
	}
}
