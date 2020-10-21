import pt.iscte.paddle.javardise.ILanguageConfiguration;
import pt.iscte.paddle.javardise.api.IEditorConfiguration;
import pt.iscte.paddle.yamleditor.Configuration;

module pt.iscte.paddle.yamleditor {
	requires pt.iscte.paddle.javardise;
	
	provides ILanguageConfiguration with Configuration;
	provides IEditorConfiguration with Configuration;
	
}