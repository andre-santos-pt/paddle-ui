package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Text;

public interface CodeElement {

	default void toCode(StringBuffer buffer) {
		buffer.append("#TODO" + this.getClass().getSimpleName() + "#");
		System.err.println("missing toCode " + this.getClass());
	}

	static void toCode(Text text, StringBuffer buffer) {
		if(text.getText().isBlank())
			buffer.append(Constants.EMPTY_EXPRESSION_SERIALIZE);
		else
			buffer.append(text.getText());
	}
}
