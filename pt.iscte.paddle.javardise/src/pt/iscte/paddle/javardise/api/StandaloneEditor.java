package pt.iscte.paddle.javardise.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.ILanguageConfiguration;
import pt.iscte.paddle.javardise.SequenceWidget;

public class StandaloneEditor {
	private Shell shell;
	private File file;

	private static final String ext = "." + IEditorConfiguration.INSTANCE.getExtension();
	private SequenceWidget seq;
	
	public StandaloneEditor(File file) {
		if(file.exists() && (!file.isFile() || !file.getName().endsWith(ext))) {
			throw new RuntimeException("The provided argument is not a " + ext + " file");
		}
		this.file = file;
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

		seq = new SequenceWidget(area, 0, false, token -> false);
		ILanguageConfiguration.INSTANCE.configureRoot(file, seq);

		Button b = new Button(shell, SWT.PUSH);
		b.setText("save");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					save(file);
				} catch (FileNotFoundException e1) {
					System.err.println("could not save file");
				}	
			}
		});
		shell.setText(file.getName());
		shell.setSize(600, 800);
		shell.open();
		return shell;
	}


	public void save(File file) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(file);
		writer.append(EditorWidget.getSource(seq));
		writer.close();

	}


	public static void main(String[] args) {
		Display display = new Display();
		String filePath;
		if(args.length == 0) {
			FileDialog fileDialog = new FileDialog(new Shell(display));
			fileDialog.setText("Select File");
			fileDialog.setFilterExtensions(new String[] { "*" + ext });
			fileDialog.setFilterNames(new String[] { "(*" + ext + ")"});
			filePath = fileDialog.open();
		}
		else
			filePath = args[0];

		if(filePath != null) {
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


}