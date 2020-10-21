package pt.iscte.paddle.yamleditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.TextWidget;

public class Comment extends EditorWidget implements TextWidget {

	private Text text;

	protected Comment(Composite parent) {
		super(parent);
		new FixedToken(parent, "# ");
		text = Constants.createText(parent, "");
	}

	@Override
	public Text getWidget() {
		return text;
	}

}
