package pt.iscte.paddle.javasde;

import static pt.iscte.paddle.javasde.Keyword.BREAK;
import static pt.iscte.paddle.javasde.Keyword.CONTINUE;
import static pt.iscte.paddle.javasde.Keyword.ELSE;
import static pt.iscte.paddle.javasde.Keyword.IF;
import static pt.iscte.paddle.javasde.Keyword.RETURN;
import static pt.iscte.paddle.javasde.Keyword.WHILE;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

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

	private final InsertWidget insertWidget;

	//	private GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	//	private Menu menu;
	//	private FocusListener updateMenuListener;

	//	private MenuItem deleteItem;

	//	private IBlock block;

	private Consumer<Integer> deleteAction = index -> {};

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

		insertWidget = new InsertWidget(this, true, tokenAccept);
		insertWidget.setHideMode();
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				insertWidget.setFocus();
			}
		});
	}

	void setDeleteAction(Consumer<Integer> action) {
		deleteAction = action;
	}

	void insertLineAt(Control location) {
		InsertWidget insert = insertWidget.copyTo(this);
		insert.moveAbove(location);
		requestLayout();
		insert.setFocus();
	}

	int findModelIndex(Control location) {
		int i = 0;
		for (Control c : getChildren()) {
			if (c == location)
				return i;

			if(!isElse(c) && !(c instanceof InsertWidget))
				i++;
		}
		assert false;
		return -1;
	}

	private int toViewIndex(int modelIndex) {
		assert modelIndex < getChildren().length : modelIndex;
		//		assert modelIndex < totalElements() : modelIndex;

		int elsesAndInserts = 0;
		int i = 0;
		for (Control c : getChildren()) {
			if(i == modelIndex) {
				return i + elsesAndInserts;
			}

			if(isElse(c) || c instanceof InsertWidget)
				elsesAndInserts++;
			else
				i++;
		}

		assert false;
		return -1;
	}


	private static boolean isElse(Control c) {
		return c instanceof ControlWidget && ((ControlWidget) c).is(Keyword.ELSE);
	}

	private int totalElements() {
		Control[] children = getChildren();
		int elsesAndInserts = 0;
		for (Control c : children)
			if(isElse(c) || c instanceof InsertWidget)
				elsesAndInserts++;

		return children.length - elsesAndInserts;
	}

	public <T extends Control> T addWidget(Function<Composite, T> f) {
		return addWidget(f, totalElements());
	}

	public <T extends Control> T addWidget(Function<Composite, T> f, int modelIndex) {
		T w = f.apply(this);
		int i = toViewIndex(modelIndex);
		Control location = getChildren()[i];
		w.moveAbove(location);
		w.requestLayout();
		return w;
	}

	public void addAction(InsertWidget.Action a) {
		insertWidget.addAction(a);
	}

	public void addActions(List<? extends InsertWidget.Action> actions) {
		for(InsertWidget.Action a : actions)
			insertWidget.addAction(a);
	}


	void addBlockListener(IBlock block) {
		//		this.block = block;
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
					if(a.is("INC") || a.is("DEC")) {
						IncrementationWidget w = new IncrementationWidget(SequenceWidget.this, idd, a.is("INC"));
						addElement(w, index);
					}
					else {
						AssignmentWidget assignmentWidget = new AssignmentWidget(SequenceWidget.this, idd, exp, true, false);
						addElement(assignmentWidget, index);
						assignmentWidget.focusExpression();
					}
				} 


				else if (element instanceof IArrayElementAssignment) {
					IArrayElementAssignment a = (IArrayElementAssignment) element;
					String id = a.getTarget().getId();
					String idd = id == null ? "variable" : id;
					String exp = "expression";
					AssignmentWidget assignmentWidget = new AssignmentWidget(SequenceWidget.this, idd, exp, true, true);
					addElement(assignmentWidget, index);
					assignmentWidget.setFocus();
					//					focusNextStatement(); // TODO?
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
					ForWidget w = new ForWidget(SequenceWidget.this, "expression", (IBlock) element);
					addElement(w, index);
					w.focusDeclaration();
				} 

				else if (element instanceof IBreak) {
					InstructionWidget inst = new InstructionWidget(SequenceWidget.this, BREAK);
					addElement(inst, index);

				} 
				else if (element instanceof IContinue) {
					InstructionWidget inst = new InstructionWidget(SequenceWidget.this, CONTINUE);
					addElement(inst, index);
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

			@Override
			public void elementRemoved(IProgramElement element, int index) {
				System.out.println("REM " + element);
				int i = toViewIndex(index);
				Control[] children = getChildren();
				Control c = children[i];
				//				focusNextStatement(c); // TODO focus next
				c.dispose();
				if(i+1 < children.length && children[i+1] instanceof ControlWidget && ((ControlWidget) children[i+1]).is(Keyword.ELSE))
					children[i+1].dispose();
				requestLayout();
			}
		});


	}

	void addElement(EditorWidget w, int modelIndex) {
		int viewIndex = toViewIndex(modelIndex);
		if(isElse(w))
			viewIndex++;
		Control location = getChildren()[viewIndex];
		w.moveAbove(location);
		w.requestLayout();
		w.setFocus();
	}

	// TODO to remove
	void delete(Predicate<Control> pred) {
		Control[] children = getChildren();
		for (int i = 0; i < children.length-1; i++) {
			if (pred.test(children[i])) {
				children[i].dispose();
				break;
			}
		}
		requestLayout();
	}

	void focusPreviousStatement(Control statement) {
		Control[] children = getChildren();
		if(children[0] == statement) {
			Composite parent = statement.getParent();
			Composite parent2 = ((SequenceWidget) parent).getParent();
			if(parent2 instanceof SequenceWidget)
				((SequenceWidget) parent2).focusPreviousStatement(statement);
			else
				parent2.setFocus();
		}
		else {
			for (int i = 1; i < children.length; i++) {
				if(children[i] == statement) {
					if(children[i-1] instanceof SequenceContainer)
						((SequenceContainer) children[i-1]).getBody().focusLast();
					else
						children[i-1].setFocus();
					break;
				}
			}
		}
	}

	void focusNextStatement(TextWidget widget) {
		Control statement = widget.getStatement();

		if(statement instanceof SequenceContainer) {
			((SequenceContainer)statement).getBody().focusFirst();
		}
		else {
			Control[] children = getChildren();
			if(children[children.length-1] == statement) {
				widget.getWidget().traverse(SWT.TRAVERSE_TAB_NEXT);
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
		children[children.length - 1].setFocus();

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

	public void deleteAction(int index) {
		deleteAction.accept(index);
		getChildren()[index].setFocus();
	}









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
