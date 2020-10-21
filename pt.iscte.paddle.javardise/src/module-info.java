import pt.iscte.paddle.javardise.ILanguageConfiguration;
import pt.iscte.paddle.javardise.api.IEditorConfiguration;


module pt.iscte.paddle.javardise {
	
//	requires transitive pt.iscte.paddle.model;
	requires transitive org.eclipse.swt;
	
	// parser
	//requires org.eclipse.core.runtime;
	//requires org.eclipse.jdt.core;	
	
	exports pt.iscte.paddle.javardise.api;
	exports pt.iscte.paddle.javardise;
	
	uses ILanguageConfiguration;
	uses IEditorConfiguration;
	
//	provides IJavardiseService with JavardiseService;
//	provides LanguageConfiguration with Configuration;
}
