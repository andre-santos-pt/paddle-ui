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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.service.IClassWidget;
import pt.iscte.paddle.javardise.service.ICodeDecoration;
import pt.iscte.paddle.javardise.service.IDeclarationWidget;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.javardise.util.HyperlinkedText;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.IBlock.IVisitor;
import pt.iscte.paddle.model.IBlockElement;
import pt.iscte.paddle.model.IExpression;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.tests.TestInvert;
import pt.iscte.paddle.model.tests.TestNaturals;
import pt.iscte.pidesco.cfgviewer.ext.CFGViewer;

public class DemoCodeDecorations {

	private static Shell shell;

	public static void main(String[] args) {

		TestNaturals t = new TestNaturals();
		t.setup();
		IModule module = t.getModule();
		IProcedure proc = module.getProcedure("naturals");

		IProcedure main = module.addProcedure("main", IType.VOID);
		IVariableDeclaration nats = main.getBody().addVariable(IType.INT.array().reference());
		nats.setId("nats");
		main.getBody().addAssignment(nats, proc.expression(IType.INT.literal(5)));
		main.getBody().addAssignment(nats, IType.INT.literal(10));
	
		Display display = new Display();
		shell = new Shell(display);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 50;
		layout.marginLeft = 50;
		layout.verticalSpacing = 20;
		shell.setLayout(layout);


		Composite codeAndCFG = new Composite(shell, SWT.NONE);
		codeAndCFG.setLayout(new GridLayout(2, true));
		
		IClassWidget widget = IJavardiseService.createClassWidget(codeAndCFG, module);
		widget.setReadOnly(true);

		CFGViewer cfg = new CFGViewer(codeAndCFG);
		cfg.setInput(proc.getCFG().getNodes());
		cfg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createMarksGroup(proc, display, widget);
		createOtherGroup(proc, display);

		Color blue = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);

		Link text = new HyperlinkedText(e -> e.forEach(e2 -> IJavardiseService.getWidget(e2).addMark(blue).show()))
				.line("hi")
				.newline()
				.words("some words ").link("and some multi-link to mark variables", proc.getVariables())
				.newline()
				.words("and yet a ").link("single link to mark the procedure", proc)
				.newline()
				.link("action link", () -> showDialog())
				.create(shell, SWT.BORDER);
		
//		Button arrowMarks = new Button(groupMarks, SWT.PUSH);
//		arrowMarks.setText("arrow marks");
//		arrowMarks.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				IWidget a = IJavardiseService.getWidget(proc.getBody().getChildren().get(0));
//				IWidget b = IJavardiseService.getWidget(proc.getBody().getChildren().get(3));
//				IWidget c = IJavardiseService.getWidget(proc.getBody().getChildren().get(5));
//				//				a.addArrow(b).show();
//				c.addArrow(b).show();
//
//			}
//		});

		//		Button markExpressions = new Button(comp, SWT.PUSH);
		//		markExpressions.setText("mark expressions");
		//		markExpressions.addSelectionListener(new SelectionAdapter() {
		//			Color blue = new Color (display, 0, 0, 255);
		//			public void widgetSelected(SelectionEvent e) {
		//				proc.accept(new IVisitor() {
		//					public boolean visit(IReturn r) {
		//						DecorationService.addMark(r.getExpression(), blue).show();
		//						return true;
		//					}
		//					public boolean visit(IVariableAssignment assignment) {
		//						DecorationService.addMark(assignment.getExpression(), blue).show();
		//						return true;
		//					}
		//					public boolean visit(IArrayElementAssignment assignment) {
		//						DecorationService.addMark(assignment.getExpression(), blue).show();
		//						return true;
		//					}
		//					public boolean visit(ILoop loop) {
		//						DecorationService.addMark(loop.getGuard(), blue).show();
		//						return true;
		//					}
		//					public boolean visit(ISelection sel) {
		//						DecorationService.addMark(sel.getGuard(), blue).show();
		//						return true;
		//					}
		//				});
		//			}
		//		});

	

	




		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private static void createOtherGroup(IProcedure proc, Display display) {
		Group groupOther = new Group(shell, SWT.BORDER);
		groupOther.setText("other");
		groupOther.setLayout(new FillLayout());
		
		Image arrow = new Image(display, "arrow.png");
		List<ICodeDecoration<Label>> decAssignments = new ArrayList<>();

		Button markAssignment = new Button(groupOther, SWT.PUSH);
		markAssignment.setText("mark assignments");
		markAssignment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(decAssignments.isEmpty()) {
					proc.accept(new IVisitor() {
						public boolean visit(IVariableAssignment a) {
							IWidget w = IJavardiseService.getWidget(a);
							ICodeDecoration<Label> d = w.addImage(arrow, ICodeDecoration.Location.LEFT);
							decAssignments.add(d);
							return true;
						}
					});
				}
				decAssignments.forEach(d -> d.show());
			}
		});


		Button removeMarkAssignment = new Button(groupOther, SWT.PUSH);
		removeMarkAssignment.setText("remove mark assignments");
		removeMarkAssignment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				decAssignments.forEach(d -> d.hide());
			}
		});


		// --------------

		List<ICodeDecoration<Text>> returnMarks = new ArrayList<>();

		Button markReturn = new Button(groupOther, SWT.PUSH);
		markReturn.setText("mark return");
		markReturn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(returnMarks.isEmpty()) {
					proc.accept(new IVisitor() {
						public boolean visit(IReturn r) {
							IWidget w = IJavardiseService.getWidget(r);
							ICodeDecoration<Text> d = w.addNote("this is the end", ICodeDecoration.Location.RIGHT);
							returnMarks.add(d);
							return true;
						}
					});
				}
				returnMarks.forEach(d -> d.show());
			}
		});

		Button removeReturnMarks = new Button(groupOther, SWT.PUSH);
		removeReturnMarks.setText("delete return marks");
		removeReturnMarks.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				returnMarks.forEach(d -> d.delete());
				returnMarks.clear();
			}
		});
	}

	private static void createMarksGroup(IProcedure proc, Display display, IClassWidget widget) {
		Group groupMarks = new Group(shell, SWT.BORDER);
		groupMarks.setText("marks");
		groupMarks.setLayout(new FillLayout());
		groupMarks.setFocus();

		Button statementsButton = new Button(groupMarks, SWT.PUSH);
		statementsButton.setText("statements next");
		statementsButton.addSelectionListener(new SelectionAdapter() {
			Color red = new Color (display, 255, 0, 0);
			Iterator<IBlockElement> it = proc.getBody().deepIterator();
			IBlockElement element = it.hasNext() ? it.next() : null;
			ICodeDecoration<Canvas> mark;
			public void widgetSelected(SelectionEvent e) {
				if(mark != null)
					mark.delete();

				if(element != null) {
					IWidget w = IJavardiseService.getWidget(element);
					mark = w.addMark(red);
					mark.show();
				}
				element = it.hasNext() ? it.next() : null;	
			}
		});

		Button expressionsButton = new Button(groupMarks, SWT.PUSH);
		expressionsButton.setText("expressions");
		expressionsButton.addSelectionListener(new SelectionAdapter() {
			Color blue = new Color (display, 0, 0, 255);
			Color green = new Color (display, 0, 255, 0);
			public void widgetSelected(SelectionEvent e) {
				proc.accept(new IBlock.IVisitor() {
					public void visitAny(IExpression exp) {
						IWidget w = IJavardiseService.getWidget(exp);
						if(w != null)
							w.addMark(exp.isSimple() ? green : blue).show();
						else
							System.err.println(exp);
					}
				});
			}
		});
		
		Button regionMark = new Button(groupMarks, SWT.PUSH);
		regionMark.setText("region mark");
		regionMark.addSelectionListener(new SelectionAdapter() {
			Color cyan = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
			public void widgetSelected(SelectionEvent e) {
				IDeclarationWidget a = IJavardiseService.getDeclarationWidget(proc.getVariables().get(1));
				IWidget w1 = IJavardiseService.getWidget(proc.getBody().getChildren().get(1));
				IWidget w2 = IJavardiseService.getWidget(proc.getBody().getChildren().get(2));
				a.addRegionMark(cyan, w1, w2).show();
			}
		});

		Button otherMarks = new Button(groupMarks, SWT.PUSH);
		otherMarks.setText("detail marks");
		otherMarks.addSelectionListener(new SelectionAdapter() {
			Color cyan = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
			Color magenta = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
			public void widgetSelected(SelectionEvent e) {
				ICodeDecoration<Canvas> dec = widget.getClassName().addMark(cyan);
				dec.show();
				widget.getProcedure(proc).getReturnType().addMark(cyan).show();
				widget.getProcedure(proc).getMethodName().addMark(magenta).show();
				IDeclarationWidget decDec = IJavardiseService.getDeclarationWidget(proc.getParameters().get(0));
				decDec.getVariableType().addMark(cyan).show();
				decDec.getVariableName().addMark(magenta).show();
			}
		});
	}

	static void showDialog() {
		MessageBox dialog =
				new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
		dialog.setText("My info");
		dialog.setMessage("Do you really want to do this?");

		dialog.open();
	}

}
