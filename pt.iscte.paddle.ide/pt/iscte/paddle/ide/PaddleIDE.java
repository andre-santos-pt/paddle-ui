package pt.iscte.paddle.ide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import pt.iscte.paddle.ide.service.IPaddleService;
import pt.iscte.paddle.ide.service.IPaddleTool;
import pt.iscte.paddle.ide.service.IPaddleView;
import pt.iscte.paddle.interpreter.IExecutionData;
import pt.iscte.paddle.interpreter.IMachine;
import pt.iscte.paddle.interpreter.IProgramState;
import pt.iscte.paddle.interpreter.IValue;
import pt.iscte.paddle.javaeditor.api.IClassWidget;
import pt.iscte.paddle.javaeditor.api.IJavardiseService;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IRecordType;
import pt.iscte.paddle.model.javaparser.Java2Paddle;

public class PaddleIDE implements IPaddleService
{
	private static Display display;
	//	private static PaddleIDE instance;

	private Shell shell;
	private TabFolder modulesFolder;
	private ToolBar toolBar;

	private File workspace;
	private IModule module;
	private List<Consumer<String>> moduleChangeListeners;
	private final NewFileTool newFileTool;
	private final  SaveTool saveTool;
	private final RunTool runTool;

	public static void main (String [] args) {
		display = new Display ();
		boolean ok = false;
		File workspace = null;
		do {
			DirectoryDialog dialog = new DirectoryDialog(new Shell(display));
			dialog.setFilterPath(System.getProperty("user.dir"));
			String filePath = dialog.open();
			if(filePath == null)
				return;
			workspace = new File(filePath);
			ok = workspace.canRead() && workspace.canWrite();
			if(!ok)
				showMessage(SWT.ICON_ERROR, "Read/write permissions", "Make sure you have read and write permissions on the selected folder.");
		}
		while(!ok);
		PaddleIDE ide = new PaddleIDE(workspace);
		ide.open();
	}

	private static void showMessage(int icon, String title, String msg) {
		MessageBox messageBox = new MessageBox(new Shell(display), icon);
		messageBox.setMessage(msg);
		messageBox.setText(title);
		messageBox.open();
	}
	public void open() {
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}


	public PaddleIDE(File root) {
		assert root.isDirectory();
		//		instance = this;
		this.workspace = root;
		shell = new Shell(display);
		shell.setText("Paddle: " + root.getAbsolutePath());
		shell.setLayout(new FillLayout());

		ServiceLoader<IJavardiseService> service = ServiceLoader.load(IJavardiseService.class);
		Optional<IJavardiseService> serv = service.findFirst();
		if(!serv.isPresent())
			System.err.println("could not access Javardise service");
		javarService = serv.get();

		loadModule();

		moduleChangeListeners = new ArrayList<>();

		SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);

		Composite leftArea = new Composite(sashForm, SWT.BORDER);
		GridLayout layout = new GridLayout(1, true);
		layout.marginTop = 0;
		layout.marginLeft = 0;
		leftArea.setLayout(layout);

		toolBar = new ToolBar(leftArea, SWT.BORDER);
		addTools(toolBar, List.of(
				newFileTool = new NewFileTool(),
				saveTool = new SaveTool(),
				runTool = new RunTool()
		));

		SashForm rightArea = new SashForm(sashForm, SWT.VERTICAL);

		insertModules(leftArea);
		createViews(rightArea);
	}

	private void loadModule() {
		try {
			module = new Java2Paddle(workspace).parse();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void addBuiltins(Java2Paddle j2p) {
		j2p.loadBuiltInProcedures(Object.class);
		j2p.loadBuiltInProcedures(String.class);
		j2p.loadBuiltInProcedures(Math.class);
		//		j2p.loadBuiltInProcedures(ImageUtil.class);
		j2p.loadBuiltInProcedures(IllegalArgumentException.class);
		j2p.loadBuiltInProcedures(IllegalStateException.class);
		j2p.loadBuiltInProcedures(NullPointerException.class);

		j2p.loadBuiltInProcedures(PrintStream.class);
		IRecordType system = j2p.addBuiltInRecordType("System");
		IRecordType out = j2p.addBuiltInRecordType("PrintStream");
		out.setNamespace("System");
		system.addField(out, "out");
	}

	private void insertModules(Composite parent) {
		modulesFolder = new TabFolder(parent, SWT.BORDER);
		modulesFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modulesFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moduleChangeListeners.forEach(c -> c.accept((String) modulesFolder.getData()));
			}
		});

		module.getNamespaces().forEach(ns -> insertModule(ns));
	}

	private TabItem insertModule(String namespace) {
		TabItem tabItem = new TabItem(modulesFolder, SWT.NULL);
		tabItem.setText(namespace);
		tabItem.setData(namespace);
		IClassWidget w = javarService.createClassWidgetScroll(modulesFolder, module, namespace, s -> tabItem.setControl(s));
	
		w.addSelectionListener(sel -> {
			if(modulesFolder.getItem(modulesFolder.getSelectionIndex()).getData() == w)
				listeners.forEach(e -> e.accept((IProgramElement) sel.getProgramElement())); // TODO send only not null?
		});	
		tabItem.setData(w);
		return tabItem;
	}

	private Map<Class<?>, IPaddleView> floatingViews = new HashMap<Class<?>, IPaddleView>();
	private void createViews(Composite parent) {
		ServiceLoader<IPaddleView> services = ServiceLoader.load(IPaddleView.class);
		services.forEach(view -> {
			if(view.isFixed()) {
				Group p = new Group(parent, SWT.NONE);
				p.setLayout(new FillLayout());
				p.setText(view.getTitle());
				p.setToolTipText(view.getClass().getModule().getName());
				view.createContents(p, this);
			}
			else
				floatingViews.put(view.getClass(), view);
			new ToolItem(toolBar, SWT.SEPARATOR);
			addTools(toolBar, view.getTools());
		});

	}

	private Map<String, IPaddleTool> toolMap = new HashMap<String, IPaddleTool>();

	private void addTools(ToolBar toolBar, List<IPaddleTool> list) {
		list.forEach(t -> {
			toolMap.put(t.getId(), t);
			ToolItem item = new ToolItem(toolBar, t.isToggle() ? SWT.CHECK : SWT.PUSH);
			item.setText(t.getText());
			item.setToolTipText(t.getTooltip());
			item.setImage(getImage(t.getClass().getModule(), t.getIcon()));
			item.setToolTipText(t.getTooltip());
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					t.execute(item.getSelection(), PaddleIDE.this);
				}
			});
		});
	}

	class NewFileTool implements IPaddleTool {
		@Override
		public String getIcon() {
			return "new.png";
		}

		@Override
		public String getText() {
			return "new file";
		}

		@Override
		public String getTooltip() {
			return "Creates a new file in the workspace";
		}

		@Override
		public void execute(boolean selected, IPaddleService service) {
			Shell dialog = new Shell(display, SWT.TITLE | SWT.APPLICATION_MODAL);
			dialog.setText("File name");
			dialog.setLayout(new FillLayout());
			Text text = new Text(dialog, SWT.BORDER);
			text.addVerifyListener(e -> e.doit = e.character == SWT.BS || e.character >= 'a' && e.character <= 'z' || e.character >= 'A' && e.character <= 'Z');
			text.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CR) {
						File f = new File(service.getWorkspacePath(), text.getText() + ".java");
						try {
							if(!f.exists())
								if(!f.createNewFile())
									System.err.println("could not create " + f);

							String ns = text.getText();
							TabItem tab = insertModule(ns);
							modulesFolder.setSelection(tab);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						dialog.close();
					}
					else if(e.keyCode == SWT.ESC) {
						dialog.close();
					}
				}
			});
			dialog.pack();
			dialog.open();
		}
	}


	class SaveTool implements IPaddleTool {
		@Override
		public String getText() {
			return "save";
		}

		@Override
		public void execute(boolean selected, IPaddleService service) {
			module.getNamespaces().forEach(ns -> {
				try {
					PrintWriter w = new PrintWriter(new File(workspace, ns+".java"));
					w.write(module.createNamespaceView(ns).toString());
					w.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			});

		}
	}

	class RunTool implements IPaddleTool {
		@Override
		public String getText() {
			return "run";
		}

		@Override
		public void execute(boolean selected, IPaddleService service) {
			service.runTool(saveTool.getId());
			loadModule();
			IProgramState state = IMachine.create(module);
			IExecutionData data = state.execute(module.getProcedures().get(0));
			IValue ret = data.getReturnValue();
			showMessage(SWT.ICON_INFORMATION, "Result", ret.toString());
		}
	}


	private static Map<String, Image> imageMap = new HashMap<String, Image>();
	private IJavardiseService javarService;


	private static Image getImage(Module module, String path) {
		if(path == null)
			return null;
		String id = module.getName()+ "/" + path;
		if(imageMap.containsKey(id))
			return imageMap.get(id);

		try {
			InputStream stream = module.getResourceAsStream(path);
			if(stream == null)
				throw new IOException();
			Image img = new Image(display, stream);
			imageMap.put(id, img);
			return img;
		} catch (IOException e) {
			System.err.println("could not load image: " + id);
			return null;
		}
	}



	@Override
	public File getWorkspacePath() {
		return workspace;
	}

	@Override
	public IModule getModule() {
		return module;
	}

	@Override
	public String getVisibleNamespace() {
		TabItem[] selection = modulesFolder.getSelection();
		return selection.length > 0 ? (String) selection[0].getData() : null;
	}

	@Override
	public void addModuleSelectionListener(Consumer<String> c) {
		assert c != null;
		moduleChangeListeners.add(c);
	}

	@Override
	public void removeModuleSelectionListener(Consumer<String> c) {
		assert c != null;
		moduleChangeListeners.remove(c);
	}

	
	private List<Consumer<IProgramElement>> listeners = new ArrayList<>();
	
	@Override
	public void addElementSelectionListener(Consumer<IProgramElement> c) {
		listeners.add(c);
		
	}
	
	@Override
	public void openView(Class<? extends IPaddleView> viewClass) {
		IPaddleView view = floatingViews.get(viewClass);
		Shell shell = new Shell(this.shell);
		shell.setText(view.getTitle());
		shell.setLayout(new RowLayout(SWT.VERTICAL));
		ToolBar toolBar = new ToolBar(shell, SWT.BORDER);
		addTools(toolBar, view.getTools().stream().filter(t -> !t.isTopLevel()).collect(Collectors.toList()));
		Composite contents = new Composite(shell, SWT.BORDER);
		contents.setLayout(new FillLayout());
		view.createContents(contents, this);
		shell.pack();
		shell.open();
	}

	@Override
	public void runTool(String id) {
		if(toolMap.containsKey(id))
			toolMap.get(id).execute(true, this);
		else
			System.err.println("no tool with id: " + id);
	}



}