package pt.iscte.paddle.javaeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.Expression;
import pt.iscte.paddle.javardise.ExpressionWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.ILanguageConfiguration;
import pt.iscte.paddle.javardise.InfixExpressionWidget;
import pt.iscte.paddle.javardise.InsertWidget;
import pt.iscte.paddle.javardise.SimpleExpressionWidget;
import pt.iscte.paddle.javardise.TextWidget;
import pt.iscte.paddle.javardise.TokenWidget;
import pt.iscte.paddle.javardise.Expression.SubstitutableExpression;
import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.ITargetExpression;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;

public class ExpressionChain extends EditorWidget implements TextWidget, Expression {

	private Text text;
	private boolean type;
	private List<CodeElementControl> elements;
	private VerifyListener verifyListener;

	private Supplier<Boolean> allowEmpty = () -> false;

	public static ExpressionChain matchType(Composite parent, IType t) {
		ExpressionChain id = null;
		if(t.isReference())
			t = ((IReferenceType) t).getTarget();

		if(t instanceof IArrayType) {
			IArrayType at = (IArrayType) t;
			id = new ExpressionChain(parent, at.getRootComponentType().getId(), true);
			for(int i = at.getDimensions(); i > 0; i--)
				id.addDimension();
		}
		else
			id = new ExpressionChain(parent, t.getId(), true);
		return id;
	}

	// TODO fix for expression resolve
	public ExpressionChain(Composite parent, IArrayElement e) {
		this(parent, e, BlockAction.variableId(((IVariableExpression) e.getTarget()).getVariable()));
		for(IExpression exp : e.getIndexes())
			new Dimension(this, Configuration.match(exp));
	}

	// TODO fix for expression resolve
	public ExpressionChain(Composite p, IArrayLength e) {
		this(p, e, ((IVariableExpression) e.getTarget()).getVariable().getId());
		addField("length");
	}


	// TODO fix for expression resolve
	public ExpressionChain(Composite p, IRecordFieldExpression e) {
		this(p, ((IVariableExpression) e.getTarget()).getVariable().getId(), false);
		addField(e.getField().getId());
	}

	public ExpressionChain(Composite p, IProcedureCall c) {
		this(p, c.getId() != null ? c.getId() : 
			c.getProcedure().getId() != null ? c.getProcedure().getId() : 
				"procedure", false);
	}
	public ExpressionChain(Composite parent, IProgramElement e, String id) {
		super(parent, e);
		this.type = false;
		init(id);
	}

	public ExpressionChain(Composite parent, String id, boolean type) {
		super(parent);
		this.type = type;
		init(id);
	}


	private void init(String id) {
		this.elements = new ArrayList<>();
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		text = Constants.createText(this, id);
		text.setBackground(Constants.COLOR_BACKGROUND);
		verifyListener = e -> e.doit = 
				ILanguageConfiguration.INSTANCE.isValidIdCharacter(e.character) || 
				e.character == Constants.DEL_KEY || 
				e.character == SWT.CR ||
				!type && e.character == '/' && (text.getText().isEmpty() || text.getText().startsWith("/")) ||
				!type && isComment();
		text.addVerifyListener(verifyListener);
		text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				text.setForeground(Constants.COLOR_FONT);
				text.setBackground(Constants.COLOR_BACKGROUND);
				text.selectAll();
			}
			public void focusLost(FocusEvent e) {
				//				setBackgroundColor();
			}
		});

		text.addModifyListener(Constants.MODIFY_PACK);
		text.addKeyListener(insertListener);
		text.setMenu(new Menu(text)); // prevent system menu
		Constants.addArrowKeys(text, this);
		Constants.addInsertLine(this);
		//		setBackgroundColor();
	}

	// TODO to fw
	private void setBackgroundColor() {
		for(Control c : getChildren()) {
			if(isComment()) {
				c.setForeground(Constants.COLOR_COMMENT);
				c.setBackground(Constants.COLOR_BACKGROUND);
			}
			else if(!text.getText().isBlank() && ExpressionChain.this.getParent() instanceof InsertWidget){
				c.setForeground(Constants.COLOR_BACKGROUND);
				c.setBackground(Constants.COLOR_ERROR);
			}
			else if(c instanceof Text && ILanguageConfiguration.INSTANCE.isKeyword(((Text)c).getText())) {
				c.setForeground(Constants.COLOR_KEYWORD);
				c.setBackground(Constants.COLOR_BACKGROUND);
			}
			else if(text.getText().isBlank() && !allowEmpty.get()) {
				c.setForeground(Constants.COLOR_FONT);
				c.setBackground(Constants.COLOR_ERROR);
			}
			else {
				c.setForeground(Constants.COLOR_FONT);
				c.setBackground(Constants.COLOR_BACKGROUND);
			}
		}
	}

	public void setAllowEmpty(Supplier<Boolean> allowEmpty) {
		this.allowEmpty = allowEmpty;
		setBackgroundColor();
	}

	public boolean isComment() {
		return text.getText().startsWith("//");
	}


	void setReadOnly() {
		text.setEditable(false);
	}

	public boolean isSingleId() {
		return elements.isEmpty();
	}

	public boolean isKeyword() {
		return isSingleId() && ILanguageConfiguration.INSTANCE.isKeyword(text.getText());
	}

	public String getId() {
		return text.getText();
	}

	@Override
	public Text getWidget() {
		return text;
	}

	private KeyListener insertListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(!isComment() && e.character == '[') // && !Keyword.VOID.isEqual(text))
				addDimension();
			else if(!type && !isComment() && e.character == '.' && !text.getText().isBlank())
				addField("field");
		}
	};

	public void addField(String id) {
		Field field = new Field(ExpressionChain.this, id);
		field.setFocus();
		requestLayout();
	}

	public void addDimension() {
		CodeElementControl e = type ? new EmptyDimension(this) : new Dimension(this, null);
		e.setFocus();
		e.getWidget().requestLayout();
	}

	public void addDimensionIndex(Expression.Creator f) {
		Dimension dimension = new Dimension(this, f);
		dimension.setFocus();
	}


	private interface CodeElementControl {
		void dispose();
		boolean setFocus();
		Control getWidget();
	}

	private class EmptyDimension implements CodeElementControl {
		TokenWidget token;

		public EmptyDimension(Composite parent) {
			elements.add(this);
			token = new TokenWidget(parent, "[]");
			token.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == Constants.DEL_KEY) {
						dispose();
						requestLayout();
						focusLastElement();
					}
					else if(e.character == '[')
						addDimension();
				}
			});
		}

		@Override
		public void dispose() {
			elements.remove(this);
			token.dispose();
		}

		@Override
		public boolean setFocus() {
			return token.setFocus();
		}

		@Override
		public Control getWidget() {
			return token.getWidget();
		}
	}


	private class Dimension implements CodeElementControl {
		private TokenWidget left;
		private ExpressionWidget expression;
		private TokenWidget right;

		public Dimension(Composite parent, Expression.Creator f) {
			elements.add(this);
			if(f == null)
				f = p -> new SimpleExpressionWidget(p, "expression");
				left = new TokenWidget(parent, "[");
				expression = new ExpressionWidget(parent, f, null);
				right = new TokenWidget(parent, "]");
				right.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if(e.character == '[') {
							addDimension();
						}
						else if(ExpressionChain.this.getParent() instanceof ExpressionWidget &&
								Configuration.match(e.character, JavaConstants.BINARY_OPERATORS) != null) {
							InfixExpressionWidget w = new InfixExpressionWidget(getParent(),null,
									Configuration.match(e.character, JavaConstants.BINARY_OPERATORS),
									p -> ExpressionChain.this.copyTo(p));
							ExpressionChain.this.requestLayout();
							w.focusRight();
						}
						else if(e.character == Constants.DEL_KEY) {
							elements.remove(Dimension.this);
							dispose();
							ExpressionChain.this.requestLayout();
							ExpressionChain.this.focusLastElement();
						}
						else {
							Event ev = new Event();
							ev.type = SWT.KeyDown;
							ev.character = e.character;
							ev.keyCode = e.keyCode;
							text.notifyListeners(SWT.KeyDown, ev);
						}
					}
				});

		}

		public boolean setFocus() {
			return expression.setFocus();
		}

		@Override
		public void dispose() {
			left.dispose();
			expression.dispose();
			right.dispose();
		}

		@Override
		public Control getWidget() {
			return expression.getControl();
		}
	}


	private class Field implements CodeElementControl {
		FixedToken dot;
		ExpressionChain field;
		public Field(Composite parent, String id) {
			elements.add(this);
			dot = new FixedToken(parent, ".");
			field = new ExpressionChain(parent, id, false);
			field.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(ExpressionChain.this.getParent() instanceof ExpressionWidget &&
							Configuration.match(e.character, JavaConstants.BINARY_OPERATORS) != null) {
						InfixExpressionWidget w = new InfixExpressionWidget(getParent(),null,
								Configuration.match(e.character, JavaConstants.BINARY_OPERATORS),
								p -> ExpressionChain.this.copyTo(p));
						ExpressionChain.this.requestLayout();
						w.focusRight();
					}
					else if(e.character == Constants.DEL_KEY && field.getCaretPosition() == 0) {
						elements.remove(Field.this);
						dispose();
						ExpressionChain.this.requestLayout();
						ExpressionChain.this.focusLastElement();
					}
				}
			});
		}

		public boolean setFocus() {
			return field.setFocus();
		}

		@Override
		public void dispose() {
			dot.dispose();
			field.dispose();
		}

		@Override
		public Control getWidget() {
			return field.getWidget();
		}

	}


	public void focusLastElement() {
		if(elements.isEmpty())
			text.setFocus();
		else
			elements.get(elements.size()-1).setFocus();
	}

	@Override
	public boolean setFocus() {
		return text.setFocus();
	}


	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	@Override
	public String toString() {
		return text.getText();
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		text.addFocusListener(listener);
	}

	public void clean() {
		text.removeVerifyListener(verifyListener);
		text.setText("");
		text.addVerifyListener(verifyListener);
		for(CodeElementControl e : new ArrayList<CodeElementControl>(elements))
			e.dispose();
		elements.clear();
		setBackgroundColor();
		requestLayout();
	}

	public boolean isEmpty() {
		return isSingleId() && text.getText().isBlank();
	}

	public boolean isArrayAccess() {
		return elements.get(elements.size()-1) instanceof Dimension;
	}

	public boolean isFieldAccess() {
		return elements.get(elements.size()-1) instanceof Field;
	}

	public List<String> getFields() {
		List<String> fields = new ArrayList<>();
		for(int i = 0; i < elements.size(); i++)
			if(elements.get(i) instanceof Field)
				fields.add(((Field)elements.get(i)).field.getText());
		return fields;
	}

	@Override
	public Expression copyTo(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSelectionCount() {
		return text.getSelectionCount();
	}

	public int getCaretPosition() {
		return text.getCaretPosition();
	}

	@Override
	public void accept(Consumer<String> visitor) {
		visitor.accept(text.getText());
		//		for(CodeElementControl e : elements)
		//			visitor.accept(e.getWidget());
	}

	public ITargetExpression getTargetExpression(IProcedure p) {
		assert !type;
		IVariableDeclaration var = p.getVariable(text.getText());
		if(var == null)
			var = new IVariableDeclaration.UnboundVariable(text.getText());

		ITargetExpression e = var.expression();
//		for(int i = 0; i < elements.size()-1; i++) {
//			if(elements.get(i) instanceof Dimension) {
//				Expression exp = ((Dimension) elements.get(i)).expression.getExpression();
//				IExpression t = extracted(p, exp);
//				e = e.element(t);
//			}
//		}
		return e;
	}

	private IExpression extracted(IProcedure p, ExpressionWidget exp) {
		IExpression t;
		Expression expression = exp.getExpression();
		if(expression instanceof SimpleExpressionWidget) {
			SimpleExpressionWidget w = (SimpleExpressionWidget) expression;
			if(ILanguageConfiguration.INSTANCE.isValidId(w.getText())) {
				String id = w.getText();
				IVariableDeclaration ivar = p.getVariable(id);
				if(ivar == null)
					ivar = new IVariableDeclaration.UnboundVariable(id);
				t = ivar.expression();
			}
			else
				t = IType.INT.literal(Integer.parseInt(w.getText()));
		}
		else {// if(expression instanceof ExpressionChain) {
			t = getTargetExpression(p);
		}
		return t;
	}

	public List<IExpression> getArrayModelExpressions(IProcedure p) {
		assert isArrayAccess();
		List<IExpression> list = new ArrayList<>();
		for(int i = elements.size()-1; i >= 0 && elements.get(i) instanceof Dimension; i--)
			list.add(0, extracted(p, ((Dimension) elements.get(i)).expression));
		return list;
	}
}