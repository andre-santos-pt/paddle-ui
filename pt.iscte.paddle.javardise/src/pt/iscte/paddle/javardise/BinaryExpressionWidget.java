package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.model.IBinaryExpression;
import pt.iscte.paddle.model.IBinaryOperator;
import pt.iscte.paddle.model.IExpression;

public class BinaryExpressionWidget extends EditorWidget implements Expression {
	private Expression left;
	private Expression right;
	private Token op;
	
	public BinaryExpressionWidget(Composite parent, String operator) {
		this(parent, 
				e -> new SimpleExpressionWidget(e, "left"),
				e -> new SimpleExpressionWidget(e, "right"),
				operator);
	}
	
	public BinaryExpressionWidget(Composite parent, Expression.Creator left, String operator) {
		this(parent, 
				left,
				e -> new SimpleExpressionWidget(e, ""),
				operator);
	}
	
	public BinaryExpressionWidget(Composite parent, Expression.Creator left, Expression.Creator right, String operator) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
	
		this.left = new ExpressionWidget(this, left, null);
		this.op = new Token(this, operator, Constants.ARITHMETIC_OPERATORS, Constants.RELATIONAL_OPERATORS, Constants.LOGICAL_OPERATORS);
		this.right = new ExpressionWidget(this, right, null);
		
		
		KeyListener delListener = op.addDeleteListener(() -> {
			Expression e = this.left.copyTo(getParent());
			((Expression) getParent()).substitute(this, e);
			e.setFocus();
		});
		this.right.addKeyListener(delListener);
		
		Menu menu = op.getMenu();
		new MenuItem(menu, SWT.SEPARATOR);
	}
	
	@Override
	public boolean setFocus() {
		return left.setFocus();
	}

	public void focusRight() {
		right.setFocus();
	}

	@Override
	public void toCode(StringBuffer buffer) {
		left.toCode(buffer);
		buffer.append(' ');
		op.toCode(buffer);
		buffer.append(' ');
		right.toCode(buffer);
	}

	@Override
	public IExpression toModel() {
		// TODO match operator
		return IBinaryExpression.create(IBinaryOperator.ADD, left.toModel(), right.toModel());
	}

	@Override
	public void setData(Object data) {
		left.setData(data);
		right.setData(data);
	}
	

	@Override
	public Expression copyTo(Composite parent) {
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
