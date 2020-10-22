package pt.iscte.paddle.javardise;

import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

public interface Expression {

	interface Creator extends Function<Composite, Expression> {  }

	Expression copyTo(Composite parent);

	void dispose();
	boolean setFocus();
	void requestLayout();
	void setData(Object data);

	void addKeyListener(KeyListener listener);

	void accept(Consumer<String> visitor);

	default boolean isSubstitutable() {
		return this instanceof SubstitutableExpression;
	}

	interface SubstitutableExpression extends Expression {
		void substitute(Expression current, Expression newExpression);
	}
	
}
