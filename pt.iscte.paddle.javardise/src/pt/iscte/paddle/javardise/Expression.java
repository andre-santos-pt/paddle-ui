package pt.iscte.paddle.javardise;

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import pt.iscte.paddle.javardise.service.ICodeElement;
import pt.iscte.paddle.model.IArrayAllocation;
import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IBinaryExpression;
import pt.iscte.paddle.model.IConstantExpression;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IRecordAllocation;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IUnaryExpression;
import pt.iscte.paddle.model.IVariableAddress;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

public interface Expression extends ICodeElement {

	interface Creator extends Function<Composite, Expression> {  }

	Expression copyTo(Composite parent);

	void dispose();
	boolean setFocus();
	void requestLayout();
	void setData(Object data);

	void addKeyListener(KeyListener listener);


	void substitute(Expression current, Expression newExpression);

	//	default boolean isEmpty() {
	//		return (this instanceof TextWidget) && ((TextWidget) this).getText().isBlank();
	//	}

	IExpression toModel();

	static Expression.Creator match(IExpression e) {
		if(e instanceof ILiteral) {
			return p -> new SimpleExpressionWidget(p, (ILiteral) e);
		}
		else if(e instanceof IConstantExpression) {
			return p -> new SimpleExpressionWidget(p, (IConstantExpression) e);
		}
		else if(e instanceof IVariableExpression) {
			IVariableExpression var = (IVariableExpression) e;
			return p -> new SimpleExpressionWidget(p, var);
		}
		else if(e instanceof IVariableAddress) {
			IVariableDeclaration var = ((IVariableAddress) e).getTarget().getVariable();
			return p -> new SimpleExpressionWidget(p, Constants.variableId(var));
		}
		else if(e instanceof IUnaryExpression) {
			IUnaryExpression u = (IUnaryExpression) e;
			return p -> new UnaryExpressionWidget(p, u);
		}
		else if(e instanceof IBinaryExpression) {
			IBinaryExpression be = (IBinaryExpression) e;
			return p -> new BinaryExpressionWidget(p, be);
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
			return p -> new ComplexId(p, (IArrayElement) e);
		}
		else if(e instanceof IArrayLength) {
			return p -> new ComplexId(p, (IArrayLength) e);
		}
		else if(e instanceof IRecordFieldExpression) {
			return p -> new ComplexId(p, (IRecordFieldExpression) e);
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

		@Override
		public void substitute(Expression current, Expression newExpression) {
			// TODO Auto-generated method stub

		}

		@Override
		public IExpression toModel() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
