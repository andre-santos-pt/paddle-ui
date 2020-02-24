package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.model.IBinaryExpression;
import pt.iscte.paddle.model.IBinaryOperator;
import pt.iscte.paddle.model.IExpression;

public class BinaryExpressionWidget extends EditorWidget implements Expression {
	private Expression left;
	private Expression right;
	private Token op;
	private boolean brackets;
	
	private RowData data = new RowData();
	
	public BinaryExpressionWidget(EditorWidget parent, String operator) {
		this(parent, 
				e -> new SimpleExpressionWidget(e, "left"),
				e -> new SimpleExpressionWidget(e, "right"),
				operator);
	}
	
	public BinaryExpressionWidget(EditorWidget parent, Expression.Creator left, String operator) {
		this(parent, 
				left,
				e -> new SimpleExpressionWidget(e, ""),
				operator);
	}
	
	public BinaryExpressionWidget(EditorWidget parent, Expression.Creator left, Expression.Creator right, String operator) {
		super(parent);
		brackets = false;
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		data.exclude = !brackets;
		
		FixedToken lBracket = new FixedToken(this, "(");
		lBracket.setLayoutData(data);
	
		this.left = new ExpressionWidget(this, left);
		this.op = new Token(this, operator, Constants.ARITHMETIC_OPERATORS, Constants.RELATIONAL_OPERATORS, Constants.LOGICAL_OPERATORS);
		this.right = new ExpressionWidget(this, right);
		
		Token rBracket = new Token(this, ")");
		rBracket.setLayoutData(data);
		
		Menu menu = op.getMenu();
		new MenuItem(menu, SWT.SEPARATOR);
		
		MenuItem brack = new MenuItem(menu, SWT.NONE);
		brack.setText("( ... )");
		brack.setAccelerator('(');
		brack.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				data.exclude = brackets;
				brackets = !brackets;
				lBracket.setVisible(brackets);
				rBracket.setVisible(brackets);
				BinaryExpressionWidget.this.requestLayout();
			}
		});
		
	}
	
	@Override
	public boolean setFocus() {
		left.setFocus();
		return true;
	}

	public void focusRight() {
		right.setFocus();
	}

	@Override
	public void toCode(StringBuffer buffer) {
		if(brackets) buffer.append('(');
		left.toCode(buffer);
		buffer.append(' ');
		op.toCode(buffer);
		buffer.append(' ');
		right.toCode(buffer);
		if(brackets) buffer.append(')');
	}

	@Override
	public IExpression toModel() {
		// TODO match operator
		return IBinaryExpression.create(IBinaryOperator.ADD, left.toModel(), right.toModel());
	}
	
//	private void deleteOperator() {
//		ExpressionWidget parent = (ExpressionWidget) getParent();
//		if(left.expression instanceof SimpleExpressionWidget)
//			parent.expression = new SimpleExpressionWidget(parent, left.expression.toString(), false); // TODO propagate
//		else if(left.expression instanceof BinaryExpressionWidget)
//			parent.expression = new BinaryExpressionWidget(parent, ((BinaryExpressionWidget) left.expression).op.toString());
//		dispose();
//		parent.expression.requestLayout();
//		parent.expression.setFocus();
//	}

	@Override
	public Expression copyTo(EditorWidget parent) {
		return new BinaryExpressionWidget(parent, p -> left.copyTo(p), op.getText());
	}

	@Override
	public void substitute(Expression current, Expression newExpression) {
		if(left == current) {
			left.dispose();
			left = newExpression;
		}
	}


}
