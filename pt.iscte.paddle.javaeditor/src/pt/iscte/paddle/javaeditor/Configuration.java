package pt.iscte.paddle.javaeditor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import pt.iscte.paddle.javardise.InfixExpressionWidget;
import pt.iscte.paddle.javardise.BracketExpressionWidget;
import pt.iscte.paddle.javardise.Expression;
import pt.iscte.paddle.javardise.Expression.Creator;
import pt.iscte.paddle.javardise.InsertWidget;
import pt.iscte.paddle.javardise.LanguageConfiguration;
import pt.iscte.paddle.javardise.SimpleExpressionWidget;
import pt.iscte.paddle.javardise.TextWidget;
import pt.iscte.paddle.javardise.PrefixExpressionWidget;
import pt.iscte.paddle.model.IArrayAllocation;
import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IBinaryExpression;
import pt.iscte.paddle.model.IConstantExpression;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IUnaryExpression;
import pt.iscte.paddle.model.IVariableAddress;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

public class Configuration implements LanguageConfiguration {

	@Override
	public boolean isKeyword(String s) {
		return Keyword.is(s);
	}

	@Override
	public boolean isValidId(String s) {
		return s.matches("[a-zA-Z_]+") && !Keyword.is(s);
	}

	//	@Override
	public static Creator match(IExpression e) {

		if(e instanceof ILiteral) {
			return p -> new SimpleExpressionWidget(p, ((ILiteral) e).getStringValue());
		}
		else if(e instanceof IConstantExpression) {
			return p -> new SimpleExpressionWidget(p, ((IConstantExpression) e).getConstant().getId());
		}
		else if(e instanceof IVariableExpression) {
			IVariableExpression var = (IVariableExpression) e;
			return p -> new SimpleExpressionWidget(p, var.getVariable().getId());
		}
		else if(e instanceof IVariableAddress) {
			IVariableDeclaration var = ((IVariableAddress) e).getTarget().getVariable();
			return p -> new SimpleExpressionWidget(p, BlockAction.variableId(var));
		}
		else if(e instanceof IUnaryExpression) {
			IUnaryExpression u = (IUnaryExpression) e;
			return p -> new PrefixExpressionWidget(p, u,
					match(u.getOperator().getSymbol().charAt(0), JavaConstants.UNARY_OPERATORS),
					match(u.getOperand()));
		}
		else if(e instanceof IBinaryExpression) {
			IBinaryExpression be = (IBinaryExpression) e;
			return p -> new InfixExpressionWidget(p, be,
					match(be.getOperator().getSymbol().charAt(0), JavaConstants.BINARY_OPERATORS),
					match(be.getLeftOperand()),
					match(be.getRightOperand()));
		}
		else if(e instanceof IArrayAllocation) {
			IType type = ((IArrayAllocation) e).getType();
			if(type instanceof IReferenceType)
				type = ((IReferenceType) type).resolveTarget();

			IArrayType t = (IArrayType) type;
			Creator[] f = creators(((IArrayAllocation) e).getParts());
			return p -> new AllocationExpression(p, t, f);
		}
		// TODO record allocation
		//		else if(e instanceof IRecordAllocation) {
		//			IType type = ((IRecordAllocation) e).getType();
		//			Creator[] f = creators(((IRecordAllocation) e).getParts());
		//			return p -> new AllocationExpression(p, type, f);
		//		}
		else if(e instanceof IArrayElement) {
			return p -> new ExpressionChain(p, (IArrayElement) e);
		}
		else if(e instanceof IArrayLength) {
			return p -> new ExpressionChain(p, (IArrayLength) e);
		}
		else if(e instanceof IRecordFieldExpression) {
			return p -> new ExpressionChain(p, (IRecordFieldExpression) e);
		}
		else if(e instanceof IProcedureCall) {
			IProcedureCall proc = (IProcedureCall) e;
			Creator[] f = creators(proc.getArguments());
			return p -> new CallWidget(p, proc, false, f);
		}
		else {
			assert false : e;
		return p -> new Unsupported(p, e);
		}

	}

	static Creator[] creators(List<IExpression> arguments) {
		Creator[] f = new Creator[arguments.size()];
		for(int i = 0; i < f.length; i++)
			f[i] = match(arguments.get(i));
		return f;
	}

	@Override
	public TextWidget createInsertWidget(InsertWidget parent, boolean permanent) {
		return new ExpressionChain(parent, "", false);
	}

	static class Unsupported implements Expression {
		Label label;
		public Unsupported(Composite p, IExpression e) {
			label = new Label(p, SWT.NONE);
			label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			label.setText(e.toString());
		}
		@Override
		public Control getControl() {
			return label;
		}

		@Override
		public Expression copyTo(Composite parent) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void dispose() {
			label.dispose();
		}

		@Override
		public boolean setFocus() {
			return label.setFocus();
		}

		@Override
		public void requestLayout() {
			label.requestLayout();
		}

		@Override
		public void setData(Object data) {
			// TODO Auto-generated method stub

		}

		@Override
		public void addKeyListener(KeyListener listener) {
			// TODO Auto-generated method stub

		}

	}


	@Override
	public void configure(SimpleExpressionWidget expression) {
		//		if(text.getCaretPosition() == 0 && (match = match(e.character, Constants.UNARY_OPERATORS)) != null) {
		//			w = new UnaryExpressionWidget((EditorWidget) getParent(), match, p -> new SimpleExpressionWidget(p, text.getText()));
		//			w.setFocus();
		//		}
		expression.addRule((t,c) -> t.isAtEnd() && match(c, JavaConstants.BINARY_OPERATORS) != null,
				(p,t,c) -> {
					InfixExpressionWidget w = new InfixExpressionWidget(p, 
							null, // TODO? ??? create model?
							match(c, JavaConstants.BINARY_OPERATORS), p2 -> new SimpleExpressionWidget(p2, t.getText()));
					w.focusRight();
					return w;
				});


		expression.addRule((t,c) -> c == SWT.SPACE && Keyword.NEW.isEqual(t.getText()),
				(p,t,c) -> {
					AllocationExpression a = new AllocationExpression(p, IType.INT.array(), p2 -> new SimpleExpressionWidget(p2, "expression"));					
					a.setFocus();
					return a;
				});


		expression.addRule((t,c) -> c == '(' && isValidId(t.getText()) && t.isAtEnd(),
				(p,t,c) -> {
					CallWidget w = new CallWidget(p, IProcedureCall.unboundExpression(t.getText()), false);
					w.focusArgument();
					return w;
				});

		expression.addRule((t,c) -> c == '(' && t.isAtBeginning() || c == ')' && t.isAtEnd(),
				(p,t,c) -> {
					BracketExpressionWidget b = new BracketExpressionWidget(p, p2 -> new SimpleExpressionWidget(p2, t.getText()),"(",")");
					b.setFocus();
					return b;
				});

		//		else if(e.character == '[' && Constants.CONF.isValidId(text.getText()) && text.getCaretPosition() == text.getText().length() && text.getSelectionCount() == 0) {
		//			ExpressionChain id = new ExpressionChain((EditorWidget) getParent(), text.getText(), false);
		//			id.addDimension();
		//			id.focusLastElement();
		//			w = id;
		//		}
		//		else if(e.character == '.' && Constants.CONF.isValidId(text.getText()) && text.getCaretPosition() == text.getText().length() && text.getSelectionCount() == 0) {
		//			ExpressionChain id = new ExpressionChain((EditorWidget) getParent(), text.getText(), false);
		//			id.addField("field");
		//			id.focusLastElement();
		//			w = id;
		//		}



	}

	private static String match(char character, List<String> operators) {
		for(String o : operators)
			if(o.charAt(0) == character)
				return o;
		return null;
	}
}
