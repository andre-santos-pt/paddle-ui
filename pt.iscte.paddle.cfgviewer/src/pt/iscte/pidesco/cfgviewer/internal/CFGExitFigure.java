package pt.iscte.pidesco.cfgviewer.internal;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class CFGExitFigure extends Ellipse {
	
//	private static Color backgroundColor = new Color(null,255,255,206);
	
/*	public CFGExitFigure(String text) {
		Label l = new Label(text);
		
		setSize(100, 50);
		setBackgroundColor(backgroundColor);
		setOpaque(true);
		setLineWidth(2);
		
		BorderLayout layout = new BorderLayout();
		setLayoutManager(layout);
		
		add(l, BorderLayout.CENTER);
	}*/
	
	public CFGExitFigure(Color color) {
		setSize(30, 30);
		setBackgroundColor(color);
		setOpaque(true);
		setAntialias(SWT.ON);
	}

}
