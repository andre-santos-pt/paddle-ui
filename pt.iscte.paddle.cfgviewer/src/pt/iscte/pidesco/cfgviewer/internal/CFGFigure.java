package pt.iscte.pidesco.cfgviewer.internal;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.graphics.Color;

public class CFGFigure extends Figure {
	
	public CFGFigure(String text, Color backgroundColor, Color borderColor, Color textColor) {
		BorderLayout layout = new BorderLayout();
		
		setLayoutManager(layout);
		setBackgroundColor(backgroundColor);
		setOpaque(true);
		
		LineBorder border = new LineBorder(borderColor, 2, Graphics.LINE_SOLID);
		setBorder(border);
		
		setPreferredSize(100, 25);
		setSize(100, 25);
		
		Label l = new Label(text);
		l.setForegroundColor(textColor);
		l.setLabelAlignment(PositionConstants.CENTER);
		add(l, BorderLayout.CENTER);
	}
}