package pt.iscte.paddle.javardise;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IConstantExpression;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.ILiteral;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

public class SimpleExpressionWidget extends EditorWidget implements TextWidget, Expression {

	private Text text;
	private Class<?> literalType;

	public SimpleExpressionWidget(Composite parent, ILiteral l) {
		super(parent, l);
		init(parent, l.getStringValue());
	}

	public SimpleExpressionWidget(Composite parent, IVariableExpression e) {
		super(parent, e);
		init(parent, e.getVariable().getId());
	}

	public SimpleExpressionWidget(Composite parent, IConstantExpression c) {
		super(parent, c);
		init(parent, c.getConstant().getId());
	}

	public SimpleExpressionWidget(Composite parent, String literal) {
		super(parent);
		init(parent, literal);
	}

	private void init(Composite parent, String string) {
		assert string != null;
		setLayout(Constants.ROW_LAYOUT_H_ZERO);

		text = Constants.createText(this, string);
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

	@Override
	public Control getControl() {
		return text;
	}

	private void addTransformationKeyListener() {
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(text.isDisposed())
					return;
				String match = null;
				Expression w = null;
				if(text.getCaretPosition() == 0 && (match = match(e.character, Constants.UNARY_OPERATORS)) != null) {
					w = new UnaryExpressionWidget((EditorWidget) getParent(), match, p -> new SimpleExpressionWidget(p, text.getText()));
					w.setFocus();
				}
				else if(text.getCaretPosition() == text.getText().length() && (match = match(e.character, Constants.BINARY_OPERATORS)) != null) {
					BinaryExpressionWidget b = new BinaryExpressionWidget((EditorWidget) getParent(), p -> new SimpleExpressionWidget(p, text.getText()) , match);
					b.focusRight();
					w = b;
				}
				else if(e.character == SWT.SPACE && Keyword.NEW.isEqual(text)) {
					AllocationExpression a = new AllocationExpression((EditorWidget) getParent(), IType.INT.array(), p -> new SimpleExpressionWidget(p, "expression"));
					a.setFocus();
					w = a;
				}
				else if(e.character == '(' && Id.isValid(text.getText()) && text.getCaretPosition() == text.getText().length() && text.getSelectionCount() == 0) {
					CallWidget c = new CallWidget((EditorWidget) getParent(), IProcedureCall.unboundExpression(text.getText()), false);
					c.focusArgument();
					w = c;
				}
				else if(e.character == '(' && (text.getText().isBlank() || text.getSelectionCount() == text.getText().length())) {
					BracketsWidget b = new BracketsWidget((EditorWidget) getParent(), p -> new SimpleExpressionWidget(p, "expression"));
					b.setFocus();
					w = b;
				}
				else if(e.character == '[' && Id.isValid(text.getText()) && text.getCaretPosition() == text.getText().length() && text.getSelectionCount() == 0) {
					ComplexId id = new ComplexId((EditorWidget) getParent(), text.getText(), false);
					id.addDimension();
					id.focusLastDimension();
					w = id;
				}
				else if(e.character == SWT.CR) {
					text.traverse(SWT.TRAVERSE_TAB_NEXT);
				}

				if(w != null) {
					((Expression) getParent()).substitute(SimpleExpressionWidget.this, w); // TODO bug cast
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
	public boolean setFocus() {
		return text.setFocus();
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
	public Expression copyTo(Composite parent) {
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
			return new IVariableDeclaration.UnboundVariable(text.getText()).expression(); // TODO Unbound Variable to Expression
		else
			return null;
	}

	private void updateContent() {
		Constants.setFont(text, false);
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
					text.setForeground(Constants.COLOR_KEYWORD);
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
