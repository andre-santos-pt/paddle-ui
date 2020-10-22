package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.SequenceContainer;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.TokenWidget;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IControlStructure;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IProgramElement;

class ControlWidget extends EditorWidget implements SequenceContainer {

	public TokenWidget keyword;
	private ExpressionWidget expression;
	protected SequenceWidget blockSeq;
	private Runnable delAction;
	private KeyListener delListener;

	public ControlWidget(Composite parent, Keyword keyword, IControlStructure structure, IProgramElement ...extras ) {
		super(parent, structure);
		delAction = () -> structure.remove();
		create(keyword, structure.getGuard(), structure.getBlock(), extras);
	}

	// for else
	public ControlWidget(Composite parent, Keyword keyword, IBlock block) {
		super(parent);
		create(keyword, null, block);
	}

	private void create(Keyword keyword, IExpression guard, IBlock block,IProgramElement ...extras) {
		setLayout(Constants.ROW_LAYOUT_V_ZERO);
		Composite header = new Composite(this, SWT.NONE);
		header.setLayout(Constants.ROW_LAYOUT_H_ZERO);
		header.setBackground(Constants.COLOR_BACKGROUND);
		this.keyword = new TokenWidget(header, keyword.keyword());

		if (guard != null) {
			new FixedToken(header, "(");
			fillHeader(guard, header, extras);
			new FixedToken(header, ")");

			Constants.addInsertLine(this.keyword);
			if (delAction != null) {
				delListener = this.keyword.addDeleteListener(delAction);
			}
		}

		new FixedToken(header, "{");
		blockSeq = new SequenceWidget(this, 1);
		blockSeq.addActions(BlockAction.all(block));
		BlockAction.addBlockListener(block, blockSeq);
		new FixedToken(this, "}");

		int i = 0;
		for (IBlockElement e : block)
			BlockAction.addModelElement(e, i++, blockSeq);
	}

	void fillHeader(IExpression expression, Composite header, IProgramElement ... extras) {
		this.expression = new ExpressionWidget(header, Configuration.match(expression), expression);
		if (delListener != null)
			this.expression.addKeyListener(delListener);
	}

	@Override
	public boolean setFocus() {
		keyword.setFocus();
		return true;
	}

	public void focusIn() {
		if (expression != null)
			expression.setFocus();
		else
			blockSeq.setFocus();
	}

	boolean isElse() {
		return this.keyword.isKeyword(Keyword.ELSE.keyword());
	}

	public SequenceWidget getBody() {
		return blockSeq;
	}
}
