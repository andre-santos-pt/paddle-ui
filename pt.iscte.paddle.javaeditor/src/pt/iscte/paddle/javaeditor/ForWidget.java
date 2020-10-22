package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.SequenceContainer;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;

// TODO for
public class ForWidget extends ControlWidget implements SequenceContainer {

	private IVariableDeclaration var;
	private ExpressionWidget guard;
	private DeclarationWidget dec;
	private ExpressionWidget statement;

	ForWidget(Composite p, ILoop loop, IVariableDeclaration var, IVariableAssignment ass) {
		super(p, Keyword.FOR, loop, var, ass);
		
	}

	@Override
	void fillHeader(IExpression guard, Composite header,IProgramElement ...extras) {
		dec = new DeclarationWidget(header, (IVariableDeclaration) extras[0], ((IVariableAssignment) extras[1]).getExpression());
//		new FixedToken(header, ";");
		this.guard = new ExpressionWidget(header, Configuration.match(guard), guard);
		new FixedToken(header, ";");
		//statement = new ExpressionWidget(header, "statement"); // TODO to statement
	}

	public void focusDeclaration() {
		dec.setFocus();
	}

}
