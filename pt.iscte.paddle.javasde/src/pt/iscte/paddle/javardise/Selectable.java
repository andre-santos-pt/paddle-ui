package pt.iscte.paddle.javardise;

import java.util.WeakHashMap;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IProgramElement;

class Selectable <T extends EditorWidget> extends EditorWidget {
 
		static WeakHashMap<IProgramElement, Selectable<?>> map = new WeakHashMap<>();
		private Composite border;
		final T target;
		
		public Selectable(Composite parent, Function<Composite, T> f, IProgramElement e) {
			super(parent);
			border = new Composite(this, SWT.NONE);
			FillLayout fillLayout = new FillLayout();
			fillLayout.marginHeight = 2;
			fillLayout.marginWidth = 2;
			border.setLayout(fillLayout);
			border.setBackground(Constants.COLOR_BACKGROUND);
			target = f.apply(border);
			map.put(e, this);
		}
		
		public void mark(Color color) {
			border.setBackground(color);
		}
		
		public void unmark() {
			border.setBackground(Constants.COLOR_BACKGROUND);
		}
		
		@Override
		public void toCode(StringBuffer buffer) {
			target.toCode(buffer);
		}
		
		static void removeAllMarks() {
			map.values().forEach(s -> s.unmark());
		}
		
		
	}
