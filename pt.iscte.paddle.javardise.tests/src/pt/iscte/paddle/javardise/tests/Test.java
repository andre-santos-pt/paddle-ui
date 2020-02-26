package pt.iscte.paddle.javardise.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.javardise.StandaloneEditor;
import pt.iscte.paddle.model.tests.BaseTest;
import pt.iscte.paddle.model.tests.TestArrayCount;
import pt.iscte.paddle.model.tests.TestArrayFind;
import pt.iscte.paddle.model.tests.TestBinarySearch;
import pt.iscte.paddle.model.tests.TestCheckEven;
import pt.iscte.paddle.model.tests.TestFactorial;
import pt.iscte.paddle.model.tests.TestIdMatrix;
import pt.iscte.paddle.model.tests.TestInvert;
import pt.iscte.paddle.model.tests.TestMax;
import pt.iscte.paddle.model.tests.TestNaturals;
import pt.iscte.paddle.model.tests.TestSelectionSort;
import pt.iscte.paddle.model.tests.TestSum;
import pt.iscte.paddle.model.tests.TestSwap;

public class Test {
	static Class<?>[] CASES = {
			TestFactorial.class,
			TestMax.class,
			TestCheckEven.class,
			TestNaturals.class,
			TestSum.class,
			TestSwap.class,
			TestArrayCount.class,
			TestArrayFind.class,
			TestBinarySearch.class,
			TestInvert.class,
			TestSelectionSort.class,
			TestIdMatrix.class
	};

	public static void main(String[] args) {
		//		StandaloneEditor e = new StandaloneEditor(new File("Test.java"));

		//		StandaloneEditor e = new StandaloneEditor(IModule.create("Titi"));

		Display display = new Display();
		List<Shell> shells = new ArrayList<>();
		for(Class<?> c : CASES) {
			try {
				BaseTest t = (BaseTest) c.newInstance();
				t.setup();
				StandaloneEditor e = new StandaloneEditor(t.getModule());
				Shell s = e.launch(display);
				shells.add(s);
				File dest = new File("src-gen");
				
				if(e.compile(dest)) {
//					s.close();
				}
				else  {
					s.setText(s.getText() + " ERROR");
				}
				e.saveToFile(new File("src-gen/"+t.getModule().getId()+".java"));
//				s.close();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}

		//		TestAverage testAverage = new TestAverage();
		//		testAverage.setup();
		//		StandaloneEditor e2 = new StandaloneEditor(testAverage.getModule());
		//		Shell shell = e2.launch(display);

		Iterator<Shell> it = shells.iterator();
		while(it.hasNext()) {
			Shell s = it.next();
			while (!s.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			it.remove();
		}
		display.dispose();
	}

}
