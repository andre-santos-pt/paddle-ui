package pt.iscte.paddle.javasde;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Text;

public enum Keyword implements CharSequence {

	CLASS,
	
	STATIC {
		public Keyword[] excludes() {
			return new Keyword[] {ABSTRACT};
		}
	},
	
	FINAL {
		public Keyword[] excludes() {
			return new Keyword[] {ABSTRACT};
		}
	},
	PUBLIC {
		public Keyword[] excludes() {
			return new Keyword[] {PRIVATE, PROTECTED};
		}
	},
	PRIVATE {
		public Keyword[] excludes() {
			return new Keyword[] {PUBLIC, PRIVATE};
		}
	},
	PROTECTED {
		public Keyword[] excludes() {
			return new Keyword[] {PRIVATE, PUBLIC};
		}
	},
	ABSTRACT {
		public Keyword[] excludes() {
			return new Keyword[] {STATIC, FINAL};
		}
	},
	
	BOOLEAN,
	BYTE,
	SHORT,
	INT,
	LONG,
	FLOAT,
	DOUBLE,
	CHAR,
	
	VOID,
	
	TRUE,
	FALSE,
	NULL,
	
	IF,
	ELSE,
	
	WHILE,
	FOR,
	RETURN,
	BREAK,
	CONTINUE,
	
	NEW;

//	private Keyword[] excludes;
//	
//	private Keyword (Keyword ... excludes) {
//		this.excludes = excludes;
//	}
	
	private static final Keyword[] EMPTY = {};
	
	private static final List<Keyword> CLASS_MODIFIERS = Arrays.asList(PUBLIC, ABSTRACT, FINAL);
	
	private static final List<Keyword> FIELD_MODIFIERS = Arrays.asList(PUBLIC, PRIVATE, PROTECTED, STATIC, FINAL);

	private static final List<Keyword> METHOD_MODIFIERS = Arrays.asList(PUBLIC, PRIVATE, PROTECTED, STATIC, ABSTRACT, FINAL);
	
	public Keyword[] excludes() {
		return EMPTY;
	}
	
	public static List<Keyword> classModifiers() {
		return CLASS_MODIFIERS;
	}
	
	public static List<Keyword> fieldModifiers() {
		return METHOD_MODIFIERS;
	}
	
	public static List<Keyword> methodModifiers() {
		return METHOD_MODIFIERS;
	}
	
	private static boolean isModifier(List<Keyword> list, String keyword) {
		return is(keyword) && list.contains(valueOf(keyword.toUpperCase()));
	}
	public static boolean isClassModifier(String keyword) {
		return isModifier(CLASS_MODIFIERS, keyword);
	}
	
	public static boolean isFieldModifier(String keyword) {
		return isModifier(FIELD_MODIFIERS, keyword);
	}
	
	public static boolean isMethodModifier(String keyword) {
		return isModifier(METHOD_MODIFIERS, keyword);
	}
	
	
	
	private static final String regex = String.join("|", keywords());
	
	private static final Keyword[] modifiers = {STATIC, FINAL, PUBLIC, PRIVATE, PROTECTED, ABSTRACT};
	static final int LONGEST = CONTINUE.name().length();
			
	private static String[] keywords() {
		Keyword[] keywords = values();
		String[] v = new String[keywords.length];
		for(int i = 0; i < v.length; i++)
			v[i] = keywords[i].name().toLowerCase();
		return v;
	}

	static List<Keyword> modifiers() {
		return Arrays.asList(modifiers);
	}
	
	static boolean is(String str) {
		return str.matches(regex);
	}
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public char getAccelerator() {
		return toString().charAt(0);
	}


	public boolean isEqual(String text) {
		return name().toLowerCase().equals(text);
	}
		
	public boolean isEqual(Text label) {
		return isEqual(label.getText());
	}


	@Override
	public int length() {
		return name().length();
	}


	@Override
	public char charAt(int index) {
		return name().charAt(index);
	}


	@Override
	public CharSequence subSequence(int start, int end) {
		return name().substring(start, end);
	}

	public static Keyword match(String last) {
		return Keyword.valueOf(last.toUpperCase());
	}

	public static Keyword[] array(List<String> insertTokens) {
		Keyword[] set = new Keyword[insertTokens.size()];
		for(int i = 0; i < insertTokens.size(); i++)
			set[i] = Keyword.valueOf(insertTokens.get(i).toUpperCase());
		return set;
	}
}
