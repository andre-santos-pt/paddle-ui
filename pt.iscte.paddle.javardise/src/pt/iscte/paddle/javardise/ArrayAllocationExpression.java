package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IExpression;


// TODO add dim
public class ArrayAllocationExpression extends EditorWidget implements Expression {
	private Id id;
	private List<ExpressionWidget> expressions;

	public ArrayAllocationExpression(Composite parent, IArrayType type, Expression.Creator ... f) {
		super(parent); // TODO program element
		new Token(this, Keyword.NEW);
		id = new Id(this, type.getRootComponentType());
		expressions = new ArrayList<>(type.getDimensions()+1);
		for(int n = 0; n < type.getDimensions(); n++) {
			new FixedToken(this, "[");
			expressions.add(new ExpressionWidget(this, f[n], null));
			new FixedToken(this, "]");
		}
	}

	@Override
	public boolean setFocus() {
		id.setFocus();
		return true;
	}

	@Override
	public Expression copyTo(Composite parent) {
		IArrayType type = (IArrayType) id.inferType();
		Expression.Creator[] f = new Expression.Creator[type.getDimensions()];
		for(int i = 0; i < f.length; i++) {
			int j = i;
			f[i] = p -> expressions.get(j).copyTo(p);
		}
		return new ArrayAllocationExpression(parent, type, f);
	}

	@Override
	public void toCode(StringBuffer buffer) {
		Keyword.NEW.toCode(buffer);
		buffer.append(' ');
		id.toCode(buffer);
		for(Expression e : expressions) {
			buffer.append('[');
			e.toCode(buffer);
			buffer.append(']');
		}
	}

	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public IExpression toModel() {
		// TODO Auto-generated method stub
		return null;
	}
}
