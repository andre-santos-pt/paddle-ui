package pt.iscte.paddle.javasde;

import java.util.function.Function;

public class ArrayAllocationExpression extends EditorWidget implements Expression {
	private EditorWidget id;
	private ExpressionWidget expressionWidget;

	public ArrayAllocationExpression(EditorWidget parent, Function<EditorWidget, Expression> f) {
		super(parent);
		new Token(this, Keyword.NEW);
		id = new Id(this, "Type", true, Constants.PRIMITIVE_TYPES_SUPPLIER);
		new FixedToken(this, "[");
		expressionWidget = new ExpressionWidget(this, f);
		new FixedToken(this, "]");
	}
	
	@Override
	public boolean setFocus() {
		id.setFocus();
		return true;
	}

	@Override
	public Expression copyTo(EditorWidget parent) {
		return new ArrayAllocationExpression(parent, p -> expressionWidget.expression.copyTo(p));
	}

	@Override
	public void toCode(StringBuffer buffer) {
		Keyword.NEW.toCode(buffer);
		buffer.append(' ');
		id.toCode(buffer);
		buffer.append('[');
		expressionWidget.toCode(buffer);
		buffer.append(']');
	}
	
	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub
		
	}
	
}
