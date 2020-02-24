package pt.iscte.paddle.javardise;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IExpression;

public class ControlWidget extends EditorWidget implements SequenceContainer {

	private final Token keyword;
	private ExpressionWidget expression;
	private final SequenceWidget blockSeq;
	private DeleteListener deleteListener;
	private FixedToken openBracket;
	private FixedToken closeBracket;

	ControlWidget(Composite parent, Keyword keyword, IExpression guard, IBlock block) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_V_ZERO);
		Composite header = new Composite(this, SWT.NONE);
		header.setLayout(Constants.ROW_LAYOUT_H_ZERO);

		deleteListener = new Constants.DeleteListener(this);
		this.keyword = new Token(header, keyword);
		Constants.addInsertLine(this.keyword);

		if(keyword == Keyword.ELSE)
			; // TODO special case delete ELSE
		else
			this.keyword.addKeyListener(deleteListener);

		if(keyword != Keyword.ELSE) {
			new FixedToken(header, "(");
			fillHeader(guard, header);
			new FixedToken(header, ")");
		}

		openBracket = new FixedToken(header, "{");
		blockSeq = new SequenceWidget(this, Constants.TAB);
		blockSeq.addActions(BlockAction.all(block));
		blockSeq.addBlockListener(block);
		blockSeq.setDeleteAction(index -> block.removeElement(index));
		closeBracket = new FixedToken(this, "}");
		
		int i = 0;
		for(IBlockElement e : block)
			blockSeq.addModelElement(e, i++);
		
//		blockSeq.addControlListener(new ControlAdapter() {
//			public void controlResized(ControlEvent e) {
//				int t = blockSeq.totalElements();
//				openBracket.setVisible(t != 1);
//				closeBracket.setVisible(t != 1);
//			}
//		});
	}

	void fillHeader(IExpression expression, Composite header) {
		this.expression = new ExpressionWidget(header, Expression.match(expression));
		this.expression.addKeyListener(deleteListener);
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
