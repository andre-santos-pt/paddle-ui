package pt.iscte.paddle.javardise;

import java.util.function.Function;

import org.eclipse.swt.events.KeyListener;

import pt.iscte.paddle.model.IArrayAllocation;
import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IBinaryExpression;
import pt.iscte.paddle.model.IConstant;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IVariable;

public interface Expression {

	interface Creator extends Function<EditorWidget, Expression> {
		
	}
	
	Expression copyTo(EditorWidget parent);
	
	void dispose();
	boolean setFocus();
	void requestLayout();
	void setData(Object data);

	void addKeyListener(KeyListener listener);
	
	void toCode(StringBuffer buffer);

	void substitute(Expression current, Expression newExpression);

	default boolean isEmpty() {
		return (this instanceof SimpleExpressionWidget) && ((SimpleExpressionWidget) this).text.getText().isBlank();
	}
	
	IExpression toModel();

	static Expression.Creator match(IExpression e) {
		if(e instanceof ILiteral)
			return p -> new SimpleExpressionWidget(p, ((ILiteral) e).getStringValue());
			else if(e instanceof IVariable)
				return p -> new SimpleExpressionWidget(p, e.getId());
				else if(e instanceof IBinaryExpression) {
					IBinaryExpression be = (IBinaryExpression) e;
					return p -> new BinaryExpressionWidget(p, match(be.getLeftOperand()), match(be.getRightOperand()), be.getOperator().getSymbol());
				}
				else if(e instanceof IArrayAllocation) {
					return p -> new ArrayAllocationExpression(p, match(((IArrayAllocation) e).getDimensions().get(0))); // TODO dims
				}
//				else if(e instanceof IArrayElement) {
//					
//				}
//				else if(e instanceof IArrayLength) {
//					//return p -> new ComplexId(p, "array", false);
//					return null;
//				}
				else if(e instanceof IProcedureCall) {
					IProcedureCall proc = (IProcedureCall) e;
					return p -> new CallWidget(p, proc.getProcedure().getId(), false); // TODO args
				}
					else {
						assert false : e.toString();
						return null;
					}
			
	}
}
