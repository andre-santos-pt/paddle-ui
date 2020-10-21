package pt.iscte.paddle.javardise;


import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public interface TextWidget {

	Text getWidget();


	default Composite getParent() {
		return getWidget().getParent();
	}

	default String getText() {
		return getWidget().getText();
	}

	default Control getStatement() {
		Control c = getWidget();
		while(!(c == null || c.getParent() instanceof SequenceWidget))
			c = c.getParent();

		return c;
	}

	default SequenceContainer getSequenceContainer() {
		Control c = getWidget().getParent();
		while(!(c instanceof SequenceContainer)) {
			c = c.getParent();
		}

		return (SequenceContainer) c;
	}

	default boolean isEmpty() {
		return getWidget().getText().isBlank();
	}

	default int  getCaretPosition() {
		return getWidget().getCaretPosition();
	}
	
	default int getSelectionCount() {
		return getWidget().getSelectionCount();
	}
	
	default boolean isAtBeginning() {
		return getWidget().getCaretPosition() == 0 && getWidget().getSelectionCount() == 0;
	}

	default boolean isAtEnd() {
		return getWidget().getCaretPosition() == getWidget().getText().length();
	}

	default boolean isSelected() {
		return getWidget().getSelectionCount() == getWidget().getText().length();
	}

	default boolean isModifiable() {
		return getWidget().getEditable();
	}

	default void setAtLeft() {
		getWidget().setSelection(0, 0);
	}

	default void setAtRight() {
		getWidget().setSelection(getWidget().getText().length());
	}

	default void setToolTip(String text) {
		getWidget().setToolTipText(text);
	}

	default void clean() {
		getWidget().setText("");
	}
	
	default boolean setFocus() {
		return getWidget().setFocus();
	}

	
	static TextWidget create(Text text) {
		return new TextWidget() {
			public Text getWidget() {
				return text;
			}
		};
	}


	default void addKeyListener(KeyListener	listener) {
		getWidget().addKeyListener(listener);
	}

	default void addFocusListener(FocusListener listener) {
		getWidget().addFocusListener(listener);
	}
	
	default KeyListener addDeleteListener(Runnable action) {
		KeyListener l = new KeyAdapter() { 
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == Constants.DEL_KEY) {
					if(e.widget instanceof Label)
						action.run();
					else if(e.widget instanceof Text) {
						Text t = (Text) e.widget;
						if(t.getText().isEmpty() && t.getCaretPosition() == 0 || !t.getEditable())
							action.run();
					}
				}
			}
		};
		getWidget().addKeyListener(l);
		return l;
	}


	default boolean isKeyword() {
		return ILanguageConfiguration.INSTANCE.isKeyword(getWidget().getText());
	}


	

}

