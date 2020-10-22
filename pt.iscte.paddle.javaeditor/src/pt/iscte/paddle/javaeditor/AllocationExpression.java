package pt.iscte.paddle.javaeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.Expression;
import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.Id;
import pt.iscte.paddle.javardise.TokenWidget;
import pt.iscte.paddle.model.IArrayType;

// TODO add dim
public class AllocationExpression extends EditorWidget implements Expression {
	private Id id;
	private List<ExpressionWidget> expressions;

	public AllocationExpression(Composite parent, IArrayType type, Expression.Creator ... f) {
		super(parent); // TODO program element
		new TokenWidget(this, Keyword.NEW.keyword());
		id = new Id(this, type.getRootComponentType().getId());
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
//		IArrayType type = null; // TODO null
//		Expression.Creator[] f = new Expression.Creator[type.getDimensions()];
//		for(int i = 0; i < f.length; i++) {
//			int j = i;
//			f[i] = p -> expressions.get(j).copyTo(p);
//		}
//		return new AllocationExpression(parent, type, f);
		return null;
	}
	
	@Override
	public void accept(Consumer<String> visitor) {
		// TODO Auto-generated method stub
		visitor.accept(id.getText());
		for(ExpressionWidget e : expressions)
			e.accept(visitor);
	}
	
}
