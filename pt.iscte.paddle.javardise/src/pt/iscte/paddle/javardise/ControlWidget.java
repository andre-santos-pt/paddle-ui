package pt.iscte.paddle.javardise;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IControlStructure;
import pt.iscte.paddle.model.IExpression;

public class ControlWidget extends EditorWidget implements SequenceContainer {

	 Token keyword;
	private ExpressionWidget expression;
	 SequenceWidget blockSeq;
	private FixedToken openBracket;
	private FixedToken closeBracket;
	private Runnable delAction;
	private KeyListener delListener;
	
	ControlWidget(Composite parent, Keyword keyword, IControlStructure structure) {
		super(parent, structure);
		delAction = () -> structure.remove();
		create(keyword, structure.getGuard(), structure.getBlock());
	}

	// for else
	ControlWidget(Composite parent, Keyword keyword, IBlock block) {
		super(parent);
		create(keyword, null, block);
	}

	private void create(Keyword keyword, IExpression guard, IBlock block) {
		setLayout(Constants.ROW_LAYOUT_V_ZERO);
		Composite header = new Composite(this, SWT.NONE);
		header.setLayout(Constants.ROW_LAYOUT_H_ZERO);
		header.setBackground(Constants.COLOR_BACKGROUND);
		this.keyword = new Token(header, keyword);
		
		if(guard != null) {
			new FixedToken(header, "(");
			fillHeader(guard, header);
			new FixedToken(header, ")");

			Constants.addInsertLine(this.keyword);
			if(delAction != null) {
				delListener = this.keyword.addDeleteListener(delAction);
			}
		}

		openBracket = new FixedToken(header, "{");
		blockSeq = new SequenceWidget(this, Constants.TAB);
		blockSeq.addActions(BlockAction.all(block));
		blockSeq.addBlockListener(block);
		closeBracket = new FixedToken(this, "}");
		
		int i = 0;
		for(IBlockElement e : block)
			blockSeq.addModelElement(e, i++);
	}

	void fillHeader(IExpression expression, Composite header) {
		this.expression = new ExpressionWidget(header, Expression.match(expression), expression);
		if(delListener != null)
			this.expression.addKeyListener(delListener);
	}

	@Override
	public boolean setFocus() {
		keyword.setFocus();
		return true;
	}

	void focusIn() {
		if(expression != null)
			expression.setFocus();
		else
			blockSeq.setFocus();
	}

	@Override
	public void toCode(StringBuffer buffer, int level) {
		appendTabs(buffer, level);
		buffer.append(keyword.toString());
		if(!keyword.isKeyword(Keyword.ELSE)) {
			buffer.append("(");
			expression.toCode(buffer);
			buffer.append(")");
		}
		buffer.append(" {").append(System.lineSeparator());
		blockSeq.toCode(buffer, level + 1);
		appendTabs(buffer, level);
		buffer.append("}").append(System.lineSeparator());
	}

	boolean is(Keyword keyword) {
		return this.keyword.isKeyword(keyword);
	}

	boolean isElse() {
		return this.keyword.isKeyword(Keyword.ELSE);
	}

	public SequenceWidget getBody() {
		return blockSeq;
	}

	
}
