package pt.iscte.pidesco.cfgviewer.internal;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.swt.graphics.Color;

public class CFGBranchFigure extends CFGFigure{

	public CFGBranchFigure(String text, Color backgroundColor, Color borderColor) {
		super(text, backgroundColor, borderColor);
		
		LineBorder border = (LineBorder) getBorder();
		border.setStyle(Graphics.LINE_DASH);
	}
}
