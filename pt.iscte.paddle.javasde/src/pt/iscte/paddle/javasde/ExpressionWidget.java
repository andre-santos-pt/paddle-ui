package pt.iscte.paddle.javasde;
import java.util.function.Function;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

public class ExpressionWidget extends EditorWidget implements Expression {
	Expression expression;

	public ExpressionWidget(EditorWidget parent, String literal) {
		this(parent, w -> new SimpleExpressionWidget(w, literal, false));
	}

	public ExpressionWidget(EditorWidget parent, Function<EditorWidget, Expression> sup) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		expression = sup.apply(this);
	}

	public void set(String expression) {
		this.expression.dispose();
		this.expression = new SimpleExpressionWidget(this, expression, false);
		this.expression.requestLayout();
	}
	
	public void substitute(Expression current, Expression newExpression) {
		current.dispose();
		this.expression = newExpression;
		this.expression.requestLayout();
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
	public Expression copyTo(EditorWidget parent) {
		return expression.copyTo(parent);
	}	
	
	@Override
	public void toCode(StringBuffer buffer) {
		expression.toCode(buffer);
	}

	public boolean isEmpty() {
		return expression.isEmpty();
	}
}
