package pt.iscte.paddle.yamleditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.TextWidget;
import pt.iscte.paddle.javardise.TokenWidget;

public class Item extends EditorWidget implements TextWidget {

	private Text text;

	protected Item(Composite parent) {
		super(parent);
		new TokenWidget(parent, "- ");
		text = Constants.createText(parent, "");
	}

	@Override
	public Text getWidget() {
		return text;
	}

}
