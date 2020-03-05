package pt.iscte.paddle.javardise.parser;

import java.io.File;

import pt.iscte.paddle.model.IModule;

public class Test {

	public static void main(String[] args) {
		JavaParser p = new JavaParser(new File("Test.java"));
		IModule m = p.parse();
		System.out.println(m);
		
	}

}
