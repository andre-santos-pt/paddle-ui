package pt.iscte.paddle.javardise;

import static pt.iscte.paddle.model.IType.BOOLEAN;
import static pt.iscte.paddle.model.IType.INT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;

import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IOperator;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

abstract class BlockAction extends NewInsertWidget.Action {

	final IBlock block;
	final Supplier<Boolean> enabled;
	
	BlockAction(CharSequence text, IBlock block, Supplier<Boolean> enabled) {
		super(text);
		this.block = block;
		this.enabled = enabled;
	}
	
	public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
		return enabled.get() && isEnabledInternal(c, id, index, caret, selection, tokens);
	}
	
	abstract boolean isEnabledInternal (char c, ComplexId id, int index, int caret, int selection, List<String> tokens);
	
	static List<BlockAction> all(IBlock block) {
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
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return Constants.isType(id.getText()) && atEnd(id.getText(), caret) && (c == SWT.SPACE || c == '[');
			}
			
			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(id.getText());
				if(c == '[')
					t = t.array();

				block.addVariableWithIdAt(t, "variable", index);
			}
		};
	}
	
	static BlockAction assignment(IBlock block) {
		return new BlockAction("assignment", block, () -> UiMode.hasSyntax(UiMode.Syntax.ASSIGNMENT)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return c == '=' && !Constants.isType(id.getText()) && !id.isKeyword() && !id.isEmpty();
			}
			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IVariableDeclaration var = block.getOwnerProcedure().getVariable(id.getId());
				if(var == null)
					var = new IVariableDeclaration.UnboundVariable(id.getId());
				
				if(id.isSingleId())
					block.addAssignmentAt(var, null, index);
				else if(id.isArrayAccess()) {
					List<IExpression> indexes = id.getArrayModelExpressions();
					block.addArrayElementAssignmentAt(var.expression(), null, index, indexes);
				}
				else if(id.isFieldAccess()) {
					Iterator<String> fields = id.getFields().iterator();
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
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return id.isKeyword(Keyword.IF) && (c == '(' || c == SWT.SPACE) && atEnd(id.getId(), caret);
			}
			
			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IVariableExpression exp = new IVariableDeclaration.UnboundVariable("expression").expression();
				block.addSelectionAt(exp, index);
			}
		};
	}
	
	static BlockAction elseStatement(IBlock block) {
		return new BlockAction(Keyword.ELSE.keyword(), block, () -> UiMode.hasSyntax(UiMode.Syntax.SELECTION)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				if(id.isKeyword(Keyword.ELSE) && (c == '{' || c == SWT.SPACE || c == SWT.CR) && index > 0 && atEnd(id.getId(), caret)) {
					IBlockElement e = block.getChildren().get(index - 1);
					return e instanceof ISelection && !((ISelection) e).hasAlternativeBlock();
				}
				return false;
			}
			
			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IBlockElement e = block.getChildren().get(index - 1);
				((ISelection) e).createAlternativeBlock();
			}
		};
	}
	
	
	static BlockAction whileLoop(IBlock block) {
		return new BlockAction(Keyword.WHILE, block, () -> UiMode.hasSyntax(UiMode.Syntax.WHILE_LOOP)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return id.isKeyword(Keyword.WHILE) && (c == '(' || c == SWT.SPACE) && atEnd(id.getId(), caret);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IVariableExpression exp = new IVariableDeclaration.UnboundVariable("expression").expression();
				block.addLoopAt(exp, index);
			}
		};
	}
	
	static BlockAction forLoop(IBlock block) {
		return new BlockAction(Keyword.FOR, block, () -> UiMode.hasSyntax(UiMode.Syntax.FOR_LOOP)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return id.isKeyword(Keyword.FOR) && (c == '(' || c == SWT.SPACE) && atEnd(id.getId(), caret);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IBlock forBlock = block.addBlockAt(index, Flag.FOR.name());
				IVariableDeclaration progVar = forBlock.addVariable(INT, Flag.FOR.name());
				ILoop loop = forBlock.addLoop(BOOLEAN.literal(true), Flag.FOR.name());
				loop.addAssignment(progVar, IOperator.ADD.on(progVar, INT.literal(1)), Flag.FOR.name());
			}
		};
	}
	
	static BlockAction call(IBlock block) {
		return new BlockAction("call(...)", block, () -> UiMode.hasSyntax(UiMode.Syntax.CALLS)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return (!id.isKeyword() || id.isKeyword(Keyword.SUPER, Keyword.THIS)) && 
						!id.isEmpty() && atEnd(id.getId(), caret) && c == '(';
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IProcedure p = block.getOwnerProcedure().getModule().getProcedure(id.getId());
				if(p == null)
					p = new IProcedure.UnboundProcedure(id.getId());
				block.addCallAt(p, index);
			}
		};
	}
	
	static BlockAction returnStatement(IBlock block) {
		return new BlockAction(Keyword.RETURN, block, () -> true) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return id.isKeyword(Keyword.RETURN) && (c == ';' || c == SWT.SPACE) && atEnd(id.getId(), caret);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
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
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return block.isInLoop() && id.isKeyword(Keyword.BREAK) && (c == ';' || c == SWT.SPACE) && atEnd(id.getId(), caret);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				block.addBreakAt(index);
			}
		};
	}
	
	static BlockAction continueStatement(IBlock block) {
		return new BlockAction(Keyword.CONTINUE, block, () -> UiMode.hasSyntax(UiMode.Syntax.WHILE_LOOP)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return block.isInLoop() && id.isKeyword(Keyword.CONTINUE) && (c == ';' || c == SWT.SPACE) && atEnd(id.getId(), caret);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				block.addContinueAt(index);
			}
		};
	}
	
	static BlockAction incrementStatement(IBlock block) {
		return new BlockAction("incrementation", block, () -> UiMode.hasSyntax(UiMode.Syntax.ASSIGN_SIMPLIFICATION)) {
			public boolean isEnabledInternal(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return !id.isKeyword() && c == '+' && atEnd(id.getId(), caret);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IVariableDeclaration var = block.getOwnerProcedure().getVariable(id.getId());
				if(var == null)
					var = new IVariableDeclaration.UnboundVariable(id.getId());
				block.addIncrementAt(var, index);
			}
		};
	}
}