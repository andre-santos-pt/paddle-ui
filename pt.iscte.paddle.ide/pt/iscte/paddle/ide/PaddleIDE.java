package pt.iscte.paddle.ide;

import static pt.iscte.paddle.model.IOperator.GREATER;
import static pt.iscte.paddle.model.IOperator.SMALLER;
import static pt.iscte.paddle.model.IType.INT;

import java.util.Optional;
import java.util.ServiceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import pt.iscte.paddle.ide.service.IPaddleService;
import pt.iscte.paddle.ide.service.View;
import pt.iscte.paddle.javardise.service.IClassWidget;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;

public class PaddleIDE implements IPaddleService
{
	private static IModule module;

	public static void main (String [] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setText("Paddle - " + System.getProperty("user.dir"));
		shell.setLayout(new FillLayout());

		module = IModule.create("MyClass");

		IProcedure max = module.addProcedure(INT);
		max.setId("max");
		IVariableDeclaration array = max.addParameter(INT.array().reference());
		array.setId("array");
		IBlock body = max.getBody();
		IVariableDeclaration m = body.addVariable(INT);
		m.setId("m");
		IVariableAssignment mAss = body.addAssignment(m, array.element(INT.literal(0)));
		IVariableDeclaration i = body.addVariable(INT);
		i.setId("i");
		IVariableAssignment iAss = body.addAssignment(i, INT.literal(1));
		ILoop loop = body.addLoop(SMALLER.on(i, array.length()));
		ISelection ifstat = loop.addSelection(GREATER.on(array.element(i), m));
		IVariableAssignment mAss_ = ifstat.addAssignment(m, array.element(i));
		IVariableAssignment iInc = loop.addIncrement(i);
		IReturn ret = body.addReturn(m);
		
		SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);
		
		TabFolder tabFolder = new TabFolder(sashForm, SWT.BORDER);
		
		createMenu(tabFolder);
		ServiceLoader<IJavardiseService> service = ServiceLoader.load(IJavardiseService.class);
		Optional<IJavardiseService> serv = service.findFirst();
		if(!serv.isPresent())
			System.err.println("could not access Javardise service");
		
		IJavardiseService javarService = serv.get();
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		IClassWidget w = javarService.createClassWidget(tabFolder, module);
		tabItem.setControl(w.getControl());
		tabItem.setText(module.getId());
		
		
		TabItem tabItem2 = new TabItem(tabFolder, SWT.NULL);
		IClassWidget w2 = javarService.createClassWidget(tabFolder, IModule.create("Other"));
		tabItem2.setControl(w2.getControl());
		tabItem2.setText("Other");
		
//		module.addProcedure("test", IType.INT);
		
		SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
		//	    new Button(sashForm2, SWT.PUSH).setText("Up");
		//	    new Button(sashForm2, SWT.PUSH).setText("Down");
		ServiceLoader<View> services = ServiceLoader.load(View.class);
		services.forEach(s -> {
			Group p = new Group(sashForm2, SWT.NONE);
			p.setLayout(new FillLayout());
			p.setText(s.getClass().getModule().getName());
			s.create(p);
		});
		shell.open();

		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}

	private static void createMenu(TabFolder tabFolder) {
		Menu menu = new Menu(tabFolder);
		tabFolder.setMenu(menu);
		
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("new file");
		MenuItem item2 = new MenuItem(menu, SWT.PUSH);
		item2.setText("delete");
		MenuItem item3 = new MenuItem(menu, SWT.PUSH);
		item3.setText("open folder");
		

	}

	@Override
	public IModule getModule() {
		return module;
	}

}