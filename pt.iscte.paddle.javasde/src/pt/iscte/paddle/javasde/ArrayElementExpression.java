package pt.iscte.paddle.javasde;

import java.util.function.Function;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class ArrayElementExpression extends EditorWidget implements Expression {

	private Id id;
	private ExpressionWidget expression;
	private Token rightBracket;
	
	public ArrayElementExpression(EditorWidget parent, String varId, String exp) {
		this(parent, varId, p -> new SimpleExpressionWidget(p, exp, false));
	}
	
	public ArrayElementExpression(EditorWidget parent, String varId, Function<EditorWidget, Expression> f) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		id = new Id(this, varId, false);
		new FixedToken(this, "[");
		expression = new ExpressionWidget(this, f);
		rightBracket = new Token(this, "]");
	}

	void addExpressionInserts() {
		rightBracket.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				String op = Constants.matchBinaryOperator(e.character);
				if(op != null) {
					EditorWidget parent = (EditorWidget) ArrayElementExpression.this.getParent();
					BinaryExpressionWidget b = new BinaryExpressionWidget(parent, 
							p -> new ArrayElementExpression(p, id.getText(), exp -> expression.copyTo(exp)), op);
					b.focusRight();
					b.requestLayout();
					dispose();
				}
			}
		});
	}

	// TODO field on array
//	void addFieldInserts() {
//		insert.addAction(new InsertWidget.Action("field access", '0') {
//			
//			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
//				return c == '.';
//			}
//
//			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
////				EditorWidget parent = (EditorWidget) ArrayElementExpression.this.getParent();
////				FieldExpression w = new FieldExpression(parent, varId)
////				String op = Constants.matchBinaryOperator(c);
////				BinaryExpressionWidget b = new BinaryExpressionWidget(parent, 
////						p -> new ArrayElementExpression(p, id.getText(), e -> expression.copyTo(e)), op);
////				b.focusRight();
////				b.requestLayout();
////				dispose();
//			}
//		});
//	}
	
	@Override
	public boolean setFocus() {
		expression.setFocus();
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
	
	@Override
	public void toCode(StringBuffer buffer) {
		id.toCode(buffer);
		buffer.append('[');
		expression.toCode(buffer);
		buffer.append(']');
	}
	
	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub
		
	}
}
