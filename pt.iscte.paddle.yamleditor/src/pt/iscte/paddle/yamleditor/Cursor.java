package pt.iscte.paddle.yamleditor;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.TextWidget;

public class Cursor extends EditorWidget implements TextWidget {

	private Text text;

	protected Cursor(Composite parent) {
		super(parent);
		setLayout(new RowLayout());
		text = Constants.createText(parent, "??");
		Constants.addArrowKeys(text, this);
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}
	
	@Override
	public Text getWidget() {
		return text;
	}

}
