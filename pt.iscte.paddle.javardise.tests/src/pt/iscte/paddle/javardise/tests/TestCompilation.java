package pt.iscte.paddle.javardise.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import pt.iscte.paddle.javaeditor.api.IClassWidget;
import pt.iscte.paddle.javaeditor.api.IJavardiseService;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.javaparser.Java2Paddle;
import pt.iscte.paddle.model.tests.BaseTest;
import pt.iscte.paddle.model.tests.TestArrayCount;
import pt.iscte.paddle.model.tests.TestArrayFind;
import pt.iscte.paddle.model.tests.TestBinarySearch;
import pt.iscte.paddle.model.tests.TestCheckEven;
import pt.iscte.paddle.model.tests.TestCircle;
import pt.iscte.paddle.model.tests.TestFactorial;
import pt.iscte.paddle.model.tests.TestIdMatrix;
import pt.iscte.paddle.model.tests.TestInvert;
import pt.iscte.paddle.model.tests.TestMatrixTranspose;
import pt.iscte.paddle.model.tests.TestMax;
import pt.iscte.paddle.model.tests.TestNaturals;
import pt.iscte.paddle.model.tests.TestSelectionSort;
import pt.iscte.paddle.model.tests.TestSum;
import pt.iscte.paddle.model.tests.TestSwap;

public class TestCompilation {
	static final List<Class<? extends BaseTest>> CASES = Arrays.asList(
			TestFactorial.class,
			TestMax.class,
			TestCircle.class,
			
			TestCheckEven.class,
			TestNaturals.class,
			TestSum.class,
			TestSwap.class,
			TestArrayCount.class,
			TestArrayFind.class,
			TestBinarySearch.class,
			TestInvert.class,
			TestSelectionSort.class,
			
			TestIdMatrix.class,
//			TestMatrixScalar.class
			TestMatrixTranspose.class
	);

	@Test
	public void test() {
		CASES.forEach(c -> runCase(c));
	}
		
	private void runCase(Class<? extends BaseTest> testCase) throws IllegalArgumentException {
		try {
			BaseTest test = testCase.getConstructor().newInstance();
			test.setupProcedures();
			IModule mod = test.getModule();
			Shell shell = new Shell();
			
			ServiceLoader<IJavardiseService> service = ServiceLoader.load(IJavardiseService.class);
			Optional<IJavardiseService> serv = service.findFirst();
			if(!serv.isPresent())
				System.err.println("could not access Javardise service");
			
			IJavardiseService javarService = serv.get();
			
			IClassWidget w = javarService.createClassWidget(shell, mod, mod.getId());
			boolean compile = compile(mod.getId(), w.getCode());
				
			assertTrue("Errors on " + mod.getId(), compile);
			File file = new File("src-gen/" + mod.getId() + ".java");
			saveToFile(w, file);
			Java2Paddle parser = new Java2Paddle(file, mod.getId());
			IModule m = parser.parse();
			BaseTest retest = new BaseTest(m);
			try {
				retest.run();
			} catch (Throwable e) {
				e.printStackTrace();
				assertFalse(true);
			}
		} catch (InvocationTargetException | NoSuchMethodException
				| SecurityException | InstantiationException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void saveToFile(IClassWidget classWidget, File file) {
		StringBuffer buffer = new StringBuffer();
		classWidget.toCode(buffer);
		try {
		
			PrintWriter w = new PrintWriter(file);
			w.append(buffer);
			w.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean compile(String className, String src) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		JavaStringObject stringObject = new JavaStringObject(className, src);
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("src-gen")));
		JavaCompiler.CompilationTask task = compiler.getTask(null,
				fileManager, diagnostics, null, null, Arrays.asList(stringObject));
		return task.call();
	}
	
	private static class JavaStringObject extends SimpleJavaFileObject {
	    private final StringBuffer source;

	    protected JavaStringObject(String name, String src) {
	    	this(name, new StringBuffer(src));
	    }
	    
	    protected JavaStringObject(String name, StringBuffer source) {
	        super(URI.create("string:///" + name.replaceAll("\\.", "/") +
	                Kind.SOURCE.extension), Kind.SOURCE);
	        this.source = source;
	    }

	    @Override
	    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
	        return source;
	    }
	}
}
