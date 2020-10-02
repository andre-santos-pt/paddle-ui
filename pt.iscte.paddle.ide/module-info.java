module pt.iscte.paddle.ide {
	requires transitive org.eclipse.swt;
	requires transitive pt.iscte.paddle.model;
	requires transitive pt.iscte.paddle.model.javaparser;
	requires transitive pt.iscte.paddle.javaeditor;
	
	exports pt.iscte.paddle.ide.service;
	
	uses pt.iscte.paddle.javaeditor.api.IJavardiseService;
	uses pt.iscte.paddle.ide.service.IPaddleView;
	
//	uses ToolGroup;
//	provides ToolGroup with PaddleIDE;
//	provides IPaddleService with PaddleIDE;
}