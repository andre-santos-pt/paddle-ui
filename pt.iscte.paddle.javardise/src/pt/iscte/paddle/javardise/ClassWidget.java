package pt.iscte.paddle.javardise;

import static java.lang.System.lineSeparator;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pt.iscte.paddle.javardise.UiMode.Syntax;
import pt.iscte.paddle.javardise.service.IClassWidget;
import pt.iscte.paddle.javardise.service.IMethodWidget;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.model.IConstantDeclaration;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IRecordType;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;

public class ClassWidget extends ModiferWidget implements SequenceContainer, IClassWidget {

	private IModule module;
	private Id id;
	private SequenceWidget methods;
	private Composite header;

	@Override
	Composite getHeader() {
		return header;
	}

	@Override
	Function<List<Keyword>, List<Keyword>> getModifierProvider() {
		return list -> Keyword.classModifiers();
	}
	
	public ClassWidget(Composite parent, IModule module, Keyword ... modifiers) {
		super(parent, module);
		this.module = module;
		GridLayout layout = new GridLayout(1, true);
		layout.verticalSpacing = 10;
		setLayout(layout);
		if (UiMode.editorMode() == UiMode.Editor.REGULAR) {
			header = Constants.createHeader(this);
			for(Keyword mod : modifiers)
				addModifier(mod);

			Token classToken = new Token(header, Keyword.CLASS);
			addModifierKey(classToken);
			id = new Id(header, module.getId());
			id.setReadOnly();
			new FixedToken(header, "{");
		}

		int margin = UiMode.editorMode() == UiMode.Editor.STATIC ? 0 : Constants.TAB;

		Predicate<String> tokenAccept = token -> acceptModifier(token) || Constants.isType(token) || IType.VOID.getId().equals(token);
		methods = new SequenceWidget(this, margin, 5, tokenAccept);
		methods.addAction(new InsertWidget.Action("field") {
			public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return (c == ';' || c == '=') && tokens.size() > 0 && Constants.isType(last);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				IType t = IType.match(tokens.get(tokens.size()-1));
				boolean assign = c == '=';
				String last = tokens.get(tokens.size()-1);
				IRecordType mainType = getMainType();
				mainType.addField(IType.match(last), id.getText(), tokens.subList(0, tokens.size()-1));
				//				if(assign)
				//					w.focusExpression();
			}
		});

		methods.addAction(new InsertWidget.Action("constructor") {
			public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return c == '(' && id.getText().equals(module.getId());
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
//				int i = module.getConstants().size() + getMainType().getFields().size();
				module.addProcedure(id.getId(), IType.VOID, Flag.CONSTRUCTOR.name());
			}
		});

		methods.addAction(new InsertWidget.Action("method") {
			public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return c == '(' && !id.isKeyword() && text.length() > 0 && tokens.size() > 0 && (Constants.isType(last) || IType.VOID.getId().equals(last));
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				String last = tokens.get(tokens.size()-1);
				IType t = last.equals(IType.VOID.getId()) ? IType.VOID : IType.match(last);
				module.addProcedure(id.getId(), t, tokens.toArray(new String[tokens.size()]));
			}
		});



		IRecordType mainType = module.getProperty(IRecordType.class);
		if(mainType != null) {
			for(IVariableDeclaration f : mainType)
				methods.addElement(p -> new FieldWidget(p, f));
			IRecordType t = mainType;
			mainType.addListener(new IRecordType.IListener() {
				public void fieldAdded(IVariableDeclaration field) {
					FieldWidget f = methods.addElement(p -> new FieldWidget(p, field), t.getFields().size()-1);
					f.setFocus();
				}
				public void fieldRemoved(IVariableDeclaration field) {
					methods.removeElement(field);
				}
			});
		}

		module.getProcedures().stream()
		.filter(p -> Flag.CONSTRUCTOR.is(p))
		.forEach(c -> methods.addLineAndElement(comp -> new MethodWidget(comp, c)));
		
		module.getProcedures().stream()
		.filter(p -> Flag.CONSTRUCTOR.isNot(p))
		.forEach(c -> methods.addLineAndElement(comp -> new MethodWidget(comp, c)));
		

		if (UiMode.editorMode() == UiMode.Editor.REGULAR)
			new FixedToken(this, "}");

		module.addListener(new IModule.IListener() {
			public void constantAdded(IConstantDeclaration constant) {
				int i = module.getConstants().indexOf(constant);
				methods.addElement(p -> new FieldWidget(p, constant), i);
			}

			public void constantRemoved(IConstantDeclaration constant) {
				methods.removeElement(constant);
			}

			public void procedureAdded(IProcedure procedure) {
				int i = module.getConstants().size() + getMainType().getFields().size();
				if(Flag.CONSTRUCTOR.is(procedure))
					i += index(module.getProcedures(), procedure);
				else {
					i += (int) module.getProcedures().stream().filter(p -> Flag.CONSTRUCTOR.is(p)).count();
					i += index(module.getProcedures(), procedure);
				}
				MethodWidget m = methods.addElement(p -> new MethodWidget(p, procedure), i);
				m.focusParameters();
			}

			int index(List<IProcedure> list, IProcedure proc) {
				int i = 0;
				for(IProcedure p : list) {
					if(p == proc)
						return i;
					
					if(Flag.CONSTRUCTOR.is(proc, p) || Flag.CONSTRUCTOR.isNot(proc, p))
						i++;
				}
				return i;
			}
			
			public void procedureRemoved(IProcedure procedure) {
				methods.removeElement(procedure);
			}

			//			@Override
			//			public void recordTypeAdded(IRecordType type) {
			//				if(type != module.getProperty(IRecordType.class))
			//					;//fields.addElement(p -> new Cl)
			//			}
		});

		addUndoFilter();
		//		Display.getDefault().addFilter(SWT.FocusIn, new Listener() {
		//			
		//			@Override
		//			public void handleEvent(Event event) {
		//				System.out.println(getOwner((Control) event.widget));
		//			}
		//			
		//			private EditorWidget getOwner(Control c) {
		//				Composite p = c.getParent();
		//				while(!(p instanceof EditorWidget))
		//					p = p.getParent();
		//				
		//				return (EditorWidget) p;
		//			}
		//		});

	}

	private IRecordType getMainType() {
		IRecordType mainType = module.getProperty(IRecordType.class);
		if(mainType == null) {
			mainType = module.addRecordType();
			mainType.setId(module.getId());
			module.setProperty(IRecordType.class, mainType);
			IRecordType t = mainType;
			mainType.addListener(new IRecordType.IListener() {
				public void fieldAdded(IVariableDeclaration field) {
					methods.addElement(p -> new FieldWidget(p, field), t.getFields().size());
				}
			});
		}
		return mainType;
	}

	private boolean acceptModifier(String token) {
		return Keyword.isMethodModifier(token) && 
				(UiMode.hasSyntax(Syntax.ENCAPSULATION) || !Keyword.isAccessModifier(token));
	}

	public boolean setFocus() {
		return id == null ? false : id.setFocus();
	}

	public SequenceWidget getBody() {
		return methods;
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
		super.toCode(buffer);
		Keyword.CLASS.toCode(buffer);
		buffer.append(' ').append(id.toString()).append(" {").append(lineSeparator());
		methods.toCode(buffer, 1);
		//		methods.toCode(buffer, 1);
		buffer.append("}").append(lineSeparator()).append(lineSeparator());
	}

	@Override
	public IWidget getClassName() {
		return id;
	}

	@Override
	public IMethodWidget getProcedure(IProcedure procedure) {
		for(Control c : methods.getChildren())
			if(c instanceof MethodWidget && ((MethodWidget) c).getProcedure() == procedure)
				return (IMethodWidget) c;

		return null;
	}
}