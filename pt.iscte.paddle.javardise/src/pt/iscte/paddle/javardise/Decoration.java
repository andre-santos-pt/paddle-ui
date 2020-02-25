package pt.iscte.paddle.javardise;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Decoration {
	private Shell s;

	Decoration(EditorWidget target, Function<Composite, Control> f, BiFunction<Point, Point, Point> loc) {
		Shell parent = target.getShell();
		s = new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP);
		s.setLayout(new FillLayout());
		Control c = f.apply(s);
		s.pack();
		setLocation(target, loc, c);
		
		parent.addControlListener(new ControlAdapter() {
			public void controlMoved(ControlEvent e) {
				if(!s.isDisposed())
					setLocation(target, loc, c);
			}
		});
	
	}

	private void setLocation(EditorWidget target, BiFunction<Point, Point, Point> loc, Control c) {
		Point targetSize = target.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		targetSize.x += 5;
		targetSize.y += 5;
		Point decoratorSize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		s.setLocation(target.getParent().toDisplay(loc.apply(targetSize, decoratorSize)));
	}

	public void show() {
		s.setVisible(true);
	}
	
	public void hide() {
		s.setVisible(false);
	}

	public void delete() {
		s.dispose();
	}

	
	
	public enum Location implements BiFunction<Point, Point, Point> {
		LEFT((t,d) -> new Point(-d.x, t.y/2 - d.y/2)),
		RIGHT((t,d) -> new Point(t.x, t.y/2 - d.y/2)),
		TOP((t,d) -> new Point(t.x/2 - d.x/2, -d.y)),
		BOTTOM((t,d) -> new Point(t.x/2 - d.x/2, d.y));

		BiFunction<Point, Point, Point> f;

		private Location(BiFunction<Point, Point, Point> f) {
			this.f = f;
		}

		public Point apply(Point targetSize, Point decoratorSize) {
			return f.apply(targetSize, decoratorSize);
		}
	}
}