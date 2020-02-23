package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariable;
import pt.iscte.paddle.model.IVariableAssignment;

public class AssignmentWidget extends EditorWidget {

	private final ComplexId id;
	private final ExpressionWidget expression;
	
//	private AssignmentWidget(SequenceWidget parent) {
//		super(parent);
//		setLayout(Constants.ROW_LAYOUT_H);
//	}
	
	AssignmentWidget(Composite parent, IVariableAssignment a) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		
		IVariable var = a.getTarget();
		String id =  var.getId() == null ? "var$" + var.procedureIndex() :  var.getId();
		
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id = new ComplexId(this, id, false);
		this.id.addKeyListener(deleteListener);
		
		new FixedToken(this, "=");
		this.expression = new ExpressionWidget(this, Expression.match(a.getExpression()));
		this.expression.addKeyListener(deleteListener);
		
		new FixedToken(this, ";");	
	}
	
	AssignmentWidget(SequenceWidget parent, IArrayElementAssignment a) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		
		IVariable var = (IVariable) a.getTarget(); // TODO check other types
		String id =  var.getId() == null ? "var$" + var.procedureIndex() :  var.getId();
		
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id = new ComplexId(this, id, false);
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
