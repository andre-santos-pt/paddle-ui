package pt.iscte.paddle.javardise;

import java.util.function.Function;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.api.ICodeElement;

public interface Expression extends ICodeElement {

	interface Creator extends Function<Composite, Expression> {  }

	Expression copyTo(Composite parent);

	void dispose();
	boolean setFocus();
	void requestLayout();
	void setData(Object data);

	void addKeyListener(KeyListener listener);


	default boolean isSubstitutable() {
		return this instanceof SubstitutableExpression;
	}

	interface SubstitutableExpression extends Expression {
		void substitute(Expression current, Expression newExpression);
	}
	

	

	

	
}
