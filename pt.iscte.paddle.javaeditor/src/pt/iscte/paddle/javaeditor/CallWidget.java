package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.Expression;
import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.SimpleExpressionWidget;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;

public class CallWidget extends EditorWidget<IProgramElement> implements Expression {
	private ExpressionChain id;
	private Composite args;
	private boolean statement;

	public CallWidget(Composite parent, IProcedureCall call, boolean statement, Expression.Creator ... f) {
		super(parent, call);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		this.id = new ExpressionChain(this, call);
		this.statement = statement;
		new FixedToken(this, "(");
		args = new Composite(this, SWT.NONE);
		args.setLayout(Constants.ROW_LAYOUT_H);
		args.setBackground(Constants.COLOR_BACKGROUND);
		for(Expression.Creator c : f)
			addArgument(c);

		new FixedToken(this, ")");
		if(statement) {
			new FixedToken(this, ";");
			this.id.addDeleteListener(() -> call.remove());
		}
		
		if(f.length == 0)
			addArgument(p -> new SimpleExpressionWidget(p, ""));
	}

	private ExpressionWidget<IExpression> addArgument(Expression.Creator f) {
		if(args.getChildren().length > 0)
			new FixedToken(args, ",");
		
		ExpressionWidget<IExpression> exp = new ExpressionWidget<>(args, f, null);
		exp.requestLayout();
		exp.setFocus();
		exp.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.character == ',') {
					addArgument(p -> new SimpleExpressionWidget(p, "argument"));
				}
			}
		});
		return exp;
	}
	
	
	
	@Override
	public void toCode(StringBuffer buffer) {
		id.toCode(buffer);
		buffer.append('(');
		for(Control c : args.getChildren()) {
			if(c instanceof ExpressionWidget)
				((ExpressionWidget) c).toCode(buffer);
			else if(c instanceof Label)
				buffer.append(", ");
		}
		buffer.append(')');
		if(statement)
			buffer.append(';');
	}

	public void focusArgument() {
		args.setFocus();
	}

	@Override
	public Expression copyTo(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
