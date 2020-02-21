package pt.iscte.paddle.javardise;

import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IType;

public class ForWidget extends ControlWidget implements SequenceContainer {

	private ExpressionWidget guard;
	private DeclarationWidget dec;
	private ExpressionWidget statement;

	ForWidget(SequenceWidget parent, String guard, IBlock block) {
		super(parent,Keyword.FOR, guard, block);
	}

	@Override
	void fillHeader(String guard, EditorWidget header) {
		dec = new DeclarationWidget(header, IType.INT, "i", "0");
		this.guard = new ExpressionWidget(header, guard);
		new FixedToken(header, ";");
		statement = new ExpressionWidget(header, "statement");
	}
	

	public void focusDeclaration() {
		dec.setFocus();
	}

}
