package pt.iscte.paddle.javardise;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.service.IDeclarationWidget;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariableDeclaration;

public class DeclarationWidget extends EditorWidget implements IDeclarationWidget {
	private final ComplexId type;
	private final Id id;
	private ExpressionWidget expression;
	private FixedToken semiColon;
	
	DeclarationWidget(Composite parent, IVariableDeclaration var, IExpression exp) {
		super(parent, var);
		this.type = ComplexId.matchType(this, var.getType());
		this.id = new Id(this, Constants.variableId(var));
		setLayout(Constants.ROW_LAYOUT_H);
		if(exp != null)
			addExpression(exp);
		
		semiColon = new FixedToken(this, ";");
		
		KeyListener delListener = this.type.addDeleteListener(() -> var.remove());
		Constants.addInsertLine(this.type);
		this.id.addKeyListener(delListener);
		this.id.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.character == '=') {
					addExpression(var.getType().getDefaultExpression());
					expression.setFocus();
				}
			}
		});
		if(exp != null)
			this.expression.addKeyListener(delListener);
	}

	private void addExpression(IExpression exp) {
		FixedToken equals = new FixedToken(this, "=");
		expression = new ExpressionWidget(this, Expression.match(exp), exp);
		expression.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == Constants.DEL_KEY && ((Text) e.widget).getCaretPosition() == 0) {
					equals.dispose();
					expression.dispose();
					expression = null;
					id.setFocus();
					DeclarationWidget.this.requestLayout();
				}
			}
		});
		semiColon.getControl().moveBelow(expression);
		expression.requestLayout();
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
	
	@Override
	public IWidget getVariableType() {
		return type;
	}
	
	@Override
	public IWidget getVariableName() {
		return id;
	}
}
