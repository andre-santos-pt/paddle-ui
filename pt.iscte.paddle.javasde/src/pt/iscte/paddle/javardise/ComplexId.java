package pt.iscte.paddle.javardise;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IExpression;

public class ComplexId extends EditorWidget implements TextWidget {

	final Text text;
	private Menu popupMenu;
	private final boolean type;
	private boolean menuMode;

	private Supplier<List<String>> idProvider;

	private SelectionListener[] typeListeners;

	private Runnable editAction = () -> {};

	ComplexId(Composite parent, String id, boolean type) {
		this(parent, id, type, () -> Collections.emptyList());
	}

	ComplexId(Composite parent, String id, boolean type, Supplier<List<String>> idProvider) {
		super(parent);
		this.idProvider = idProvider;
		this.type = type;
		this.menuMode = false;
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		text = new Text(this, SWT.NONE);
		text.setText(id);
//		text.setBackground(Constants.COLOR_HIGHLIGHT);
		List<String> provider = idProvider.get();
		Constants.setFont(text, true);

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
		return getChildren().length == 1;
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

	public void addDimensionIndex(Function<EditorWidget, Expression> f) {
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
				dimension.moveBelow(left);
			dimension.setFocus();
		}
		requestLayout();
	}

	private class Dimension extends EditorWidget {
		private ExpressionWidget expression;
		public Dimension(Composite parent, Function<EditorWidget, Expression> f) {
			super(parent);
			if(f == null)
				f = p -> new SimpleExpressionWidget(p, "expression");
			new FixedToken(this, "[");
			expression = new ExpressionWidget(this, f);
			Token rightBracket = new Token(this, "]");
			rightBracket.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.character == '[') {
						addDimension(Dimension.this);
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
		public boolean setFocus() {
			return expression.setFocus();
		}

		public void toCode(StringBuffer buffer) {
			buffer.append('[');
			expression.toCode(buffer);
			buffer.append(']');
		}
	}

	private class Field extends EditorWidget {
		Text field;
		public Field(Composite parent, String id) {
			super(parent);
			new FixedToken(this, ".");
			field = new Text(this, SWT.NONE);
			field.setText(id);
			field.addVerifyListener(e -> e.doit = menuMode || Constants.isLetter(e.character) || e.character == SWT.BS);
			field.addModifyListener(Constants.MODIFY_PACK);
			// TODO empty modify
			Constants.setFont(field, true);
			Constants.addArrowKeys(field, TextWidget.create(field));
			Constants.addFocusSelectAll(field);
			field.addKeyListener(addListener);
		}

		@Override
		public boolean setFocus() {
			return field.setFocus();
		}

		@Override
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
		if(text.getText().isBlank())
			buffer.append(Constants.EMPTY_EXPRESSION_SERIALIZE);
		else
			buffer.append(text.getText());

		Control[] children = getChildren();
		for(int i = 1; i < children.length; i++)
			((EditorWidget) children[i]).toCode(buffer);
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
		Control[] children = getChildren();
		return children[children.length-1] instanceof Dimension;
	}
	
	public List<IExpression> getArrayModelExpressions() {
		assert isArrayAccess();
		Control[] children = getChildren();
		List<IExpression> list = new ArrayList<>();
		for(int i = children.length-1; children[i] instanceof Dimension; i--)
			list.add(0, ((Dimension) children[i]).expression.toModel());
		return list;
	}
	

	public boolean isFieldAccess() {
		Control[] children = getChildren();
		return children[children.length-1] instanceof Field;
	}

	public List<String> getFields() {
		Control[] children = getChildren();
		List<String> fields = new ArrayList<>();
		for(int i = 1; i < children.length; i++)
			if(children[i] instanceof Field)
				fields.add(((Field) children[i]).field.getText());
		return fields;
	}

	public boolean isChild(Control control) {
		for(Control c : getChildren())
			if(c == control)
				return true;
		return false;
	}

}
