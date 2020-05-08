package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProcedureCall;

public class CallWidget extends EditorWidget implements Expression {
	private ComplexId id;
	private Composite args;
	private boolean statement;

	public CallWidget(Composite parent, IProcedureCall call, boolean statement, Expression.Creator ... f) {
		super(parent, call);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		this.id = new ComplexId(this, call);
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

	private ExpressionWidget addArgument(Expression.Creator f) {
		if(args.getChildren().length > 0)
			new FixedToken(args, ",");
		
		ExpressionWidget exp = new ExpressionWidget(args, f, null);
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

	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public IExpression toModel() {
		List<IExpression> list = new ArrayList<>();
		for(Control c : args.getChildren())
			if(c instanceof ExpressionWidget)
				list.add(((ExpressionWidget) c).toModel());

		return new IProcedure.UnboundProcedure(id.getId()).expression(list);
	}
}
