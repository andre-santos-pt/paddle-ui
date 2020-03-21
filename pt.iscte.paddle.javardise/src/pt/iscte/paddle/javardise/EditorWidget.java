package pt.iscte.paddle.javardise;
import static java.lang.System.lineSeparator;

import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.IObjectContributor;

import pt.iscte.paddle.javardise.service.ICodeDecoration;
import pt.iscte.paddle.javardise.service.ICodeElement;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.model.IProgramElement;

public class EditorWidget extends Composite implements ICodeElement, IWidget {

	public static final WeakHashMap<IProgramElement, EditorWidget> map = new WeakHashMap<>();

	IProgramElement element;

	EditorWidget(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(Constants.ROW_LAYOUT_H_ZERO);
		setBackground(Constants.COLOR_BACKGROUND);
		element = null;
	}

	EditorWidget(Composite parent, IProgramElement element) {
		this(parent);
		this.element = element;
		if(element != null) 
			map.put(element, this);
		else
			System.err.println(this.getClass());
	}

	public void setReadOnly(boolean readonly) {
		setEnabled(!readonly);
	}

	@Override
	public Control getControl() {
		return this;
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
	//	public void toCode(StringBuffer buffer) {
	//		buffer.append("#TODO" + this.getClass().getSimpleName() + "#");
	//		System.err.println("missing toCode " + this.getClass());
	//	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		toCode(b);
		return b.toString();
	}

	SequenceWidget getOwnerSequence() {
		Composite parent = getParent();
		while(!(parent instanceof SequenceWidget))
			parent = parent.getParent();

		assert parent != null : "not applicable";
		return (SequenceWidget) parent;
	}


	public ICodeDecoration<Canvas> addMark(Color color) {
		class Rec extends Canvas {
			Control control;
			Rec(Composite parent, Control control) {
				super(parent, SWT.NONE);
				this.control = control;
				Point dim = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
				addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.setForeground(color);
						e.gc.setLineWidth(3);
						e.gc.drawRectangle(0, 0, dim.x+2, dim.y+2);
					}
				});

			}
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Point size = control.computeSize(wHint, hHint, changed);
				size.x += 4;
				size.y += 4;
				return size;
			}

			@Override
			public Point computeSize(int wHint, int hHint) {
				return computeSize(wHint, hHint, true);
			}
		}
		ICodeDecoration<Canvas> dec = addDecoration((p, c) -> new Rec(p, c), (t,d) -> new Point(-2,-2));
		if(dec != null) {
			Decoration<Canvas> d = (Decoration<Canvas>) dec;
			d.setAlpha(128);
			d.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
		}
		return dec;
	}
	
	public ICodeDecoration<Canvas> addRegionMark(Color color, IWidget ... following) {
		class Rec extends Canvas {
			Control control;
			Rec(Composite parent, Control control) {
				super(parent, SWT.NONE);
				this.control = control;
				setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
				addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.setForeground(color);
						e.gc.setLineWidth(3);
						Point r = calcRectangle(SWT.DEFAULT, SWT.DEFAULT, true, following);
						e.gc.drawRectangle(0, 0, r.x-3, r.y-3);
					}
				});

			}
			
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return calcRectangle(wHint, hHint, changed, following);
			}
			
			private Point calcRectangle(int wHint, int hHint, boolean changed, IWidget... following) {
				Point size = control.computeSize(wHint, hHint, changed);
				int maxX = size.x;
				int maxY = size.y;
				for(IWidget w : following) {
					Point targetSize = w.getControl().computeSize(wHint, hHint);
					Point targetLoc = w.getControl().getLocation();
					if(targetSize.x > maxX)
						maxX = targetSize.x;
					if(targetLoc.y + targetSize.y > maxY)
						maxY = targetLoc.y + targetSize.y;
				}
				size.x = maxX;
				size.y = maxY;
				return size;
			}

			@Override
			public Point computeSize(int wHint, int hHint) {
				return computeSize(wHint, hHint, true);
			}
		}
		ICodeDecoration<Canvas> dec = addDecoration((p, c) -> new Rec(p, c), (t,d) -> new Point(-2,-2));
		if(dec != null) {
			Decoration<Canvas> d = (Decoration<Canvas>) dec;
			d.setAlpha(128);
			d.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
		}
		return dec;
	}

	public ICodeDecoration<Label> addImage(Image image, BiFunction<Point, Point, Point> loc) {
		return addDecoration((p,c) -> {
			Label l = new Label(p, SWT.NONE);
			l.setImage(image);
			return l;
		}, loc);
	}

	public ICodeDecoration<Text> addNote(String text, BiFunction<Point, Point, Point> loc) {
		return addDecoration((p,c) -> {
			Text t = new Text(p, SWT.BORDER);
			t.setText(text);
			t.setEnabled(false);
			t.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			t.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			return t;
		}, loc);
	}

	public ICodeDecoration<Canvas> addArrow(IWidget targetWidget) {
		final int W = 40;
		class Arrow extends Canvas {
			Control from;
			Control target;
			Arrow(Composite parent, Control from) {
				super(parent, SWT.NONE);
				this.from = from;
				this.target = targetWidget.getControl();
				Point dim;
				int arrowY;
				if(target.getLocation().y < from.getLocation().y) {
					Control t = target;
					target = from;
					from = t;
					dim = target.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					arrowY = dim.y/2;
				}
				else {
					dim = from.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					arrowY = target.getLocation().y;
				}
				setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
				addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.drawArc(0, dim.y/2, (int) (W*1.5), y(), 90, 180);
						e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
						Path path = createArrowForLine( e.display, new Point( W, arrowY), 180, 15, 15 );
						e.gc.fillPath( path );
						path.dispose();
					}
				});

			}

			Path createArrowForLine( Device device, Point fromPoint, double rotationDeg, double length, double wingsAngleDeg ) {
				double ax = fromPoint.x;
				double ay = fromPoint.y;
				double radB = Math.toRadians( -rotationDeg + wingsAngleDeg );
				double radC = Math.toRadians( -rotationDeg - wingsAngleDeg );
				Path resultPath = new Path( device );
				resultPath.moveTo( ( float )( length * Math.cos( radB ) + ax ), ( float )( length * Math.sin( radB ) + ay ) );
				resultPath.lineTo( ( float )ax, ( float )ay );
				resultPath.lineTo( ( float )( length * Math.cos( radC ) + ax ), ( float )( length * Math.sin( radC ) + ay ) );
				return resultPath;
			}

			private int y() {
				Point size = target.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int y = target.getLocation().y - size.y/2;
//				if(y < from.getLocation().y)
//					y = -y;
				return y;
				
			}
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Point size = target.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				return new Point(W, target.getLocation().y + size.y);
			}

			@Override
			public Point computeSize(int wHint, int hHint) {
				return computeSize(wHint, hHint, true);
			}
		}
		ICodeDecoration<Canvas> dec = addDecoration(
				(p, c) -> new Arrow(p, c),
				targetWidget.getControl().getLocation().y < getLocation().y ?  ICodeDecoration.Location.LEFT_TOP : ICodeDecoration.Location.LEFT_BOTTOM);
//				(t,d) -> new Point(-W,0));
		if(dec != null) {
			Decoration<Canvas> d = (Decoration<Canvas>) dec;
			//			d.setAlpha(50);
			d.setBackground(Constants.COLOR_BACKGROUND);
		}

		return dec;
	}


	public <T extends Control> ICodeDecoration<T> addDecoration(BiFunction<Composite,Control,T> f, BiFunction<Point, Point, Point> loc) {
		return new Decoration<T>(getControl(), f, loc);
	}
}
