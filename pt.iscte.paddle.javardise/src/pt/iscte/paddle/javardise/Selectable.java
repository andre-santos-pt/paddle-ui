package pt.iscte.paddle.javardise;

import java.util.WeakHashMap;
import java.util.function.Function;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;

import pt.iscte.paddle.model.IProgramElement;

public class Selectable <T extends EditorWidget> extends EditorWidget {
 
		public interface Icon {
			void remove();
		}
		
		private static final WeakHashMap<IProgramElement, Selectable<?>> map = new WeakHashMap<>();
		
		public static void mark(IProgramElement e, Color c) {
			Selectable<?> s = map.get(e);
			if(s != null)
				s.mark(c);
		}
		
		public static void unmark(IProgramElement e) {
			Selectable<?> s = map.get(e);
			if(s != null)
				s.unmark();
		}
		
		public static void removeAllMarks() {
			map.values().forEach(s -> s.unmark());
		}
		
		public static void addNote(IProgramElement e, String message) {
			Selectable<?> s = map.get(e);
			if(s != null)
				s.addNote(message);
		}
		
		public static Icon addIcon(IProgramElement e, Image icon, String tooltip) {
			Selectable<?> s = map.get(e);
			if(s != null)
				return s.addIcon(icon, tooltip);
			return null;
		}
		
		private Composite border;
		
		final T target;

		
		Selectable(Composite parent, Function<Composite, T> f, IProgramElement e) {
			super(parent);
			map.put(e, this);

			setLayout(new FillLayout());
			border = new Composite(this, SWT.NONE);
			FillLayout fillLayout = new FillLayout();
			fillLayout.marginHeight = 2;
			fillLayout.marginWidth = 2;
			border.setLayout(fillLayout);
			border.setBackground(Constants.COLOR_BACKGROUND);
			target = f.apply(border);

			
		}
		
		
		Icon addIcon(Image icon, String tooltip) {
			final ControlDecoration dec = new ControlDecoration(target, SWT.LEFT | SWT.TOP);
			dec.setMarginWidth(10);
			dec.setDescriptionText(tooltip);
			dec.setImage(icon);
			return () -> { dec.dispose(); target.requestLayout(); };
		}
		
		void addNote(String message) {
			Shell shellTip = new Shell(getDisplay());
			ToolTip tip = new ToolTip(shellTip, SWT.BALLOON);
			tip.setMessage(message);
			Point loc = shellTip.toDisplay(getLocation());
			tip.setLocation(loc.x + getSize().x, loc.y);
			tip.setAutoHide(true);
			tip.setVisible(true);
			
		}
		
		void removeNote() {
			
		}
		
		
		void mark(Color color) {
			border.setBackground(color);
		}
		
		void unmark() {
			border.setBackground(Constants.COLOR_BACKGROUND);
		}
		
		// TODO ideia
		void setToolTip(String message) {
			target.setToolTipText(message);
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
