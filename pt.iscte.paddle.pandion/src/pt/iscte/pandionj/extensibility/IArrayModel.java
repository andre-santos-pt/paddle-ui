package pt.iscte.pandionj.extensibility;

import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;

import pt.iscte.paddle.pandion.PrimitiveType;

public interface IArrayModel<T> extends IEntityModel {
	int getLength();
	int getDimensions();
	String getComponentType();
	Object getValues();
	boolean isMatrix();
	Dimension getMatrixDimension();
	
	boolean isDecimal();
	T getElementModel(int index);
	List<T> getModelElements();
	Iterator<Integer> getValidModelIndexes();
	boolean isValidModelIndex(int i);
	default boolean isPrimitiveType() {
		return getDimensions() == 1 && PrimitiveType.isPrimitive(getComponentType());
	}
	
	default boolean isReferenceType() {
		return getDimensions() > 1 || !PrimitiveType.isPrimitive(getComponentType());
	}
}
