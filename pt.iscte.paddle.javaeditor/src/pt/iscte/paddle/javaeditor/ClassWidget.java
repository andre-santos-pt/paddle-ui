package pt.iscte.paddle.javaeditor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pt.iscte.paddle.javaeditor.UiMode.Syntax;
import pt.iscte.paddle.javaeditor.api.IClassWidget;
import pt.iscte.paddle.javaeditor.api.IMethodWidget;
import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.FixedToken;
import pt.iscte.paddle.javardise.Id;
import pt.iscte.paddle.javardise.InsertWidget;
import pt.iscte.paddle.javardise.ModiferWidget;
import pt.iscte.paddle.javardise.SequenceContainer;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.TextWidget;
import pt.iscte.paddle.javardise.TokenWidget;
import pt.iscte.paddle.javardise.api.IWidget;
import pt.iscte.paddle.model.IConstantDeclaration;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IModuleView;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IRecordType;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;

public class ClassWidget extends ModiferWidget<IRecordType> implements SequenceContainer, IClassWidget {

	private IModule module;
	private IRecordType mainType;

	private Id id;
	private SequenceWidget methods;
	private Composite header;

	@Override
	public Composite getHeader() {
		return header;
	}

	@Override
	public Function<List<String>, List<String>> getModifierProvider() {
		return list -> Keyword.classModifiers();
	}

	public ClassWidget(Composite parent, IModule module, String namespace, Keyword ... modifiers) {
		super(parent, module.getRecordType(namespace));
		this.module = module;
		mainType = module.getRecordType(namespace);

		GridLayout layout = new GridLayout(1, true);
		layout.verticalSpacing = 10;
		setLayout(layout);
		if (UiMode.editorMode() == UiMode.Editor.REGULAR) {
			header = Constants.createHeader(this);
			for(Keyword mod : modifiers)
				addModifier(mod.keyword());

			TokenWidget classToken = new TokenWidget(header, Keyword.CLASS.keyword());
			addModifierKey(classToken);
			id = new Id(header, namespace);
			id.setReadOnly();
			new FixedToken(header, "{");
		}

		int tabs = UiMode.editorMode() == UiMode.Editor.STATIC ? 0 : 1;

		Predicate<String> tokenAccept = token -> acceptModifier(token) || BlockAction.isType(token) || IType.VOID.getId().equals(token);
		methods = new SequenceWidget(this, tabs, true, tokenAccept);
		methods.addAction(new InsertWidget.Action("add field") {
			public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				return (c == ';' || c == '=') && tokens.size() > 0 && BlockAction.isType(last);
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
//				IType t = IType.match(tokens.get(tokens.size()-1));
//				boolean assign = c == '=';
				String last = tokens.get(tokens.size()-1);
				mainType.addField(IType.match(last), id.getText(), tokens.subList(0, tokens.size()-1));
				//				if(assign)
				//					w.focusExpression();
			}
		});

		methods.addAction(new InsertWidget.Action("add constructor") {
			public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				return c == '(' && id.getText().equals(module.getId());
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				//				int i = module.getConstants().size() + getMainType().getFields().size();
//				IProcedure p = module.addProcedure(id.getId(), IType.VOID, Flag.CONSTRUCTOR.name());
//				p.setNamespace(namespace);
				
				module.addProcedure(mainType, p -> {
					p.setId(id.getText());
					p.setNamespace(namespace);
					p.setFlag(Flag.CONSTRUCTOR.name());
					for(String flag : tokens)
						p.setFlag(flag);
				});
				
			}
		});

		methods.addAction(new InsertWidget.Action("method") {
			public boolean isEnabled(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				if(tokens.size() < 1)
					return false;
				String last = tokens.get(tokens.size()-1);
				System.out.println(tokens);
				return c == '(' && !id.isKeyword() && text.length() > 0 && tokens.size() > 0 && (BlockAction.isType(last) || IType.VOID.getId().equals(last));
			}

			public void run(char c, TextWidget id, int index, int caret, int selection, List<String> tokens) {
				String last = tokens.get(tokens.size()-1);
				IType t = last.equals(IType.VOID.getId()) ? IType.VOID : IType.match(last);
				module.addProcedure(t, p -> {
					p.setId(id.getText());
					p.setNamespace(namespace);
					p.setFlag(Flag.INSTANCE.name());
					for(String flag : tokens)
						p.setFlag(flag);
				});
			}
		});

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

		IModuleView view = module.createNamespaceView(namespace);
		view.getProcedures().stream()
		.filter(p -> Flag.CONSTRUCTOR.is(p))
		.forEach(c -> methods.addLineAndElement(comp -> new MethodWidget(comp, c)));

		view.getProcedures().stream()
		.filter(p -> Flag.CONSTRUCTOR.isNot(p))
		.forEach(c -> methods.addLineAndElement(comp -> new MethodWidget(comp, c)));


		if (UiMode.editorMode() == UiMode.Editor.REGULAR)
			new FixedToken(this, "}");

		view.addListener(new IModule.IListener() {
			public void constantAdded(IConstantDeclaration constant) {
				int i = module.getConstants().indexOf(constant);
				methods.addElement(p -> new FieldWidget(p, constant), i);
			}

			public void constantRemoved(IConstantDeclaration constant) {
				methods.removeElement(constant);
			}

			public void procedureAdded(IProcedure procedure) {
				int i = module.getConstants().size() + mainType.getFields().size();
				if(Flag.CONSTRUCTOR.is(procedure))
					i += index(module.getProcedures(), procedure);
				else {
					i += (int) module.getProcedures().stream().filter(p -> Flag.CONSTRUCTOR.is(p)).count();
					i += index(module.getProcedures(), procedure);
				}
				MethodWidget m = methods.addElement(p -> new MethodWidget(p, procedure), i);
				m.focusParameters();
			}

			public void procedureRemoved(IProcedure procedure) {
				methods.removeElement(procedure);
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

	
	@Override
	public void addSelectionListener(Consumer<IWidget> l) {
		Display.getDefault().addFilter(SWT.FocusIn, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Control w = (Control) event.widget;
				if(w.getParent() instanceof IWidget)
					l.accept((IWidget)w.getParent());
			}
		});
	}
	
//	private IRecordType getMainType() {
//		IRecordType mainType = module.getProperty(IRecordType.class);
//		if(mainType == null) {
//			mainType = module.addRecordType();
//			mainType.setId(module.getId());
//			module.setProperty(IRecordType.class, mainType);
//			IRecordType t = mainType;
//			mainType.addListener(new IRecordType.IListener() {
//				public void fieldAdded(IVariableDeclaration field) {
//					methods.addElement(p -> new FieldWidget(p, field), t.getFields().size());
//				}
//			});
//		}
//		return mainType;
//	}

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

	@Override
	public IWidget getClassName() {
		return id;
	}

	@Override
	public IMethodWidget getProcedure(IProcedure procedure) {
		for(Control c : methods.getChildren())
			if(c instanceof MethodWidget && ((MethodWidget) c).getProgramElement() == procedure)
				return (IMethodWidget) c;

		return null;
	}
}