package pt.iscte.paddle.javasde;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javasde.Constants.DeleteListener;

public class InstructionWidget extends EditorWidget {
	private final Keyword keyword;
	private final Token keywordToken;
	private final ExpressionWidget expressionWidget;

	InstructionWidget(Composite parent, Keyword keyword) {
		this(parent, keyword, null);
	}

	InstructionWidget(Composite parent, Keyword keyword, String expression) {
		super(parent);
		this.keyword = keyword;
		setLayout(Constants.ROW_LAYOUT_H);
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.keywordToken = new Token(this, keyword);
		this.keywordToken.addKeyListener(deleteListener);
		Constants.addInsertLine(this.keywordToken);

		if(expression != null) {
			expressionWidget = new ExpressionWidget(this, expression);
			expressionWidget.addKeyListener(deleteListener);
		}
		else
			expressionWidget = null;
		new FixedToken(this, ";");

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
	public String toString() {
		return this.keywordToken.getText() + ";";
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
