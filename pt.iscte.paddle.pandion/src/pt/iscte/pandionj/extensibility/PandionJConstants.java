package pt.iscte.pandionj.extensibility;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import pt.iscte.paddle.pandion.PandionJView;

public interface PandionJConstants {
	String PLUGIN_ID = PandionJView.class.getPackage().getName();
	String CONTEXT_ID = PLUGIN_ID + ".context";
	String VIEW_ID = PLUGIN_ID + ".view";
	String ARRAYTAG_EXTENSION_ID = PLUGIN_ID + ".arraytags";
	String TYPE_EXTENSION_ID = PLUGIN_ID + ".typewidgets";
	String VALUETAG_EXTENSION_ID =  PLUGIN_ID + ".valuetags";

	String ERROR_REPORT_MAIL = "pandionj@iscte-iul.pt";
	
	String IMAGE_FOLDER = "images";
	
	String MARKER_ID = PLUGIN_ID + ".marker";
	
	int STACK_LIMIT = 10; // TODO stack limit
	
	int MESSAGE_FONT_SIZE = 14;

	int ARROW_EDGE = 4;
	int ARROW_LINE_WIDTH = 1;
	int POSITION_WIDTH = 32;
	int POSITION_WIDTH_V = POSITION_WIDTH / 2;
	
	int CANVAS_MARGIN = 20;
	int NODE_SPACING = 100;

	Dimension OBJECT_CORNER = new Dimension(15, 15);
	int OBJECT_PADDING = 2;
	int OBJECT_MARGIN = 10;
	
	int BUTTON_FONT_SIZE = 12;

	int OBJECT_HEADER_FONT_SIZE = 14;

	int VALUE_FONT_SIZE = 16;
	String FONT_FACE = "Monospace";

	int POSITION_LINE_WIDTH = 1;
	int INDEX_FONT_SIZE = 10;

	int ARRAY_POSITION_SPACING = 2;
	int ARRAY_MARGIN = ARRAY_POSITION_SPACING*2;
	
	int ARRAY_LINE_WIDTH = 1;

	int STACKFRAME_LINE_WIDTH = 3;

	int STACKCOLUMN_MIN_WIDTH = 100;
	int STACK_TO_OBJECTS_GAP = 30;

	
	int ILLUSTRATION_LINE_WIDTH = 2;

	
	int VAR_FONT_SIZE = 18;

	interface Colors {
		Color OBJECT = new Color(Display.getDefault(), 225, 225, 225);
		Color OBJECT_HEADER_FONT = new Color(Display.getDefault(), 128, 128, 128);
		
		Color VARIABLE_BOX = new Color(Display.getDefault(), 255, 255, 255);

		Color ILLUSTRATION = ColorConstants.blue;

		Color ROLE_ANNOTATIONS = ColorConstants.gray;
		
		Color HIGHLIGHT = new Color(Display.getDefault(), 223, 234, 255);

		Color INST_POINTER = new Color(Display.getDefault(), 198, 219, 174);

		Color SELECT = new Color(Display.getDefault(), 0, 0, 200);

		Color VIEW_BACKGROUND = ColorConstants.white;
		Color ERROR = ColorConstants.red;
		
		Color FRAME_BORDER = new Color(Display.getDefault(), 200, 200, 200);
		
		Color OBSOLETE = new Color(Display.getDefault(), 240, 240, 240);

		Color TRUE = ColorConstants.darkGreen;
		Color FALSE = ColorConstants.red;
		
		Color[] ROLE_VARS = { ColorConstants.darkBlue, ColorConstants.darkGreen, ColorConstants.orange};
		Color CONSTANT = new Color(null, 160, 160, 160);

		static Color getVarColor(int i) {
			assert i >= 0;
			return i >= ROLE_VARS.length ? ColorConstants.black : ROLE_VARS[i];
		}
	}
	
	interface Messages {
		String START = "Illustrations will be displayed once a breakpoint is hit.";
		String INSTALLED_TAGS = "view installed tags (@)";
		String TRASH = "Simulates the behavior of Java's garbage collector, removing all the unferenced objects.";
		String RUN_DIALOG = "Do you want to open PandionJ view?";
		String COPY_CLIPBOARD = "Copy image to clipboard";
		String SET_ARRAY_MAX = "Set array maximum length";
		String PRESS_TO_INVOKE = "press Enter do execute";
		String CLEAR = "Clear";
		String INVOKE = "Invoke";
		String REPORT_BUG = "Report bug";
		
		static String prettyException(String name) {
			String excName = name.substring(name.lastIndexOf('.')+1);
			if(excName.endsWith("Exception"))
				excName = excName.substring(0, excName.length()-"Exception".length());
			
			return String.join(" ", excName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
		}
	}
	

	



	int COMBO_STRING_WIDTH = 200;

	int COMBO_WIDTH = 75;
	
	GridData TOP_ALIGN = new GridData(SWT.DEFAULT, SWT.BEGINNING, false, false);

	
	
	
	static GridLayout getOneColGridLayout() {
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return layout;
	}


	static final String TRASH_ICON = "trash.gif";
	

}
