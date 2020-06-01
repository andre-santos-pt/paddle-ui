package pt.iscte.paddle.ide.demo;

import java.util.List;
import java.util.ServiceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.ide.service.IPaddleService;
import pt.iscte.paddle.ide.service.IPaddleTool;
import pt.iscte.paddle.ide.service.IPaddleView;
import pt.iscte.paddle.interpreter.IExecutionData;
import pt.iscte.paddle.interpreter.IMachine;
import pt.iscte.paddle.interpreter.IProgramState;
import pt.iscte.paddle.interpreter.IValue;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.model.IModule;

public class DemoView implements IPaddleView {
	private IPaddleService paddleService;
	private IJavardiseService javarService;
	
	public DemoView() {
//		paddleService = ServiceLoader.load(IPaddleService.class).findFirst().get();		
		javarService = ServiceLoader.load(IJavardiseService.class).findFirst().get();
	}

	@Override
	public String getTitle() {
		return "My View";
	}

	@Override
	public void createContents(Composite parent, IPaddleService service) {
		paddleService = service;
		parent.setLayout(new FillLayout());
		Text text = new Text(parent, SWT.BORDER);
		paddleService.addModuleSelectionListener(c -> System.out.println(c));
		
	}

	@Override
	public List<IPaddleTool> getTools() {
		return List.of(new RunTool());
	}
	
	
	class RunTool implements IPaddleTool {
		@Override
		public String getIcon() {
			return "upload.png";
		}

		@Override
		public void execute(boolean selected, IPaddleService service) {
			IProgramState state = IMachine.create(service.getModule());
			IExecutionData data = state.execute(service.getModule().getProcedures().get(0));
			IValue ret = data.getReturnValue();
			System.out.println(ret);

		}
	}

}
