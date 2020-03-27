package pt.iscte.paddle.javardise;

import static pt.iscte.paddle.javardise.Keyword.BREAK;
import static pt.iscte.paddle.javardise.Keyword.CONTINUE;
import static pt.iscte.paddle.javardise.Keyword.RETURN;
import static pt.iscte.paddle.javardise.Keyword.WHILE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

import pt.iscte.paddle.javardise.service.ICodeElement;
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
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;

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

		insertWidget = new NewInsertWidget(this, true, getParent() instanceof ClassWidget, tokenAccept);
//		insertWidget.setHideMode();
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				insertWidget.setFocus();
			}
		});
//
//		Supplier<List<Action>> sup = () -> {
//			ArrayList<Action> list = new ArrayList<>();
//			list.add(new Action() {
//				public void run() {
//					System.out.println("!!");
//				}
//				public String getText() {
//					return "action";
//				}
//			});
//			return list;
//		};
//		
//		Menu menu = new Menu(insertWidget);
//		menu.addListener(SWT.Show, new Listener() {
//			public void handleEvent(Event event) {
//				for(MenuItem i : menu.getItems())
//					i.dispose();
//				for(Action a : sup.get()) {
//					MenuItem i = new MenuItem(menu, SWT.PUSH);
//					i.setText(a.getText());
//					i.addSelectionListener(new SelectionAdapter() {
//						public void widgetSelected(SelectionEvent e) {
//							a.run();
//						}
//					});
//				}
//
//			}
//		});
//		insertWidget.setMenu(menu);
	}

//	interface Action {
//		String getText();
//		void run();
//	}

	void setDeleteAction(Consumer<Integer> action) {
		deleteAction = action;
	}

	void insertLineAt(Control location) {
		NewInsertWidget insert = insertWidget.copyTo(this);
		insert.moveAbove(location);
		requestLayout();
		insert.setFocus();
	}
	
	void insertLineAfter(Control location) {
		NewInsertWidget insert = insertWidget.copyTo(this);
		insert.moveBelow(location);
		requestLayout();
		insert.setFocus();
	}

	int findModelIndex(Control location) {
		int i = 0;
		for (Control c : getChildren()) {
//			if(c instanceof Markable)
//				c = ((Markable)c).target.getControl();

			if (c == location)
				return i;

			if(!isElse(c) && !(c instanceof NewInsertWidget))
				i++;
		}
		assert false;
		return -1;
	}

	private int toViewIndex(int modelIndex) {
		assert modelIndex < getChildren().length : modelIndex;
		int elsesAndInserts = 0;
		Control[] children = getChildren();
		for(int i = 0; i <= modelIndex; i++)
			if(isElse(children[i]) || children[i] instanceof NewInsertWidget)
				elsesAndInserts++;
		
		return modelIndex + elsesAndInserts;
	}

	private static boolean isElse(Control c) {
//		if(c instanceof Markable)
//			c = ((Markable) c).target.getControl();
		
		return c instanceof ControlWidget && ((ControlWidget) c).is(Keyword.ELSE);
	}

	int totalElements() {
		Control[] children = getChildren();
		int elsesAndInserts = 0;
		for (Control c : children)
			if(isElse(c) || c instanceof NewInsertWidget)
				elsesAndInserts++;

		return children.length - elsesAndInserts;
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

	public <T extends EditorWidget> T addElement(Function<Composite, T> f) {
		return addElement(f, totalElements());
	}

	public <T extends EditorWidget> T addElement(Function<Composite, T> f, int modelIndex) {
		Control el = viewElement(modelIndex);
		T w = f.apply(this);
		if(isElse(w))
			w.moveBelow(el);
		else
			w.moveAbove(el);
		return w;
	}
	
	private Control viewElement(int modelIndex) {
		assert modelIndex < getChildren().length : modelIndex;
		int elsesAndInserts = 0;
		Control[] children = getChildren();
		for(int i = 0, m = 0; m <= modelIndex && i < children.length; i++)
			if(isElse(children[i]) || children[i] instanceof NewInsertWidget)
				elsesAndInserts++;
			else
				m++;
		
		int v = modelIndex + elsesAndInserts;
		return v < children.length ? children[v] : insertWidget;
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
//					if(children[i-1] instanceof Markable && ((Markable<?>) children[i-1]).isSequenceContainer())
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

//		if(c instanceof Markable && ((Markable<?>) c).isSequenceContainer()) {
		if(c instanceof SequenceContainer) {
			((SequenceContainer) c).getBody().focusFirst();
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
			if (control instanceof ICodeElement)
				((ICodeElement) control).toCode(buffer, level);
	}

	public List<String> getInsertTokens() {
		return insertWidget.getTokens();
	}

	public void deleteAction(int index) {
		System.out.println("delete " + index);
		deleteAction.accept(index);
		getChildren()[index].setFocus();
	}

	void addModelElement(IProgramElement element, int index) {
		if (element instanceof IVariableDeclaration && Flag.FOR.isNot(element)) {
			IVariableDeclaration v = (IVariableDeclaration) element;
			DeclarationWidget w = addElement(p -> new DeclarationWidget(p, v, null), index);
			w.focusId();
		} 

		else if (element instanceof IVariableAssignment && Flag.FOR.isNot(element)) {
			IVariableAssignment a = (IVariableAssignment) element;
			AssignmentWidget w = addElement(p -> new AssignmentWidget(p, a), index);
			w.focusExpression();
		} 

		else if (element instanceof IArrayElementAssignment) {
			IArrayElementAssignment a = (IArrayElementAssignment) element;
			AssignmentWidget w = addElement(p -> new AssignmentWidget(p, a), index);
			w.focusExpression();
		} 

		else if (element instanceof ISelection) {
			ISelection s = (ISelection) element;
			IfElseWidget w = addElement(p -> new IfElseWidget(p, s), index);
			w.focusIn();
		} 
		
		else if (element instanceof ILoop && Flag.FOR.isNot(element)) {
			ILoop l = (ILoop) element;
			ControlWidget w =  addElement(p -> new ControlWidget(p, WHILE, l), index);
			w.focusIn();
		} 

		//		else if (element instanceof IBlock && element.is(Constants.FOR_FLAG)) { 
		//			ForWidget w = new ForWidget(SequenceWidget.this, null, (IBlock) element);   // TODO guard
		//			addElement(w, index);
		//			w.focusDeclaration();
		//		} 

		else if (element instanceof IBreak) {
			addElement(p -> new InstructionWidget(p, BREAK, (IBreak) element), index);

		} 
		else if (element instanceof IContinue) {
			addElement(p -> new InstructionWidget(p, CONTINUE, (IContinue) element), index);
		} 

		else if (element instanceof IProcedureCall) {
			IProcedureCall call = (IProcedureCall) element;
			CallWidget w = addElement(p -> new CallWidget(p, call, true, Expression.creators(call.getArguments())), index);
			w.focusArgument();
		} 

		else if (element instanceof IReturn) {
			IReturn ret = (IReturn) element;
			InstructionWidget w = addElement(p -> new InstructionWidget(p, RETURN, ret, ret.getExpression()), index);
			w.focusExpression();
			w.getWidget().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.SPACE) {
						IType retType = ret.getOwnerProcedure().getReturnType();
						w.addExpression(retType.getDefaultExpression());
						w.focusExpression();
					}
				}
			});
		} else {
			System.err.println("unhandled: " + element);
			assert false;
		}
	}

}
