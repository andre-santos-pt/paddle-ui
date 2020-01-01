package pt.iscte.paddle.javasde;

import static pt.iscte.paddle.javasde.Keyword.BREAK;
import static pt.iscte.paddle.javasde.Keyword.CONTINUE;
import static pt.iscte.paddle.javasde.Keyword.ELSE;
import static pt.iscte.paddle.javasde.Keyword.IF;
import static pt.iscte.paddle.javasde.Keyword.RETURN;
import static pt.iscte.paddle.javasde.Keyword.WHILE;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBreak;
import pt.iscte.paddle.model.IContinue;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariable;
import pt.iscte.paddle.model.IVariableAssignment;

public class SequenceWidget extends Composite {

	class MenuCommand {
		final String text;
		final char accelerator;
		final BiConsumer<Integer, String> action;
		final Function<Integer, Boolean> enabled;

		MenuCommand(String text, char accelerator, BiConsumer<Integer, String> action, Function<Integer, Boolean> enabled) {
			this.text = text;
			this.accelerator = accelerator;
			this.action = action;
			this.enabled = enabled;
		}

		MenuItem createItem(Menu parent) {
			if (text == null)
				return new MenuItem(parent, SWT.SEPARATOR);

			MenuItem item = new MenuItem(parent, SWT.NONE);
			item.setText(text);
			item.setAccelerator(accelerator);
			item.setData(this);
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					run(null);
				}
			});
			return item;
		}

		void run(String param) {
			int i = findModelIndex(Display.getDefault().getFocusControl());
			action.accept(i, param);
			updateMenu();
		}
	}

	static boolean isKeyword(Control control, String keyword) {
		return control instanceof Label && ((Label) control).getText().equals(keyword);
	}

	static int findModelIndex(Control location) {
		int index = -1;
		int elses = 0;
		int i = 0;
		for (Control c : location.getParent().getChildren()) {
			if(c instanceof ControlWidget && ((ControlWidget) c).is(Keyword.ELSE))
				elses++;
			if (c == location) {
				index = i;
				break;
			}
			if(!(c instanceof InsertWidget))
				i++;
		}	
		return index - elses;
	}

	private static int toViewIndex(Control location, int index) {
		int elsesAndInserts = 0;
		for (Control c : location.getParent().getChildren())
			if(c instanceof ControlWidget && ((ControlWidget) c).is(Keyword.ELSE) || c instanceof InsertWidget)
				elsesAndInserts++;

		return index + elsesAndInserts - 1;
	}
	
	private int totalElements() {
		Control[] children = getChildren();
		int elsesAndInserts = 0;
		for (Control c : children)
			if(c instanceof ControlWidget && ((ControlWidget) c).is(Keyword.ELSE) || c instanceof InsertWidget)
				elsesAndInserts++;

		return children.length - elsesAndInserts;
	}

	private final InsertWidget insertWidget;

//	private GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	private Menu menu;
	private FocusListener updateMenuListener;

	private MenuItem deleteItem;

	public SequenceWidget(Composite parent, int margin) {
		this(parent, margin, token -> false);
	}
	
	public SequenceWidget(Composite parent, int marginLeft, Predicate<String> tokenAccept) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = marginLeft;
		layout.verticalSpacing = 3;
		layout.horizontalSpacing = 2;
		setBackground(Constants.COLOR_BACKGROUND);
		setLayout(layout);

		insertWidget = new InsertWidget(this, tokenAccept);
		//		insertWidget.setLayoutData(data);
//		insertWidget.addFocusListener(Constants.ADD_HIDE);

		menu = new Menu(insertWidget);
		addDeleteItem(menu);
		new MenuItem(menu, SWT.SEPARATOR);
		insertWidget.setMenu(menu);
		updateMenuListener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				((Text)e.widget).selectAll();
				updateMenu();
			}
		};
		insertWidget.addFocusListener(updateMenuListener);

		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				insertWidget.setFocus();
			}
		});
	}

	public <T extends Control> T addWidget(Function<Composite, T> f) {
		return addWidget(f, totalElements());
	}
	
	public <T extends Control> T addWidget(Function<Composite, T> f, int index) {
		T w = f.apply(this);
		Control location = getChildren()[toViewIndex(w, index)];
		w.moveAbove(location);
//		w.addTokenKeyHandler(keyListener);
		w.requestLayout();
//		w.setFocus();
//		child.moveAbove(insertWidget);
		return w;
	}
	
	public void addAction(InsertWidget.Action a) {
		insertWidget.addAction(a);
	}
	
	public void addActions(List<? extends InsertWidget.Action> actions) {
		for(InsertWidget.Action a : actions)
			insertWidget.addAction(a);
	}

	private void updateMenu() {
		Control control = Display.getDefault().getFocusControl();
		deleteItem.setEnabled(control != insertWidget.text); // FIXME disposed
		Object widget = control.getData();

		int i = findModelIndex(control);
		for (MenuItem item : menu.getItems()) {
			Object data = item.getData();
			if(data instanceof MenuCommand) {
				MenuCommand cmd = (MenuCommand) item.getData();
				if(cmd != null && cmd.enabled != null) {
					boolean isElse = widget instanceof ControlWidget && ((ControlWidget) widget).is(Keyword.ELSE);
					item.setEnabled(!isElse && cmd.enabled.apply(i));
				}
			}
		}
	}


	MenuCommand addChildCommand(String text, char accelerator, BiConsumer<Integer, String> action) {
		return addChildCommand(text, accelerator, action, i -> true);
	}
//
//	MenuCommand addChildCommand(Keyword keyword, BiConsumer<Integer, String> action) {
//		return addChildCommand(keyword, action, i -> true);
//	}
//
	MenuCommand addChildCommand(String text, char accelerator, BiConsumer<Integer, String> action, Function<Integer, Boolean> enabled) {
		MenuCommand cmd = new MenuCommand(text, accelerator, action, enabled);
		cmd.createItem(menu); 
		return cmd;
	}
//
//	MenuCommand addChildCommand(Keyword keyword, BiConsumer<Integer, String> action, Function<Integer, Boolean> enabled) {
//		MenuCommand cmd = new MenuCommand(keyword.toString(), keyword.getAccelerator(), action, enabled);
//		cmd.createItem(menu); 
//		return cmd;
//	}


	
//	void addStatementCommands(IBlock block) {
//		addChildCommand("type variable", 'v', (i, p) -> {
//			boolean array = p != null && p.endsWith("[");
//			if(array)
//				p = p.substring(0, p.length()-1);
//			IType t = IType.match(p);
//			if(array)
//				t = t.array();
//
//			IVariable var = block.addVariableAt(t, i);
//			var.setId("id");
//		});
//
//		addChildCommand("variable = ...", 'a', (i, p) -> block.addAssignmentAt(p, null, i));
//
//		addChildCommand(IF, (i, p) -> block.addSelectionAt(BOOLEAN.literal(true), i));
//
//		addChildCommand(ELSE, (i, p) -> {
//			IBlockElement e = block.getChildren().get(i - 1);
//			if (e instanceof ISelection && !((ISelection) e).hasAlternativeBlock()) {
//				((ISelection) e).createAlternativeBlock();
//			}
//		},i -> {
//			if(i > 0) {
//				IBlockElement e = block.getChildren().get(i - 1);
//				return e instanceof ISelection && !((ISelection) e).hasAlternativeBlock();
//			}
//			return false;
//		});
//
//		addChildCommand("call(...)", 'p', (i, p) -> block.addCallAt(null, i));
//
//		addChildCommand("return", 'r', (i, p) -> block.addReturnAt(null, i));
//
//		new MenuItem(menu, SWT.SEPARATOR);
//
//		addChildCommand(WHILE, (i, p) -> block.addLoopAt(BOOLEAN.literal(true), i));
//
//		addChildCommand(FOR, (i, p) -> {
//			IBlock forBlock = block.addBlockAt(i, Constants.FOR_FLAG);
//			IVariable progVar = forBlock.addVariable(INT, Constants.FOR_FLAG);
//			ILoop loop = forBlock.addLoop(BOOLEAN.literal(true), Constants.FOR_FLAG);
//			IVariableAssignment inc = loop.addAssignment(progVar, IOperator.ADD.on(progVar, INT.literal(1)),
//					Constants.FOR_FLAG);
//		});
//
//		addChildCommand(BREAK, (i, p) -> block.addBreakAt(i), i -> block.isInLoop());
//		addChildCommand(CONTINUE, (i, p) -> block.addContinueAt(i), i -> block.isInLoop());
//	}

	void addBlockListener(IBlock block) {
		block.addListener(new IBlock.IListener() {
			public void elementAdded(IProgramElement element, int index) {
				if (element instanceof IVariable && element.not(Constants.FOR_FLAG)) {
					IVariable v = (IVariable) element;
					IType type = v.getType();
					if(type == null)
						type = IType.UNBOUND;

					String id = v.getId() != null ? v.getId() : "variable";
					String exp = "expression";
					DeclarationWidget declarationWidget = new DeclarationWidget(SequenceWidget.this, type, id, exp);
					addElement(declarationWidget, index);
					if(v.getId() == null)
						declarationWidget.focusId();
					else 
						declarationWidget.setFocus();
				} 

				else if (element instanceof IVariableAssignment && element.not(Constants.FOR_FLAG)) {
					IVariableAssignment a = (IVariableAssignment) element;
					String id = a.getTarget().getId();
					String idd = id == null ? "variable" : id;
					String exp = "expression";
					AssignmentWidget assignmentWidget = new AssignmentWidget(SequenceWidget.this, idd, exp, true, false);
					addElement(assignmentWidget, index);
					assignmentWidget.focusExpression();
				} 

				
				else if (element instanceof IArrayElementAssignment) {
					IArrayElementAssignment a = (IArrayElementAssignment) element;
					String id = a.getTarget().getId();
					String idd = id == null ? "variable" : id;
					String exp = "expression";
					AssignmentWidget assignmentWidget = new AssignmentWidget(SequenceWidget.this, idd, exp, true, true);
					addElement(assignmentWidget, index);
					assignmentWidget.setFocus();
					focusNext(); // TODO?
				} 
				
				else if (element instanceof ISelection) {
					ISelection s = (ISelection) element;
					ControlWidget w = new ControlWidget(SequenceWidget.this, IF, "expression", s.getBlock());
					addElement(w, index);
					w.focusIn();
					if (s.hasAlternativeBlock())
						addElement(new ControlWidget(SequenceWidget.this, ELSE, null, s.getAlternativeBlock()), index);
					s.addPropertyListener((k,n,o) -> {
						if(k.equals(Constants.ELSE_FLAG)) {
							ControlWidget e = new ControlWidget(SequenceWidget.this, ELSE, null, s.getAlternativeBlock());
							addElement(e, index);
							e.focusIn();
						}
					});
				} 

				else if (element instanceof ILoop && element.not(Constants.FOR_FLAG)) {
					ILoop l = (ILoop) element;
					ControlWidget w = new ControlWidget(SequenceWidget.this, WHILE, "expression", l.getBlock());
					addElement(w, index);
					w.focusIn();
				} 

				else if (element instanceof IBlock && element.is(Constants.FOR_FLAG)) {
					ForWidget w = new ForWidget(SequenceWidget.this, IType.INT, "i", "expression", "expression", (IBlock) element);
					addElement(w, index);
					w.focusDeclaration();
				} 

				else if (element instanceof IBreak) {
					addElement(new InstructionWidget(SequenceWidget.this, BREAK), index);
				} 
				else if (element instanceof IContinue) {
					addElement(new InstructionWidget(SequenceWidget.this, CONTINUE), index);
				} 

				else if (element instanceof IProcedureCall) {
					IProcedureCall call = (IProcedureCall) element;
					CallWidget w = new CallWidget(SequenceWidget.this, call.getProcedure().getId(), true);
					addElement(w, index);
					w.focusArgument();
				} 

				else if (element instanceof IReturn) {
					IReturn ret = (IReturn) element;
					String exp = " ";
					if(!ret.isVoid())
						exp = ret.getExpression().toString();
					InstructionWidget w = new InstructionWidget(SequenceWidget.this, RETURN, exp);
					addElement(w, index);
					w.focusExpression();
				} else
					System.err.println("unhandled: " + element);
			}
		});
	}

	void addElement(EditorWidget w) {
		addElement(w, getChildren().length - 1);
	}

	void addElement(EditorWidget w, int index) {
		Control location = getChildren()[toViewIndex(w, index)];
		w.moveAbove(location);
		w.addTokenKeyHandler(keyListener);
		w.requestLayout();
		w.setFocus();
	}

	private void addDeleteItem(Menu menu) {
		deleteItem = new MenuItem(menu, SWT.NONE);
		deleteItem.setText("delete");
		deleteItem.setAccelerator(Constants.DEL_KEY);
		SelectionListener l = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//deleteItem();
				System.out.println("delete");
			}
		};
		deleteItem.addSelectionListener(l);
		deleteItem.setData(l);
	}

	void delete(Predicate<EditorWidget> pred) {
		Control[] children = getChildren();
		for (int i = 1; i < children.length; i += 2) {
			if (pred.test((EditorWidget) children[i])) {
				if (i != 1)
					children[i - 1].dispose();
				children[i].dispose();
			}
		}
		requestLayout();
	}

	void delete(EditorWidget e) {
		Control[] children = getChildren();
		for (int i = 1; i < children.length; i += 2) {
			if (children[i] == e) {
				children[i - 1].dispose();
				children[i].dispose();
				requestLayout();
				return;
			}
		}
	}

	private Control getOwnerWidget(Control control) {
		Control c = control;
		while(!(c.getParent() instanceof SequenceWidget))
			c = c.getParent();

		return c;
	}

	private KeyAdapter keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(e.keyCode == Constants.DEL_KEY) {
				Control w = getOwnerWidget((Control) e.widget);
				Composite parent = w.getParent();
				int i = 0;
				for(Control c : parent.getChildren()) {
					if(c == w)
						break;
					i++;
				}
				w.setMenu(null);
				w.dispose();
				Control[] children = parent.getChildren();
				if(i < children.length) // FIXME
					children[i].setFocus();

				requestLayout();
			}
			else if(e.keyCode == SWT.CR) {
				Control focusControl = getOwnerWidget((Control) e.widget);
				InsertWidget w =  new InsertWidget(SequenceWidget.this);
				w.setMenu(menu);
				w.addKeyListener(keyListener);
				w.text.moveAbove(focusControl);
				w.text.requestLayout();
				w.setFocus();
			}
		}
	};


	void focusPrevious(Control statement) {
		Control[] children = getChildren();
		if(children[0] == statement) {
			Composite parent = getParent().getParent();
			if(parent instanceof SequenceWidget)
				((SequenceWidget) parent).focusLast();
		}
		else {
			for (int i = 1; i < children.length; i++) {
				if(children[i] == statement) {
					children[i-1].setFocus();
					break;
				}
			}
		}
	}

	void focusNext() {
		Control focusControl = Display.getDefault().getFocusControl();
		if(focusControl != null)
			focusNext(focusControl);
	}
	
	void focusNext(Control statement) {
		if(statement instanceof ControlWidget) {
			((ControlWidget) statement).getSequence().focusFirst();
		}
		else {
			Control[] children = getChildren();
			if(children[children.length-1] == statement) {
				Composite parent = getParent();
				Composite parent2 = parent.getParent();
				if(parent2 instanceof SequenceWidget)
					((SequenceWidget) parent2).focusNext(parent);
			}
			else {
				for (int i = 0; i < children.length-1; i++) {
					if(children[i] == statement) {
						children[i+1].setFocus();
						break;
					}
				}
			}
		}
	}

	void focusFirst() {
		Control[] children = getChildren();
		children[0].setFocus();

	}

	void focusLast() {
		Control[] children = getChildren();
		int i = children.length == 1 ? 1 : 2;
		children[children.length - i].setFocus();

	}


	public void toCode(StringBuffer buffer) {
		buffer.append("#" + this.getClass().getSimpleName() + "#");
	}
	
	public void toCode(StringBuffer buffer, int level) {
		for (Control control : getChildren())
			if (control instanceof EditorWidget)
				((EditorWidget) control).toCode(buffer, level);
	}

	public List<String> getInsertTokens() {
		return insertWidget.getTokens();
	}
//	public Keyword[] getInsertModifiers() {
//		return insertWidget.getModifiers().toArray(new Keyword[insertWidget.getModifiers().size()]);
//	}
	
	// TODO drag n drop to move statements
	private void addDragNDrop(Control label) {
		DragSource source = new DragSource(label, DND.DROP_NONE);
		source.setTransfer(TextTransfer.getInstance());
		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				System.out.println("start - " + label + " " + findModelIndex(label));
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = Integer.toString(findModelIndex(label));
			}

			//			@Override
			//			public void dragFinished(DragSourceEvent event) {
			//				System.out.println("end - " + event.widget);
			//			}
		});

		DropTarget target = new DropTarget(label, DND.DROP_NONE);
		target.setTransfer(TextTransfer.getInstance());
		target.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				int fromIndex = Integer.parseInt((String) event.data);
				int toIndex = findModelIndex(label);
				System.out.println("move " + fromIndex + " -> " + toIndex);
			}

			//			@Override
			//			public void dropAccept(DropTargetEvent event) {
			//				
			//			}

			//			@Override
			//			public void dragOver(DropTargetEvent event) {
			//				// TODO Auto-generated method stub
			//
			//			}
			//
			//			@Override
			//			public void dragOperationChanged(DropTargetEvent event) {
			//				// TODO Auto-generated method stub
			//
			//			}
			//
			//			@Override
			//			public void dragLeave(DropTargetEvent event) {
			//				// TODO Auto-generated method stub
			//
			//			}
			//
			//			@Override
			//			public void dragEnter(DropTargetEvent event) {
			//				// TODO Auto-generated method stub
			//
			//			}
		});
	}

	








}
