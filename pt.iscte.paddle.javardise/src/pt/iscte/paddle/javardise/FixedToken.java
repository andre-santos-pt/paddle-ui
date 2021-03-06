package pt.iscte.paddle.javardise;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class FixedToken {
	private final Label label;
	
	public FixedToken(Composite parent, Keyword keyword) {
		this(parent, keyword.toString());
	}
	
	public FixedToken(Composite parent, String token) {
		label = new Label(parent, SWT.NONE);
		label.setText(token);
		Constants.setFont(label, true);
		if(token.equals("."))
			label.setFont(Constants.FONT_DOT);
		
		if(token.equals(";") && UiMode.hideSemiColons())
			label.setVisible(false);
	}

	public String toString() {
		return label.getText();
	}
	
	public void setLayoutData(RowData data) {
		label.setLayoutData(data);
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
