package pt.iscte.paddle.javardise;

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

	static void mark(IProgramElement e, Color c) {
		Markable<?> s = Markable.map.get(e);
		if (s != null)
			s.mark(c);
	}

	static void unmark(IProgramElement e) {
		Markable<?> s = Markable.map.get(e);
		if (s != null)
			s.unmark();
	}

	static void removeAllMarks() {
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
		t.setEditable(false);
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
