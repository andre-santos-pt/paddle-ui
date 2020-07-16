package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;
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

import pt.iscte.paddle.javardise.service.ICodeElement;
import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableExpression;

public class ComplexId extends EditorWidget implements TextWidget, Expression {

	private Text text;
	private boolean type;
	private List<CodeElementControl> elements;
	private VerifyListener verifyListener;

	private Supplier<Boolean> allowEmpty = () -> false;

	static ComplexId matchType(Composite parent, IType t) {
		ComplexId id = null;
		if(t.isReference())
			t = ((IReferenceType) t).getTarget();

		if(t instanceof IArrayType) {
			IArrayType at = (IArrayType) t;
			id = new ComplexId(parent, at.getRootComponentType().getId(), true);
			for(int i = at.getDimensions(); i > 0; i--)
				id.addDimension();
		}
		else
			id = new ComplexId(parent, t.getId(), true);
		return id;
	}

	// TODO fix for expression resolve
	ComplexId(Composite parent, IArrayElement e) {
		this(parent, e, Constants.variableId(((IVariableExpression) e.getTarget()).getVariable()));
		for(IExpression exp : e.getIndexes())
			new Dimension(this, Expression.match(exp));
	}

	// TODO fix for expression resolve
	ComplexId(Composite p, IArrayLength e) {
		this(p, e, ((IVariableExpression) e.getTarget()).getVariable().getId());
		for(IExpression exp : e.getIndexes())
			new Dimension(this, Expression.match(exp));
		addField("length");
	}


	// TODO fix for expression resolve
	ComplexId(Composite p, IRecordFieldExpression e) {
		this(p, ((IVariableExpression) e.getTarget()).getVariable().getId(), false);
		addField(e.getField().getId());
	}

	ComplexId(Composite p, IProcedureCall c) {
		this(p, c.getId() != null ? c.getId() : 
			c.getProcedure() != null ? c.getProcedure().getId() : 
				"procedure", false);
	}

	static ComplexId createLength(Composite parent, IArrayLength e) {
		IExpression target = e.getTarget();
		ComplexId id = null;
		if(target instanceof IArrayElement) {
			id = new ComplexId(parent, e, ((IArrayElement) target).getId());
			for(IExpression exp : ((IArrayElement) target).getIndexes())
				id.new Dimension(id, Expression.match(exp));
		}
		else if(target instanceof IVariableExpression)
			id = new ComplexId(parent, e, ((IVariableExpression) target).getId());
		id.addField("length");
		return id;
	}


	ComplexId(Composite parent, String id, boolean type) {
		super(parent);
		this.type = type;
		init(id);
	}

	ComplexId(Composite parent, IProgramElement e, String id) {
		super(parent, e);
		this.type = false;
		init(id);
	}

	private void init(String id) {
		this.elements = new ArrayList<>();
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		text = Constants.createText(this, id);
		verifyListener = e -> e.doit = 
				isValidCharacter(e.character) || 
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
				setBackgroundColor();
			}
		});

		text.addModifyListener(Constants.MODIFY_PACK);
		text.addKeyListener(insertListener);
		text.setMenu(new Menu(text)); // prevent system menu
		Constants.addArrowKeys(text, this);
		Constants.addInsertLine(this);
		setBackgroundColor();
	}

	private void setBackgroundColor() {
		for(Control c : getChildren()) {
			if(isComment()) {
				c.setForeground(Constants.COLOR_COMMENT);
				c.setBackground(Constants.COLOR_BACKGROUND);
			}
			else if(!text.getText().isBlank() && ComplexId.this.getParent() instanceof InsertWidget){
				c.setForeground(Constants.COLOR_BACKGROUND);
				c.setBackground(Constants.COLOR_ERROR);
			}
			else if(c instanceof Text && Keyword.is(((Text)c).getText())) {
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

	void setAllowEmpty(Supplier<Boolean> allowEmpty) {
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
		return isSingleId() && Keyword.is(text.getText());
	}

	public boolean isKeyword(Keyword ... keywords) {
		for(Keyword k : keywords)
			if(k.isEqual(text))
				return true;
		return false;
	}

	public boolean isKeyword(Keyword keyword) {
		return isSingleId() && keyword.isEqual(text);
	}

	String getId() {
		return text.getText();
	}

	@Override
	public Text getWidget() {
		return text;
	}

	private KeyListener insertListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(!isComment() && e.character == '[' && !Keyword.VOID.isEqual(text))
				addDimension((Control) e.widget);
			else if(!type && !isComment() && e.character == '.' && !text.getText().isBlank())
				addField("field");
		}
	};

	void addField(String id) {
		//		ComplexId cid = new ComplexId(this, id, false);
		//		cid.setFocus();
		Field2 field = new Field2(ComplexId.this, id);
		field.setFocus();
		requestLayout();
	}

	public void addDimension() {
		addDimension(null);
	}

	public void addDimensionIndex(Expression.Creator f) {
		Dimension dimension = new Dimension(this, f);
		dimension.setFocus();
	}

	private void addDimension(Control left) {
		CodeElementControl e = type ? new EmptyDimension(this) : new Dimension(this, null);
		e.setFocus();
		e.getControl().requestLayout();
	}

	private interface CodeElementControl extends ICodeElement {
		void dispose();
		boolean setFocus();
	}

	private class EmptyDimension implements CodeElementControl {
		Token token;

		public EmptyDimension(Composite parent) {
			elements.add(this);
			token = new Token(parent, "[]");
			token.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == Constants.DEL_KEY) {
						dispose();
						requestLayout();
						focusLastElement();
					}
					else if(e.character == '[')
						addDimension(null);
				}
			});
		}
		@Override
		public Control getControl() {
			return token.getControl();
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
		public void toCode(StringBuffer buffer) {
			token.toCode(buffer);
		}

	}

	private class Dimension implements CodeElementControl {
		private Token left;
		private ExpressionWidget expression;
		private Token right;

		public Dimension(Composite parent, Expression.Creator f) {
			elements.add(this);
			if(f == null)
				f = p -> new SimpleExpressionWidget(p, "expression");
				left = new Token(parent, "[");
				expression = new ExpressionWidget(parent, f, null);
				right = new Token(parent, "]");
				right.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if(e.character == '[') {
							addDimension(Dimension.this.expression);
						}
						else if(e.character == Constants.DEL_KEY) {
							elements.remove(Dimension.this);
							dispose();
							ComplexId.this.requestLayout();
							ComplexId.this.focusLastElement();
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

		@Override
		public Control getControl() {
			return expression;
		}

		public boolean setFocus() {
			return expression.setFocus();
		}

		public void toCode(StringBuffer buffer) {
			buffer.append('[');
			expression.toCode(buffer);
			buffer.append(']');
		}

		@Override
		public void dispose() {
			left.dispose();
			expression.dispose();
			right.dispose();
		}
	}

	private class Field2 implements CodeElementControl {
		FixedToken dot;
		ComplexId field;
		public Field2(Composite parent, String id) {
			elements.add(this);
			dot = new FixedToken(parent, ".");
			field = new ComplexId(parent, id, false);
			//			field.addKeyListener(insertListener);
			field.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.character == Constants.DEL_KEY && field.getCaretPosition() == 0) {
						elements.remove(Field2.this);
						dispose();
						ComplexId.this.requestLayout();
						ComplexId.this.focusLastElement();
					}
					//					else {
					//						Event ev = new Event();
					//						ev.type = SWT.KeyDown;
					//						ev.character = e.character;
					//						ev.keyCode = e.keyCode;
					//						text.notifyListeners(SWT.KeyDown, ev);
					//					}
				}
			});
		}

		public boolean setFocus() {
			return field.setFocus();
		}

		public void toCode(StringBuffer buffer) {
			buffer.append('.');
			field.toCode(buffer);
			//			if(field.getText().isBlank())
			//				buffer.append(Constants.EMPTY_EXPRESSION_SERIALIZE);
			//			else
			//				buffer.append(field.getText());
		}

		@Override
		public void dispose() {
			dot.dispose();
			field.dispose();
		}

		@Override
		public Control getControl() {
			return field;
		}
	}

	private class Field implements CodeElementControl {
		FixedToken dot;
		Text field;
		public Field(Composite parent, String id) {
			elements.add(this);
			dot = new FixedToken(parent, ".");
			field = Constants.createText(parent, id);
			field.addVerifyListener(e -> e.doit = Constants.isLetter(e.character) || e.character == SWT.BS);
			field.addModifyListener(Constants.MODIFY_PACK);
			// TODO empty modify
			Constants.addArrowKeys(field, TextWidget.create(field));
			Constants.addFocusSelectAll(field);
			field.addKeyListener(insertListener);
			field.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.character == Constants.DEL_KEY && field.getCaretPosition() == 0) {
						elements.remove(Field.this);
						dispose();
						ComplexId.this.requestLayout();
						ComplexId.this.focusLastElement();
					}
					else if(e.character == '.') {

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
			return field.setFocus();
		}

		public void toCode(StringBuffer buffer) {
			buffer.append('.');
			if(field.getText().isBlank())
				buffer.append(Constants.EMPTY_EXPRESSION_SERIALIZE);
			else
				buffer.append(field.getText());
		}

		@Override
		public void dispose() {
			dot.dispose();
			field.dispose();
		}

		@Override
		public Control getControl() {
			return field;
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

	public static boolean isValidCharacter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	public static boolean isValid(String s) {
		return s.matches("[a-zA-Z_]+") && !Keyword.is(s);
	}

	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	@Override
	public String toString() {
		return text.getText();
	}

	@Override
	public void toCode(StringBuffer buffer) {
		ICodeElement.toCode(text, buffer);
		for(ICodeElement e : elements)
			e.toCode(buffer);
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


	public List<IExpression> getArrayModelExpressions() {
		assert isArrayAccess();
		List<IExpression> list = new ArrayList<>();
		for(int i = elements.size()-1; i >= 0 && elements.get(i) instanceof Dimension; i--)
			list.add(0, ((Dimension) elements.get(i)).expression.toModel());
		return list;
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

	@Override
	public void substitute(Expression current, Expression newExpression) {
		// TODO Auto-generated method stub

	}

	@Override
	public IExpression toModel() {
		// TODO resolve hole expression
		return null;
	}

	public int getSelectionCount() {
		return text.getSelectionCount();
	}

	public int getCaretPosition() {
		return text.getCaretPosition();
	}

}
