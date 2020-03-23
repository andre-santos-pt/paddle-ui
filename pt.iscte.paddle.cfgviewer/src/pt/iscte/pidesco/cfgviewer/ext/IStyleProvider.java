package pt.iscte.pidesco.cfgviewer.ext;

import org.eclipse.swt.graphics.Color;

import pt.iscte.paddle.model.cfg.INode;

public interface IStyleProvider {
	
	/*********** Node ***********/
	
	/**
	 * 
	 * @param Node Node to be colored
	 * @return Returns the color to be used as the given node's background
	 */
	Color getNodeColor(INode node);
	
	/**
	 * 
	 * @param Node Node to be colored
	 * @return	Returns the color to be used as the given node's border color
	 */
	Color getNodeBorderColor(INode node);
	
	/**
	 * 
	 * @return Returns the color to be used as the start node color
	 */
	Color getStartNodeColor();
	
	/**
	 * 
	 * @return Returns the color to be used as the final node color
	 */
	Color getEndNodeColor();
	
	/*********** Connection ***********/
	
	/**
	 * 
	 * @param source Node where the connection starts
	 * @param destination Node where the connection ends
	 * @return Returns the color of a connection between the two nodes
	 */
	Color getConnectionColor(INode source, INode destination);
	
	/**
	 * 
	 * @param source Node where the connection starts
	 * @param destination Node where the connection ends
	 * @return Returns the text for a connection between the two nodes
	 */
	String getConnectionText(INode source, INode destination);
	
	/**
	 * 
	 * @param source Node where the connection starts
	 * @param destination Node where the connection ends
	 * @return Returns the color when the connection the two nodes is highlighted
	 */
	Color getHighlightColor(INode source, INode destination);
}
