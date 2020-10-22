package pt.iscte.paddle.javardise;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

public class SimpleExpressionWidget extends EditorWidget implements TextWidget, Expression {

	private Text text;
	private Class<?> literalType;


	public SimpleExpressionWidget(Composite parent, String literal) {
		super(parent);
		init(parent, literal);
	}

	private void init(Composite parent, String string) {
		assert string != null;
		setLayout(Constants.ROW_LAYOUT_H_ZERO);

		text = Constants.createText(this, string);
		text.addVerifyListener(e -> e.doit = 
				validCharacter(e.character) || 
				e.character == '.' && text.getText().indexOf('.') == -1 && (text.getText().isEmpty() || Constants.isNumber(text.getText())) || 
				e.character == Constants.DEL_KEY);

		text.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				text.selectAll();
			}
			@Override
			public void focusLost(FocusEvent e) {
				updateContent();
			}
		});

		Constants.addArrowKeys(text, this);
		ILanguageConfiguration.INSTANCE.configure(this);
		addTransformationKeyListener2();
		text.addModifyListener(Constants.MODIFY_PACK);
		text.setMenu(new Menu(text));
	}

	@Override
	public Control getControl() {
		return text;
	}
	
	private static class Rule {
		final BiPredicate<TextWidget, Character> accept;
		final TriFunction<Composite, TextWidget, Character, Expression> creator;
		Rule(BiPredicate<TextWidget, Character> accept, TriFunction<Composite, TextWidget, Character, Expression> creator) {
			this.accept = accept;
			this.creator = creator;
		}
	}
	
	private List<Rule> rules = new ArrayList<Rule>();;
	
	public void addRule(BiPredicate<TextWidget, Character> accept, TriFunction<Composite, TextWidget, Character, Expression> creator) {
		rules.add(new Rule(accept, creator));	
	}
	
	public interface TriFunction<X,Y,Z,R> {
		R apply(X x, Y y, Z z);
	}
	
	private void addTransformationKeyListener2() {
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(text.isDisposed())
					return;
				Expression w = null;
				for(Rule rule : rules) {
					if(rule.accept.test(SimpleExpressionWidget.this, e.character)) {
						w = rule.creator.apply((EditorWidget) getParent(), SimpleExpressionWidget.this, e.character);
						break;
					}
				}
				
				if(w == null && e.character == SWT.CR)
					text.traverse(SWT.TRAVERSE_TAB_NEXT);
				
				if(w != null) {
					Expression p = (Expression) getParent();
					if(p.isSubstitutable())
						((SubstitutableExpression)p).substitute(SimpleExpressionWidget.this, w);
				}
			}
		});
	}

	@Override
	public void setData(Object data) {
		text.setData(data);
	}

	private boolean validCharacter(char c) {
		return ILanguageConfiguration.INSTANCE.isValidIdCharacter(c) || c >= '0' && c <= '9';
	}

	@Override
	public void setMenu(Menu menu) {
		text.setMenu(menu);
	}

	public void set(String expression) {
		text.setText(expression);
	}

	@Override
	public boolean setFocus() {
		return text.setFocus();
	}

	@Override
	public String toString() {
		return text.getText();
	}

	@Override
	public Text getWidget() {
		return text;
	}

	@Override
	public Expression copyTo(Composite parent) {
		return new SimpleExpressionWidget(parent, text.getText());
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		text.addKeyListener(listener);
	}
	
	public Class<?> getLiteralType() {
		return literalType;
	}

	@Override
	public String getTextToSerialize() {
		return text.getText().isBlank() ? Constants.EMPTY_EXPRESSION_SERIALIZE : text.getText();
	}

	private void updateContent() {
		Constants.setFont(text, false);
		text.setToolTipText("");
		literalType = null;
		try {
			Integer.parseInt(text.getText());
			literalType = Integer.class;
		}
		catch(NumberFormatException ex) {
			try {
				Double.parseDouble(text.getText());
				literalType = Double.class;
				text.setForeground(Constants.COLOR_LITERAL);
			}
			catch(NumberFormatException ex2) {
				if(text.getText().matches("'.'")) {
					literalType = Character.class;
					text.setForeground(Constants.COLOR_LITERAL);
				}
				else if(text.getText().matches("true|false")) {
					literalType = Boolean.class;
					text.setForeground(Constants.COLOR_KEYWORD);
				}
				else if(ILanguageConfiguration.INSTANCE.isValidId(text.getText()))
					literalType = null;
				else {
					text.setBackground(Constants.COLOR_ERROR);
					text.setToolTipText("invalid expression");
					text.requestLayout();
					literalType = null;
				}
			}
		}
		text.requestLayout();
	}
	
	@Override
	public void accept(Consumer<String> visitor) {
		visitor.accept(text.getText());
	}
}
