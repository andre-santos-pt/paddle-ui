package pt.iscte.paddle.javardise;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.service.ICodeElement;
import pt.iscte.paddle.model.IArrayType;
import pt.iscte.paddle.model.IReferenceType;
import pt.iscte.paddle.model.IType;

public class Id extends EditorWidget implements TextWidget {

	private final String initialId;
	private boolean menuMode = false;
	private final Text text;
	private Menu popupMenu;
	private final boolean type;
	
	private SelectionListener[] typeListeners;

	private Supplier<List<String>> idProvider;

	private Runnable editAction = () -> {};

	Id(Composite parent, IType type) {
		this(parent, rootType(type), true, () -> Collections.emptyList());
				
		if(type instanceof IReferenceType)
			type = ((IReferenceType) type).resolveTarget();
		
		if(type instanceof IArrayType) {
			int n = ((IArrayType) type).getDimensions();
			while(n-- > 0)
				addDimension();
		}
	}
	
	private static String rootType(IType type) {
		IType t = type;
		if(t instanceof IReferenceType)
			t = ((IReferenceType) t).resolveTarget();
		
		if(t instanceof IArrayType) 
			return ((IArrayType) t).getRootComponentType().getId();
		else
			return t.getId();
	}
	
	// identifier
	Id(Composite parent, String id) {
		this(parent, id, false, () -> Collections.emptyList());
	}

	Id(Composite parent, String id, boolean type, Supplier<List<String>> idProvider) {
		super(parent);
		this.idProvider = idProvider;
		this.type = type;
		setLayout(Constants.ROW_LAYOUT_H_SHRINK);
		setBackground(Constants.COLOR_BACKGROUND);
		initialId = id;
		text = new Text(this, SWT.NONE);
		text.setText(id);
		List<String> provider = idProvider.get();
		Constants.setFont(text, true);

		text.addVerifyListener(e -> e.doit = menuMode ||
				//!(e.keyCode == 'z' && (( e.stateMask & SWT.MODIFIER_MASK ) == SWT.CTRL || ( e.stateMask & SWT.MODIFIER_MASK ) == SWT.COMMAND)) && // TODO UNDO to function
				isValidCharacter(e.character) || e.character == Constants.DEL_KEY || e.character == SWT.CR);

		text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				text.setBackground(Constants.COLOR_BACKGROUND);
				text.selectAll();
			}
			
			public void focusLost(FocusEvent e) {
				if(text.getText().isBlank() || !type && Keyword.is(text.getText())) {
//					menuMode = true;
//					text.setText(initialId);
//					menuMode = false;
//					text.requestLayout();
					text.setBackground(Constants.COLOR_ERROR);
				}
				else
					text.setBackground(Constants.COLOR_BACKGROUND);
				
				if(Keyword.VOID.isEqual(text))
					removeAllDimensions();
				
				Constants.setFont(text, false); 
				editAction.run();
			}			
		});

		text.addModifyListener(Constants.MODIFY_PACK);
		if(type)
			text.addKeyListener(addDimListener);

	
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
	
	IType inferType() {
		IType t = IType.match(text.getText()); // TODO other types
		int n = numberOfDimensions();
		while(n-- > 0)
			t = t.array();
		return t;
	}
	@Override
	public Text getWidget() {
		return text;
	}

	private KeyListener addDimListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(e.character == '[' && !Keyword.VOID.isEqual(text))
				addDimension();
		}
	};
	
	public void setEditAction(Runnable editAction) {
		this.editAction = editAction == null ? () -> {} : editAction;
	}

	public void addDimension() {
		Token t = new Token(this, "[]");
		t.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == Constants.DEL_KEY)
					removeDimension((Control) e.widget);
				else if(e.character == '[')
					addDimension();
			}
		});
		t.setFocus();
		requestLayout();
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

	public int numberOfDimensions() {
		return getChildren().length-1;
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
			items[i].setSelection(provider.get(i).equals(initialId));
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
	public void toCode(StringBuffer buffer) {
		ICodeElement.toCode(text, buffer);
		int n = numberOfDimensions();
		while(n-- > 0)
			buffer.append("[]");
	}

	public void set(String id) {
		text.setText(id);
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}
}
