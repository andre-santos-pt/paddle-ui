package pt.iscte.paddle.ide.demo;

import java.util.ServiceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import pt.iscte.paddle.ide.service.IPaddleService;
import pt.iscte.paddle.ide.service.View;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;

public class DemoView implements View {
	private IPaddleService paddleService;
	private IJavardiseService javarService;
	
	public DemoView() {
		paddleService = ServiceLoader.load(IPaddleService.class).findFirst().get();		
		javarService = ServiceLoader.load(IJavardiseService.class).findFirst().get();
	}

	@Override
	public void create(Composite parent) {
		parent.setLayout(new FillLayout());
		IModule module = paddleService.getModule();
		//		CFGViewer view = new CFGViewer(group);
		//		view.setInput(paddleService.getModule().getProcedures().get(0).getCFG());

		List list = new List(parent, SWT.NONE);
		list.setItems(procsToArray(module));
//		list.addSelectionListener(new SelectionAdapter() {
//			Color blue = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				System.out.println("!!!");
//				ICodeDecoration<Canvas> m = javarService.getWidget(module.getProcedures().get(list.getSelectionIndex())).addMark(blue);
//				m.show();
//			}
//		});
		module.addListener(new IModule.IListener() {
			@Override
			public void procedureAdded(IProcedure procedure) {
				list.setItems(procsToArray(module));
			}
			@Override
			public void procedureRemoved(IProcedure procedure) {
				list.setItems(procsToArray(module));
			}
		});
	}

	private String[] procsToArray(IModule m) {
		java.util.List<IProcedure> procedures = m.getProcedures();
		String[] procs = new String[procedures.size()];
		for(int i = 0; i < procs.length; i++)
			procs[i] = procedures.get(i).getId();
		return procs;
	}

}
