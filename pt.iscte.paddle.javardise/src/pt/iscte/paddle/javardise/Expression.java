package pt.iscte.paddle.javardise;

import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IArrayAllocation;
import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IBinaryExpression;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IUnaryExpression;
import pt.iscte.paddle.model.IVariableAddress;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

public interface Expression extends CodeElement {

	interface Creator extends Function<Composite, Expression> {  }

	Expression copyTo(Composite parent);

	void dispose();
	boolean setFocus();
	void requestLayout();
	void setData(Object data);

	void addKeyListener(KeyListener listener);


	void substitute(Expression current, Expression newExpression);

	default boolean isEmpty() {
		return (this instanceof SimpleExpressionWidget) && ((SimpleExpressionWidget) this).text.getText().isBlank();
	}

	IExpression toModel();

	static Expression.Creator match(IExpression e) {
		if(e instanceof ILiteral) {
			return p -> new SimpleExpressionWidget(p, ((ILiteral) e).getStringValue());
		}
		else if(e instanceof IVariableExpression) {
			IVariableExpression var = (IVariableExpression) e;
			return p -> new SimpleExpressionWidget(p, Constants.variableId(var.getVariable()));
		}
		else if(e instanceof IVariableAddress) {
			IVariableDeclaration var = ((IVariableAddress) e).getTarget().getVariable();
			return p -> new SimpleExpressionWidget(p, Constants.variableId(var));
		}
		else if(e instanceof IUnaryExpression) {
			IUnaryExpression u = (IUnaryExpression) e;
			return p -> new UnaryExpressionWidget(p, u.getOperator().getSymbol(), match(u.getOperand()));
		}
		else if(e instanceof IBinaryExpression) {
			IBinaryExpression be = (IBinaryExpression) e;
			return p -> new BinaryExpressionWidget(p, match(be.getLeftOperand()), match(be.getRightOperand()), be.getOperator().getSymbol());
		}
		else if(e instanceof IArrayAllocation) {
			IType type = ((IArrayAllocation) e).getType();
			if(type instanceof IReferenceType)
				type = ((IReferenceType) type).resolveTarget();
			
			IArrayType t = (IArrayType) type;
			Creator[] f = creators(((IArrayAllocation) e).getParts());
			return p -> new ArrayAllocationExpression(p, t, f);
		}
		else if(e instanceof IArrayElement) {
			IArrayElement a = (IArrayElement) e;
			
			return p -> new ComplexId(p, a);
		}
		else if(e instanceof IArrayLength) {
			return p -> new ComplexId(p, (IArrayLength) e);
		}
		else if(e instanceof IProcedureCall) {
			IProcedureCall proc = (IProcedureCall) e;
			Creator[] f = creators(proc.getArguments());
			return p -> new CallWidget(p, proc.getProcedure().getId(), false, f);
		}
		else {
			assert false : e;
		return null;
		}

	}

	static Creator[] creators(List<IExpression> arguments) {
		Creator[] f = new Creator[arguments.size()];
		for(int i = 0; i < f.length; i++)
			f[i] = match(arguments.get(i));
		return f;
	}
}
