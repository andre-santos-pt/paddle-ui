package pt.iscte.pidesco.cfgviewer.internal;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class CFGExitFigure extends Ellipse {
	
	public CFGExitFigure(Color color) {
		setSize(30, 30);
		setBackgroundColor(color);
		setOpaque(true);
		setAntialias(SWT.ON);
	}

}
