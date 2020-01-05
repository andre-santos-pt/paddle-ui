package pt.iscte.paddle.javasde;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javasde.Constants.DeleteListener;

public class InstructionWidget extends EditorWidget {
	private final Token keyword;
	private final ExpressionWidget expressionWidget;
	
	InstructionWidget(Composite parent, Keyword keyword) {
		this(parent, keyword, null);
	}
	
	InstructionWidget(Composite parent, Keyword keyword, String expression) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.keyword = new Token(this, keyword);
		this.keyword.addKeyListener(deleteListener);
		Constants.addInsertLine(this.keyword);
		
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
		return keyword.setFocus();
	}
	
	void focusExpression() {
		if(expressionWidget != null)
			expressionWidget.setFocus();
	}
	
	boolean is(String keyword) {
		return this.keyword.isKeyword(keyword);
	}
	
	@Override
	public String toString() {
		return this.keyword.getText() + ";";
	}
	
}
