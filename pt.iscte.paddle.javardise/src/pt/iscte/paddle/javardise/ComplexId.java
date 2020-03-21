package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;

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
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariableExpression;

public class ComplexId extends EditorWidget implements TextWidget, Expression {

	private final Text text;
	private final boolean type;
	private List<CodeElementControl> elements;

	private final VerifyListener verifyListener;
		
	// TODO fix for expression resolve
	ComplexId(Composite parent, IArrayElement e) {
		this(parent, Constants.variableId(((IVariableExpression) e.getTarget()).getVariable()), false);
		for(IExpression exp : e.getIndexes())
			new Dimension(this, Expression.match(exp));
	}

	// TODO fix for expression resolve
	ComplexId(Composite p, IArrayLength e) {
		this(p, ((IVariableExpression) e.getTarget()).getVariable().getId(), false);
		addField("length");
	}
	
	ComplexId(Composite parent, String id, boolean type) {
		super(parent);
		this.type = type;
		this.elements = new ArrayList<>();
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		text = Constants.createText(this, id);
		verifyListener = e -> e.doit = 
				isValidCharacter(e.character) || 
				e.character == Constants.DEL_KEY || 
				e.character == SWT.CR ||
				e.character == '/' && (text.getText().isEmpty() || text.getText().startsWith("/")) ||
				isComment();
		text.addVerifyListener(verifyListener);
		text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				text.setForeground(Constants.FONT_COLOR);
				text.setBackground(Constants.COLOR_BACKGROUND);
				text.selectAll();
			}
			public void focusLost(FocusEvent e) {
				if(isComment()) {
					text.setForeground(Constants.COLOR_COMMENT);
					text.setBackground(Constants.COLOR_BACKGROUND);
				}
				else if(!text.getText().isBlank() && ComplexId.this.getParent() instanceof NewInsertWidget){
					text.setForeground(Constants.COLOR_ERROR);
					text.setBackground(Constants.COLOR_HIGHLIGHT);
				}
				else {
					text.setForeground(Constants.FONT_COLOR);
					text.setBackground(Constants.COLOR_BACKGROUND);
				}
			}
		});

		text.addModifyListener(Constants.MODIFY_PACK);
		text.addKeyListener(insertListener);
		text.setMenu(new Menu(text)); // prevent system menu
		Constants.addArrowKeys(text, this);
		Constants.addInsertLine(this);
	}

	private boolean isComment() {
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
			else if(!isComment() && e.character == '.' && !text.getText().isBlank())
				addField("field");
		}
	};

	void addField(String id) {
		Field field = new Field(ComplexId.this, id);
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
	
	public void addDimension(Control left) {
		if(type) {
			Token t = new Token(this, "[]");
			t.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == Constants.DEL_KEY)
						removeDimension((Control) e.widget);
					else if(e.character == '[')
						addDimension(null);
				}
			});
			t.setFocus();
		}
		else {
			Dimension dimension = new Dimension(this, null);
//			if(left != null)
//				dimension.expression.moveBelow(left);
			dimension.setFocus();
		}
		requestLayout();
	}

	private interface CodeElementControl extends ICodeElement {
		void dispose();
	}
	
	private class Dimension implements CodeElementControl {
		private FixedToken left;
		private ExpressionWidget expression;
		private Token right;
		
		public Dimension(Composite parent, Expression.Creator f) {
			elements.add(this);
			if(f == null)
				f = p -> new SimpleExpressionWidget(p, "expression");
			left = new FixedToken(parent, "[");
			expression = new ExpressionWidget(parent, f, null);
			right = new Token(parent, "]");
			right.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.character == '[') {
						addDimension(Dimension.this.expression);
					}
					else if(e.character == Constants.DEL_KEY) {
						dispose();
						ComplexId.this.requestLayout();
						ComplexId.this.setFocus();
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

	private void removeDimension(Control control) {
		Control[] children = getChildren();
		for(int i = 1; i < children.length; i++)
			if(children[i] == control) {
				children[i].dispose();
				children[i-1].setFocus();
			}
		requestLayout();
	}

	public void focusLastDimension() {
		Control[] children = getChildren();
		children[children.length-1].setFocus();
	}

	

	

	public void setToolTip(String text) {
		this.text.setToolTipText(text);
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

	public void set(String id) {
		text.setText(id);
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
		for(CodeElementControl e : elements)
			e.dispose();
		elements.clear();
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
