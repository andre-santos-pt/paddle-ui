package pt.iscte.paddle.ide.service;

import java.io.File;
import java.util.function.Consumer;

import org.eclipse.swt.graphics.Image;

import pt.iscte.paddle.model.IModule;

public interface IPaddleService {
	File getWorkspacePath();
	IModule getVisibleModule();
	void addModuleSelectionListener(Consumer<IModule> c);
	void removeModuleSelectionListener(Consumer<IModule> c);
	
	void openView(Class<? extends IPaddleView> viewClass);
//	Image createImage(String modulePath);
}
