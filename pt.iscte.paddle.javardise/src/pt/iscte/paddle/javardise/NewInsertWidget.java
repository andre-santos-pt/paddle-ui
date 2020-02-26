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

public class NewInsertWidget extends Composite implements TextWidget {

	private final ComplexId complexId;
	private final boolean permanent;
	private List<Action> actions;
	private final Predicate<String> tokenAccept;

	public NewInsertWidget(Composite parent, boolean permanent, Predicate<String> tokenAccept) {
		super(parent, SWT.NONE);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		setBackground(Constants.COLOR_BACKGROUND);
		this.permanent = permanent;
		this.complexId = createInsertWidget();
		this.tokenAccept = tokenAccept;
		this.actions = new ArrayList<>();
	}

	private ComplexId createInsertWidget() {
		ComplexId complexId = new ComplexId(this, "", false);
		complexId.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				String last = complexId.getText();
				if(e.character == SWT.SPACE && tokenAccept.test(last)) {
					Token token = new Token(NewInsertWidget.this, last);
					token.moveAbove(complexId);
					token.addKeyListener(new KeyAdapter() {
						public void keyPressed(KeyEvent e) {
							if(e.keyCode == Constants.DEL_KEY) {
								token.dispose();
								complexId.setFocus();
								requestLayout();
							}
						}
					});
					complexId.clean();
					requestLayout();
					e.doit = false;
					return;
				}

				// TODO FIX?
				int index = getParent() instanceof SequenceWidget ? ((SequenceWidget) getParent()).findModelIndex(NewInsertWidget.this) : 0; 
				List<String> tokens = getTokens();
				for(Action a : actions) {
					if(a.isEnabled(e.character, complexId, index, complexId.text.getCaretPosition(), complexId.text.getSelectionCount(), tokens)) {
						a.run(e.character, complexId, index, complexId.text.getCaretPosition(), complexId.text.getSelectionCount(), tokens);
						clearTokens();
						if(!permanent)
							dispose();
						e.doit = false;
						return;
					}
				}
			}
			
		});
		
//		complexId.addFocusListener(new FocusAdapter() {
//			public void focusLost(FocusEvent e) {
//				System.out.println(Display.getDefault().getFocusControl());
//				complexId.clean();
//				clearTokens();
//			}
//		});
		return complexId;
	}

	public NewInsertWidget copyTo(Composite parent) {
		NewInsertWidget w = new NewInsertWidget(parent, false, tokenAccept);
		w.actions = actions;
		return w;
	}
	
	void setHideMode() {
		complexId.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.SHOW_GRID : GridDatas.SHOW_ROW);
				requestLayout();
			}
			public void focusLost(FocusEvent e) {
				if(complexId.isEmpty()) {
					setLayoutData(getParent().getLayout() instanceof GridLayout ? GridDatas.HIDE_GRID : GridDatas.HIDE_ROW);
					requestLayout();
					complexId.text.setBackground(Constants.COLOR_BACKGROUND);
				}
				else {
					complexId.text.setBackground(Constants.COLOR_HIGHLIGHT);
				}
					
			}
		});

	}
	private void clearTokens() {
		if(!isDisposed()) {
			Control[] children = getChildren();
			for(int i = 0; i < children.length-1; i++)
				children[i].dispose();
			complexId.clean();
		}
	}

	public static abstract class Action {
		final CharSequence text;
		final char accelerator;
		public Action(CharSequence text, char accelerator) {
			this.text = text;
			this.accelerator = accelerator;
		}
		public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
			return true;
		}
		public abstract void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens);
	}



	void addAction(Action a) {
		assert a != null;
		actions.add(a);
	}

	public List<String> getTokens() {
		Control[] children = getChildren();
		List<String> tokens = new ArrayList<>(children.length-1);
//		for(int i = 0; i < children.length-1; i++)
//			tokens.add(((Text) children[i]).getText()); // TODO Bug
		return tokens;
	}

	@Override
	public Text getWidget() {
		return complexId.text;
	}
}
