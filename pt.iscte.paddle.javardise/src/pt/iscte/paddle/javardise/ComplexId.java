package pt.iscte.paddle.javardise;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IArrayElement;
import pt.iscte.paddle.model.IArrayLength;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IVariable;

public class ComplexId extends EditorWidget implements TextWidget, Expression {

	final Text text;
	private Menu popupMenu;
	private final boolean type;
	private boolean menuMode;

	private Supplier<List<String>> idProvider;

	private SelectionListener[] typeListeners;

	private Runnable editAction = () -> {};

	private List<CodeElement> elements = new ArrayList<>();

	ComplexId(Composite parent, IArrayElement e) {
		this(parent, Constants.variableId((IVariable) e.getTarget()), false);
		for(IExpression exp : e.getIndexes())
			new Dimension(this, Expression.match(exp));
	}

	ComplexId(EditorWidget p, IArrayLength e) {
		this(p, e.getVariable().getId(), false);
		addField("length");
	}
	
	ComplexId(Composite parent, String id, boolean type) {
		this(parent, id, type, () -> Collections.emptyList());
	}

	ComplexId(Composite parent, String id, boolean type, Supplier<List<String>> idProvider) {
		super(parent);
		this.idProvider = idProvider;
		this.type = type;
		this.menuMode = false;
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		text = Constants.createText(parent, id);
		List<String> provider = idProvider.get();

		text.addVerifyListener(e -> e.doit = menuMode ||
				isValidCharacter(e.character) || e.character == Constants.DEL_KEY || e.character == SWT.CR || e.character == '/');

		text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				//				text.setBackground(Constants.COLOR_BACKGROUND);
				text.selectAll();
			}

			public void focusLost(FocusEvent e) {
				//				if(text.getText().isBlank() || !type && Keyword.is(text.getText())) {
				//					text.setBackground(Constants.COLOR_ERROR);
				//				}
				//				else
				//					text.setBackground(Constants.COLOR_BACKGROUND);

				//				if(Keyword.VOID.isEqual(text))
				//					removeAllDimensions();

				//				Constants.setFont(text, false); 
				//				editAction.run();
			}			
		});

		text.addModifyListener(Constants.MODIFY_PACK);

		text.addKeyListener(addListener);


		if(!provider.isEmpty()) {
			addMenu(provider);
			//			addKeyListeners();
		}
		else
			text.setMenu(new Menu(text)); // prevent system menu

		Constants.addArrowKeys(text, this);
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

	private KeyListener addListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(e.character == '[' && !Keyword.VOID.isEqual(text))
				addDimension((Control) e.widget);
			else if(e.character == '.' && !text.getText().isBlank())
				addField("field");
		}
	};

	void addField(String id) {
		Field field = new Field(ComplexId.this, id);
		field.setFocus();
		requestLayout();
	}

	public void setEditAction(Runnable editAction) {
		this.editAction = editAction == null ? () -> {} : editAction;
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
			if(left != null)
				dimension.expression.moveBelow(left);
			dimension.setFocus();
		}
		requestLayout();
	}

	private class Dimension implements CodeElement {
		private ExpressionWidget expression;
		public Dimension(Composite parent, Expression.Creator f) {
			elements.add(this);
			if(f == null)
				f = p -> new SimpleExpressionWidget(p, "expression");
			new FixedToken(parent, "[");
			expression = new ExpressionWidget(parent, f);
			Token rightBracket = new Token(parent, "]");
			rightBracket.addKeyListener(new KeyAdapter() {
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

		public boolean setFocus() {
			return expression.setFocus();
		}

		public void toCode(StringBuffer buffer) {
			buffer.append('[');
			expression.toCode(buffer);
			buffer.append(']');
		}
	}

	private class Field implements CodeElement {
		Text field;
		public Field(Composite parent, String id) {
			elements.add(this);
			new FixedToken(parent, ".");
			field = Constants.createText(parent, id);
			field.addVerifyListener(e -> e.doit = menuMode || Constants.isLetter(e.character) || e.character == SWT.BS);
			field.addModifyListener(Constants.MODIFY_PACK);
			// TODO empty modify
			Constants.addArrowKeys(field, TextWidget.create(field));
			Constants.addFocusSelectAll(field);
			field.addKeyListener(addListener);
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
	}

	private void removeAllDimensions() {
		Control[] children = getChildren();
		for(int i = 1; i < children.length; i++)
			children[i].dispose();

		requestLayout();
		children[0].setFocus();
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

	private void addMenu(List<String> provider) {
		popupMenu = new Menu(text);
		MenuItem[] items = new MenuItem[provider.size()];
		typeListeners = new SelectionListener[provider.size()];
		for(int i = 0; i < provider.size(); i++) {
			MenuItem it = new MenuItem(popupMenu, SWT.CHECK);
			items[i] = it;
			items[i].setText(provider.get(i));
			//			items[i].setSelection(provider.get(i).equals(initialId));
			typeListeners[i] = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					for(MenuItem m : items)
						m.setSelection(m == it);
					menuMode = true;
					text.setText(it.getText());
					menuMode = false;
					Constants.setFont(text, false);
					text.requestLayout();
				}
			};
			items[i].addSelectionListener(typeListeners[i]);
		}
		setMenu(popupMenu);
	}

	private void addKeyListeners() {
		text.addKeyListener(KeyListener.keyPressedAdapter(e -> {
			List<String> list = idProvider.get();
			if(e.keyCode == Constants.MENU_KEY && popupMenu != null) {
				popup(popupMenu, text);
			}
			else {
				for(String i : list) {
					if(i.charAt(0) == e.character && !text.getText().equals(i)) {
						menuMode = true;
						text.setText(i); 
						menuMode = false;
						e.doit = false;
						break;
					}
				}
			}
			setAtRight();
		}));
	}

	public void setToolTip(String text) {
		this.text.setToolTipText(text);
	}

	@Override
	public boolean setFocus() {
		text.setFocus();
		return true;
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
		CodeElement.toCode(text, buffer);
		for(CodeElement e : elements)
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
		menuMode = true;
		text.setText("");
		Control[] children = getChildren();
		for(int i = 1; i < children.length; i++)
			children[i].dispose();
		menuMode = false;
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
		for(int i = elements.size()-1; elements.get(i) instanceof Dimension; i--)
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
	public Expression copyTo(EditorWidget parent) {
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

//	public boolean isChild(Control control) {
//		for(Control c : getChildren())
//			if(c == control)
//				return true;
//		return false;
//	}

}
