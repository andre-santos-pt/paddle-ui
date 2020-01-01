package pt.iscte.paddle.javasde;

import static java.lang.System.lineSeparator;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pt.iscte.paddle.model.IConstant;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IType;

public class ClassWidget extends EditorWidget {

	private IModule module;
	private Id id;
	private SequenceWidget body;

	public ClassWidget(Composite parent, IModule module, UiMode mode, Keyword ... modifiers) {
		super(parent);
		this.module = module;
		GridLayout layout = new GridLayout(1, true);
		layout.verticalSpacing = 10;
		setLayout(layout);
		
		if (!UiMode.isStatic()) {
			EditorWidget header = new EditorWidget(this);
			header.setLayout(Constants.ROW_LAYOUT_H);
			for(Keyword mod : modifiers)
				new Token(header, mod);
				
			new Token(header, Keyword.CLASS);
			id = new Id(header, module.getId(), false);
			new FixedToken(header, "{");
		}

		int margin = UiMode.isStatic() ? 0 : Constants.TAB;
		body = new SequenceWidget(this, margin, token -> Keyword.isMethodModifier(token) || Constants.isType(token));
//		body.addChildCommand("constant", 'c', (i, p) -> module.addConstant(IType.INT, ILiteral.matchValue("1")));
//		body.addChildCommand("procedure", 'p', (i, p) -> module.addProcedure(IType.VOID));

//		body.acceptKeywords(Keyword.STATIC, Keyword.FINAL, Keyword.ABSTRACT, Keyword.PUBLIC, Keyword.PRIVATE, Keyword.PROTECTED);
		
		body.addAction(new InsertWidget.Action("method", 'm') {

			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return c == '(' && tokens.size() > 0 && (Constants.isType(last) || IType.VOID.getId().equals(last));
			}
			
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(tokens.get(tokens.size()-1));
				IProcedure proc = module.addProcedure(text, t, tokens.toArray(new String[tokens.size()]));
				proc.setId(text);
			}
		});
		
		body.addAction(new InsertWidget.Action("field", 'f') {

			public boolean isEnabled(char c, String text, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return (c == ';' || c == '=') && tokens.size() > 0 && Constants.isType(last);
			}
			
			public void run(char c, String text, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(tokens.get(tokens.size()-1));
				FieldWidget w = body.addWidget(p -> new FieldWidget(p, t, text, Keyword.array(tokens.subList(0, tokens.size()-1)), c == '='));
					
				if(c == '=')
					w.focusExpression();
//				IConstant constant = module.addConstant(text, t, ILiteral.matchValue("1"));
			}
		});
		
		module.getConstants().forEach(c -> body.addElement(new FieldWidget(body, c)));
		module.getProcedures().forEach(p -> body.addElement(new MethodWidget(body, p)));

		if (!UiMode.isStatic())
			new FixedToken(this, "}");

		module.addListener(new IModule.IListener() {
			public void constantAdded(IConstant constant) {
				body.addElement(new FieldWidget(body, constant));
			}

			public void constantDeleted(IConstant constant) {
				body.delete(e -> e instanceof FieldWidget && ((FieldWidget) e).constant == constant);
			}

			public void procedureAdded(IProcedure procedure) {
				MethodWidget m = new MethodWidget(body, procedure);
				body.addElement(m);
				m.setFocus();
			}

			public void procedureDeleted(IProcedure procedure) {
				body.delete(e -> e instanceof MethodWidget && ((MethodWidget) e).procedure == procedure);
			}
		});

		addUndoFilter();
	}

	public boolean setFocus() {
		return id.setFocus();
	}
	
	private void addUndoFilter() {
		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL && event.keyCode == 'z') {
					System.out.println("UNDO");
					module.undo();
				}
			}
		});
	}

	public void toCode(StringBuffer buffer) {
		buffer.append("public class ").append(id.toString()).append(" {").append(lineSeparator());
		for (Control c : getChildren())
			if (c instanceof MethodWidget)
				((MethodWidget) c).toCode(buffer);

		buffer.append("}").append(lineSeparator()).append(lineSeparator());
	}


}
