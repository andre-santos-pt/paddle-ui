package pt.iscte.paddle.javardise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
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

import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProgramElement;


public class StandaloneEditor {
	private IModule module;
	private ClassWidget classWidget;
	private UiMode mode;
	private Shell shell;

	public StandaloneEditor(File file) {
		if(file.exists() && (!file.isFile() || !file.getName().endsWith(".java"))) {
			throw new RuntimeException("The provided argument is not a .java file");
		}

		String className = file.getName().substring(0, file.getName().indexOf('.'));
		
		if(file.exists()) {
			// TODO load model
			module = IModule.create();
			module.setId(className);
		}
		else {
			try {
				if(!file.createNewFile())
					throw new RuntimeException("Could not create file " + file.getAbsolutePath());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
			
			module = IModule.create();
			module.setId(className);
		}
		mode = new UiMode();
	}

	public StandaloneEditor(IModule module) {
		if(module == null)
			throw new IllegalArgumentException("null");
		this.module = module;
	}


	public Shell launch(Display display) {
		shell = new Shell(display);
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
				Point size = area.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				size.x += 100;
				size.y += 100;
				scroll.setMinSize(size);
				scroll.requestLayout();
			}
		});		

		SequenceWidget seq = new SequenceWidget(area, 0, Constants.METHOD_SPACING, token -> Keyword.isClassModifier(token));
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
					saveToFile();
					compile();
				}
			}
		});
		
		shell.setText(module.getId() + ".java");
		seq.addWidget(p -> classWidget = new ClassWidget(p, module, mode));
		shell.setSize(600, 800);
		shell.open();
		return shell;
	}

	public void saveToFile() {
		saveToFile(new File(module.getId() + ".java"));
	}
	
	public void saveToFile(File file) {
		StringBuffer buffer = new StringBuffer();
		classWidget.toCode(buffer, 0);
		try {
		
			PrintWriter w = new PrintWriter(file);
			w.append(buffer);
			w.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean compile() {
		StringBuffer buffer = new StringBuffer();
		classWidget.toCode(buffer, 0);
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
			
			return false;
		}
		else
			System.out.println("compilation success");
			return true;
	}
	
	
	public void mark(IProgramElement s, Color color) {
		classWidget.mark(s, color);
	}
	
	public void removeAllMarks() {
		classWidget.removeAllMarks();
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			System.err.println("Please provide a file as argument");
		}
		else {
			StandaloneEditor editor = new StandaloneEditor(new File(args[0]));
			Display display = new Display();
			Shell shell = editor.launch(display);
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();
		}
	}

	
}