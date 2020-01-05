package pt.iscte.paddle.javasde;

import static pt.iscte.paddle.model.IType.BOOLEAN;
import static pt.iscte.paddle.model.IType.INT;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;

import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IOperator;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariable;

abstract class BlockAction extends InsertWidget.Action {

	final IBlock block;

	BlockAction(Keyword keyword, IBlock block) {
		this(keyword.toString(), keyword.getAccelerator(), block);
	}
	
	BlockAction(String text, char accelerator, IBlock block) {
		super(text, accelerator);
		this.block = block;
	}

	static List<BlockAction> all(IBlock block) {
		List<BlockAction> all = new ArrayList<>();
		all.add(declaration(block));
		all.add(assignment(block));
		all.add(ifStatement(block));
		all.add(elseStatement(block));
		all.add(whileLoop(block));
		all.add(forLoop(block));
		all.add(call(block));
		all.add(returnStatement(block));
		all.add(breakStatement(block));
		all.add(continueStatement(block));
		all.add(incrementStatement(block));
		return all;
	}

	private static boolean atEnd(String text, int caret) {
		return caret == text.length();
	}
	
	static BlockAction declaration(IBlock block) {
		return new BlockAction("variable", 'v', block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return Constants.isType(text) && atEnd(text, caret) && (c == SWT.SPACE || c == '[');
			}
			
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(text);
				if(c == '[')
					t = t.array();

				IVariable var = block.addVariableAt(t, index);
				var.setId(null);
//				block.addAssignmentAt(var, null, index);
			}
		};
	}
	
	static BlockAction assignment(IBlock block) {
		return new BlockAction("assignment", 'a', block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return !Constants.isType(text) && !Keyword.is(text) && !text.isEmpty() && atEnd(text, caret) && (c == '=' || c == '[');
			}
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IVariable var = block.getOwnerProcedure().getVariable(text);
				if(var == null)
					var = new IVariable.UnboundVariable(text);
				
				if(c == '=')
					block.addAssignmentAt(var, null, index);
				else
					block.addArrayElementAssignmentAt(var, null, index, IType.INT.literal(0));
			}
		};
	}

	static BlockAction ifStatement(IBlock block) {
		return new BlockAction(Keyword.IF, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return Keyword.IF.isEqual(text) && (c == '(' || c == SWT.SPACE) && atEnd(text, caret);
			}
			
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				block.addSelectionAt(BOOLEAN.literal(true), index);
			}
		};
	}
	
	static BlockAction elseStatement(IBlock block) {
		return new BlockAction(Keyword.ELSE, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				if(Keyword.ELSE.isEqual(text) && (c == '{' || c == SWT.SPACE) && index > 0 && atEnd(text, caret)) {
					IBlockElement e = block.getChildren().get(index - 1);
					return e instanceof ISelection && !((ISelection) e).hasAlternativeBlock();
				}
				return false;
			}
			
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IBlockElement e = block.getChildren().get(index - 1);
				((ISelection) e).createAlternativeBlock();
			}
		};
	}
	
	
	static BlockAction whileLoop(IBlock block) {
		return new BlockAction(Keyword.WHILE, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return Keyword.WHILE.isEqual(text) && (c == '(' || c == SWT.SPACE) && atEnd(text, caret);
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				block.addLoopAt(BOOLEAN.literal(true), index);
			}
		};
	}
	
	static BlockAction forLoop(IBlock block) {
		return new BlockAction(Keyword.FOR, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return Keyword.FOR.isEqual(text) && (c == '(' || c == SWT.SPACE) && atEnd(text, caret);
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IBlock forBlock = block.addBlockAt(index, Constants.FOR_FLAG);
				IVariable progVar = forBlock.addVariable(INT, Constants.FOR_FLAG);
				ILoop loop = forBlock.addLoop(BOOLEAN.literal(true), Constants.FOR_FLAG);
				loop.addAssignment(progVar, IOperator.ADD.on(progVar, INT.literal(1)), Constants.FOR_FLAG);
			}
		};
	}
	
	static BlockAction call(IBlock block) {
		return new BlockAction("call(...)", 'p', block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return !Keyword.is(text) && !text.isEmpty() && atEnd(text, caret) && c == '(';
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IProcedure p = block.getOwnerProcedure().getModule().getProcedure(text);
				if(p == null)
					p = new IProcedure.UnboundProcedure(text);
				block.addCallAt(p, index);
			}
		};
	}
	
	static BlockAction returnStatement(IBlock block) {
		return new BlockAction(Keyword.RETURN, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return Keyword.RETURN.isEqual(text) && (c == ';' || c == SWT.SPACE) && atEnd(text, caret);
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				if(c == ';')
					block.addReturnAt(index);
				else
					block.addReturnAt(null, index);
			}
		};
	}

	
	static BlockAction breakStatement(IBlock block) {
		return new BlockAction(Keyword.BREAK, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return block.isInLoop() && Keyword.BREAK.isEqual(text) && (c == ';' || c == SWT.SPACE) && atEnd(text, caret);
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				block.addBreakAt(index);
			}
		};
	}
	
	static BlockAction continueStatement(IBlock block) {
		return new BlockAction(Keyword.CONTINUE, block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return block.isInLoop() && Keyword.CONTINUE.isEqual(text) && (c == ';' || c == SWT.SPACE) && atEnd(text, caret);
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				block.addContinueAt(index);
			}
		};
	}
	
	static BlockAction incrementStatement(IBlock block) {
		return new BlockAction("incrementation", '+', block) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return !Keyword.is(text) && c == '+' && atEnd(text, caret);
			}

			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IVariable var = block.getOwnerProcedure().getVariable(text);
				if(var == null)
					var = new IVariable.UnboundVariable(text);
				block.addIncrementAt(var, index);
			}
		};
	}
}