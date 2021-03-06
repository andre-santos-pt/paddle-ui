package pt.iscte.paddle.javardise.editor;
import java.io.IOException;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class JavaStringObject extends SimpleJavaFileObject {
    private final StringBuffer source;

    protected JavaStringObject(String name, String src) {
    	this(name, new StringBuffer(src));
    }
    
    protected JavaStringObject(String name, StringBuffer source) {
        super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return source;
    }
}