package pt.iscte.paddle.javardise;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants.GridDatas;

public class InsertWidget extends Composite implements TextWidget {

	private final TextWidget insert;
	private final boolean permanent;
	private final boolean type;
	private List<Action> actions;
	private final Predicate<String> tokenAccept;

	public InsertWidget(Composite parent, boolean permanent, boolean type, Predicate<String> tokenAccept) {
		super(parent, SWT.NONE);
		setLayout(Constants.ROW_LAYOUT_H);
		setBackground(Constants.COLOR_BACKGROUND);
		this.permanent = permanent;
		this.type = type;
		this.insert = createInsertWidget(type);
		this.tokenAccept = tokenAccept;
		this.actions = new ArrayList<>();
	}

	private TextWidget createInsertWidget(boolean type) {
		TextWidget insert = ILanguageConfiguration.INSTANCE.createInsertWidget(this, true);
		insert.getWidget().setBackground(Constants.COLOR_BACKGROUND);
		insert.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				String last = insert.getText();
				if(e.character == SWT.SPACE && tokenAccept.test(last)) {
					TokenWidget token = new TokenWidget(InsertWidget.this, last);
					token.moveAbove((Control) insert);
					token.addKeyListener(new KeyAdapter() {
						public void keyPressed(KeyEvent e) {
							if(e.keyCode == Constants.DEL_KEY) {
								token.dispose();
								insert.setFocus();
								requestLayout();
							}
						}
					});
					insert.clean();
					requestLayout();
					e.doit = false;
					return;
				}
				// TODO suggestion
				else if(e.character == SWT.SPACE && last.isBlank()) {
					for(Action a : actions) {
						String suggestion = a.getSuggestion(insert);
						if(suggestion != null) {
							System.out.println("help " + suggestion);
						}
					}
				}
				else if(!permanent && e.character == Constants.DEL_KEY && last.isBlank()) {
					Composite parent = getParent();
					dispose();
					parent.requestLayout();
					return;
				}

				// TODO FIX?
				int index = getParent() instanceof SequenceWidget ? ((SequenceWidget) getParent()).findModelIndex(InsertWidget.this) : 0; 
				List<String> tokens = getTokens();
				for(Action a : actions) {
					if(a.isEnabled(e.character, insert, index, insert.getCaretPosition(), insert.getSelectionCount(), tokens)) {
						a.run(e.character, insert, index, insert.getCaretPosition(), insert.getSelectionCount(), tokens);
						clearTokens();
						if(!permanent)
							dispose();
						e.doit = false;
						return;
					}
				}
			}
			
		});
		return insert;
	}

	public boolean hasTokens() {
		return getChildren()[0] != insert;
	}
	
	public InsertWidget copyTo(Composite parent) {
		InsertWidget w = new InsertWidget(parent, false, type, tokenAccept);
		w.actions = actions;
		return w;
	}
	
	void setHideMode() {
		insert.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.SHOW_GRID : GridDatas.SHOW_ROW);
				requestLayout();
			}
			public void focusLost(FocusEvent e) {
				if(insert.isEmpty() && !hasTokens()) {
					setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.HIDE_GRID : GridDatas.HIDE_ROW);
					requestLayout();
				}
				else
					;// TODO mark tokens
			}
		});
		setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.HIDE_GRID : GridDatas.HIDE_ROW);
		requestLayout();
	}
	
	public void clearTokens() {
		if(!isDisposed()) {
			Control[] children = getChildren();
			for(int i = 0; i < children.length-1; i++)
				children[i].dispose();
			insert.clean();
		}
	}

	public static abstract class Action {
		protected final CharSequence text;
		
		public Action(CharSequence text) {
			this.text = text;
		}
		public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
			return true;
		}
		public abstract void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens);
		
		public String getSuggestion(TextWidget id) {
			return null;
		}
	}



	void addAction(Action a) {
		assert a != null;
		actions.add(a);
	}

	public List<String> getTokens() {
		Control[] children = getChildren();
		List<String> tokens = new ArrayList<>(children.length-1);
		for(int i = 0; i < children.length-1; i++)
			if(children[i] instanceof Text)
				tokens.add(((Text) children[i]).getText());
			else
				tokens.add(((TextWidget) children[i]).getText()); // TODO Bug
		return tokens;
	}

	@Override
	public Text getWidget() {
		return insert.getWidget();
	}
	
	@Override
	public String getTextToSerialize() {
		return "";
	}
	
}
