package pt.iscte.paddle.javasde;

import pt.iscte.paddle.javasde.Constants.DeleteListener;
import pt.iscte.paddle.model.IBlock;

public class ControlWidget extends EditorWidget implements SequenceContainer {

	private final Token keyword;
	private ExpressionWidget expression;
	private final SequenceWidget blockSeq;
	private DeleteListener deleteListener;

	ControlWidget(SequenceWidget parent, Keyword keyword, String guard, IBlock block) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_V_ZERO);
		EditorWidget header = new EditorWidget(this);
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

		new FixedToken(header, "{");
		blockSeq = new SequenceWidget(this, Constants.TAB);
		blockSeq.addActions(BlockAction.all(block));
		blockSeq.addBlockListener(block);
		blockSeq.setDeleteAction(index -> block.removeElement(index));
		new FixedToken(this, "}");
	}

	void fillHeader(String expression, EditorWidget header) {
		this.expression = new ExpressionWidget(header, expression == null ? "expression" : expression);
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
	public void toCode(StringBuffer buffer) {
		buffer.append("while" + "(");
		expression.toCode(buffer);
		buffer.append(") {").append(System.lineSeparator());
		blockSeq.toCode(buffer);
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

	@Override
	public String toString() {
		return this.keyword.getText() + "(...)";
	}
}
