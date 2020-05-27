import pt.iscte.paddle.ide.demo.DemoView;
import pt.iscte.paddle.ide.service.IPaddleView;
import pt.iscte.paddle.javardise.service.IJavardiseService;

module pt.iscte.paddle.ide.demo {
	requires pt.iscte.paddle.ide;
	
	uses IJavardiseService;
	
	provides IPaddleView with DemoView;
}


