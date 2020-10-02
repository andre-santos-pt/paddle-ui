package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.javardise.Expression.SubstitutableExpression;

public class PrefixExpressionWidget<T> extends EditorWidget<T> implements SubstitutableExpression {
	private TokenWidget op;
	private Expression expression;

	public PrefixExpressionWidget(Composite parent, T element, String operator, Expression.Creator f) {
		super(parent, element);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		op = new TokenWidget(this, operator); //JavaConstants.UNARY_OPERATORS);
		
		KeyListener delListener = op.addDeleteListener(() -> {
			Expression e = this.expression.copyTo(getParent());
			Expression p = (Expression) getParent();
			if(p.isSubstitutable())
				((SubstitutableExpression) p).substitute(this, e);
//			((Expression) getParent()).substitute(this, e);
			e.setFocus();
		});
		this.expression = f.apply(this);
		
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
		return new PrefixExpressionWidget(parent, element, op.getText(), p -> expression.copyTo(p));
	}
	
	@Override
	public void substitute(Expression current, Expression newExpression) {
		current.dispose();
		this.expression = newExpression;
		this.expression.requestLayout();
	}

}
