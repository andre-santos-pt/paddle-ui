package pt.iscte.paddle.javardise.javaeditor;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.Expression;
import pt.iscte.paddle.javardise.ExpressionChain;
import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.LanguageConfiguration;
import pt.iscte.paddle.javardise.SimpleExpressionWidget;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IStatement;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

class AssignmentWidget extends EditorWidget {

	private ExpressionChain id;
	private ExpressionWidget expression;
	
	public AssignmentWidget(Composite parent, IVariableAssignment a) {
		this(parent, a, a.getTarget(), a.getExpression());
	}
	
	// TODO fix target
	public AssignmentWidget(Composite parent, IArrayElementAssignment a) {
		this(parent, a, ((IVariableExpression) a.getTarget().resolveReference()).getVariable(), a.getExpression()); // TODO check other types
		
		for(IExpression i : a.getIndexes())
			this.id.addDimensionIndex(LanguageConfiguration.INSTANCE.match(i));
	}
	
	private AssignmentWidget(Composite parent, IStatement statement, IVariableDeclaration var, IExpression exp) {
		super(parent, statement);
		setLayout(Constants.ROW_LAYOUT_H);
		
		this.id = new ExpressionChain(this, Constants.variableId(var), false);
		KeyListener delListener = this.id.addDeleteListener(() -> statement.remove());
		
		new FixedToken(this, "=");
		Expression.Creator f = exp == null ?
				p -> new SimpleExpressionWidget(p, "expression") : LanguageConfiguration.INSTANCE.match(exp);
				
		this.expression = new ExpressionWidget(this, f, exp);
		this.expression.addKeyListener(delListener);
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
