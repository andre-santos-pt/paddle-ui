package pt.iscte.paddle.javardise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.ClassWidget;
import pt.iscte.paddle.javardise.ComplexId;
import pt.iscte.paddle.javardise.Keyword;
import pt.iscte.paddle.javardise.NewInsertWidget;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.UiMode;
import pt.iscte.paddle.model.IModule;


public class StandaloneEditor {
	private File file;
	private ClassWidget classWidget;

	public StandaloneEditor(File file) {
		this.file = file;
	}

	public void launch() {
		Display display = new Display();
		Shell shell = new Shell(display);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.wrap = false;
		layout.marginLeft = 10;
		shell.setLayout(new GridLayout());

		shell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		ScrolledComposite scroll = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setLayout(new GridLayout(1, false));
		scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite area = new Composite(scroll, SWT.NONE);
		area.setLayout(new FillLayout());

		scroll.setContent(area);
		scroll.setMinSize(100, 100);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		area.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				scroll.setMinSize(area.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				scroll.requestLayout();
			}
		});		

		String className = file.getName();
		className = className.substring(0, className.indexOf('.'));

		UiMode mode = new UiMode();
		IModule module = IModule.create();
		module.setId(className);
//		IProcedure proc = module.addProcedure("test", IType.VOID);
//		IVariable param = proc.addParameter(IType.INT);
//		proc.getBody().addAssignment(param, IType.INT.literal(3)); // not working

		SequenceWidget seq = new SequenceWidget(area, 0, token -> Keyword.isClassModifier(token));
		seq.addAction(new NewInsertWidget.Action("class", 'c') {
			public boolean isEnabled(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				return seq.getChildren().length == 1 && c == SWT.SPACE && id.isKeyword(Keyword.CLASS);
			}

			public void run(char c, ComplexId id, int index, int caret, int selection, List<String> tokens) {
				classWidget = seq.addWidget(p -> new ClassWidget(p, module, mode, Keyword.array(seq.getInsertTokens())));		
				classWidget.setFocus();
			}
		});

		Menu menu = new Menu(seq);
		seq.setMenu(menu);
		MenuItem subSyntax = new MenuItem(menu, SWT.CASCADE);		
		subSyntax.setText("Syntax");

		Menu syntaxMenu = new Menu(menu);
		subSyntax.setMenu(syntaxMenu);
		MenuItem loop = new MenuItem(syntaxMenu, SWT.CHECK); 
		loop.setText("Loops");
		MenuItem simp = new MenuItem(syntaxMenu, SWT.CHECK); 
		simp.setText("Simplified assignments");
		MenuItem brackets = new MenuItem(syntaxMenu, SWT.CHECK); 
		brackets.setText("Optional brackets");
		MenuItem encap = new MenuItem(syntaxMenu, SWT.CHECK); 
		encap.setText("Encapsulation");

		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL && event.keyCode == 's') {
					serialize();
				}
			}
		});

		shell.setText(file.getName());
		seq.addWidget(p -> classWidget = new ClassWidget(p, module, mode));
		shell.setSize(600, 800);
		shell.open();

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in the event queue
				display.sleep();
			}
		}
		display.dispose();
	}

	private void serialize() {
		StringBuffer buffer = new StringBuffer();
		classWidget.toCode(buffer, 0);
		try {
			PrintWriter w = new PrintWriter(file);
			w.append(buffer);
			w.close();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
			 JavaStringObject stringObject = new JavaStringObject("Test", buffer);
			JavaCompiler.CompilationTask task = compiler.getTask(null,
					null, diagnostics, null, null, Arrays.asList(stringObject));
			//				DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
//			if(compiler.run(null, null, null, file.getPath()) == 0)
//				System.out.println("compilation success");
			if (!task.call()) {
	            
				diagnostics.getDiagnostics().forEach(d -> System.err.println(d));
	            
	        }
			else
				System.out.println("compilation success");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length == 0) {
			System.err.println("Please provide a file as argument");
			return;
		}

		File file = new File(args[0]);
		if(!file.exists() || !file.isFile()) {
			System.err.println("The provided argument is not a file");
			return;
		}

		if(!(file.getName().endsWith(".java"))) {
			System.err.println("The provided file must have extension .java");
			return;
		}

		StandaloneEditor editor = new StandaloneEditor(file);
		editor.launch();
	}

	//	private static ClassWidget instantiationExample(IModule module, Composite area, UiMode mode) {
	//		IConstant pi = module.addConstant(DOUBLE, DOUBLE.literal(3.14));
	//		pi.setId("PI");
	//		
	//		IProcedure max = module.addProcedure(INT);
	//		IVariable array = max.addParameter(INT.array());
	//		IBlock body = max.getBody();
	//		IVariable m = body.addVariable(INT);
	//		IVariableAssignment mAss = body.addAssignment(m, array.element(INT.literal(0)));
	//		IVariable i = body.addVariable(INT);
	//		IVariableAssignment iAss = body.addAssignment(i, INT.literal(1));
	//		ILoop loop = body.addLoop(SMALLER.on(i, array.length()));
	//		ISelection ifstat = loop.addSelection(GREATER.on(array.element(i), m));
	//		IVariableAssignment mAss_ = ifstat.addAssignment(m, array.element(i));
	//		IVariableAssignment iInc = loop.addIncrement(i);
	//		IReturn ret = body.addReturn(m);
	//		
	//		max.setId("max");
	//		array.setId("array");
	//		m.setId("m");
	//		i.setId("i");



	//		l.createCall("proc");
	//		WhileWidget l2 = l.createLoop("false");
	//		ReturnWidget createReturn = l2.createReturn("true");
	//		return c;
	//	}
}