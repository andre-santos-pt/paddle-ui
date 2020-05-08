package pt.iscte.paddle.javardise;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.service.IClassWidget;
import pt.iscte.paddle.javardise.service.IDeclarationWidget;
import pt.iscte.paddle.javardise.service.IJavardiseService;
import pt.iscte.paddle.javardise.service.IMethodWidget;
import pt.iscte.paddle.javardise.service.IWidget;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IVariableDeclaration;

public class JavardiseService implements IJavardiseService {
	public IClassWidget createClassWidget(Composite parent, IModule module) {
		return new ClassWidget(parent, module);
	}
	
	public IMethodWidget createMethodWidget(Composite parent, IProcedure procedure) {
		return new MethodWidget(parent, procedure);
	}
	
	// TODO record
	
	public IDeclarationWidget getDeclarationWidget(IVariableDeclaration varDeclaration) {
		return (IDeclarationWidget) EditorWidget.map.get(varDeclaration);
	}
	
	public IWidget getWidget(IProgramElement element) {
		return EditorWidget.map.get(element);
	}
}
