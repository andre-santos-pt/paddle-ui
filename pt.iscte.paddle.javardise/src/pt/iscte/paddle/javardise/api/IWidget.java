package pt.iscte.paddle.javardise.api;

import java.util.function.BiFunction;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public interface IWidget {

	Control getControl();
	
	Object getProgramElement();
	
	void setReadOnly(boolean readonly);
	
	ICodeDecoration<Canvas> addMark(Color color);

	ICodeDecoration<Label> addImage(Image image, BiFunction<Point, Point, Point> loc);

	ICodeDecoration<Text> addNote(String text, BiFunction<Point, Point, Point> loc);

	ICodeDecoration<Canvas> addRegionMark(Color color, IWidget ... following);
	
	ICodeDecoration<Canvas> addArrow(IWidget target);
	
	<T extends Control> ICodeDecoration<T> addDecoration(BiFunction<Composite,Control,T> f, BiFunction<Point, Point, Point> loc);

	String getSource();
}

