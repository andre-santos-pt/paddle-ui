package pt.iscte.paddle.javardise;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import pt.iscte.paddle.javardise.api.ICodeElement;

public class SequenceWidget extends Composite {

	private final InsertWidget insertWidget;

	public SequenceWidget(Composite parent, int margin) {
		this(parent, margin, 3, false, token -> false);
	}

	public SequenceWidget(Composite parent, int marginLeft, int verticalSpacing, boolean type, Predicate<String> tokenAccept) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginLeft = marginLeft;
		layout.verticalSpacing = verticalSpacing;
		layout.horizontalSpacing = 2;
		setBackground(Constants.COLOR_BACKGROUND);
		setLayout(layout);

		insertWidget = new InsertWidget(this, true, type, tokenAccept);
		insertWidget.setHideMode();
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

//	void setDeleteAction(Consumer<Integer> action) {
//		deleteAction = action;
//	}

	void insertLineAt(Control location) {
		InsertWidget insert = insertWidget.copyTo(this);
		insert.moveAbove(location);
		requestLayout();
		insert.setFocus();
	}
	
	void insertLineAfter(Control location) {
		InsertWidget insert = insertWidget.copyTo(this);
		insert.moveBelow(location);
		requestLayout();
		insert.setFocus();
	}

	public int findModelIndex(Control location) {
		int i = 0;
		for (Control c : getChildren()) {
			if (c == location)
				return i;

			if(!(c instanceof InsertWidget))
				i++;
		}
		assert false;
		return -1;
	}


	int totalElements() {
		Control[] children = getChildren();
		int inserts = 0;
		for (Control c : children)
			if(c instanceof InsertWidget)
				inserts++;

		return children.length - inserts;
	}


	public void addAction(InsertWidget.Action a) {
		insertWidget.addAction(a);
	}

	public void addActions(List<? extends InsertWidget.Action> actions) {
		for(InsertWidget.Action a : actions)
			insertWidget.addAction(a);
	}


	public <T extends EditorWidget> T addLineAndElement(Function<Composite, T> f) {
		T e = addElement(f, totalElements());
		insertLineAt(e);
		return e;
	}
	
	public <T extends EditorWidget> T addElement(Function<Composite, T> f) {
		return addElement(f, totalElements());
	}

	public <T extends EditorWidget> T addElement(Function<Composite, T> f, int modelIndex) {
		Control el = viewElement(modelIndex);
		T w = f.apply(this);
		w.moveAbove(el);
		return w;
	}
	
	private Control viewElement(int modelIndex) {
		assert modelIndex < getChildren().length : modelIndex;
		int inserts = 0;
		Control[] children = getChildren();
		for(int i = 0, m = 0; m <= modelIndex && i < children.length; i++)
			if(children[i] instanceof InsertWidget)
				inserts++;
			else
				m++;
		
		int v = modelIndex + inserts;
		return v < children.length ? children[v] : insertWidget;
	}

	public void removeElement(Object e) {
		Control[] children = getChildren();
		for (int i = 0; i < children.length-1; i++) {
			if (children[i] instanceof EditorWidget && ((EditorWidget) children[i]).element == e) {
				children[i].dispose();
				children[i+1].setFocus();
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

	public void focusLast() {
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
}
