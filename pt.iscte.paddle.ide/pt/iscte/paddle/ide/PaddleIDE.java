package pt.iscte.paddle.ide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import pt.iscte.paddle.ide.service.IPaddleService;
import pt.iscte.paddle.ide.service.IPaddleView;
import pt.iscte.paddle.ide.service.Tool;
import pt.iscte.paddle.interpreter.IExecutionData;
import pt.iscte.paddle.interpreter.IMachine;
import pt.iscte.paddle.interpreter.IProgramState;
import pt.iscte.paddle.interpreter.IValue;
import pt.iscte.paddle.javardise.service.IClassWidget;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.javaparser.Java2Paddle;

public class PaddleIDE implements IPaddleService
{
	private static Display display;
//	private static PaddleIDE instance;

	private Shell shell;
	private TabFolder modulesFolder;
	private ToolBar toolBar;

	private File root;
	private List<IModule> modules;
	private List<Consumer<IModule>> moduleChangeListeners;


	public static void main (String [] args) {
		display = new Display ();
		DirectoryDialog dialog = new DirectoryDialog(new Shell(display));
		dialog.setFilterPath(System.getProperty("user.dir"));
		String filePath = dialog.open();
		PaddleIDE ide = new PaddleIDE(new File(filePath));
		ide.open();
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
		this.root = root;
		shell = new Shell(display);
		shell.setText("Paddle: " + root.getAbsolutePath());
		shell.setLayout(new FillLayout());

		ServiceLoader<IJavardiseService> service = ServiceLoader.load(IJavardiseService.class);
		Optional<IJavardiseService> serv = service.findFirst();
		if(!serv.isPresent())
			System.err.println("could not access Javardise service");
		javarService = serv.get();
		
		modules = new ArrayList<>(); 
		Java2Paddle p = new Java2Paddle(root, j -> j.getName().equals("ImageUtil.java"));
		try {
			modules.add(p.parse());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		File[] files = root.listFiles((dir,name) -> name.endsWith(".java"));
//		for(File f : files) {
//			System.out.println("FILE: " + f.getAbsolutePath());
//		}

		moduleChangeListeners = new ArrayList<>();

		SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);

		Composite leftArea = new Composite(sashForm, SWT.BORDER);
		GridLayout layout = new GridLayout(1, true);
		layout.marginTop = 0;
		layout.marginLeft = 0;
		leftArea.setLayout(layout);

		toolBar = new ToolBar(leftArea, SWT.BORDER);
		addTools(toolBar, List.of(new NewFileTool()));
		
		SashForm rightArea = new SashForm(sashForm, SWT.VERTICAL);

		insertModules(leftArea);
		createViews(rightArea);
	}

	private void insertModules(Composite parent) {
		modulesFolder = new TabFolder(parent, SWT.BORDER);
		modulesFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		modulesFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IModule m = getVisibleModule();
				moduleChangeListeners.forEach(c -> c.accept(m));
			}
		});
		modules.forEach(m -> insertModule(m));
	}

	private TabItem insertModule(IModule module) {
		TabItem tabItem = new TabItem(modulesFolder, SWT.NULL);
		IClassWidget w = javarService.createClassWidget(modulesFolder, module);
		tabItem.setControl(w.getControl());
		tabItem.setText(module.getId());
		tabItem.setData(module);
		return tabItem;
	}

	private Map<Class<?>, IPaddleView> floatingViews = new HashMap<Class<?>, IPaddleView>();
	private void createViews(Composite parent) {
		ServiceLoader<IPaddleView> services = ServiceLoader.load(IPaddleView.class);
		services.forEach(s -> {
			if(s.isFixed()) {
				Group p = new Group(parent, SWT.NONE);
				p.setLayout(new FillLayout());
				p.setText(s.getTitle());
				p.setToolTipText(s.getClass().getModule().getName());
				s.createContents(p, this);
			}
			else
				floatingViews.put(s.getClass(), s);
			new ToolItem(toolBar, SWT.SEPARATOR);
			addTools(toolBar, s.getTools().stream().filter(t -> t.isTopLevel()).collect(Collectors.toList()));
		});
		
	}

	private void addTools(ToolBar toolBar, List<Tool> tools) {
		tools.forEach(t -> {
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
	
	public List<Tool> getTools() {
		return List.of(new NewFileTool());
	}

	class NewFileTool implements Tool {
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
							if(f.createNewFile()) {
								IModule m = IModule.create(text.getText());
								TabItem tab = insertModule(m);
								modulesFolder.setSelection(tab);
							}
							else
								System.err.println("could not create " + f);
							
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

	static class RunTool implements Tool {
		@Override
		public String getIcon() {
			return "debug.png";
		}

		@Override
		public void execute(boolean selected, IPaddleService service) {
			IModule m = service.getVisibleModule();
			IProgramState state = IMachine.create(m);
			IExecutionData data = state.execute(m.getProcedures().get(0));
			IValue ret = data.getReturnValue();
			System.out.println(ret);

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
		return root;
	}

	@Override
	public IModule getVisibleModule() {
		TabItem[] selection = modulesFolder.getSelection();
		return selection.length > 0 ? (IModule) selection[0].getData() : null;
	}

	@Override
	public void addModuleSelectionListener(Consumer<IModule> c) {
		assert c != null;
		moduleChangeListeners.add(c);
	}

	@Override
	public void removeModuleSelectionListener(Consumer<IModule> c) {
		assert c != null;
		moduleChangeListeners.remove(c);
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



}