package pt.iscte.pidesco.cfgviewer.internal;

import static pt.iscte.paddle.model.IOperator.GREATER;
import static pt.iscte.paddle.model.IOperator.SMALLER;
import static pt.iscte.paddle.model.IType.INT;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.model.IBlock;
import pt.iscte.paddle.model.ILoop;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.ISelection;
import pt.iscte.paddle.model.IVariableAssignment;
import pt.iscte.paddle.model.IVariableDeclaration;
import pt.iscte.paddle.model.cfg.IControlFlowGraph;
import pt.iscte.pidesco.cfgviewer.ext.CFGViewer;

//Test Class
public class CFGWindow {
	
	public CFGWindow(IControlFlowGraph cfg) {
		Display display = new Display();
		
		Shell shell = new Shell(display);
		shell.setText("Control Flow Graph Viewer");
		shell.setLayout(new FillLayout());
		
		Composite viewArea = new Composite(shell, SWT.NONE);
		viewArea.setLayout(new FillLayout());
		CFGViewer view = new CFGViewer(viewArea);
		view.setInput(cfg);
		
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
	
	public static void main(String[] args) {
		
		IModule module = IModule.create(); 
		IProcedure max = module.addProcedure(INT);
		IVariableDeclaration array = max.addParameter(INT.array().reference());
		IBlock body = max.getBody();
		IVariableDeclaration m = body.addVariable(INT);
		IVariableAssignment mAss = body.addAssignment(m, array.element(INT.literal(0)));
		IVariableDeclaration i = body.addVariable(INT);
		IVariableAssignment iAss = body.addAssignment(i, INT.literal(1));
		ILoop loop = body.addLoop(SMALLER.on(i, array.length()));
		ISelection ifstat = loop.addSelection(GREATER.on(array.element(i), m));
		IVariableAssignment mAss_ = ifstat.addAssignment(m, array.element(i));
		IVariableAssignment iInc = loop.addIncrement(i);
		IReturn ret = body.addReturn(m);
		
		IControlFlowGraph cfg = max.getCFG();
		
		new CFGWindow(cfg);
	}

}
