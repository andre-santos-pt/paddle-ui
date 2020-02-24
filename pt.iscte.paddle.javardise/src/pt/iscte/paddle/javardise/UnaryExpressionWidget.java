package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IUnaryExpression;
import pt.iscte.paddle.model.IUnaryOperator;

public class UnaryExpressionWidget extends EditorWidget implements Expression {
	private Token op;
	private Expression expression;

	public UnaryExpressionWidget(EditorWidget parent, String operator, Expression.Creator f) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		op = new Token(this, operator, Constants.UNARY_OPERATORS);
		this.expression = f.apply(this);

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
	public Expression copyTo(EditorWidget parent) {
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
