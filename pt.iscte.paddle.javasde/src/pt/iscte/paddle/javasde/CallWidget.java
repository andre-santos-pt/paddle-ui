package pt.iscte.paddle.javasde;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

// TODO incomplete
public class CallWidget extends EditorWidget {
	private EditorWidget id;
	private InsertWidget insert;
	private EditorWidget args;
	
	public CallWidget(Composite parent, String id, boolean statement) {
		super(parent);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		
		this.id = new Id(this, id, false);
		new FixedToken(this, "(");
		args = new EditorWidget(this);
		insert = new InsertWidget(args, true);
		
		new FixedToken(this, ")");
		if(statement) {
			new Token(this, ";");
			this.id.addKeyListener(new Constants.DeleteListener(this));
		}
		
		insert.addAction(new InsertWidget.Action("argument", (char) 0) {
			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				return !Keyword.is(text) && (c == SWT.SPACE || c == ',');
			}
			
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				insert.dispose();
				ExpressionWidget arg = addArgument(text);
				if(c == ',') {
					new FixedToken(args, ",");
					ExpressionWidget arg2 = addArgument("expression");
					arg2.setFocus();
				}
				else
					arg.setFocus();
			}

			private ExpressionWidget addArgument(String text) {
				ExpressionWidget exp = new ExpressionWidget(args, text);
				exp.requestLayout();
				exp.setFocus();
				exp.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if(e.character == ',') {
							new FixedToken(args, ",");
							addArgument("expression");
						}
					}
				});
				return exp;
			}
		});
	}
	
	@Override
	public void toCode(StringBuffer buffer) {
		buffer.append(id).append("()"); // TODO call arguments
	}
	
	@Override
	public String toString() {
		return id + "(...)";
	}
	
	public void focusArgument() {
		insert.setFocus();
	}
}
