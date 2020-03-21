package pt.iscte.paddle.javardise.service;

import java.util.function.BiFunction;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public interface ICodeDecoration<T extends Control> {
	void show();
	void hide();
	void delete();
	
	T getControl();
	
	default void setTooltip(String text) {
		getControl().setToolTipText(text);
	}
	
 	public enum Location implements BiFunction<Point, Point, Point> {
		LEFT((t,d) -> new Point(-d.x, t.y/2 - d.y/2)),
		LEFT_TOP((t,d) -> new Point(-d.x, t.y - d.y)),
		LEFT_BOTTOM((t,d) -> new Point(-d.x, t.y/2)),
		RIGHT((t,d) -> new Point(t.x, t.y/2 - d.y/2)),
		TOP((t,d) -> new Point(t.x/2 - d.x/2, -d.y)),
		BOTTOM((t,d) -> new Point(t.x/2 - d.x/2, d.y)),
		ORIGIN((t,d) -> new Point(0,0));

		BiFunction<Point, Point, Point> f;

		private Location(BiFunction<Point, Point, Point> f) {
			this.f = f;
		}

		public Point apply(Point targetSize, Point decoratorSize) {
			return f.apply(targetSize, decoratorSize);
		}
	}
}
