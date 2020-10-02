package pt.iscte.paddle.javardise;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class FixedToken {
	private final Label label;
	
	public FixedToken(Composite parent, String token) {
		label = new Label(parent, SWT.NONE);
		label.setText(token);
		Constants.setFont(label, true);
		if(token.equals("."))
			label.setFont(Constants.FONT_DOT);
	}

	public String toString() {
		return label.getText();
	}
	
	public void addKeyListener(KeyListener keyListener) {
		label.addKeyListener(keyListener);
	}

	public void dispose() {
		label.dispose();
	}

	public void setFocus() {
		label.setFocus();
	}
	
	public Control getControl() {
		return label;
	}
}
