package pt.iscte.paddle.javasde;
import static java.lang.System.lineSeparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class EditorWidget extends Composite {

	public EditorWidget(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		setBackground(Constants.COLOR_BACKGROUND);
//		addPaintListener(new PaintListener() {
//			public void paintControl(PaintEvent e) {
//				GC gc=e.gc;
//				Color red=new Color(null,255,0,0);
//				gc.setForeground(red);
//			    Rectangle rect = getBounds();
////				Rectangle rect1 = new Rectangle(rect.x+2, rect.y+2, rect.width-2, rect.height-2);
//				gc.drawRectangle(rect);
////				gc.drawLine(0, 0, 100, 100);
//			}
//		});
	}
	
	// TODO to think
	void highlight() {
		setBackground(Constants.COLOR_HIGHLIGHT);
	}
	
	void unhighlight() {
		setBackground(Constants.COLOR_BACKGROUND);
	}
	
	// fragile
	MethodWidget getParentMethod() {
		EditorWidget e = this;
		while(!(e instanceof MethodWidget))
			e = (EditorWidget)e.getParent();
		
		return (MethodWidget) e;
	}
	

	void popup(Menu menu, Control control) {
		menu.setLocation(control.toDisplay(0, 40));
	}

//	void addTokenKeyHandler(KeyListener listener) {
//		
//	}
	
	void setSibling(Control sibling) {
		sibling.addFocusListener(new FocusListener() {
			Color prev;
			public void focusLost(FocusEvent e) {
				setBackground(prev);
			}
			public void focusGained(FocusEvent e) {
				prev = getBackground();
				setBackground(Constants.COLOR_HIGHLIGHT);
			}
		});
		addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				sibling.setBackground(Constants.COLOR_INSERT);
			}
			public void focusGained(FocusEvent e) {
				sibling.setBackground(Constants.COLOR_HIGHLIGHT);
			}
		});
	}
	
	void appendTabs(StringBuffer buffer, int n) {
		while(n-- > 0)
			buffer.append("\t");
	}
	
	public void toCode(StringBuffer buffer, int level) {
		appendTabs(buffer, level);
		toCode(buffer);
		buffer.append(lineSeparator());
	}

	// to override
	public void toCode(StringBuffer buffer) {
		buffer.append("#TODO" + this.getClass().getSimpleName() + "#");
		System.err.println("missing toCode " + this.getClass());
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		toCode(b);
		return b.toString();
	}
}
