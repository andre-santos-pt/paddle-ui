package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
		while(!(c.getParent() instanceof SequenceWidget))
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
	
	default boolean isAtBeginning() {
		return getWidget().getCaretPosition() == 0 && getWidget().getSelectionCount() == 0;
	}
	
	default boolean isAtEnd() {
		return getWidget().getCaretPosition() == getWidget().getText().length();
	}
	
	default void setAtLeft() {
		getWidget().setSelection(0, 0);
	}
	
	default void setAtRight() {
		getWidget().setSelection(getWidget().getText().length());
	}
	
	static TextWidget create(Text text) {
		return new TextWidget() {
			public Text getWidget() {
				return text;
			}
		};
	}
	
}

