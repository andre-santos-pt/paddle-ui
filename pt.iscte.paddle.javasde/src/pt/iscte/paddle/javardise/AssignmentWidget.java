package pt.iscte.paddle.javardise;

import java.util.List;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IExpression;

public class AssignmentWidget extends EditorWidget {

	private final ComplexId id;
	private final ExpressionWidget expression;
	private boolean statement;
	private boolean array;
	
	AssignmentWidget(SequenceWidget parent, String id, String expression, boolean statement, List<IExpression> indexes) {
		super(parent);
		this.statement = statement;
		this.array = indexes.size() > 0;
		setLayout(Constants.ROW_LAYOUT_H);
		DeleteListener deleteListener = new Constants.DeleteListener(this);
//		Function<EditorWidget, Expression> f = 
//				e -> array ? new ArrayElementExpression(this, id, "expression") : new SimpleExpressionWidget(e, id, false);
//		this.id = new ExpressionWidget(this, f);
		this.id = new ComplexId(this, id, false);
		
		for(IExpression i : indexes)
			this.id.addDimensionIndex(Expression.match(i));
		
		this.id.addKeyListener(deleteListener);
//		if(this.id.expression instanceof TextWidget)
//			Constants.addInsertLine((TextWidget) this.id.expression);
		new FixedToken(this, "=");
		this.expression = new ExpressionWidget(this, "expression");
		this.expression.addKeyListener(deleteListener);
		if(statement)
			new FixedToken(this, ";");	
	}
	
	public boolean setFocus() {
		return id.setFocus();
	}
	
	public void focusExpression() {
		expression.setFocus();
	}
	
	void addField(String field) {
		id.addField(field);
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		id.toCode(buffer);
		buffer.append(" = ");
		expression.toCode(buffer);
		buffer.append(";");
	}

}
