package pt.iscte.paddle.javardise;

import java.io.File;
import java.util.ServiceLoader;

public interface ILanguageConfiguration {

	ILanguageConfiguration INSTANCE = ServiceLoader.load(ILanguageConfiguration.class).findFirst().get();

	default boolean isKeyword(String s) {
		return false;
	}
	
	default boolean isValidId(String s) {
		return s.matches("[a-zA-Z]+");
	}
	
	default boolean isValidIdCharacter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}
	
	void configureRoot(File file, SequenceWidget sequence);

	void configure(SimpleExpressionWidget expression);

	
	TextWidget createInsertWidget(InsertWidget parent, boolean permanent);
	
	void compile(StringBuffer src, File binDest);
	
}
