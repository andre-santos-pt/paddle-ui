package pt.iscte.paddle.javardise.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.interpreter.ExecutionError;
import pt.iscte.paddle.interpreter.IMachine;
import pt.iscte.paddle.interpreter.IProgramState;
import pt.iscte.paddle.interpreter.IReference;
import pt.iscte.paddle.javardise.service.IClassWidget;
import pt.iscte.paddle.javardise.service.ICodeDecoration;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.model.IArrayElementAssignment;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.IVariableExpression;
import pt.iscte.paddle.model.tests.TestNaturals;

public class DebuggerTest {

	private static Shell shell;

	public static void main(String[] args) {

		TestNaturals t = new TestNaturals();
		t.setup();
		IModule module = t.getModule();
		IProcedure proc = module.getProcedure("naturals");

		Display display = new Display();
		shell = new Shell(display);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 50;
		layout.marginLeft = 50;
		layout.verticalSpacing = 20;
		shell.setLayout(layout);

		ServiceLoader<IJavardiseService> service = ServiceLoader.load(IJavardiseService.class);
		Optional<IJavardiseService> serv = service.findFirst();
		if(!serv.isPresent())
			System.err.println("could not access Javardise service");
		
		IJavardiseService javarService = serv.get();
		
		IClassWidget widget = javarService.createClassWidget(shell, module, module.getId());
		widget.setReadOnly(true);

		Map<IVariableDeclaration, ICodeDecoration<Text>> decMap = new HashMap<>();
		
		Color blue = new Color (display, 0, 0, 255);
		IProgramState state = IMachine.create(module);
		state.addListener(new IProgramState.IListener() {
			public void step(IProgramElement ip) {
				if(ip instanceof IVariableAssignment) {
					IVariableDeclaration var = ((IVariableAssignment) ip).getTarget();
					IReference store = state.getCallStack().getTopFrame().getVariableStore(var);
					decMap.get(var).getControl().setText(store.getTarget().toString());
				}
				else if(ip instanceof IArrayElementAssignment) {
					IVariableDeclaration var = ((IVariableExpression) ((IArrayElementAssignment) ip).getTarget()).getVariable();
					IReference store = state.getCallStack().getTopFrame().getVariableStore(var);
					decMap.get(var).getControl().setText(store.getTarget().toString());
				}
			}
		});
		
		try {
			state.setupExecution(proc, 5);
		} catch (ExecutionError e2) {
			e2.printStackTrace();
		}

		
		
		Composite comp = new Composite(shell, SWT.BORDER);
		comp.setLayout(new FillLayout());

		Button run = new Button(comp, SWT.PUSH);
		run.setText("run");
		run.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (IVariableDeclaration var : proc.getVariables()) {
					IWidget w = javarService.getWidget(var);
					ICodeDecoration d = w.addNote("?", ICodeDecoration.Location.RIGHT);
					if(d != null) {
						d.show();
						decMap.put(var, d);
					}
				}
			}
		});
		
		Button next = new Button(comp, SWT.PUSH);
		next.setText("next");
		next.addSelectionListener(new SelectionAdapter() {
			ICodeDecoration m;
			public void widgetSelected(SelectionEvent e) {
				try {
					if(state.isOver()) {
						showDialog("over");
						return;
					}
					state.stepIn();
					IProgramElement ip = state.getInstructionPointer();
					System.out.println(ip);
					if(m != null)
						m.delete();
					IWidget w = javarService.getWidget(ip);
					m = w.addMark(blue);
					if(m != null)
						m.show();
				} catch (ExecutionError e1) {
					e1.printStackTrace();
				}
			}
		});

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

	static void showDialog(String msg) {
		MessageBox dialog =
				new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
		dialog.setText("My info");
		dialog.setMessage(msg);

		dialog.open();
	}

}
