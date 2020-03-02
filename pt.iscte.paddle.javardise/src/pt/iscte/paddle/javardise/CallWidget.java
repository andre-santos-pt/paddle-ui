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

public class CallWidget extends EditorWidget implements Expression {
	private ComplexId id;
	private Composite args;
	private boolean statement;
	private InsertWidget insert;

	public CallWidget(Composite parent, String id, boolean statement, Expression.Creator ... f) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);

		this.id = new ComplexId(this, id, false);
		this.statement = statement;
		new FixedToken(this, "(");
		args = new Composite(this, SWT.NONE);
		args.setLayout(Constants.ROW_LAYOUT_H);
		args.setBackground(Constants.COLOR_BACKGROUND);
		insert = new InsertWidget(args, true, token -> Constants.isNumber(token));
		//		insert.setHideMode();

		for(Expression.Creator c : f)
			addArgument(c);

		new FixedToken(this, ")");
		if(statement) {
			new FixedToken(this, ";");
			this.id.addKeyListener(new Constants.DeleteListener(this));
		}

		insert.addAction(new InsertWidget.Action("argument", (char) 0) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return (!Keyword.is(text) || Constants.isNumber(text)) && (c == ',' && !text.isBlank() || c == SWT.CR && args.getChildren().length == 1) ||
						Id.isValid(text) && c == '(';
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				//				insert.dispose();
				ExpressionWidget arg = null;
				if(c == ',') {
					if(args.getChildren().length == 1)
						addArgument(p -> new SimpleExpressionWidget(p, text));
					arg = addArgument(p -> new SimpleExpressionWidget(p, "argument"));
					arg.setFocus();
				}
				else if(c == '(') {
					arg = addArgument(p -> new CallWidget(p, text, false));
					((CallWidget) arg.expression).focusArgument();
				}
				else { // CR
					if(!text.isBlank()) {
						arg = addArgument(p -> new SimpleExpressionWidget(p, text));
						arg.traverse(SWT.TRAVERSE_TAB_NEXT);
					}
				}
				if(arg != null) {
					insert.moveBelow(arg);
					arg.requestLayout();
				}
				else
					insert.traverse(SWT.TRAVERSE_TAB_NEXT);
			}
		});
	}

	private ExpressionWidget addArgument(Expression.Creator f) {
		if(args.getChildren().length > 1)
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
		insert.setFocus();
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

		return new IProcedure.UnboundProcedure(id.getId()).call(list);
	}
}
