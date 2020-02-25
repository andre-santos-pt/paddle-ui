package pt.iscte.paddle.javardise;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.model.IProgramElement;

public interface MarkerService {

	public class Mark {
		final Iterable<IProgramElement> elements;
		Color color;
		
		Mark(Color color, Iterable<IProgramElement> elements) {
			this.elements = elements;
			this.color = color;
		}
		
		public void mark() {
			elements.forEach(e -> {
				Markable<?> s = Markable.map.get(e);
				if (s != null)
					s.mark(color);
			});
		}
		
		public void unmark() {
			elements.forEach(e -> MarkerService.unmark(e));
		}
		
		public void changeColor(Color c) {
			color = c;
			mark();
		}
	}
	
	static Mark mark(Color c, IProgramElement ... e) {
		return mark(c, Arrays.asList(e));
	}

	static Mark mark(Color c, Iterable<IProgramElement> elements) {
		Mark mark = new Mark(c, elements);
		mark.mark();
		return mark;
	}
	
	static void unmark(IProgramElement e) {
		Markable<?> s = Markable.map.get(e);
		if (s != null)
			s.unmark();
	}

	static void unmarkAll() {
		Markable.map.values().forEach(s -> s.unmark());
	}

	
	
	
	
	static Decoration addDecoration(IProgramElement e, Image image, BiFunction<Point, Point, Point> loc) {
		return addDecoration(e, p -> {
			Label l = new Label(p, SWT.NONE);
			l.setImage(image);
			return l;
		}, loc);
	}

	static Decoration addDecoration(IProgramElement e, String text, BiFunction<Point, Point, Point> loc) {
		return addDecoration(e, p -> createToolip(p, text), loc);
	}

	private static Text createToolip(Composite parent, String text) {
		Text t = new Text(parent, SWT.BORDER);
		t.setText(text);
		t.setEnabled(false);
		t.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		t.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		return t;
	}

	static Decoration addDecoration(IProgramElement e, Function<Composite,Control> f, BiFunction<Point, Point, Point> loc) {
		Markable<?> s = Markable.map.get(e);
		if (s != null)
			return s.addDecoration(f, loc);
		return null;
	}
		
}
