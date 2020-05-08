module pt.iscte.paddle.cfgviewer {
	exports pt.iscte.pidesco.cfgviewer.ext;
	exports pt.iscte.pidesco.cfgviewer.internal;
	
	requires org.eclipse.swt.cocoa.macosx;
	
	requires java.desktop;
	requires org.eclipse.core.commands;
	requires org.eclipse.draw2d;
	requires org.eclipse.jface;
	requires org.eclipse.ui.workbench;
	requires org.eclipse.zest.core;
	requires org.eclipse.zest.layouts;
	requires pt.iscte.paddle.model;
}