package pt.iscte.paddle.javardise.tests;

import static pt.iscte.paddle.model.IOperator.ADD;
import static pt.iscte.paddle.model.IOperator.SMALLER;
import static pt.iscte.paddle.model.IOperator.SMALLER_EQ;
import static pt.iscte.paddle.model.IType.INT;

import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IVariableDeclaration;

public class SumAllTest extends Test {
	
	public SumAllTest() {
		module = IModule.create();
		module.setId("SumAllTest");
		
		procedure = module.addProcedure(INT);
		procedure.setId("sumAll");
		
		IVariableDeclaration n = procedure.addParameter(INT);
		n.setId("n");
		
		IBlock body = procedure.getBody();
		
		IVariableDeclaration i1 = body.addVariable(INT, INT.literal(0));
		i1.setId("i1");
		
		IVariableDeclaration array = body.addVariable(INT.array().reference());
		array.setId("array");
		body.addAssignment(array, INT.array().heapAllocation(n));
		
		ILoop loop1 = body.addLoop(SMALLER.on(i1, n));
		loop1.addArrayElementAssignment(array, ADD.on(i1, INT.literal(1)), i1);
		loop1.addIncrement(i1);
		
		IVariableDeclaration i2 = body.addVariable(INT, INT.literal(0));
		i2.setId("i2");
		
		IVariableDeclaration sum = body.addVariable(INT, INT.literal(0));
		sum.setId("sum");
		
		ILoop loop = body.addLoop(SMALLER_EQ.on(i2, n));
		loop.addAssignment(sum, ADD.on(sum, array.element(i2)));
		loop.addIncrement(i2);
		
		body.addReturn(sum);
	}
	
}
