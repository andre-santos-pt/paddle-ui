import pt.iscte.paddle.javaeditor.Configuration;
import pt.iscte.paddle.javaeditor.EditorConfiguration;
import pt.iscte.paddle.javaeditor.JavardiseService;
import pt.iscte.paddle.javaeditor.api.IJavardiseService;
import pt.iscte.paddle.javardise.ILanguageConfiguration;
import pt.iscte.paddle.javardise.api.IEditorConfiguration;

module pt.iscte.paddle.javaeditor {
	requires transitive pt.iscte.paddle.javardise;
	requires org.eclipse.swt;
	requires pt.iscte.paddle.model;
	requires pt.iscte.paddle.model.javaparser;
	requires jdk.compiler;
	requires java.compiler;
	
	exports pt.iscte.paddle.javaeditor.api;
	
	provides IJavardiseService with JavardiseService;
	
	uses ILanguageConfiguration;
	provides ILanguageConfiguration with Configuration;
	provides IEditorConfiguration with EditorConfiguration;
}