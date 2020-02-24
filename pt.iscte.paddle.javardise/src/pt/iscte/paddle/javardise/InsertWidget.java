package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;

import pt.iscte.paddle.javardise.Constants.GridDatas;

public class InsertWidget extends Composite implements TextWidget {

	final Text text;
	private boolean edit;
	private final boolean permanent;
	private List<Action> actions;

	private final Predicate<String> tokenAccept;

	public InsertWidget(Composite parent, boolean permanent) {
		this(parent, permanent, token -> false);
	}

	public InsertWidget(Composite parent, boolean permanent, Predicate<String> tokenAccept) {
		super(parent, SWT.NONE);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		setBackground(Constants.COLOR_BACKGROUND);
		this.permanent = permanent;
		this.text = createInsertWidget();
		this.actions = new ArrayList<>();
		this.tokenAccept = tokenAccept;
	}

	public InsertWidget copyTo(Composite parent) {
		InsertWidget w = new InsertWidget(parent, false, tokenAccept);
		w.actions = actions;
		return w;
	}

	private Text createInsertWidget() {
		Text text = new Text(this, SWT.SINGLE);
		text.setText(" ");
		text.setForeground(Constants.FONT_COLOR);
		text.setEditable(true);
		text.setFont(Constants.FONT);
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				e.doit = 
						edit || Constants.isLetter(e.character) ||
						//Constants.isNumber(e.character) ||
						//e.character == '.' || //e.character == '=' || 
						e.character == Constants.DEL_KEY || e.character == SWT.CR ||
						e.character == '/' && (text.getText().isBlank() || text.getText().startsWith("/")) || 
						((e.character == ' ' || Constants.isNumber(e.character))  && text.getText().startsWith("//"));
			}
		});
		text.addModifyListener(Constants.MODIFY_PACK);
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				text.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
				boolean comment = text.getText().startsWith("//");
				if(!comment)
					clearTokens();
			}
		});
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				String last = text.getText();
				if(e.character == SWT.SPACE && tokenAccept.test(last)) {
					Token token = new Token(InsertWidget.this, last);
					token.moveAbove(text);
					requestLayout();
					edit = true;
					text.setText(" ");
					edit = false;
					e.doit = false;
					return;
				}

				if(e.character == Constants.DEL_KEY && text.getCaretPosition() == 0 && text.getSelectionCount() == 0) {
					if(permanent) {
						text.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
					}
					else {
						dispose();
					}
					e.doit = false;
					return;
				}
				else if(e.keyCode == SWT.SPACE) {
//					Shell shell = new Shell(getDisplay());
//					tip = new ToolTip(shell, SWT.BALLOON);
//					tip.setMessage("Help?");
//					Point loc = text.toDisplay(text.getLocation());
//					tip.setLocation(loc);  
//					tip.setVisible(true);
				} 
				else {
//					if(tip != null)
//						tip.setVisible(false);
				}
				
				// TODO FIX?
				int index = getParent() instanceof SequenceWidget ? ((SequenceWidget) getParent()).findModelIndex(InsertWidget.this) : 0; 
				List<String> tokens = getTokens();
				for(Action a : actions) {
					if(a.isEnabled(e.character, last, index, text.getCaretPosition(), text.getSelectionCount(), tokens)) {
						a.run(e.character, last, index, text.getCaretPosition(), text.getSelectionCount(), tokens);
						clearTokens();
						if(!permanent)
							dispose();
						e.doit = false;
						return;
					}
				}
				Menu menu = text.getMenu();
				if(menu != null) {
					if (e.keyCode == Constants.MENU_KEY && !text.getText().startsWith("//"))
						;//showMenu(text, menu);

					//					else if (e.keyCode == Constants.DEL_KEY && text.getText().isEmpty())
					//						deleteAction(menu);

					else if(e.keyCode == SWT.CR && text.getText().startsWith("//")) {
						InsertWidget w = new InsertWidget(InsertWidget.this.getParent(), false);
						w.setFocus();
						w.text.requestLayout();
					} 
				}
			}
		});
		Constants.addArrowKeys(text, TextWidget.create(text));
		return text;
	}

	void setHideMode() {
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.SHOW_GRID : GridDatas.SHOW_ROW);
				requestLayout();
			}
			@Override
			public void focusLost(FocusEvent e) {
				setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.HIDE_GRID : GridDatas.HIDE_ROW);
				requestLayout();
			}
		});

	}
	private void clearTokens() {
		if(!isDisposed()) {
			Control[] children = getChildren();
			for(int i = 0; i < children.length-1; i++)
				children[i].dispose();
			edit = true;
			text.setText(" ");
			edit = false;
		}
	}

	public static abstract class Action {
		final CharSequence text;
		final char accelerator;
		public Action(CharSequence text, char accelerator) {
			this.text = text;
			this.accelerator = accelerator;
		}
		public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
			return true;
		}
		public abstract void run(char c, String text, int index, int caret, int selection, List<String> tokens);
	}



	void addAction(Action a) {
		assert a != null;
		actions.add(a);
	}

	public List<String> getTokens() {
		Control[] children = getChildren();
		List<String> tokens = new ArrayList<>(children.length-1);
		for(int i = 0; i < children.length-1; i++)
			tokens.add(((Text) children[i]).getText());
		return tokens;
	}

	//	private void showMenu(Text label, Menu menu) {
	//		int c = 0;
	//		MenuItem item = null;
	//		for (MenuItem menuItem : menu.getItems()) {
	//			if (menuItem.isEnabled() && menuItem.getStyle() != SWT.SEPARATOR
	//					&& menuItem.getAccelerator() != Constants.DEL_KEY
	//					&& (menuItem.getData() instanceof SelectionListener
	//							|| menuItem.getData() instanceof SequenceWidget.MenuCommand)) {
	//				item = menuItem;
	//				c++;
	//			}
	//		}
	//		if (c == 1) {
	//			((SelectionListener) item.getData()).widgetSelected(null);
	//		} 
	//		else {
	//			menu.setLocation(label.toDisplay(0, 20));
	//			menu.setVisible(true);
	//		}
	//	}

	//	private void deleteAction(Menu menu) {
	//		for (MenuItem menuItem : menu.getItems()) {
	//			if (menuItem.isEnabled() && menuItem.getAccelerator() == Constants.DEL_KEY) {
	//				((SelectionListener) menuItem.getData()).widgetSelected(null);
	//				break;
	//			}
	//		}
	//	}


	//	public boolean setFocus() {
	//		return text.setFocus();
	//	}

	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	public Menu createMenu() {
		Menu menu = new Menu(text);
		text.setMenu(menu);
		return menu;
	}

	@Override
	public Text getWidget() {
		return text;
	}
}
