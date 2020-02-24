package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariable;
import pt.iscte.paddle.model.IVariableAssignment;

public class AssignmentWidget extends EditorWidget {

	private ComplexId id;
	private ExpressionWidget expression;
	
	private AssignmentWidget(Composite parent, IVariable var, IExpression exp) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		
		String id = var.isUnbound() ? var.getId() : Constants.variableId(var);		
		this.id = new ComplexId(this, Constants.variableId(var), false);
		
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id.addKeyListener(deleteListener);
		
		new FixedToken(this, "=");
		
		Expression.Creator f = exp == null ?
				p -> new SimpleExpressionWidget(p, "expression") : Expression.match(exp);
		this.expression = new ExpressionWidget(this, f);
		this.expression.addKeyListener(deleteListener);
		
		new FixedToken(this, ";");
	}
	
	AssignmentWidget(Composite parent, IVariableAssignment a) {
		this(parent, a.getTarget(), a.getExpression());
	}
	
	AssignmentWidget(Composite parent, IArrayElementAssignment a) {
		this(parent, (IVariable) a.getTarget(), a.getExpression()); // TODO check other types
		
		for(IExpression i : a.getIndexes())
			this.id.addDimensionIndex(Expression.match(i));
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
