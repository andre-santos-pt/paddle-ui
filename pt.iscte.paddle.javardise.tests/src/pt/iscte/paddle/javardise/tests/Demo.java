package pt.iscte.paddle.javardise.tests;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.Constants;
import pt.iscte.paddle.javardise.StandaloneEditor;
import pt.iscte.paddle.model.IReturn;
import pt.iscte.paddle.model.IBlock.IVisitor;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.tests.TestArrayCount;
import pt.iscte.paddle.model.tests.TestCheckEven;
import pt.iscte.paddle.model.tests.TestMax;
import pt.iscte.paddle.tests.asg.BooleanFuncions.IsEven;

public class Demo {

	public static void main(String[] args) {
		//		StandaloneEditor e = new StandaloneEditor(new File("Test.java"));

		TestArrayCount t = new TestArrayCount();
		t.setup();

		StandaloneEditor editor = new StandaloneEditor(t.getModule());
		Display display = new Display();
		Shell shell = editor.launch(display);
		
		IProcedure proc = t.getModule().getProcedure("count");
//		proc.getParameters().forEach(p -> editor.mark(p, Constants.COLOR_HIGHLIGHT));
		proc.accept(new IVisitor() {
			public boolean visit(IReturn r) {
				editor.mark(r,Constants.COLOR_HIGHLIGHT);
				return true;
			}
		});
		
//		editor.mark(s, color);
		
//		editor.removeAllMarks();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}

}
