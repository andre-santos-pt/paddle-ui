package pt.iscte.paddle.javardise.service;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IModuleView;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IVariableDeclaration;

public interface IJavardiseService {

	IClassWidget createClassWidget(Composite parent, IModule module, String namespace);

	default IClassWidget createClassWidget(Composite parent, IModule module) {
		return createClassWidget(parent, module, module.getId());
	}

	default IClassWidget createClassWidgetScroll(Composite parent, IModule module, String namespace, Consumer<ScrolledComposite> c) {
		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setLayout(new GridLayout(1, false));
		scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		IClassWidget w = createClassWidget(scroll, module, namespace);
		scroll.setContent(w.getControl());
		scroll.setMinSize(100, 100);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		w.getControl().addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point size = w.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				size.x += 100;
				size.y += 100;
				scroll.setMinSize(size);
				scroll.requestLayout();
			}
		});
		c.accept(scroll);
		return w;
	}
	
	IMethodWidget createMethodWidget(Composite parent, IProcedure procedure);

	// TODO record

	IDeclarationWidget getDeclarationWidget(IVariableDeclaration varDeclaration);

	IWidget getWidget(IProgramElement element);


}
