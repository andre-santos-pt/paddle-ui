package pt.iscte.paddle.javali.demo;
import java.io.File;

import pt.iscte.paddle.interpreter.ExecutionError;
import pt.iscte.paddle.javali.translator.ElementLocation;
import pt.iscte.paddle.javali.translator.Translator;
import pt.iscte.paddle.model.IBlock.IVisitor;
import pt.iscte.paddle.model.IModel2CodeTranslator;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IVariableAssignment;

public class DemoTranslator {

	public static void main(String[] args) throws ExecutionError {

		// instantiate model from file
		Translator translator = new Translator(new File("naturals.javali").getAbsolutePath());
		IModule module = translator.createProgram();
		IProcedure nats = module.getProcedures().iterator().next(); // first procedure

		// prints model as code
		String src = module.translate(new IModel2CodeTranslator.Java());
		System.out.println(src);

		// Model visitor
		System.out.println("Assignments to variable i:");
		nats.accept(new IVisitor() {
			public boolean visit(IVariableAssignment assignment) {
				ElementLocation loc = (ElementLocation) assignment.getProperty(ElementLocation.Part.WHOLE);
				if (assignment.getTarget().getId().equals("i"))
					System.out.println(assignment + "\n" + loc + "\n");
				return true;
			}
		});

	}
}