package pt.iscte.paddle.javardise;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IStatement;

public class InstructionWidget extends EditorWidget implements TextWidget {
	private final Keyword keyword;
	private final Token keywordToken;
	private ExpressionWidget expressionWidget;
	private FixedToken semiColon;
	private KeyListener delListener;
			
	InstructionWidget(Composite parent, Keyword keyword, IStatement statement) {
		this(parent, keyword, statement, null);
	}

	InstructionWidget(Composite parent, Keyword keyword, IStatement statement, IExpression expression) {
		super(parent, statement);
		this.keyword = keyword;
		setLayout(Constants.ROW_LAYOUT_H);
		keywordToken = new Token(this, keyword);
		delListener = keywordToken.addDeleteListener(() -> statement.remove());
		Constants.addInsertLine(keywordToken);

		if(expression != null)
			addExpression(expression);
		
		semiColon = new FixedToken(this, ";");
	}

	void addExpression(IExpression expression) {
		expressionWidget = new ExpressionWidget(this, Expression.match(expression), expression);
		if(semiColon != null)
			expressionWidget.moveAbove(semiColon.getControl());
		expressionWidget.addKeyListener(delListener);
		expressionWidget.requestLayout();
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

	@Override
	public Text getWidget() {
		return keywordToken.getWidget();
	}
}
