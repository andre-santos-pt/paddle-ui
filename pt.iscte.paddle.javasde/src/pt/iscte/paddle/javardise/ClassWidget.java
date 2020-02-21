package pt.iscte.paddle.javardise;

import static java.lang.System.lineSeparator;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pt.iscte.paddle.model.IConstant;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IType;

public class ClassWidget extends EditorWidget implements SequenceContainer {

	private IModule module;
	private Id id;
	private SequenceWidget body;
	private Keyword[] modifiers;
	
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
			this.modifiers = modifiers;
			
			new Token(header, Keyword.CLASS);
			id = new Id(header, module.getId(), false);
			id.setReadOnly();
			new FixedToken(header, "{");
		}

		int margin = UiMode.isStatic() ? 0 : Constants.TAB;
		body = new SequenceWidget(this, margin, Constants.METHOD_SPACING, token -> Keyword.isMethodModifier(token) || Constants.isType(token) || IType.VOID.getId().equals(token));

		body.setDeleteAction(index -> {
			IProcedure p = module.getProcedures().get(index);
			module.removeProcedure(p);
		});
		
		body.addAction(new NewInsertWidget.Action("method", 'm') {

			public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return c == '(' && !id.isKeyword() && text.length() > 0 && tokens.size() > 0 && (Constants.isType(last) || IType.VOID.getId().equals(last));
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				String last = tokens.get(tokens.size()-1);
				IType t = last.equals(IType.VOID.getId()) ? IType.VOID : IType.match(last);
				IProcedure proc = module.addProcedure(id.getId(), t, tokens.toArray(new String[tokens.size()]));
				proc.setId(id.getId());
			}
		});

		body.addAction(new NewInsertWidget.Action("field", 'f') {

			public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return (c == ';' || c == '=') && tokens.size() > 0 && Constants.isType(last);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(tokens.get(tokens.size()-1));
				FieldWidget w = body.addWidget(p -> new FieldWidget(p, t, id.getId(), Keyword.array(tokens.subList(0, tokens.size()-1)), c == '='));

				if(c == '=')
					w.focusExpression();
			}
		});

		module.getProcedures().forEach(p -> body.addWidget(c -> new MethodWidget(body, p)));
		
//		module.getConstants().forEach(c -> body.addElement(new FieldWidget(body, c), module.getConstants().size()-1));
//		module.getProcedures().forEach(p -> body.addElement(new MethodWidget(body, p), module.getProcedures().size()-1));

		if (!UiMode.isStatic())
			new FixedToken(this, "}");

		module.addListener(new IModule.IListener() {
			public void constantAdded(IConstant constant) {
				int i = module.getConstants().indexOf(constant);
				body.addElement(new FieldWidget(body, constant), i);
			}

			public void constantRemoved(IConstant constant) {
				body.delete(e -> e instanceof FieldWidget && ((FieldWidget) e).constant == constant);
			}

			public void procedureAdded(IProcedure procedure) {
				MethodWidget m = new MethodWidget(body, procedure);
				int i = module.getProcedures().indexOf(procedure);
				body.addElement(m, module.getConstants().size() + i);
				m.focusParameters();
			}

			public void procedureRemoved(IProcedure procedure) {
				body.delete(e -> e instanceof MethodWidget && ((MethodWidget) e).procedure == procedure);
			}
		});

		addUndoFilter();
	}

	public boolean setFocus() {
		return body.setFocus();
	}

	public SequenceWidget getBody() {
		return body;
	}

	private void addUndoFilter() {
		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL && event.keyCode == 'z') {
					System.out.println("UNDO");
					module.undo();
					event.doit = false;
				}
			}
		});
	}

	public void toCode(StringBuffer buffer) {
		for(Keyword k : modifiers)
			buffer.append(k.toString()).append(' ');
		
		Keyword.CLASS.toCode(buffer);
		buffer.append(' ').append(id.toString()).append(" {").append(lineSeparator());
		body.toCode(buffer, 1);
		buffer.append("}").append(lineSeparator()).append(lineSeparator());
	}

}
