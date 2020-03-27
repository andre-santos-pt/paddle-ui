package pt.iscte.paddle.javardise.parser;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.google.common.collect.ImmutableMap;

import pt.iscte.paddle.javardise.Flag;
import pt.iscte.paddle.javardise.Keyword;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IBinaryOperator;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IRecordType;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IUnaryOperator;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

class ParserVisitor extends DefaultASTVisitor {
	IModule module;
	IRecordType classType;

	IProcedure currentProcedure;
	ArrayDeque<IBlock> blockStack = new ArrayDeque<>();

	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		module = IModule.create(node.getName().toString());
		classType = module.addRecordType();
		module.setProperty(IRecordType.class, classType);
		node.accept(new ASTVisitor() {
			public boolean visit(FieldDeclaration node) {
				List parts = node.fragments();
				for(Object o : parts) {
					VariableDeclarationFragment f = (VariableDeclarationFragment) o;
					IVariableDeclaration var = classType.addField(match(node.getType()));
					var.setId(f.getName().toString());
					addModifiers(node.getModifiers(), var);
				}
				return false;
			}

			public boolean visit(MethodDeclaration node) {
				IType retType = node.isConstructor() ? IType.VOID : match(node.getReturnType2());
				IProcedure p = module.addProcedure(node.getName().toString(), retType);
				Flag.CONSTRUCTOR.setIf(p, node.isConstructor());
				addModifiers(node.getModifiers(), p);
				for(Object o : node.parameters()) {
					SingleVariableDeclaration d = (SingleVariableDeclaration) o;
					IVariableDeclaration param = p.addParameter(match(d.getType()));
					param.setId(d.getName().toString());
				}
				return false;
			}
		});
		return true;
	}

	private void addModifiers(int mod, IProgramElement e) {
		if(Modifier.isPublic(mod))
			Keyword.PUBLIC.setFlag(e);
		if(Modifier.isPrivate(mod))
			Keyword.PRIVATE.setFlag(e);
		if(Modifier.isProtected(mod))
			Keyword.PROTECTED.setFlag(e);
		
		if(Modifier.isAbstract(mod))
			Keyword.ABSTRACT.setFlag(e);
		if(Modifier.isFinal(mod))
			Keyword.FINAL.setFlag(e);
		
		if(Modifier.isStatic(mod))
			Keyword.STATIC.setFlag(e);
	}
	
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		List parameters = node.parameters();
		IType[] paramTypes = new IType[parameters.size()];
		for(int i = 0; i != paramTypes.length; i++)
			paramTypes[i] = match(((SingleVariableDeclaration) parameters.get(i)).getType());
		currentProcedure = module.resolveProcedure(node.getName().toString(), paramTypes);
		assert currentProcedure != null : node;
		blockStack.push(currentProcedure.getBody());
		return true;
	}

	public boolean visit(SingleVariableDeclaration node) {
		return true;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		blockStack.pop();
	}

	@Override
	public boolean visit(Block node) {
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		ILoop loop = blockStack.peek().addLoop(match(node.getExpression()));
		blockStack.push(loop.getBlock());
		return true;
	}

	@Override
	public void endVisit(WhileStatement node) {
		blockStack.pop();
	}

	@Override
	public boolean visit(IfStatement node) {
		ISelection sel = blockStack.peek().addSelection(match(node.getExpression()));
		blockStack.push(sel.getBlock());
		return true;
	}

	@Override
	public void endVisit(IfStatement node) {
		blockStack.pop();
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		VariableDeclarationStatement a = (VariableDeclarationStatement)node.getParent();
		IVariableDeclaration var = blockStack.peek().addVariable(match(a.getType()));
		var.setId(node.getName().toString());
		return true;
	}

	IVariableDeclaration varLookup(Name name) {
		String id = name.toString();
		for (IVariableDeclaration v : currentProcedure.getVariables())
			if(v.getId().equals(id))
				return v;

		return new IVariableDeclaration.UnboundVariable(id);
	}

	@Override
	public boolean visit(Assignment node) {
		assert node.getOperator() == Assignment.Operator.ASSIGN;

		IBlock block = blockStack.peek();
		IExpression exp = match(node.getRightHandSide());

		Expression left = node.getLeftHandSide();
		if(left instanceof SimpleName)
			block.addAssignment(varLookup((SimpleName) left), exp);
		else {
			IExpression target = match(node.getLeftHandSide());
			if(left instanceof ArrayAccess)
				block.addArrayElementAssignment(target, exp, Collections.emptyList());
			else if(left instanceof FieldAccess)
				block.addRecordFieldAssignment(null, exp);
			else
				return super.visit(node);
		}
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		Expression exp = node.getExpression();
		if(exp == null)
			blockStack.peek().addReturn();
		else
			blockStack.peek().addReturn(match(exp));
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IExpression[] args = matchExpressions(node.arguments());
		IProcedure p = module.resolveProcedure(node.getName().toString(), getTypes(args));
		blockStack.peek().addCall(p, args);
		return false;
	}

	private IExpression[] matchExpressions(List expressions) {
		IExpression[] args = new IExpression[expressions.size()];
		for(int i = 0; i != args.length; i++)
			args[i] = match((Expression) expressions.get(i));
		return args;
	}

	private static IType[] getTypes(IExpression[] expressions) {
		IType[] types = new IType[expressions.length];
		for(int i = 0; i != types.length; i++)
			types[i] = expressions[i].getType();
		return types;
	}

	public boolean visit(ExpressionStatement node) { 	return true; }
	public boolean visit(SimpleName node) {				return true; }
	public boolean visit(SimpleType node) {				return true; }
	public boolean visit(Modifier node) {				return true; }
	public boolean visit(PrimitiveType node) {			return true; }
	public boolean visit(BooleanLiteral node) { 		return true; }
	public boolean visit(NumberLiteral node) { 			return true; }
	public boolean visit(ArrayType node) { 				return true; }
	public boolean visit(ArrayAccess node) { 			return true; }
	public boolean visit(Dimension node) { 				return true; }
	public boolean visit(QualifiedName node) { 			return true; }
	public boolean visit(FieldAccess node) { 			return true; }
	public boolean visit(InfixExpression node) { 		return true; }
	public boolean visit(PrefixExpression node) { 		return true; }

	private IExpression match(Expression e) {
		if(e instanceof BooleanLiteral)
			return IType.BOOLEAN.literal(((BooleanLiteral) e).booleanValue());

		else if(e instanceof NumberLiteral) {
			String token = ((NumberLiteral) e).getToken();
			if(token.matches("[0-9]+"))
				return IType.INT.literal(Integer.parseInt(token));
			else if(token.matches("[0-9]*\\.[0-9]*"))
				return IType.DOUBLE.literal(Double.parseDouble(token));
		}
		//		else if(e instanceof CharacterLiteral)

		//		else if(e instanceof NullLiteral)

		else if(e instanceof SimpleName)
			return varLookup((SimpleName) e).expression();

		else if(e instanceof ArrayAccess) {
			IExpression match = match(((ArrayAccess) e).getArray());
			Expression index = ((ArrayAccess) e).getIndex();
			if(match instanceof IVariableExpression)
				return ((IVariableExpression) match).element(match(index));
			return match;
		}

		else if(e instanceof QualifiedName) {
			QualifiedName qn = (QualifiedName) e;
			IVariableDeclaration var = varLookup(qn.getQualifier());
			if(var.getType() instanceof IReferenceType && ((IReferenceType) var.getType()).getTarget() instanceof IArrayType)
				return var.length(); // TODO indexes
		}
		else if(e instanceof FieldAccess) {
			FieldAccess fa = (FieldAccess) e;
			IExpression target = match(fa.getExpression());
		}

		else if(e instanceof PrefixExpression) {
			PrefixExpression pe = (PrefixExpression) e;
			IUnaryOperator op = matchUnaryOperator(pe.getOperator());
			return op.on(match(pe.getOperand()));
		}

		else if(e instanceof InfixExpression) {
			InfixExpression ie = (InfixExpression) e;
			IExpression left = match(ie.getLeftOperand());
			IExpression right = match(ie.getRightOperand());
			IBinaryOperator op = matchBinaryOperator(ie, left, right);
			return op.on(left, right);
		}

		else if(e instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation) e;
			IExpression[] args = matchExpressions(inv.arguments());
			IProcedure p = module.resolveProcedure(inv.getName().toString(), getTypes(args));
			return p.expression(args);
		}

		else if(e instanceof ArrayCreation) {
			ArrayCreation ac = (ArrayCreation) e;
			IType t = match(ac.getType());
			for (Object exp : ac.dimensions())
				t = t.array();
			return ((IArrayType) t).heapAllocation(matchExpressions(ac.dimensions()));
		}

		assert false : e.toString() + " (" + e.getClass() + ")"; 

		return null;
	}


	private static IUnaryOperator matchUnaryOperator(PrefixExpression.Operator o) {
		if(o == PrefixExpression.Operator.NOT)
			return IUnaryOperator.NOT;
		else if(o == PrefixExpression.Operator.MINUS)
			return IUnaryOperator.MINUS;
		//		else if(o == PrefixExpression.Operator.PLUS)
		//			return IUnaryOperator.NOT;
		assert false : o.toString() + " (" + o.getClass() + ")"; 
		return null;
	}

	private static final Map<InfixExpression.Operator, IBinaryOperator> binaryOperatorMap = 
			ImmutableMap.<InfixExpression.Operator, IBinaryOperator>builder()
			.put(InfixExpression.Operator.EQUALS, 			IBinaryOperator.EQUAL)
			.put(InfixExpression.Operator.NOT_EQUALS, 		IBinaryOperator.DIFFERENT)
			.put(InfixExpression.Operator.LESS, 			IBinaryOperator.SMALLER)
			.put(InfixExpression.Operator.LESS_EQUALS, 		IBinaryOperator.SMALLER_EQ)
			.put(InfixExpression.Operator.GREATER, 			IBinaryOperator.GREATER)
			.put(InfixExpression.Operator.GREATER_EQUALS, 	IBinaryOperator.GREATER_EQ)

			.put(InfixExpression.Operator.AND, 				IBinaryOperator.AND)
			.put(InfixExpression.Operator.CONDITIONAL_AND, 	IBinaryOperator.AND)
			.put(InfixExpression.Operator.OR, 				IBinaryOperator.OR)
			.put(InfixExpression.Operator.CONDITIONAL_OR, 	IBinaryOperator.OR)
			.put(InfixExpression.Operator.XOR, 				IBinaryOperator.XOR)					

			.put(InfixExpression.Operator.PLUS, 			IBinaryOperator.ADD) 
			.put(InfixExpression.Operator.MINUS, 			IBinaryOperator.SUB)
			.put(InfixExpression.Operator.TIMES, 			IBinaryOperator.MUL) 
			.put(InfixExpression.Operator.DIVIDE, 			IBinaryOperator.DIV)
			.put(InfixExpression.Operator.REMAINDER, 		IBinaryOperator.MOD)

			.build();

	private static IBinaryOperator matchBinaryOperator(InfixExpression e, IExpression left, IExpression right) {
		IBinaryOperator op = binaryOperatorMap.get(e.getOperator());
		if(op == IBinaryOperator.DIV && left.getType().equals(IType.INT) && right.getType().equals(IType.INT))
			op = IBinaryOperator.IDIV;

		assert op != null : e.toString() + " (" + e.getClass() + ")"; 
		return op;
	}

	private IType match(Type t) {
		if(t.isPrimitiveType())
			return matchPrimitive((PrimitiveType) t);
		else if(t.isArrayType()) {
			ArrayType at = (ArrayType) t;
			IType comp = match(at.getElementType());
			int d = at.dimensions().size();
			while(d-- > 0)
				comp = comp.array();
			return comp.reference();
		}

		if(t.isSimpleType()) {
			String id = ((SimpleType) t).getName().toString();
			IRecordType type = null;
			for (IRecordType rt : module.getRecordTypes()) {
				if(id.equals(rt.getId()))
					type = rt;
			}
			if(type == null)
				type = module.addRecordType(id);

			return type;
		}
		assert false : t.toString();
		return null;
	}

	private static IType matchPrimitive(PrimitiveType t) {
		Code code = ((PrimitiveType) t).getPrimitiveTypeCode();
		if(code == PrimitiveType.BOOLEAN)
			return IType.BOOLEAN;
		else if(code == PrimitiveType.INT)
			return IType.INT;
		else if(code == PrimitiveType.DOUBLE)
			return IType.DOUBLE;
		else if(code == PrimitiveType.VOID)
			return IType.VOID;

		assert false : t.toString();
		return null;
	}
}
