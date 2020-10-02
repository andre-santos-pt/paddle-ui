package pt.iscte.paddle.javardise.javaeditor;

import java.util.Arrays;
import java.util.List;

public interface JavaConstants {

	List<String> BINARY_OPERATORS = 		Arrays.asList("+", "-", "*", "/", "%", "==", "!=", "<", "<=", ">", ">=", "&&", "||", "^");
	List<String> ARITHMETIC_OPERATORS = 	Arrays.asList("+", "-", "*", "/", "%");
	List<String> RELATIONAL_OPERATORS = 	Arrays.asList("==", "!=", "<", "<=", ">", ">=");
	List<String> LOGICAL_OPERATORS = 		Arrays.asList("&&", "||", "^");
	List<String> UNARY_OPERATORS = Arrays.asList("!", "-", "+");  //"(int)", "(double)");

}
