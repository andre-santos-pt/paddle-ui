package pt.iscte.paddle.javardise;

import java.util.ServiceLoader;

public interface LanguageConfiguration {

	LanguageConfiguration INSTANCE = ServiceLoader.load(LanguageConfiguration.class).findFirst().get();

	default boolean isKeyword(String s) {
		return false;
	}
	
	default boolean isValidId(String s) {
		return s.matches("[a-zA-Z]+");
	}
	
	default boolean isValidIdCharacter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}
	
	void configure(SimpleExpressionWidget expression);

	TextWidget createInsertWidget(InsertWidget parent, boolean permanent);
	
	
}
