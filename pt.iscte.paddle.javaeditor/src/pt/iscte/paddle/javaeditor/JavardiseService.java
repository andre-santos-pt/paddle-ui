package pt.iscte.paddle.javaeditor;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javaeditor.api.IClassWidget;
import pt.iscte.paddle.javaeditor.api.IDeclarationWidget;
import pt.iscte.paddle.javaeditor.api.IJavardiseService;
import pt.iscte.paddle.javaeditor.api.IMethodWidget;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.api.IWidget;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IVariableDeclaration;

public class JavardiseService implements IJavardiseService {
	public IClassWidget createClassWidget(Composite parent, IModule module, String namespace) {
		return new ClassWidget(parent, module, namespace);
	}
	
	public IMethodWidget createMethodWidget(Composite parent, IProcedure procedure) {
		return new MethodWidget(parent, procedure);
	}
	
	// TODO record
	
	public IDeclarationWidget getDeclarationWidget(IVariableDeclaration varDeclaration) {
		return (IDeclarationWidget) EditorWidget.getWidget(varDeclaration);
	}
	
	public IWidget getWidget(IProgramElement element) {
		return EditorWidget.getWidget(element);
	}
}
