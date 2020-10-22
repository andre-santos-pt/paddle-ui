package pt.iscte.paddle.javaeditor;


import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.Id;
import pt.iscte.paddle.javardise.ModiferWidget;
import pt.iscte.paddle.javardise.SimpleExpressionWidget;
import pt.iscte.paddle.model.IConstantDeclaration;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;

class FieldWidget extends ModiferWidget<IProgramElement> {
	private ExpressionChain type;
	private Id id;
	private SimpleExpressionWidget expression;
	
	@Override
	public Composite getHeader() {
		return this;
	}
	
	@Override
	public Function<List<String>, List<String>> getModifierProvider() {
		return list -> Keyword.fieldModifiers();
	}
	
	private FieldWidget(Composite parent, IProgramElement e, Supplier<IType> ts) {
		super(parent, e);
		setLayout(Constants.ROW_LAYOUT_H);
		addModifiers(e);
		this.type = ExpressionChain.matchType(this, ts.get());
		addModifierKey(type);
		String id = e.getId();
		if(id == null)
			id = "field";
		
		this.id = new Id(this, id);
		this.id.setEditAction(() -> e.setId(this.id.getText()));
	}
	
	public FieldWidget(Composite parent, IVariableDeclaration var) {
		this(parent, var, () -> var.getType());
		new FixedToken(this, ";");
		KeyListener delListener = this.type.addDeleteListener(() -> var.remove());
		this.id.addKeyListener(delListener);
	}
	
	public FieldWidget(Composite parent, IConstantDeclaration constant) {
		this(parent, constant, () -> constant.getType());
		new FixedToken(this, "=");
		this.expression = new SimpleExpressionWidget(this, constant.getValue().getStringValue());
		
		// TODO remove constan
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
			public void propertyChanged(Object key, Object newValue, Object oldValue) {
				if(key.equals("ID"))
					FieldWidget.this.id.set(oldValue == null ? "" : oldValue.toString());
				else if(key.equals("VALUE"))
					FieldWidget.this.expression.set(newValue.toString());
			}
		});
	}

	private void addModifiers(IProgramElement e) {
		for (String mod : Keyword.fieldModifiers())
			if(e.is(mod))
				addModifier(mod);
	}
	
	@Override
	public boolean setFocus() {
		id.setFocus();
		return true;
	}

	public void focusExpression() {
		if(expression != null)
			expression.setFocus();
	}
}
