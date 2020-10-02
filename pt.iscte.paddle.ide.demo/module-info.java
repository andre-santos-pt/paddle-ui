import pt.iscte.paddle.ide.demo.DemoView;
import pt.iscte.paddle.ide.demo.DemoView2;
import pt.iscte.paddle.ide.service.IPaddleView;
import pt.iscte.paddle.javaeditor.api.IJavardiseService;

module pt.iscte.paddle.ide.demo {
	requires pt.iscte.paddle.ide;
	
	uses IJavardiseService;
	
	provides IPaddleView with DemoView, DemoView2;
}


