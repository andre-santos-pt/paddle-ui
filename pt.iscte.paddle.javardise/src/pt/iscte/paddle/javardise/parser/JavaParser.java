package pt.iscte.paddle.javardise.parser;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import pt.iscte.paddle.model.IModule;

public class JavaParser {
	
	private final String source;
	private Visitor visitor;
	private IProblem[] problems;
	
	public JavaParser(File file) {
		source = readSource(file);
	}
	
	public JavaParser(StringBuffer buffer) {
		source = buffer.toString();
	}
	
	public IModule parse() {
		visitor = new Visitor();
		problems = JavaParser.parse(source, visitor);
		return visitor.module;
	}
	
	public boolean hasParseProblems() {
		return problems.length > 0 || visitor.incomplete();
	}
	
	static IProblem[] parse(String source, ASTVisitor visitor) {
		ASTParser parser = ASTParser.newParser(AST.JLS12);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(visitor);
		return cu.getProblems();
	}
	
	private static String readSource(File file) {
		StringBuilder src = new StringBuilder();
		try {
			Scanner scanner = new Scanner(file);
			while(scanner.hasNextLine())
				src.append(scanner.nextLine()).append('\n');
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return src.toString();
	}
}

