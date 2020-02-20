package pt.iscte.paddle.javasde;

import org.eclipse.swt.events.KeyListener;

public interface Expression {

	Expression copyTo(EditorWidget parent);
	
	void dispose();
	boolean setFocus();
	void requestLayout();
	void setData(Object data);

	void addKeyListener(KeyListener listener);
	
	void toCode(StringBuffer buffer);

	void substitute(Expression current, Expression newExpression);

	default boolean isEmpty() {
		return (this instanceof SimpleExpressionWidget) && ((SimpleExpressionWidget) this).text.getText().isBlank();
	}
}
