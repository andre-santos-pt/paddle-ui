package pt.iscte.pidesco.cfgviewer.ext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IEntityConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;

import pt.iscte.paddle.model.cfg.IBranchNode;
import pt.iscte.paddle.model.cfg.IControlFlowGraph;
import pt.iscte.paddle.model.cfg.INode;
import pt.iscte.paddle.model.cfg.IStatementNode;
import pt.iscte.pidesco.cfgviewer.internal.CFGBranchFigure;
import pt.iscte.pidesco.cfgviewer.internal.CFGExitFigure;
import pt.iscte.pidesco.cfgviewer.internal.CFGFigure;
import pt.iscte.pidesco.cfgviewer.internal.CFGLayout;

public class CFGViewer extends Composite {
	
	private GraphViewer gv;
	private IStyleProvider ics;
	
	private Map<INode, CFGFigure> figures = new HashMap<>();
	
	public CFGViewer(Composite viewArea) {
		this(viewArea, new StyleProvider());
	}
	
	
	public CFGViewer(Composite viewArea, IStyleProvider ics) {
		super(viewArea, SWT.NONE);
		setLayout(new FillLayout());
		gv = new GraphViewer(this, SWT.BORDER);
		this.ics = ics;

		gv.setContentProvider(new GraphNodeContentProvider());
		gv.setLabelProvider(new GraphLabelContentProvider());
		gv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			private Map<INode, CFGFigure> highlights = new HashMap<>();		//Currently highlighted nodes
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				System.out.println(event.getStructuredSelection().getFirstElement());
				
				if(!highlights.isEmpty()) {
					highlights.forEach((node, figure) -> {					//Reset colors from previous highlights 
						figure.setBackgroundColor(ics.getNodeColor(node));
						LineBorder border = (LineBorder) figure.getBorder();
						border.setColor(ics.getNodeBorderColor(node));
					});
					highlights.clear();
				}
				
				for(Object obj : event.getStructuredSelection().toList()) {	//List of nodes to highlight
					if(!(obj instanceof INode)) continue;					//Only nodes require highlight
					INode node = (INode) obj;
					CFGFigure figure = figures.get(node);
					if(figure != null) {									//Entry and Exit Nodes are null
						figure.setBackgroundColor(ics.getHighlightedNodeColor(node));
						LineBorder border = (LineBorder) figure.getBorder();
						border.setColor(ics.getHighlightedNodeBorderColor(node));
						highlights.put(node, figure);
					}
				}
			}	
		});
		
		gv.setLayoutAlgorithm(new CFGLayout(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		gv.applyLayout();
	}
	
	public void setInput(IControlFlowGraph icfg) {
		gv.setInput(icfg.getNodes());
	}
	
	//TODO Completar
//	public void selectNodes(Iterable<INode> it) {
////		System.out.println(it);
//		System.out.println(gv.findGraphItem(it.iterator().next()));
//		
////		List<GraphItem> nodes = new ArrayList<>();
////		it.forEach(node -> nodes.add(gv.findGraphItem(node)));
//		StructuredSelection selection = new StructuredSelection(it);
////		gv.setSelection(selection, true);											//Não ativa o selectionChangedListener?
//		gv.getGraphControl().setSelection(new GraphItem[] {gv.findGraphItem(it.iterator().next())});
//	}
	
	private class GraphNodeContentProvider extends ArrayContentProvider implements IGraphEntityContentProvider {

		@Override
		public Object[] getConnectedTo(Object entity) {
			List<INode> connections = new LinkedList<>();
			connections.add(((INode) entity).getNext());
			
			if(entity instanceof IBranchNode) 
				connections.add(((IBranchNode) entity).getAlternative());
			
			return connections.toArray();
		}
		
		@Override
		public Object[] getElements(Object inputElement) {
			return super.getElements(inputElement);
		}
	}
	
	private class GraphLabelContentProvider extends LabelProvider implements IFigureProvider, IEntityConnectionStyleProvider {
		
		@Override
		public int getConnectionStyle(Object src, Object dest) {
			if(src instanceof IBranchNode && ((IBranchNode)src).getAlternative().equals(dest)) {
				return ZestStyles.CONNECTIONS_DASH | ZestStyles.CONNECTIONS_DIRECTED;
			}
			return ZestStyles.CONNECTIONS_DIRECTED;
		}

		@Override
		public Color getColor(Object src, Object dest) {
			return ics.getConnectionColor((INode)src, (INode)dest);
		}

		@Override
		public Color getHighlightColor(Object src, Object dest) {
			return ics.getHighlightColor((INode)src, (INode)dest);
		}

		@Override
		public int getLineWidth(Object src, Object dest) {
			return 2;
		}

		@Override
		public IFigure getTooltip(Object entity) {
//			return new CFGFigure("Tooltip");
			return null;
		}

		@Override
		public IFigure getFigure(Object element) {
			if (element instanceof IStatementNode) {
				IStatementNode node = (IStatementNode) element;
				CFGFigure figure = new CFGFigure(node.getElement().toString(), ics.getNodeColor(node), ics.getNodeBorderColor(node), ics.getNodeTextColor());
				figures.put(node, figure);
				return figure;
			} else if (element instanceof IBranchNode) {
				IBranchNode node = (IBranchNode) element;
				CFGBranchFigure figure = new CFGBranchFigure(node.getElement().toString(), ics.getNodeColor(node), ics.getNodeBorderColor(node), ics.getNodeTextColor());
				figures.put(node, figure);
				return figure;
			} else {
				INode node = (INode) element;
				if(node.isEntry()) 
					return new CFGExitFigure(ics.getStartNodeColor());
				else 
					return new CFGExitFigure(ics.getEndNodeColor());
			}
		}
		
		@Override
		public String getText(Object element) {
			if(element instanceof EntityConnectionData) {
				EntityConnectionData ecd = (EntityConnectionData) element;
				return ics.getConnectionText((INode)ecd.source, (INode)ecd.dest);
			}
			return "";
		}
	}
	
	private static class StyleProvider implements IStyleProvider {}		//Empty class to use IStyleProvider default values
}
