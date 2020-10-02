package pt.iscte.paddle.javaeditor;

import static pt.iscte.paddle.javaeditor.Keyword.BREAK;
import static pt.iscte.paddle.javaeditor.Keyword.CONTINUE;
import static pt.iscte.paddle.javaeditor.Keyword.RETURN;
import static pt.iscte.paddle.javaeditor.Keyword.WHILE;
import static pt.iscte.paddle.model.IType.BOOLEAN;
import static pt.iscte.paddle.model.IType.INT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.InsertWidget;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.TextWidget;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IBreak;
import pt.iscte.paddle.model.IContinue;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IOperator;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

abstract class BlockAction extends InsertWidget.Action {

	final IBlock block;
	final Supplier<Boolean> enabled;
	
	public BlockAction(CharSequence text, IBlock block, Supplier<Boolean> enabled) {
		super(text);
		this.block = block;
		this.enabled = enabled;
	}
	

	public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
		return enabled.get() && isEnabledInternal(c, id, index, caret, selection, tokens);
	}
	
	abstract boolean isEnabledInternal (char c, TextWidget id, int index, int caret, int selection, List<String> tokens);
	
	public static List<BlockAction> all(IBlock block) {
		List<BlockAction> all = new ArrayList<>();
		all.add(declaration(block));
		all.add(assignment(block));
		all.add(ifStatement(block));
		all.add(elseStatement(block));
		all.add(whileLoop(block));
		all.add(call(block));
		all.add(returnStatement(block));
		all.add(breakStatement(block));
		all.add(continueStatement(block));
		
//		all.add(forLoop(block));
//		all.add(incrementStatement(block));
		return all;
	}

	private static boolean atEnd(String text, int caret) {
		return caret == text.length();
	}
	
	static BlockAction declaration(IBlock block) {
		return new BlockAction("variable", block, () -> UiMode.hasSyntax(UiMode.Syntax.ASSIGNMENT)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return isType(id.getText()) && atEnd(id.getText(), caret) && (c == SWT.SPACE || c == '[');
			}
			
			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(id.getText());
				if(c == '[')
					t = t.array();

				block.addVariableWithIdAt(t, "variable", index);
			}
		};
	}
	
	static BlockAction assignment(IBlock block) {
		return new BlockAction("assignment", block, () -> UiMode.hasSyntax(UiMode.Syntax.ASSIGNMENT)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return c == '=' && !isType(id.getText()) && !id.isKeyword() && !id.isEmpty();
			}
			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IVariableDeclaration var = block.getOwnerProcedure().getVariable(id.getText());
				if(var == null)
					var = new IVariableDeclaration.UnboundVariable(id.getText());
				
				ExpressionChain chain = (ExpressionChain) id;
				
				if(chain.isSingleId())
					block.addAssignmentAt(var, null, index);
				else if(chain.isArrayAccess()) {
//					List<IExpression> indexes = id.getArrayModelExpressions();
					// TODO indexes
					List<IExpression> indexes = Collections.emptyList();
					block.addArrayElementAssignmentAt(var.expression(), null, index, indexes);
				}
				else if(chain.isFieldAccess()) {
					Iterator<String> fields = chain.getFields().iterator();
					IRecordFieldExpression exp = var.field(new IVariableDeclaration.UnboundVariable(fields.next()));
					while(fields.hasNext())
						exp = exp.field(new IVariableDeclaration.UnboundVariable(fields.next()));
					block.addRecordFieldAssignmentAt(exp, null, index);
					// TODO mix resolve
				}
			}
		};
	}

	static BlockAction ifStatement(IBlock block) {
		return new BlockAction(Keyword.IF, block, () -> UiMode.hasSyntax(UiMode.Syntax.SELECTION)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return isKeyword(id, Keyword.IF) && (c == '(' || c == SWT.SPACE) && atEnd(id.getText(), caret);
			}
			
			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IVariableExpression exp = new IVariableDeclaration.UnboundVariable("expression").expression();
				block.addSelectionAt(exp, index);
			}
		};
	}
	
	static BlockAction elseStatement(IBlock block) {
		return new BlockAction(Keyword.ELSE.keyword(), block, () -> UiMode.hasSyntax(UiMode.Syntax.SELECTION)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				if(isKeyword(id, Keyword.ELSE) && (c == '{' || c == SWT.SPACE || c == SWT.CR) && index > 0 && atEnd(id.getText(), caret)) {
					IBlockElement e = block.getChildren().get(index - 1);
					return e instanceof ISelection && !((ISelection) e).hasAlternativeBlock();
				}
				return false;
			}
			
			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IBlockElement e = block.getChildren().get(index - 1);
				((ISelection) e).createAlternativeBlock();
			}
		};
	}
	
	
	static BlockAction whileLoop(IBlock block) {
		return new BlockAction(Keyword.WHILE, block, () -> UiMode.hasSyntax(UiMode.Syntax.WHILE_LOOP)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return isKeyword(id, Keyword.WHILE) && (c == '(' || c == SWT.SPACE) && atEnd(id.getText(), caret);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IVariableExpression exp = new IVariableDeclaration.UnboundVariable("expression").expression();
				block.addLoopAt(exp, index);
			}
		};
	}
	
	static BlockAction forLoop(IBlock block) {
		return new BlockAction(Keyword.FOR, block, () -> UiMode.hasSyntax(UiMode.Syntax.FOR_LOOP)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return isKeyword(id, Keyword.FOR) && (c == '(' || c == SWT.SPACE) && atEnd(id.getText(), caret);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IBlock forBlock = block.addBlockAt(index, Flag.FOR.name());
				IVariableDeclaration progVar = forBlock.addVariable(INT, Flag.FOR.name());
				ILoop loop = forBlock.addLoop(BOOLEAN.literal(true), Flag.FOR.name());
				loop.addAssignment(progVar, IOperator.ADD.on(progVar, INT.literal(1)), Flag.FOR.name());
			}
		};
	}
	
	static BlockAction call(IBlock block) {
		return new BlockAction("call(...)", block, () -> UiMode.hasSyntax(UiMode.Syntax.CALLS)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return (!id.isKeyword() || isKeyword(id, Keyword.SUPER, Keyword.THIS)) && 
						!id.isEmpty() && atEnd(id.getText(), caret) && c == '(';
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IProcedure p = block.getOwnerProcedure().getModule().getProcedure(id.getText());
				if(p == null)
					p = IProcedure.createUnbound(id.getText(), null);
				block.addCallAt(p, index);
			}
		};
	}
	
	static BlockAction returnStatement(IBlock block) {
		return new BlockAction(Keyword.RETURN, block, () -> true) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return isKeyword(id, Keyword.RETURN) && (c == ';' || c == SWT.SPACE) && atEnd(id.getText(), caret);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IType retType = block.getOwnerProcedure().getReturnType();
				if(c == ';' || retType.isVoid())
					block.addReturnAt(index);
				else {
					IExpression retExp = retType.getDefaultExpression();
					block.addReturnAt(retExp, index);
				}
			}
		};
	}

	
	static BlockAction breakStatement(IBlock block) {
		return new BlockAction(Keyword.BREAK, block, () -> UiMode.hasSyntax(UiMode.Syntax.WHILE_LOOP)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return block.isInLoop() && isKeyword(id, Keyword.BREAK) && (c == ';' || c == SWT.SPACE) && atEnd(id.getText(), caret);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				block.addBreakAt(index);
			}
		};
	}
	
	static BlockAction continueStatement(IBlock block) {
		return new BlockAction(Keyword.CONTINUE, block, () -> UiMode.hasSyntax(UiMode.Syntax.WHILE_LOOP)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return block.isInLoop() && isKeyword(id, Keyword.CONTINUE) && (c == ';' || c == SWT.SPACE) && atEnd(id.getText(), caret);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				block.addContinueAt(index);
			}
		};
	}
	
	static BlockAction incrementStatement(IBlock block) {
		return new BlockAction("incrementation", block, () -> UiMode.hasSyntax(UiMode.Syntax.ASSIGN_SIMPLIFICATION)) {
			public boolean isEnabledInternal(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return !id.isKeyword() && c == '+' && atEnd(id.getText(), caret);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				IVariableDeclaration var = block.getOwnerProcedure().getVariable(id.getText());
				if(var == null)
					var = new IVariableDeclaration.UnboundVariable(id.getText());
				block.addIncrementAt(var, index);
			}
		};
	}
	
	static void addModelElement(IProgramElement element, int index, SequenceWidget seq) {
		if(Flag.CONSTRUCTOR.is(element))
			return;
		
		if (element instanceof IVariableDeclaration && Flag.FOR.isNot(element)) {
			IVariableDeclaration v = (IVariableDeclaration) element;
			DeclarationWidget w = seq.addElement(p -> new DeclarationWidget(p, v, null), index);
			w.focusId();
		} 

		else if (element instanceof IVariableAssignment && Flag.FOR.isNot(element)) {
			IVariableAssignment a = (IVariableAssignment) element;
			AssignmentWidget w = seq.addElement(p -> new AssignmentWidget(p, a), index);
			w.focusExpression();
		} 

		else if (element instanceof IArrayElementAssignment) {
			IArrayElementAssignment a = (IArrayElementAssignment) element;
			AssignmentWidget w = seq.addElement(p -> new AssignmentWidget(p, a), index);
			w.focusExpression();
		} 

		else if (element instanceof ISelection) {
			ISelection s = (ISelection) element;
			IfElseWidget w = seq.addElement(p -> new IfElseWidget(p, s), index);
			w.focusIn();
		} 
		
		else if (element instanceof ILoop && Flag.FOR.isNot(element)) {
			ILoop l = (ILoop) element;
			ControlWidget w =  seq.addElement(p -> new ControlWidget(p, WHILE, l), index);
			w.focusIn();
		} 

		//		else if (element instanceof IBlock && element.is(Constants.FOR_FLAG)) { 
		//			ForWidget w = new ForWidget(SequenceWidget.this, null, (IBlock) element);   // TODO guard
		//			addElement(w, index);
		//			w.focusDeclaration();
		//		} 

		else if (element instanceof IBreak) {
			seq.addElement(p -> new InstructionWidget(p, BREAK, (IBreak) element), index);

		} 
		else if (element instanceof IContinue) {
			seq.addElement(p -> new InstructionWidget(p, CONTINUE, (IContinue) element), index);
		} 

		else if (element instanceof IProcedureCall) {
			IProcedureCall call = (IProcedureCall) element;
			CallWidget w = seq.addElement(p -> new CallWidget(p, call, true, Configuration.creators(call.getArguments())), index);
			w.focusArgument();
		} 

		else if (element instanceof IReturn) {
			IReturn ret = (IReturn) element;
			InstructionWidget w = seq.addElement(p -> new InstructionWidget(p, RETURN, ret, ret.getExpression()), index);
			w.focusExpression();
			w.getWidget().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.SPACE) {
						IType retType = ret.getOwnerProcedure().getReturnType();
						w.addExpression(retType.getDefaultExpression());
						w.focusExpression();
					}
				}
			});
		} else {
			System.err.println("unhandled: " + element);
			assert false;
		}
	}
	
	static void addBlockListener(IBlock block, SequenceWidget seq) {
		block.addListener(new IBlock.IListener() {
			public void elementAdded(IProgramElement element, int index) {
				addModelElement(element, index, seq);
			}

			public void elementRemoved(IProgramElement element, int index) {
				seq.removeElement(element);
			}
		});
	}
	


	public boolean isKeyword(TextWidget id, Keyword ... keywords) {
		for(Keyword k : keywords)
			if(isKeyword(id, k))
				return true;
		return false;
	}

	public boolean isKeyword(TextWidget id, Keyword keyword) {
		return keyword.isEqual(id.getText());
	}
	
	static String variableId(IVariableDeclaration var) {
		return var.getId() == null ? "var$" + var.procedureIndex() :  var.getId();
	}

	// TODO other types
	static boolean isType(String text) {
		return  IType.match(text) != null;
	}
	
	public static ExpressionChain matchType(Composite parent, IType t) {
		ExpressionChain id = null;
		if(t.isReference())
			t = ((IReferenceType) t).getTarget();

		if(t instanceof IArrayType) {
			IArrayType at = (IArrayType) t;
			id = new ExpressionChain(parent, at.getRootComponentType().getId(), true);
			for(int i = at.getDimensions(); i > 0; i--)
				id.addDimension();
		}
		else
			id = new ExpressionChain(parent, t.getId(), true);
		return id;
	}
}