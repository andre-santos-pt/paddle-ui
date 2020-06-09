package pt.iscte.paddle.ide.service;

public interface IPaddleTool {
	default String getText() {
		return "";
	}
	
	default String getTooltip() {
		return "";
	}
	
	default String getIcon() {
		return null;
	}
	default boolean isToggle() {
		return false;
	}
	
	default boolean isTopLevel() {
		return true;
	}
	
	// isEnabled?
	void execute(boolean selected, IPaddleService service);
	
	default String getId() {
		return getClass().getModule().getName() + "." + getClass().getSimpleName();
	}
	
//	abstract class AbstractTool implements Tool {
//		
//	}
}
