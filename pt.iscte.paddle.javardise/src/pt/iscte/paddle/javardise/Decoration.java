package pt.iscte.paddle.javardise;

import java.util.function.BiFunction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import pt.iscte.paddle.javardise.service.ICodeDecoration;

class Decoration<T extends Control> implements ICodeDecoration<T> {
	private Shell shell;
	private T control;

	Decoration(Control target, BiFunction<Composite, Control, T> f, BiFunction<Point, Point, Point> loc) {
		Shell parent = target.getShell();
		shell = new Shell(parent, SWT.NO_TRIM);
		shell.setLayout(new FillLayout());
		control = f.apply(shell, target);
		shell.pack();
		setLocation(target, loc, control);
		
		parent.addControlListener(new ControlAdapter() {
			public void controlMoved(ControlEvent e) {
				if(!shell.isDisposed())
					setLocation(target, loc, control);
			}
			public void controlResized(ControlEvent e) {
				if(!shell.isDisposed())
					setLocation(target, loc, control);
			}
		});
		
		target.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if(!shell.isDisposed())
					setLocation(target, loc, control);
			}
		});
	}

	void setAlpha(int alpha) {
		shell.setAlpha(alpha);
	}

	void setBackground(Color color) {
		shell.setBackground(color);
	}
	
	@Override
	public T getControl() {
		return control;
	}
	
	private void setLocation(Control target, BiFunction<Point, Point, Point> loc, Control c) {
		Point targetSize = target.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		targetSize.x += 5;
		targetSize.y += 5;
		Point decoratorSize = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shell.setLocation(target.toDisplay(loc.apply(targetSize, decoratorSize)));
	}

	public void setText(String text) {
		if(control instanceof Text) {
			((Text) control).setText(text);
			shell.pack();
		}
	}
	
	public void show() {
		shell.setVisible(true);
	}
	
	public void hide() {
		shell.setVisible(false);
	}

	public void delete() {
		shell.dispose();
	}
	
}