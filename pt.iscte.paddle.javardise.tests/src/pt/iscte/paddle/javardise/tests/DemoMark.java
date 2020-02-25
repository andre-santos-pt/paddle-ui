package pt.iscte.paddle.javardise.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.ClassWidget;
import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.Decoration;
import pt.iscte.paddle.javardise.MarkerService;
import pt.iscte.paddle.javardise.MarkerService.Mark;
import pt.iscte.paddle.javardise.util.HyperlinkedText;
import pt.iscte.paddle.model.IBlock.IVisitor;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.tests.TestNaturals;

public class DemoMark {

	private static Shell shell;

	//	static Button createButton(String text, Runnable runnable) 

	public static void main(String[] args) {

		TestNaturals t = new TestNaturals();
		t.setup();
		IModule module = t.getModule();
		IProcedure proc = module.getProcedure("naturals");

		Display display = new Display();
		shell = new Shell(display);
		shell.setBackground(Constants.COLOR_BACKGROUND);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 50;
		layout.marginLeft = 50;
		layout.verticalSpacing = 20;
		shell.setLayout(layout);


		ClassWidget widget = new ClassWidget(shell, module);
		widget.setEnabled(false);

		List<IBlockElement> children = proc.getBody().getChildren();

		Composite comp = new Composite(shell, SWT.BORDER);
		comp.setLayout(new FillLayout());

		Button next = new Button(comp, SWT.PUSH);
		next.setText("next");
		next.addSelectionListener(new SelectionAdapter() {
			Color red = new Color (display, 255, 0, 0);
			Iterator<IBlockElement> it = children.iterator();
			IBlockElement element = it.next();
			Mark mark;
			public void widgetSelected(SelectionEvent e) {
				if(mark != null)
					mark.unmark();

				mark = MarkerService.mark(red, element);
				if(it.hasNext())
					element = it.next();
			}
		});

		// --------------

		Image arrow = new Image(display, "arrow.png");
		List<Decoration> decAssignments = new ArrayList<>();

		Button markAssignment = new Button(comp, SWT.PUSH);
		markAssignment.setText("mark assignments");
		markAssignment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(decAssignments.isEmpty()) {
					proc.accept(new IVisitor() {
						public boolean visit(IVariableAssignment a) {
							Decoration d = MarkerService.addDecoration(a, arrow, Decoration.Location.LEFT);
							decAssignments.add(d);
							return true;
						}
					});
				}
				decAssignments.forEach(d -> d.show());
			}
		});


		Button removeMarkAssignment = new Button(comp, SWT.PUSH);
		removeMarkAssignment.setText("remove mark assignments");
		removeMarkAssignment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				decAssignments.forEach(d -> d.hide());
			}
		});


		// --------------

		List<Decoration> returnMarks = new ArrayList<>();

		Button markReturn = new Button(comp, SWT.PUSH);
		markReturn.setText("mark return");
		markReturn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(returnMarks.isEmpty()) {
					proc.accept(new IVisitor() {
						public boolean visit(IReturn r) {
							Decoration d = MarkerService.addDecoration(r, "this is the end", Decoration.Location.RIGHT);
							returnMarks.add(d);
							return true;
						}
					});
				}
				returnMarks.forEach(d -> d.show());
			}
		});

		Button removeReturnMarks = new Button(comp, SWT.PUSH);
		removeReturnMarks.setText("delete return marks");
		removeReturnMarks.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				returnMarks.forEach(d -> d.delete());
				returnMarks.clear();
			}
		});



		// --------------



		Color blue = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);

		Link text = new HyperlinkedText(e -> MarkerService.mark(blue, e))
				.line("hi")
				.newline()
				.words("some words ").link("and some multi-link", proc.getVariables())
				.newline()
				.words("and yet a ").link("single link", proc)
				.newline()
				.link("action link", () -> showDialog())
				.create(shell, SWT.BORDER);


		// --------------


		shell.pack();
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
				new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
		dialog.setText("My info");
		dialog.setMessage("Do you really want to do this?");

		dialog.open();
	}

}
