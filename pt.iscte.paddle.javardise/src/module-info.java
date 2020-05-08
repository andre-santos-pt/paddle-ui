import pt.iscte.paddle.javardise.JavardiseService;
import pt.iscte.paddle.javardise.service.IJavardiseService;


module pt.iscte.paddle.javardise {
	
	requires transitive pt.iscte.paddle.model;
	requires transitive pt.iscte.paddle.model.java;
	requires transitive org.eclipse.swt.cocoa.macosx;
	
	// parser
	//requires org.eclipse.core.runtime;
	//requires org.eclipse.jdt.core;	
	
	exports pt.iscte.paddle.javardise.service;
	exports pt.iscte.paddle.javardise.util;
	exports pt.iscte.paddle.javardise.editor;
	
	provides IJavardiseService with JavardiseService;
}
