package pt.iscte.paddle.javardise;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import pt.iscte.paddle.javardise.service.ICodeElement;


public class Token implements TextWidget, ICodeElement {
	private final Text text;
	private Multimap<Character, String> map;

	private static final List<String>[] EMPTY_ARRAY  = new List[0];

	public Token(Composite parent, Keyword token) {
		this(parent, token.toString());
	}

	public Token(Composite parent, String token) {
		this(parent, token, EMPTY_ARRAY);
	}

	public Token(Composite parent, Keyword keyword, List<Keyword> keywords) {
		this(parent, keyword.keyword(), keywords.stream().map(k -> k.keyword()).collect(Collectors.toList())) ;
	}

	public Token(Composite parent, String token, List<String> ... alternatives) {
		text = new Text(parent, SWT.NONE);
		text.setText(token);
		text.setEditable(false);
		text.setBackground(Constants.COLOR_BACKGROUND);

		if(Keyword.is(token)) {
			text.setFont(Constants.FONT_KEYWORD);
			text.setForeground(Constants.COLOR_KEYWORD);
		}
		else {
			text.setFont(token.equals(".") ? Constants.FONT_DOT : Constants.FONT);
			text.setForeground(Constants.COLOR_FONT);
		}
		map = ArrayListMultimap.create();

		Menu menu = new Menu(text);
		for(List<String> set : alternatives) {
			for(String t : set) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(t);
				map.put(t.charAt(0), t);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						((Text) text).setText(item.getText());
						text.requestLayout();
						text.selectAll();
						text.setFocus();
					}
				});
			}
			new MenuItem(menu, SWT.SEPARATOR);
		}

		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == Constants.MENU_KEY && menu != null) {
					menu.setLocation(text.toDisplay(0, 20));
					menu.setVisible(true);
				}
				else if(map.containsKey(e.character)) {
					ArrayList<String> list = new ArrayList<>(map.get(e.character));
					int i = list.indexOf(text.getText());
					String item = list.get((i+1) % list.size());
					text.setText(item);
					text.requestLayout();
					text.selectAll();
				}
			}
		});

		text.setMenu(menu);
		Constants.addFocusSelectAll(text);

		if(!Keyword.ELSE.isEqual(token))
			Constants.addArrowKeys(text, this);
	}

	@Override
	public Text getWidget() {
		return text;
	}

	public Menu getMenu() {
		return text.getMenu();
	}

	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	public void setVisible(boolean visible) {
		text.setVisible(visible);
	}

	@Override
	public String toString() {
		return getText();
	}

	boolean isKeyword(String keyword) {
		return getText().equals(keyword);
	}

	boolean isKeyword(Keyword keyword) {
		return getText().equals(keyword.toString());
	}

	public void setLayoutData(RowData data) {
		text.setLayoutData(data);
	}


	public void dispose() {
		text.dispose();
	}

	public boolean setFocus() {
		return text.setFocus();
	}

	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}

	public void moveAbove(Control c) {
		text.moveAbove(c);
	}

	public void moveBelow(Control c) {
		text.moveBelow(c);
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		buffer.append(text.getText());
	}

	@Override
	public Control getControl() {
		return text;
	}

}
