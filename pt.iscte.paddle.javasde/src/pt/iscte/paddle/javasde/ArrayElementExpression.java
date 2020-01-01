package pt.iscte.paddle.javasde;

import java.util.List;
import java.util.function.Function;

public class ArrayElementExpression extends EditorWidget implements Expression {

	private Id id;
	private ExpressionWidget expression;
	private InsertWidget insert;
	
	public ArrayElementExpression(EditorWidget parent, String varId, String exp) {
		this(parent, varId, p -> new SimpleExpressionWidget(p, exp, false));
	}
	
	public ArrayElementExpression(EditorWidget parent, String varId, Function<EditorWidget, Expression> f) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		id = new Id(this, varId, false);
		new FixedToken(this, "[");
		expression = new ExpressionWidget(this, f);
		new FixedToken(this, "]");
		insert = new InsertWidget(this);
	}

	void addExpressionInserts() {
		insert.addAction(new InsertWidget.Action("binary expression", '0') {
			
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return Constants.matchBinaryOperator(c) != null;
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				String op = Constants.matchBinaryOperator(c);
				EditorWidget parent = (EditorWidget) ArrayElementExpression.this.getParent();
				BinaryExpressionWidget b = new BinaryExpressionWidget(parent, 
						p -> new ArrayElementExpression(p, id.getText(), e -> expression.copyTo(e)), op);
				b.focusRight();
				b.requestLayout();
				dispose();
			}
		});
	}

	void addFieldInserts() {
		insert.addAction(new InsertWidget.Action("field access", '0') {
			
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return c == '.';
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
//				EditorWidget parent = (EditorWidget) ArrayElementExpression.this.getParent();
//				FieldExpression w = new FieldExpression(parent, varId)
//				String op = Constants.matchBinaryOperator(c);
//				BinaryExpressionWidget b = new BinaryExpressionWidget(parent, 
//						p -> new ArrayElementExpression(p, id.getText(), e -> expression.copyTo(e)), op);
//				b.focusRight();
//				b.requestLayout();
//				dispose();
				System.out.println("TODO field");
			}
		});
	}
	
	@Override
	public boolean setFocus() {
		id.setFocus();
		return true;
	}
	

	public void focusExpression() {
		id.setForeground(Constants.FONT_COLOR);
		id.requestLayout();
		expression.setFocus();
	}

	@Override
	public Expression copyTo(EditorWidget parent) {
		return new ArrayElementExpression(parent, id.getText(), p -> expression.copyTo(p));
	}
}
