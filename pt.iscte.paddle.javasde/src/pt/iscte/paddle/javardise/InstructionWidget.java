package pt.iscte.paddle.javardise;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IExpression;

public class InstructionWidget extends EditorWidget {
	private final Keyword keyword;
	private final Token keywordToken;
	private final ExpressionWidget expressionWidget;

	InstructionWidget(Composite parent, Keyword keyword) {
		this(parent, keyword, null);
	}

	InstructionWidget(Composite parent, Keyword keyword, IExpression expression) {
		super(parent);
		
		this.keyword = keyword;
		setLayout(Constants.ROW_LAYOUT_H);
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.keywordToken = new Token(this, keyword);
		this.keywordToken.addKeyListener(deleteListener);
//		Constants.addInsertLine(this.keywordToken);

		if(expression != null) {
			expressionWidget = new ExpressionWidget(this, Expression.match(expression));
			expressionWidget.addKeyListener(deleteListener);
		}
		else
			expressionWidget = null;
		new FixedToken(this, ";");

//		final ControlDecoration dec = new ControlDecoration(this, SWT.LEFT | SWT.CENTER);
//		dec.setMarginWidth(10);
//		dec.setDescriptionText("blabla");
//		dec.setImage(new Image(Display.getDefault(), "/Users/andresantos/git/paddle-ui/pt.iscte.paddle.javasde/arrow.png")); 
		
	}

	@Override
	public boolean setFocus() {
		return keywordToken.setFocus();
	}

	void focusExpression() {
		if(expressionWidget != null)
			expressionWidget.setFocus();
	}

	boolean is(String keyword) {
		return this.keywordToken.isKeyword(keyword);
	}


	@Override
	public void toCode(StringBuffer buffer) {
		keyword.toCode(buffer);
		if(expressionWidget != null) {
			if(!expressionWidget.isEmpty() || keyword != Keyword.RETURN) {
				buffer.append(" ");
				expressionWidget.toCode(buffer);
			}
		}
		buffer.append(";").append(System.lineSeparator());
	}
}
