import pt.iscte.paddle.ide.PaddleIDE;
import pt.iscte.paddle.ide.service.IPaddleService;

module pt.iscte.paddle.ide {
	requires transitive org.eclipse.swt.cocoa.macosx;
	requires transitive pt.iscte.paddle.model;
	requires transitive pt.iscte.paddle.javardise;
	
	exports pt.iscte.paddle.ide.service;
	
	uses pt.iscte.paddle.javardise.service.IJavardiseService;
	uses pt.iscte.paddle.ide.service.View;
	
	provides IPaddleService with PaddleIDE;
}