package pt.iscte.paddle.javardise.parser;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;

import pt.iscte.paddle.model.IModule;

public class JavaParser {
	
	private final String filename;
	private final String source;
	private ParserVisitor visitor;
	private IProblem[] problems;
	
	public JavaParser(File file) {
		source = readSource(file);
		filename = file.getName().substring(0, file.getName().lastIndexOf('.'));
	}
	
	public JavaParser(StringBuffer buffer) {
		source = buffer.toString();
		filename = "undefined";
	}
	
	public IModule parse() {
		visitor = new ParserVisitor(filename);
		problems = parse(source);
		return visitor.module;
	}
	
	public boolean hasParseProblems() {
		return problems.length > 0 || visitor.incomplete();
	}
	
	IProblem[] parse(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS12);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(visitor);
		
		for (Comment comment : (List<Comment>) cu.getCommentList())
			comment.accept(new CommentVisitor(cu, source));
		
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
	
	static class CommentVisitor extends ASTVisitor {
		CompilationUnit cu;
		String source;
	 
		public CommentVisitor(CompilationUnit cu, String source) {
			super();
			this.cu = cu;
			this.source = source;
		}
	 
		public boolean visit(LineComment node) {
			int start = node.getStartPosition();
			int end = start + node.getLength();
			String comment = source.substring(start, end);
			System.out.println(comment);
			int l=cu.getLineNumber(node.getStartPosition());
			System.err.println(l);
			return true;
		}
	 
		public boolean visit(BlockComment node) {
			int start = node.getStartPosition();
			int end = start + node.getLength();
			String comment = source.substring(start, end);
			System.out.println(comment);
			return true;
		}
	 
	}
}

