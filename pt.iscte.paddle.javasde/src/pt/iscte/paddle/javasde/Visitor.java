package pt.iscte.paddle.javasde;

public interface Visitor {

	default void visit(FieldWidget w) { }
	default void visit(MethodWidget w) { }
	default void visit(ControlWidget w) { }
	default void visit(CallWidget w) { }
}
