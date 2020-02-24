package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants.DeleteListener;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariable;

public class DeclarationWidget extends EditorWidget {
	private final Id type;
	private final Id id;
	private final ExpressionWidget expression;
	
	DeclarationWidget(Composite parent, IVariable var, IExpression exp) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H);
		DeleteListener deleteListener = new Constants.DeleteListener(this);

		this.type = new Id(this, var.getType());
		
		String id = var.isUnbound() ? var.getId() : Constants.variableId(var);
		
		this.id = new Id(this, Constants.variableId(var));
		if(exp != null) {
			new FixedToken(this, "=");
			this.expression = new ExpressionWidget(this, Expression.match(exp));
			this.expression.addKeyListener(deleteListener);
		}
		else
			this.expression = null;
		new FixedToken(this, ";");
		
		this.type.addKeyListener(deleteListener);
		Constants.addInsertLine(this.type);
		this.id.addKeyListener(deleteListener);
	}
	
	@Override
	public boolean setFocus() {
		return type.setFocus();
	}
	
	public void focusId() {
		id.setFocus();
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		type.toCode(buffer);
		buffer.append(" ");
		id.toCode(buffer);
		if(expression != null) {
			buffer.append(" = ");
			expression.toCode(buffer);
		}
		buffer.append(";");
	}
}
