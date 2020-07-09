package pt.iscte.paddle.javardise;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IType;
import pt.iscte.paddle.model.IVariableDeclaration;

public interface Constants {

	int TAB = 25;
	int METHOD_SPACING = 30;
	
	String FONT_FACE = "Monaco";

	Color COLOR_FONT = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	Color COLOR_KEYWORD = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	Color COLOR_ERROR = new Color(Display.getDefault(), 200, 200, 200);
	Color COLOR_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	Color COLOR_COMMENT = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
	Color COLOR_LITERAL = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
//	Color COLOR_HIGHLIGHT = new Color(Display.getDefault(), 0, 200, 200);


	List<String> BINARY_OPERATORS = 		Arrays.asList("+", "-", "*", "/", "%", "==", "!=", "<", "<=", ">", ">=", "&&", "||", "^");
	List<String> ARITHMETIC_OPERATORS = 	Arrays.asList("+", "-", "*", "/", "%");
	List<String> RELATIONAL_OPERATORS = 	Arrays.asList("==", "!=", "<", "<=", ">", ">=");
	List<String> LOGICAL_OPERATORS = 		Arrays.asList("&&", "||", "^");

	List<String> UNARY_OPERATORS = Arrays.asList("!", "-", "+");  //"(int)", "(double)");
	
	int FONT_SIZE = 16;
	int MENU_KEY = SWT.SPACE;
	int DEL_KEY = SWT.BS;
	Font FONT_TINY = new Font(null, FONT_FACE, 10, SWT.NONE);
	Font FONT = new Font(null, FONT_FACE, FONT_SIZE, SWT.NONE);
	Font FONT_DOT = new Font(null, "Arial", FONT_SIZE, SWT.BOLD);
	Font FONT_KEYWORD = new Font(null, FONT_FACE, FONT_SIZE, SWT.BOLD);
	Font FONT_PH = new Font(null, FONT_FACE, FONT_SIZE, SWT.NONE);



	List<String> PRIMITIVE_TYPES = Arrays.asList("boolean", "int", "char", "double");

	Supplier<List<String>> PRIMITIVE_TYPES_SUPPLIER = () -> PRIMITIVE_TYPES;
	List<String> PRIMITIVE_TYPES_VOID = Arrays.asList("void", "boolean", "int", "char","double");

	Supplier<List<String>> PRIMITIVE_TYPES_VOID_SUPPLIER = () -> PRIMITIVE_TYPES_VOID;

	RowLayout ROW_LAYOUT_H_SHRINK = create(SWT.HORIZONTAL, -3);
	RowLayout ROW_LAYOUT_H_ZERO = create(SWT.HORIZONTAL, 2);
	RowLayout ROW_LAYOUT_H = create(SWT.HORIZONTAL, 3);
	RowLayout ROW_LAYOUT_H_DOT = create(SWT.HORIZONTAL, 0);
	RowLayout ROW_LAYOUT_V_ZERO = create(SWT.VERTICAL, 2);
	
	GridData ALIGN_TOP = new GridData(SWT.LEFT, SWT.TOP, false, false);
	
	static RowLayout create(int style, int spacing) {
		RowLayout layout = new RowLayout(style);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.spacing = spacing;
		return layout;
	}

	static boolean isLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	static boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	static Text createText(Composite parent, String text) {
		Text t = new Text(parent, SWT.NONE);
		t.setText(Constants.EMPTY_EXPRESSION_SERIALIZE.equals(text) ? "" : text);
		setFont(t, true);
		return t;
	}

	static void setFont(Control control, boolean init) {
		assert control instanceof Text || control instanceof Label;
		String text = control instanceof Text ? ((Text) control).getText() : ((Label) control).getText();
		if (Keyword.is(text)) {
			control.setFont(FONT_KEYWORD);
			control.setForeground(COLOR_KEYWORD);

		} else {
			control.setFont(FONT);
			control.setForeground(COLOR_FONT);
		}
		control.setBackground(COLOR_BACKGROUND);
	}

	GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);

	class GridDatas {
		final static GridData SHOW_GRID = new GridData(SWT.FILL, SWT.FILL, true, true);
		final static GridData HIDE_GRID = new GridData(SWT.LEFT, SWT.TOP, false, false);
		final static RowData SHOW_ROW = new RowData(SWT.DEFAULT, SWT.DEFAULT);
		final static RowData HIDE_ROW = new RowData(SWT.DEFAULT, SWT.DEFAULT);
		static {
			SHOW_GRID.exclude = false;
			HIDE_GRID.exclude = true;
			SHOW_ROW.exclude = false;
			HIDE_ROW.exclude = true;
			//			SHOW_ROW.width = SWT.DEFAULT;
			//			HIDE_ROW.width = 0;
			HIDE_ROW.height = 0;
		}
	}
	//	FocusListener ADD_HIDE = new FocusListener() {
	//		public void focusLost(FocusEvent e) {
	//			Control c = (Control) e.widget;
	//			c.setLayoutData(c.getParent().getLayout() instanceof GridLayout ? GridDatas.HIDE_GRID : GridDatas.HIDE_ROW);
	//			c.requestLayout();
	//		}
	//
	//		public void focusGained(FocusEvent e) {
	//			Control c = (Control) e.widget;
	//			c.setLayoutData(c.getParent().getLayout() instanceof GridLayout ? GridDatas.SHOW_GRID : GridDatas.SHOW_ROW);
	//			c.requestLayout();
	//		}
	//	};

	KeyListener LISTENER_ARROW_KEYS = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			Control focusControl = Display.getDefault().getFocusControl();
			if(focusControl != null && focusControl.getData() instanceof TextWidget) {
				TextWidget w = (TextWidget) focusControl.getData();
				Text text = w.getWidget();

				if(e.keyCode == SWT.ARROW_RIGHT && (!text.getEditable() || text.getText().length() == text.getCaretPosition() && text.getSelectionCount() == 0)) {
					if(text.traverse(SWT.TRAVERSE_TAB_NEXT)) {
						w.setAtLeft();
					}
					e.doit = false;
				}
				else if(e.keyCode == SWT.ARROW_LEFT && (!text.getEditable() || text.getCaretPosition() == 0 && text.getSelectionCount() == 0)) {
					if(text.traverse(SWT.TRAVERSE_TAB_PREVIOUS))
						w.setAtRight();
					e.doit = false;
				}
				else if(e.keyCode == SWT.ARROW_UP) {
					moveCursorUp(w);
					e.doit = false;
				}
				else if(e.keyCode == SWT.ARROW_DOWN) {
					moveCursorDown(w);
					e.doit = false;
				}
			}
		}
	};

	static void addArrowKeys(Control control, TextWidget widget) {
		control.addKeyListener(LISTENER_ARROW_KEYS);
		control.setData(widget);
	}

	KeyListener INSERT_LINE = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(e.character == SWT.CR) {
				TextWidget w = (TextWidget) e.widget.getData();
				Control statement = w.getStatement();
				SequenceWidget seq = (SequenceWidget) statement.getParent();
				if(w instanceof Token || (w.isAtBeginning() && !w.isAtEnd())) 
					seq.insertLineAt(statement);
				else
					seq.insertLineAfter(statement);
				e.doit = false;
			}
		}
	};

	static void addInsertLine(TextWidget widget) {
		widget.getWidget().addKeyListener(INSERT_LINE);
		widget.getWidget().setData(widget);
	}

	ModifyListener MODIFY_PACK = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			
//			((Control) e.widget).setLayoutData(new RowData(SWT.DEFAULT, SWT.DEFAULT));
			((Control) e.widget).pack();
			((Control) e.widget).requestLayout();
		}
	};

	static void addFocusSelectAll(Text e) {
		e.addFocusListener(FOCUS_SELECTALL);
	}

	FocusListener FOCUS_SELECTALL = new FocusAdapter() {
		public void focusGained(FocusEvent e) {
			((Text) e.widget).selectAll();
		}
	};

	String EMPTY_EXPRESSION_SERIALIZE = "$EMPTY$";


	static void moveCursorUp(TextWidget widget) {
		Control statement = widget.getStatement();
		if(statement != null) {
			SequenceWidget seq = (SequenceWidget) statement.getParent();
			seq.focusPreviousStatement(statement);
		}
	}

	static void moveCursorDown(TextWidget widget) {
		Control statement = widget.getStatement();
		if(statement != null) {
			SequenceWidget seq = (SequenceWidget) statement.getParent();
			seq.focusNextStatement(widget);
		}
	}

	static Composite createHeader(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(ROW_LAYOUT_H);
		c.setBackground(COLOR_BACKGROUND);
		return c;
	}

	static String variableId(IVariableDeclaration var) {
		return var.getId() == null ? "var$" + var.procedureIndex() :  var.getId();
	}

	static String matchBinaryOperator(char character) {
		for(String o : BINARY_OPERATORS)
			if(o.charAt(0) == character)
				return o;
		return null;
	}

	// TODO other types
	static boolean isType(String text) {
		return  IType.match(text) != null;
	}

	static boolean isNumber(String text) {
		try {
			Integer.parseInt(text);
			return true;
		}
		catch(NumberFormatException e) {
			try {
				Double.parseDouble(text);
				return true;
			}
			catch(NumberFormatException ex) {
				return false;
			}
		}
	}



}
