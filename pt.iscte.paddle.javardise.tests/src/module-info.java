import pt.iscte.paddle.javardise.service.IJavardiseService;

module pt.iscte.paddle.javardise.tests {
	requires pt.iscte.paddle.javardise;
	requires pt.iscte.paddle.model.java;
	requires pt.iscte.paddle.model.tests;
	requires java.compiler;
	requires junit;
	
	exports pt.iscte.paddle.javardise.tests;

	uses IJavardiseService;
}