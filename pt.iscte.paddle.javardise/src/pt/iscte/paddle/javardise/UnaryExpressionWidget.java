package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IUnaryExpression;
import pt.iscte.paddle.model.IUnaryOperator;

public class UnaryExpressionWidget extends EditorWidget implements Expression {
	private Token op;
	private Expression expression;

	public UnaryExpressionWidget(Composite parent, String operator, Expression.Creator f) {
		this(parent, null);
	}

	public UnaryExpressionWidget(Composite parent, IUnaryExpression ue) {
		super(parent, ue);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		op = new Token(this, ue.getOperator().getSymbol(), Constants.UNARY_OPERATORS);
		KeyListener delListener = op.addDeleteListener(() -> {
			Expression e = this.expression.copyTo(getParent());
			((Expression) getParent()).substitute(this, e);
			e.setFocus();
		});
		this.expression = Expression.match(ue.getOperand()).apply(this);
		
		// TODO
		//this.expression.addKeyListener(delListener);
		
		Menu menu = op.getMenu();
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem simple = new MenuItem(menu, SWT.NONE);
		simple.setText("simple expression");
		simple.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dispose(); // TODO
			}
		});
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		buffer.append(op);
		expression.toCode(buffer);
	}

	@Override
	public Expression copyTo(Composite parent) {
		return new UnaryExpressionWidget(parent, op.getText(), p -> expression.copyTo(p));
	}
	
	@Override
	public void substitute(Expression current, Expression newExpression) {
		current.dispose();
		this.expression = newExpression;
		this.expression.requestLayout();
	}

	@Override
	public IExpression toModel() {
		// TODO map operator
		return IUnaryExpression.create(IUnaryOperator.NOT, expression.toModel());
	}
}
