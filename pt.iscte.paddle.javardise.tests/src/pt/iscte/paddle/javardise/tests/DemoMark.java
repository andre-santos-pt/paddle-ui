package pt.iscte.paddle.javardise.tests;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.MethodWidget;
import pt.iscte.paddle.javardise.Selectable;
import pt.iscte.paddle.model.IBlock.IVisitor;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.tests.TestArrayCount;

public class DemoMark {

	public static void main(String[] args) {

		TestArrayCount t = new TestArrayCount();
		t.setup();
		IModule module = t.getModule();
		
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		Color red = new Color (display, 255, 0, 0);
		
		IProcedure proc = module.getProcedure("count");
		MethodWidget widget = new MethodWidget(shell, proc);		
		List<IBlockElement> children = proc.getBody().getChildren();
		
		Composite comp = new Composite(shell, SWT.BORDER);
		comp.setLayout(new RowLayout());
				
		// mark iterator
		Button next = new Button(comp, SWT.PUSH);
		next.setText("next");
		next.addSelectionListener(new SelectionAdapter() {
			Iterator<IBlockElement> it = children.iterator();
			IBlockElement element = it.next();
			IBlockElement prev = null;
			public void widgetSelected(SelectionEvent e) {
				Selectable.mark(element, red);
				if(prev != null)
					Selectable.unmark(prev);
				prev = element;
				if(it.hasNext())
					element = it.next();
			}
		});
		
		Button markAssignment = new Button(comp, SWT.PUSH);
		markAssignment.setText("mark assignments");
		markAssignment.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				proc.accept(new IVisitor() {
					@Override
					public boolean visit(IVariableAssignment a) {
						Selectable.addNote(a, "This changes the value of variable " + a.getTarget().getId());
						return true;
					}
				});
			}
		});
		
		Image arrow = new Image(display, "arrow.png");
		
		Button markReturn = new Button(comp, SWT.PUSH);
		markReturn.setText("mark return");
		markReturn.addSelectionListener(new SelectionAdapter() {
			Selectable.Icon icon;
			
			public void widgetSelected(SelectionEvent e) {
				proc.accept(new IVisitor() {
					@Override
					public boolean visit(IReturn r) {
						if(icon == null)
							icon = Selectable.addIcon(r, arrow, "that's the end");
						else {
							icon.remove();
							icon = null;
						}
						return true;
					}
				});
			}
		});
		shell.open();
		
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

}
