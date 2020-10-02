package pt.iscte.paddle.javaeditor;

import java.util.EnumSet;

// TODO UI mode
// observable
class UiMode {
	
	public enum Editor {
		REGULAR, STATIC, SCRIPT
	}
	
	public enum Syntax {
		SELECTION, CALLS, ASSIGNMENT, WHILE_LOOP, FOR_LOOP, ARRAYS, RECORDS, ASSIGN_SIMPLIFICATION, ENCAPSULATION;
	}
	// font size
	// tab space
	private static Editor editor = Editor.REGULAR;

	private static EnumSet<Syntax> syntax = EnumSet.allOf(Syntax.class);
	
	public static void addSyntax(Syntax ... elements) {
		for(Syntax s : elements)
			syntax.add(s);
	}
	
	public static void removeSyntax(Syntax ... elements) {
		for(Syntax s : elements)
			syntax.remove(s);
	}
	
	public static boolean hasSyntax(Syntax s) {
		return syntax.contains(s);
	}
	
	public static Editor editorMode() {
		return editor;
	}
	

}
