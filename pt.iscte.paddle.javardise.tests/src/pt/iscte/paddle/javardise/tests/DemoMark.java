package pt.iscte.paddle.javardise.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.MethodWidget;
import pt.iscte.paddle.javardise.util.HyperlinkedText;
import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.Decoration;
import pt.iscte.paddle.javardise.MarkerService;
import pt.iscte.paddle.model.IBlock.IVisitor;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IRecordFieldAssignment;
import pt.iscte.paddle.model.IRecordFieldExpression;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariable;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.tests.TestArrayCount;
import pt.iscte.paddle.model.tests.TestMax;

public class DemoMark {

	public static void main(String[] args) {

		TestArrayCount t = new TestArrayCount();
//		TestMax t = new TestMax();
		t.setup();
		IModule module = t.getModule();
		IProcedure proc = module.getProcedure("count");
		
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setBackground(Constants.COLOR_BACKGROUND);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 50;
		layout.marginLeft = 50;
		layout.verticalSpacing = 20;
		shell.setLayout(layout);
		
		
		MethodWidget widget = new MethodWidget(shell, proc);
		widget.setEnabled(false);
		List<IBlockElement> children = proc.getBody().getChildren();
		
		Composite comp = new Composite(shell, SWT.BORDER);
		comp.setLayout(new FillLayout());
				
		// mark iterator
		Button next = new Button(comp, SWT.PUSH);
		next.setText("next");
		next.addSelectionListener(new SelectionAdapter() {
			Color red = new Color (display, 255, 0, 0);
			Iterator<IBlockElement> it = children.iterator();
			IBlockElement element = it.next();
			IBlockElement prev = null;
			public void widgetSelected(SelectionEvent e) {
				MarkerService.mark(element, red);
				if(prev != null)
					MarkerService.unmark(prev);
				prev = element;
				if(it.hasNext())
					element = it.next();
			}
		});
		
		Button markAssignment = new Button(comp, SWT.PUSH);
		markAssignment.setText("mark assignments");
		markAssignment.addSelectionListener(new SelectionAdapter() {
			Image arrow = new Image(display, "arrow.png");
			public void widgetSelected(SelectionEvent e) {
				proc.accept(new IVisitor() {
					public boolean visit(IVariableAssignment a) {
						Decoration d = MarkerService.addDecoration(a, arrow, Decoration.Location.LEFT);
						d.show();
						return true;
					}
				});
			}
		});
		
		List<Decoration> returnMarks = new ArrayList<>();
		
		Button markReturn = new Button(comp, SWT.PUSH);
		markReturn.setText("mark return");
		markReturn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				proc.accept(new IVisitor() {
					public boolean visit(IReturn r) {
						Decoration d = MarkerService.addDecoration(r, "this is the end", Decoration.Location.RIGHT);
						d.show();
						returnMarks.add(d);
						return true;
					}
				});
			}
		});
		
		Button removeReturnMarks = new Button(comp, SWT.PUSH);
		removeReturnMarks.setText("remove return marks");
		removeReturnMarks.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				returnMarks.forEach(d -> d.delete());
			}
		});
		
		
		Color blue = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		
		Link text = new HyperlinkedText(e -> MarkerService.mark(e, blue))
		.line("hi")
		.newline()
		.words("some words ").link("and some multi-link", proc.getVariables())
		.newline()
		.words("and yet a ").link("single link", proc)
		.newline()
		.link("action link", () -> showDialog())
		.create(shell, SWT.BORDER);
		

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
	
	static void showDialog() {
		MessageBox dialog =
			    new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
			dialog.setText("My info");
			dialog.setMessage("Do you really want to do this?");
			
			dialog.open();
	}

}
