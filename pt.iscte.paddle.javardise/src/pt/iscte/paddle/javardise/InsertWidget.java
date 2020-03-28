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
import pt.iscte.paddle.javardise.service.ICodeElement;

public class InsertWidget extends Composite implements TextWidget, ICodeElement {

	private final ComplexId complexId;
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
		this.complexId = createInsertWidget(type);
		this.tokenAccept = tokenAccept;
		this.actions = new ArrayList<>();
	}

	private ComplexId createInsertWidget(boolean type) {
		ComplexId complexId = new ComplexId(this, "", type);
		complexId.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				String last = complexId.getText();
				if(e.character == SWT.SPACE && tokenAccept.test(last)) {
					Token token = new Token(InsertWidget.this, last);
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
					if(a.isEnabled(e.character, complexId, index, complexId.getCaretPosition(), complexId.getSelectionCount(), tokens)) {
						a.run(e.character, complexId, index, complexId.getCaretPosition(), complexId.getSelectionCount(), tokens);
						clearTokens();
						if(!permanent)
							dispose();
						e.doit = false;
						return;
					}
				}
			}
			
		});
		complexId.setAllowEmpty(() -> true);
		
//		complexId.addFocusListener(new FocusAdapter() {
//			public void focusLost(FocusEvent e) {
//				System.out.println(Display.getDefault().getFocusControl());
//				complexId.clean();
//				clearTokens();
//			}
//		});
		return complexId;
	}

	public boolean hasTokens() {
		return getChildren()[0] != complexId;
	}
	
	public InsertWidget copyTo(Composite parent) {
		InsertWidget w = new InsertWidget(parent, false, type, tokenAccept);
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
				if(complexId.isEmpty() && !hasTokens()) {
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
		
		public Action(CharSequence text) {
			this.text = text;
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
		for(int i = 0; i < children.length-1; i++)
			tokens.add(((Text) children[i]).getText()); // TODO Bug
		return tokens;
	}

	@Override
	public Text getWidget() {
		return complexId.getWidget();
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		if(complexId.isComment())
			buffer.append(complexId.getText());
	}

	@Override
	public Control getControl() {
		return complexId;
	}
}
