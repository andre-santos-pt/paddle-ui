import pt.iscte.paddle.javaeditor.api.IJavardiseService;

module pt.iscte.paddle.javardise.tests {
	requires pt.iscte.paddle.javardise;
	requires pt.iscte.paddle.model.javaparser;
	requires pt.iscte.paddle.model.tests;
	requires java.compiler;
	requires junit;
	requires pt.iscte.paddle.javaeditor;
	
	exports pt.iscte.paddle.javardise.tests;

	uses IJavardiseService;
}