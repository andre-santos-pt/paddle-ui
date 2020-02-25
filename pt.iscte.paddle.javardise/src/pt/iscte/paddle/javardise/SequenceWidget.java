package pt.iscte.paddle.javardise;

import static pt.iscte.paddle.javardise.Keyword.BREAK;
import static pt.iscte.paddle.javardise.Keyword.CONTINUE;
import static pt.iscte.paddle.javardise.Keyword.ELSE;
import static pt.iscte.paddle.javardise.Keyword.IF;
import static pt.iscte.paddle.javardise.Keyword.RETURN;
import static pt.iscte.paddle.javardise.Keyword.WHILE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBreak;
import pt.iscte.paddle.model.IContinue;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IProcedureCall;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IVariable;
import pt.iscte.paddle.model.IVariableAssignment;

public class SequenceWidget extends Composite {

	private final NewInsertWidget insertWidget;

	private Consumer<Integer> deleteAction = index -> {};

	public SequenceWidget(Composite parent, int margin) {
		this(parent, margin, 3, token -> false);
	}

	public SequenceWidget(Composite parent, int marginLeft, int verticalSpacing, Predicate<String> tokenAccept) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = marginLeft;
		layout.verticalSpacing = verticalSpacing;
		layout.horizontalSpacing = 2;
		setBackground(Constants.COLOR_BACKGROUND);
		setLayout(layout);

		insertWidget = new NewInsertWidget(this, true, tokenAccept);
		insertWidget.setHideMode();
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				insertWidget.setFocus();
			}
		});
		
		Supplier<List<Action>> sup = () -> {
			ArrayList<Action> list = new ArrayList<>();
			list.add(new Action() {
				public void run() {
					System.out.println("!!");
				}
				public String getText() {
					return "action";
				}
			});
			return list;
		};
		Menu menu = new Menu(insertWidget);
		menu.addListener(SWT.Show, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				for(MenuItem i : menu.getItems())
					i.dispose();
				for(Action a : sup.get()) {
					MenuItem i = new MenuItem(menu, SWT.PUSH);
					i.setText(a.getText());
					i.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							a.run();
						}
					});
				}
					
			}
		});
		insertWidget.setMenu(menu);
	}

	interface Action {
		String getText();
		void run();
	}
	
	void setDeleteAction(Consumer<Integer> action) {
		deleteAction = action;
	}

	void insertLineAt(Control location) {
		NewInsertWidget insert = insertWidget.copyTo(this);
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

	int totalElements() {
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

	public void addAction(NewInsertWidget.Action a) {
		insertWidget.addAction(a);
	}

	public void addActions(List<? extends NewInsertWidget.Action> actions) {
		for(NewInsertWidget.Action a : actions)
			insertWidget.addAction(a);
	}


	void addBlockListener(IBlock block) {
		block.addListener(new IBlock.IListener() {
			public void elementAdded(IProgramElement element, int index) {
				addModelElement(element, index);
			}

			@Override
			public void elementRemoved(IProgramElement element, int index) {
				int i = toViewIndex(index);
				Control[] children = getChildren();
				Control c = children[i];
				c.dispose();
				if(i+1 < children.length && children[i+1] instanceof ControlWidget && ((ControlWidget) children[i+1]).is(Keyword.ELSE))
					children[i+1].dispose();
				requestLayout();
			}
		});
	}

	<T extends EditorWidget> T addElement(Function<Composite, T> f, IProgramElement e) {
		return addElement(f, e, totalElements());
	}
	
	<T extends EditorWidget> T addElement(Function<Composite, T> f, IProgramElement e, int modelIndex) {
		Markable<T> sel = new Markable<>(this, f, e);
		T w = sel.target;
//		T w = f.apply(this);
		int viewIndex = toViewIndex(modelIndex);
		if(isElse(w))
			viewIndex++;
		Control location = getChildren()[viewIndex];
		sel.moveAbove(location);
		sel.requestLayout();
		sel.setFocus();
		return w;
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
		Control c = widget.getStatement();
		
		if(c instanceof Markable && ((Markable<?>) c).target instanceof SequenceContainer) {
			((SequenceContainer) ((Markable<?>) c).target).getBody().focusFirst();
		}
		else {
			Control[] children = getChildren();
			if(children[children.length-1] == c) {
				widget.getWidget().traverse(SWT.TRAVERSE_TAB_NEXT);
			}
			else {
				for (int i = 0; i < children.length-1; i++) {
					if(children[i] == c) {
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

	void addModelElement(IProgramElement element, int index) {
		if (element instanceof IVariable && element.not(Constants.FOR_FLAG)) {
			IVariable v = (IVariable) element;
			DeclarationWidget w = addElement(p -> new DeclarationWidget(p, v, null), v, index);
			w.focusId();
		} 

		else if (element instanceof IVariableAssignment && element.not(Constants.FOR_FLAG)) {
			IVariableAssignment a = (IVariableAssignment) element;
//			String id = a.getTarget().getId();
//			String idd = id == null ? "variable" : id;
//			if(a.is("INC") || a.is("DEC")) {
//				IncrementationWidget w = new IncrementationWidget(SequenceWidget.this, idd, a.is("INC"));
//				addElement(w, index);
//			}
//			else {
//				Selectable<AssignmentWidget> w = new Selectable<>(SequenceWidget.this, p -> new AssignmentWidget(p, a), a);
				AssignmentWidget w = addElement(p -> new AssignmentWidget(p, a), a, index);
				w.focusExpression();
//			}
		} 

		else if (element instanceof IArrayElementAssignment) {
			IArrayElementAssignment a = (IArrayElementAssignment) element;
			AssignmentWidget w = addElement(p -> new AssignmentWidget(p, a), a, index);
			w.focusExpression();
		} 

		else if (element instanceof ISelection) {
			ISelection s = (ISelection) element;
			ControlWidget w = addElement(p -> new ControlWidget(p, IF, s.getGuard(), s.getBlock()), s, index);
			w.focusIn();
			if (s.hasAlternativeBlock())
				addElement(p -> new ControlWidget(p, ELSE, null, s.getAlternativeBlock()), s.getAlternativeBlock(), index);
			s.addPropertyListener((k,n,o) -> {
				if(k.equals(Constants.ELSE_FLAG)) {
					ControlWidget e = addElement(p -> new ControlWidget(p, ELSE, null, s.getAlternativeBlock()), s.getAlternativeBlock(), index);
					e.focusIn();
				}
			});
		} 

		else if (element instanceof ILoop && element.not(Constants.FOR_FLAG)) {
			ILoop l = (ILoop) element;
			ControlWidget w =  addElement(p -> new ControlWidget(p, WHILE, l.getGuard(), l.getBlock()), l, index);
			w.focusIn();
		} 

//		else if (element instanceof IBlock && element.is(Constants.FOR_FLAG)) { 
//			ForWidget w = new ForWidget(SequenceWidget.this, null, (IBlock) element);   // TODO guard
//			addElement(w, index);
//			w.focusDeclaration();
//		} 

		else if (element instanceof IBreak) {
			addElement(p -> new InstructionWidget(p, BREAK), element, index);

		} 
		else if (element instanceof IContinue) {
			addElement(p -> new InstructionWidget(p, CONTINUE), element, index);
		} 

		else if (element instanceof IProcedureCall) {
			IProcedureCall call = (IProcedureCall) element;
			String id = call.getProcedure().getId();
			CallWidget w = addElement(p -> new CallWidget(p, id, true, Expression.creators(call.getArguments())), call, index);
			w.focusArgument();
		} 

		else if (element instanceof IReturn) {
			IReturn ret = (IReturn) element;
			InstructionWidget w = addElement(p -> new InstructionWidget(p, RETURN, ret.getExpression()), ret, index);
			w.focusExpression();
		} else {
			System.err.println("unhandled: " + element);
			assert false;
		}
	}
	
}