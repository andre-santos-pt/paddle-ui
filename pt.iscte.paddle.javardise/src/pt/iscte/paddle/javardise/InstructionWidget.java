package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IExpression;

public class InstructionWidget extends EditorWidget implements TextWidget {
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
		keywordToken = new Token(this, keyword);
		keywordToken.addKeyListener(deleteListener);
		Constants.addInsertLine(keywordToken);

		if(expression != null) {
			Markable<CodeElement> markable = new Markable<CodeElement>(this, p -> new ExpressionWidget(p, Expression.match(expression), expression), expression);
			expressionWidget = (ExpressionWidget) markable.target;
//			expressionWidget = new ExpressionWidget(this, Expression.match(expression), expression);
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
