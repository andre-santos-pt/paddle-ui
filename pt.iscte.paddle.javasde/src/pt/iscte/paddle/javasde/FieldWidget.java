package pt.iscte.paddle.javasde;


import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IConstant;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IType;

public class FieldWidget extends EditorWidget {
	IConstant constant;
	private Id type;
	private Id id;
	private SimpleExpressionWidget expression;
	
	public FieldWidget(Composite parent, IType type, String id, Keyword[] modifiers, boolean assign) {
		super(parent);
		for(Keyword mod : modifiers)
			new Token(this, mod);
		this.type = new Id(this, type.getId(), true, Constants.PRIMITIVE_TYPES_SUPPLIER);
		this.id = new Id(this, id, false);
		if(assign) {
			new FixedToken(this, "=");
			this.expression = new SimpleExpressionWidget(this, "expression", false);			
		}
		new FixedToken(this, ";");
	}
	
	public FieldWidget(Composite parent, IConstant constant) {
		super(parent);
		this.constant = constant;
		
		for (Keyword mod : Keyword.fieldModifiers())
			if(constant.is(mod.toString()))
				new Token(this, mod);
		
		this.type = new Id(this, constant.getType().getId(), true, Constants.PRIMITIVE_TYPES_SUPPLIER);
		
		String id = constant.getId();
		if(id == null)
			id = "CONSTANT";
		
		this.id = new Id(this, id, false);
		this.id.setEditAction(() -> constant.setId(this.id.getText()));
		
		new FixedToken(this, "=");
		this.expression = new SimpleExpressionWidget(this, constant.getValue().getStringValue(), false);
		
		new FixedToken(this, ";");
		
		expression.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				ILiteral val = ILiteral.matchValue(expression.getText());
				if(val == null)
					val = ILiteral.matchValue("0");
				
				constant.setValue(val);
			}
		});
		
		constant.addPropertyListener(new IProgramElement.IPropertyListener() {
			
			@Override
			public void propertyChanged(Object key, Object newValue, Object oldValue) {
				if(key.equals("ID"))
					FieldWidget.this.id.set(oldValue == null ? "" : oldValue.toString());
				else if(key.equals("VALUE"))
					FieldWidget.this.expression.set(newValue.toString());
			}
		});
	}
	
	@Override
	public String toString() {
		return type + " " + id + " = " + expression + ";";
	}
	
	@Override
	public boolean setFocus() {
		type.setFocus();
		return true;
	}

	public void focusExpression() {
		if(expression != null)
			expression.setFocus();
	}
	
	public void accept(Visitor visitor) {
		visitor.visit(this);
		super.accept(visitor);
	}

	
}
