package pt.iscte.pidesco.cfgviewer.ext;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import pt.iscte.paddle.model.cfg.IBranchNode;
import pt.iscte.paddle.model.cfg.INode;

public interface IStyleProvider {
	
	/*********** Node ***********/
	
	/**
	 * 
	 * @param Node Node to be colored
	 * @return Returns the color to be used as the given node's background
	 */
	default Color getNodeColor(INode node) {
		return ColorConstants.tooltipBackground;
	}
	
	/**
	 * 
	 * @param Node Node to be colored
	 * @return	Returns the color to be used as the given node's border color
	 */
	default Color getNodeBorderColor(INode node) {
		return ColorConstants.black;
	}
	
	/**
	 * 
	 * @return Returns the color to be used as the start node color
	 */
	default Color getStartNodeColor() {
		return ColorConstants.black;
	}
	
	/**
	 * 
	 * @return Returns the color to be used as the final node color
	 */
	default Color getEndNodeColor() {
		return ColorConstants.black;
	}
	
	/**
	 * 
	 * @return Returns the color to be used as the given node's text color
	 */
	default Color getNodeTextColor() {
		return ColorConstants.black;
	}
	
	/*********** Connection ***********/
	
	/**
	 * 
	 * @param source Node where the connection starts
	 * @param destination Node where the connection ends
	 * @return Returns the color of a connection between the two nodes
	 */
	default Color getConnectionColor(INode source, INode destination) {
		if(source instanceof IBranchNode) {
			IBranchNode branch = (IBranchNode) source;
			if(destination.equals(branch.getAlternative()))
				return ColorConstants.darkGreen;
			else
				return ColorConstants.red;
		} else {
			return ColorConstants.black;
		}
	}
	
	/**
	 * 
	 * @param source Node where the connection starts
	 * @param destination Node where the connection ends
	 * @return Returns the text for a connection between the two nodes
	 */
	default String getConnectionText(INode source, INode destination) {
		if(source instanceof IBranchNode) {
			IBranchNode branch = (IBranchNode) source;
			if(destination.equals(branch.getAlternative()))
				return "true";
			else
				return "false";
		}
		
		return "";
	}
	
	/**
	 * 
	 * @param source Node where the connection starts
	 * @param destination Node where the connection ends
	 * @return Returns the color when the connection the two nodes is highlighted
	 */
	default Color getHighlightColor(INode source, INode destination) {
		return ColorConstants.blue;
	}
}
