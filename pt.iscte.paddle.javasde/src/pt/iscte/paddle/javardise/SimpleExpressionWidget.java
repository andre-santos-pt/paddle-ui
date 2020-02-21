package pt.iscte.paddle.javardise;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariable;

public class SimpleExpressionWidget extends Composite implements TextWidget, Expression {

	final Text text;
	private Class<?> literalType;
	
	public SimpleExpressionWidget(EditorWidget parent, String literal) {
		super(parent, SWT.NONE);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		
		text = new Text(this, SWT.NONE);
		text.setText(literal);
		text.setBackground(Constants.COLOR_BACKGROUND);
		Constants.setFont(text, true);
		text.addVerifyListener(e -> e.doit = 
				validCharacter(e.character) || 
				e.character == '.' && text.getText().indexOf('.') == -1 || 
				e.character == '-' && text.getText().indexOf('-') == -1 ||
				e.character == SWT.BS);

		text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				text.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
				updateContent();
			}
		});
		
		Constants.addArrowKeys(text, this);
		
		addTransformationKeyListener();
		text.addModifyListener(Constants.MODIFY_PACK);
		text.setMenu(new Menu(text));
	}

	private void addTransformationKeyListener() {
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(text.isDisposed())
					return;
				String match = null;
				Expression w = null;
				if(text.getCaretPosition() == 0 && (match = match(e.character, Constants.UNARY_OPERATORS)) != null) {
					w = new UnaryExpressionWidget((EditorWidget) getParent(), match, SimpleExpressionWidget.this);
					w.setFocus();
				}
				else if(text.getCaretPosition() == text.getText().length() && (match = match(e.character, Constants.BINARY_OPERATORS)) != null) {
					BinaryExpressionWidget b = new BinaryExpressionWidget((EditorWidget) getParent(), p -> new SimpleExpressionWidget(p, text.getText()) , match);
					b.focusRight();
					w = b;
				}
				else if(e.character == SWT.SPACE && Keyword.NEW.isEqual(text)) {
					ArrayAllocationExpression a = new ArrayAllocationExpression((EditorWidget) getParent(), p -> new SimpleExpressionWidget(p, "expression"));
					a.setFocus();
					w = a;
				}
				else if(e.character == '(' && Id.isValid(text.getText()) && text.getCaretPosition() == text.getText().length() && text.getSelectionCount() == 0) {
					CallWidget c = new CallWidget((EditorWidget) getParent(), text.getText(), false);
					c.focusArgument();
					w = c;
				}
				
				else if(e.character == '[' && Id.isValid(text.getText()) && text.getCaretPosition() == text.getText().length() && text.getSelectionCount() == 0) {
//					ComplexId id = new ComplexId((EditorWidget) getParent(), text.getText(), false);
//					ArrayElementExpression a = new ArrayElementExpression((EditorWidget) getParent(), text.getText(), "expression");
//					a.addExpressionInserts();
//					a.focusExpression();
//					w = a;
				}
				
//				else if(e.character == '.' && Id.isValid(text.getText())) {
//					FieldExpression f = new FieldExpression((EditorWidget) getParent(), text.getText());
//					f.focusExpression();
//					w = f;
//				}
				
				
				else if(e.character == SWT.CR) {
					text.traverse(SWT.TRAVERSE_TAB_NEXT);
				}
				
				if(w != null) {
					((Expression) getParent()).substitute(SimpleExpressionWidget.this, w);
					e.doit = false;
				}
			}
		});
	}

	@Override
	public void setData(Object data) {
		text.setData(data);
	}
	
	private String match(char character, List<String> operators) {
		for(String o : operators)
			if(o.charAt(0) == character)
				return o;
		return null;
	}

	private boolean validCharacter(char c) {
		return Id.isValidCharacter(c) || c >= '0' && c <= '9';
	}

	@Override
	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	public void set(String expression) {
		text.setText(expression);
	}

	@Override
	public String toString() {
		return text.getText();
	}

	@Override
	public Text getWidget() {
		return text;
	}
	
	@Override
	public Expression copyTo(EditorWidget parent) {
		return new SimpleExpressionWidget(parent, text.getText());
	}
	
	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		buffer.append(text.getText().isBlank() ? Constants.EMPTY_EXPRESSION_SERIALIZE : text.getText());
	}

	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public IExpression toModel() {
		if(literalType == Integer.class)
			return IType.INT.literal(Integer.parseInt(text.getText()));
		else if(Id.isValid(text.getText()))
			return new IVariable.UnboundVariable(text.getText());
		else
			return null;
	}

	private void updateContent() {
		Constants.setFont(text, false);
		text.setBackground(Constants.COLOR_BACKGROUND);
		text.setForeground(Constants.FONT_COLOR);
		text.setToolTipText("");
		literalType = null;
		try {
			Integer.parseInt(text.getText());
			literalType = Integer.class;
		}
		catch(NumberFormatException ex) {
			try {
				Double.parseDouble(text.getText());
				literalType = Double.class;
				text.setForeground(Constants.COLOR_LITERAL);
			}
			catch(NumberFormatException ex2) {
				if(text.getText().matches("'.'")) {
					literalType = Character.class;
					text.setForeground(Constants.COLOR_LITERAL);
				}
				else if(text.getText().matches("true|false")) {
					literalType = Boolean.class;
					text.setForeground(Constants.COLOR_KW);
				}
				else if(Id.isValid(text.getText()))
					literalType = null;
				else {
					text.setBackground(Constants.COLOR_ERROR);
					text.setToolTipText("invalid expression");
					text.requestLayout();
					literalType = null;
				}
			}
		}
		text.requestLayout();
	}
}
