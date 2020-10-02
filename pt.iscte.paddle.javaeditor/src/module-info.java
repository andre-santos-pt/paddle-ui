import pt.iscte.paddle.javaeditor.Configuration;
import pt.iscte.paddle.javaeditor.JavardiseService;
import pt.iscte.paddle.javaeditor.api.IJavardiseService;
import pt.iscte.paddle.javardise.LanguageConfiguration;

module pt.iscte.paddle.javaeditor {
	requires transitive pt.iscte.paddle.javardise;
	requires org.eclipse.swt;
	requires pt.iscte.paddle.model;
	requires pt.iscte.paddle.model.javaparser;
	
	exports pt.iscte.paddle.javaeditor.api;
	
	provides IJavardiseService with JavardiseService;
	
	uses LanguageConfiguration;
	provides LanguageConfiguration with Configuration;
}