package pt.iscte.paddle.javardise;

import java.util.List;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariableAssignment;

public class AssignmentWidget extends EditorWidget {

	private final ComplexId id;
	private final ExpressionWidget expression;
	
//	private AssignmentWidget(SequenceWidget parent) {
//		super(parent);
//		setLayout(Constants.ROW_LAYOUT_H);
//	}
	
	AssignmentWidget(SequenceWidget parent, IVariableAssignment a) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		
		String id = a.getTarget().getId();
		String idd = id == null ? "variable" : id;
		
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id = new ComplexId(this, idd, false);
		this.id.addKeyListener(deleteListener);
		
		new FixedToken(this, "=");
		this.expression = new ExpressionWidget(this, Expression.match(a.getExpression()));
		this.expression.addKeyListener(deleteListener);
		
		new FixedToken(this, ";");	
	}
	
	AssignmentWidget(SequenceWidget parent, IArrayElementAssignment a) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		
		String id = a.getTarget().getId();
		String idd = id == null ? "variable" : id;
		
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id = new ComplexId(this, idd, false);
		this.id.addKeyListener(deleteListener);
		
		for(IExpression i : a.getIndexes())
			this.id.addDimensionIndex(Expression.match(i));
		
		new FixedToken(this, "=");
		this.expression = new ExpressionWidget(this, Expression.match(a.getExpression()));
		this.expression.addKeyListener(deleteListener);

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
