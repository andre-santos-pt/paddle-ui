package pt.iscte.paddle.yamleditor;

import java.io.File;
import java.util.List;

import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.ILanguageConfiguration;
import pt.iscte.paddle.javardise.InsertWidget;
import pt.iscte.paddle.javardise.InsertWidget.Action;
import pt.iscte.paddle.javardise.api.IEditorConfiguration;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.SimpleExpressionWidget;
import pt.iscte.paddle.javardise.TextWidget;
import pt.iscte.paddle.javardise.TokenWidget;

public class Configuration implements ILanguageConfiguration, IEditorConfiguration {

	@Override
	public String getExtension() {
		return "yml";
	}
	
	@Override
	public void configureRoot(File file, SequenceWidget sequence) {
		sequence.addAction(new Action("header") {
			
			@Override
			public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return c == '-' && id.getText().contentEquals("--");
			}
			
			@Override
			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				sequence.addElement(p -> new Document(p));
			}
		});
		
		sequence.addAction(new Action("comment") {
			
			@Override
			public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return c == '#';
			}
			
			@Override
			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				sequence.addElement(p -> new Comment(p));
			}
		});
	}

	@Override
	public void configure(SimpleExpressionWidget expression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TextWidget createInsertWidget(InsertWidget parent, boolean permanent) {
		return new Cursor(parent);
	}


}
