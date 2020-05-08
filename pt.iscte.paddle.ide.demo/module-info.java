import pt.iscte.paddle.javardise.service.IJavardiseService;

module pt.iscte.paddle.ide.demo {
	requires pt.iscte.paddle.ide;
	//requires pt.iscte.paddle.model;
	//requires pt.iscte.paddle.javardise;
	//requires org.eclipse.swt.cocoa.macosx;

	uses pt.iscte.paddle.ide.service.IPaddleService;
	uses IJavardiseService;
	
	provides pt.iscte.paddle.ide.service.View with pt.iscte.paddle.ide.demo.DemoView;
}


