package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.SequenceContainer;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILoop;

// TODO for
public class ForWidget extends ControlWidget implements SequenceContainer {

	private ExpressionWidget guard;
	private DeclarationWidget dec;
	private ExpressionWidget statement;

	ForWidget(SequenceWidget parent, ILoop loop) {
		super(parent, Keyword.FOR, loop);
	}

	@Override
	void fillHeader(IExpression guard, Composite header) {
//		dec = new DeclarationWidget(header, IType.INT, "i", "0");
//		this.guard = new ExpressionWidget(header, Expression.match(guard));
		new FixedToken(header, ";");
		//statement = new ExpressionWidget(header, "statement"); // TODO to statement
	}
	

	public void focusDeclaration() {
		dec.setFocus();
	}

}
