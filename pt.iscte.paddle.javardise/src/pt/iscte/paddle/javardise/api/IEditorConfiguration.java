package pt.iscte.paddle.javardise.api;

import java.util.ServiceLoader;


public interface IEditorConfiguration {

	IEditorConfiguration INSTANCE = ServiceLoader.load(IEditorConfiguration.class).findFirst().get();
	
	String getExtension();
	

	default int getTabCharacters() {
		return 4;
	}
	
	default String getFontFace() {
		return "Monospace";
	}
	// style
	
}
