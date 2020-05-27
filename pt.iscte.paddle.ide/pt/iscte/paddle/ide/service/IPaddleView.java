package pt.iscte.paddle.ide.service;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

public interface IPaddleView {
	default String getTitle() {
		return getClass().getName();
	}
	
	default boolean isFixed() {
		return true;
	}
	
	void createContents(Composite parent, IPaddleService service);
	
	default List<Tool> getTools() {
		return List.of();
	}
}
