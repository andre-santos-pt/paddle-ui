package pt.iscte.paddle.javardise;

import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.paddle.model.IProgramElement;

class Markable <T extends EditorWidget> extends EditorWidget {

	static final WeakHashMap<IProgramElement, Markable<?>> map = new WeakHashMap<>();

	private Composite border;

	final T target;


	Markable(Composite parent, Function<Composite, T> f, IProgramElement e) {
		super(parent);
		map.put(e, this);

		setLayout(new FillLayout());
		border = new Composite(this, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = Constants.MARK_MARGIN;
		fillLayout.marginWidth = Constants.MARK_MARGIN;
		border.setLayout(fillLayout);
		border.setBackground(Constants.COLOR_BACKGROUND);
		target = f.apply(border);
	}

	Decoration addDecoration(Function<Composite, Control> f, BiFunction<Point, Point, Point> loc) {
		return new Decoration(target, f, loc);
	}

	void mark(Color color) {
		border.setBackground(color);
	}

	void unmark() {
		border.setBackground(Constants.COLOR_BACKGROUND);
	}

	@Override
	public void toCode(StringBuffer buffer) {
		target.toCode(buffer);
	}

	public void toCode(StringBuffer buffer, int level) {
		target.toCode(buffer, level);
	}

	@Override
	public boolean setFocus() {
		return target.setFocus();
	}
}
