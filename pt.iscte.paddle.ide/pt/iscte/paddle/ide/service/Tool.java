package pt.iscte.paddle.ide.service;

import java.util.function.Consumer;

public interface Tool {
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
	
	abstract class AbstractTool implements Tool {
		
	}
}
