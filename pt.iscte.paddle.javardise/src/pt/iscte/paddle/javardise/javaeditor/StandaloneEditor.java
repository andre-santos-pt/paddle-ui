package pt.iscte.paddle.javardise.javaeditor;

import static pt.iscte.paddle.javardise.javaeditor.UiMode.Syntax.ASSIGNMENT;
import static pt.iscte.paddle.javardise.javaeditor.UiMode.Syntax.CALLS;
import static pt.iscte.paddle.javardise.javaeditor.UiMode.Syntax.SELECTION;
import static pt.iscte.paddle.javardise.javaeditor.UiMode.Syntax.WHILE_LOOP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

//import javax.tools.DiagnosticCollector;
//import javax.tools.JavaCompiler;
//import javax.tools.JavaFileObject;
//import javax.tools.StandardJavaFileManager;
//import javax.tools.StandardLocation;
//import javax.tools.ToolProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.ExpressionChain;
import pt.iscte.paddle.javardise.InsertWidget;
import pt.iscte.paddle.javardise.SequenceWidget;
import pt.iscte.paddle.javardise.javaeditor.UiMode.Syntax;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.commands.ICommand;
import pt.iscte.paddle.model.javaparser.Java2Paddle;

public class StandaloneEditor {
	private IModule module;
	private ClassWidget classWidget;
	private Shell shell;

	public StandaloneEditor(File file) {
		if(file.exists() && (!file.isFile() || !file.getName().endsWith(".java"))) {
			throw new RuntimeException("The provided argument is not a .java file");
		}

		String className = file.getName().substring(0, file.getName().indexOf('.'));

		if(file.exists()) {
			Java2Paddle parser = new Java2Paddle(file);
			try {
				module = parser.parse();
			} catch (IOException e) {
				e.printStackTrace();
				module = IModule.create();
				module.setId(className);
			}
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
	}

	public StandaloneEditor(IModule module) {
		if(module == null)
			throw new IllegalArgumentException("null");
		this.module = module;
	}


	public Shell launch(Display display) {
		//		Display.setAppName("Javardise");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Javardise");
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

		SequenceWidget seq = new SequenceWidget(area, 0, Constants.METHOD_SPACING, true,token -> Keyword.isClassModifier(token));
		seq.addAction(new InsertWidget.Action("class") {
			public boolean isEnabled(char c, ExpressionChain id, int index, int caret, int selection, List<String> tokens) {
				return seq.getChildren().length == 1 && c == SWT.SPACE && id.getText().equals(Keyword.CLASS.keyword());
			}

			public void run(char c, ExpressionChain id, int index, int caret, int selection, List<String> tokens) {
				classWidget = seq.addElement(p -> new ClassWidget(p, module, module.getId(), Keyword.match(seq.getInsertTokens())), 0);
				classWidget.setFocus();
			}
		});

		module.addListener(new IModule.IListener() {
			public void commandExecuted(ICommand<?> command) {
				if(!shell.getText().startsWith("*"))
					shell.setText("*" + shell.getText());
			}
		});

		Menu menu = new Menu(seq);
		seq.setMenu(menu);
		MenuItem subSyntax = new MenuItem(menu, SWT.CASCADE);		
		subSyntax.setText("Syntax");
		Menu syntaxMenu = new Menu(menu);
		subSyntax.setMenu(syntaxMenu);

		for(SyntaxLevel level : SyntaxLevel.values())
			level.createMenuItem(syntaxMenu);

		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event event) {
				if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL && event.keyCode == 's') {
					File f = saveToFile();
					compile(f);
					if(shell.getText().startsWith("*"))
						shell.setText(shell.getText().substring(1));
				}
			}
		});

		shell.setText(module.getId() + ".java");
		seq.addElement(p -> classWidget = new ClassWidget(p, module, module.getId()));
		classWidget.setFocus();
		shell.setSize(600, 800);
		shell.open();
		return shell;
	}

	enum SyntaxLevel {
		RECURSIVE("Recursion", true, SELECTION, CALLS),
		LOOPS("Loops", true, WHILE_LOOP, ASSIGNMENT),
		ARRAYS("Arrays", true, Syntax.ARRAYS),
		OBJECTS("Objects", false, Syntax.RECORDS),
		ENCAPSULATION("Encapsulation", false, Syntax.ENCAPSULATION);

		String uiText;
		boolean selected;
		Syntax[] elements;
		
		SyntaxLevel(String uiText, boolean selected, Syntax ... elements) {
			this.uiText = uiText;
			this.selected = selected;
			this.elements = elements;
			if(selected)
				UiMode.addSyntax(elements);
			else
				UiMode.removeSyntax(elements);
		}

		void createMenuItem(Menu parent) {
			MenuItem item = new MenuItem(parent, SWT.CHECK);
			item.setText(uiText);
			item.setSelection(selected);
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(item.getSelection())
						UiMode.addSyntax(elements);
					else
						UiMode.removeSyntax(elements);
				}
			});
		}
	}

	public File saveToFile() {
		File file = new File(module.getId() + ".java");
		saveToFile(file);
		return file;
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


	//	public boolean compile(File destinationDir) {
	//		StringBuffer buffer = new StringBuffer();
	//		classWidget.toCode(buffer, 0);
	//		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	//	
	//		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
	//		JavaStringObject stringObject = new JavaStringObject("Test", buffer);
	//		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
	//		try {
	//			fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(destinationDir));
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		JavaCompiler.CompilationTask task = compiler.getTask(null,
	//				fileManager, diagnostics, null, null, Arrays.asList(stringObject));
	//		if (!task.call()) {
	//			diagnostics.getDiagnostics().forEach(d -> System.err.println(d));
	//			return false;
	//		}
	//		else
	//			return true;
	//	}

	public boolean compile(File f) {
//		JavaParser parser = new JavaParser(f);
//		try {
//			parser.parse();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return parser.hasParseProblems();
		return false;
	}

	public static void main(String[] args) {
		Display display = new Display();
		String filePath;
		if(args.length == 0) {
			FileDialog fileDialog = new FileDialog(new Shell(display));
			fileDialog.setText("Select File");
			fileDialog.setFilterExtensions(new String[] { "*.java" });
			fileDialog.setFilterNames(new String[] { "Java files (*.java)" });
			filePath = fileDialog.open();
		}
		else
			filePath = args[0];
		
		StandaloneEditor editor = new StandaloneEditor(new File(filePath));
		Shell shell = editor.launch(display);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}	
		display.dispose();
	}


}