package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IStatement;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

public class AssignmentWidget extends EditorWidget {

	private ComplexId id;
	private ExpressionWidget expression;
	
	AssignmentWidget(Composite parent, IVariableAssignment a) {
		this(parent, a, a.getTarget(), a.getExpression());
	}
	
	// TODO fix target
	AssignmentWidget(Composite parent, IArrayElementAssignment a) {
		this(parent, a, ((IVariableExpression) a.getTarget().resolveReference()).getVariable(), a.getExpression()); // TODO check other types
		
		for(IExpression i : a.getIndexes())
			this.id.addDimensionIndex(Expression.match(i));
	}
	
	private AssignmentWidget(Composite parent, IStatement statement, IVariableDeclaration var, IExpression exp) {
		super(parent, statement);
		setLayout(Constants.ROW_LAYOUT_H);
		
		this.id = new ComplexId(this, Constants.variableId(var), false);
		
		DeleteListener deleteListener = new Constants.DeleteListener(this);
		this.id.addKeyListener(deleteListener);
		
		new FixedToken(this, "=");
		
		Expression.Creator f = exp == null ?
				p -> new SimpleExpressionWidget(p, "expression") : Expression.match(exp);
				
		this.expression = new ExpressionWidget(this, f, exp);
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
