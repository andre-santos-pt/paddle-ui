package pt.iscte.paddle.javardise.service;

import static java.lang.System.lineSeparator;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.Constants;

public interface ICodeElement {

	default void toCode(StringBuffer buffer) {
		buffer.append("#TODO" + this.getClass().getSimpleName() + "#");
		System.err.println("missing toCode " + this.getClass());
	}

	private static void appendTabs(StringBuffer buffer, int n) {
		while(n-- > 0)
			buffer.append("\t");
	}

	default void toCode(StringBuffer buffer, int level) {
		appendTabs(buffer, level);
		toCode(buffer);
		buffer.append(lineSeparator());
	}
	
	static void toCode(Text text, StringBuffer buffer) {
		if(text.getText().isBlank())
			buffer.append(Constants.EMPTY_EXPRESSION_SERIALIZE);
		else
			buffer.append(text.getText());
	}
	
	static void append(StringBuffer buffer, int level, String content) {
		appendTabs(buffer, level);
		buffer.append(content);
	}
	
	default String getCode() {
		StringBuffer b = new StringBuffer();
		toCode(b);
		return b.toString();
	}
	
	default boolean isSerializationEmpty(String id) {
		return Constants.EMPTY_EXPRESSION_SERIALIZE.equals(id);
	}
	
	Control getControl();
}
