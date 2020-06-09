package pt.iscte.paddle.ide.service;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProgramElement;

public interface IPaddleService {
	File getWorkspacePath();
	IModule getModule();
	String getVisibleNamespace();
	void addModuleSelectionListener(Consumer<String> c);
	void removeModuleSelectionListener(Consumer<String> c);
	
	void addElementSelectionListener(Consumer<IProgramElement> c);
	
	void openView(Class<? extends IPaddleView> viewClass);

	void runTool(String id);
	
//	void showErrorMessage(String title, String message);
	
	//	Composite createContent();
//	Image createImage(String modulePath);
}
