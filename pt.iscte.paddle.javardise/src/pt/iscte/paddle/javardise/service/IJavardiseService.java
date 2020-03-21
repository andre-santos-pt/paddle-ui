package pt.iscte.paddle.javardise.service;

import org.eclipse.swt.widgets.Composite;

import pt.iscte.paddle.javardise.ClassWidget;
import pt.iscte.paddle.javardise.EditorWidget;
import pt.iscte.paddle.javardise.MethodWidget;
import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IProgramElement;
import pt.iscte.paddle.model.IVariableDeclaration;

public interface IJavardiseService {

	static IClassWidget createClassWidget(Composite parent, IModule module) {
		return new ClassWidget(parent, module);
	}
	
	static IMethodWidget createMethodWidget(Composite parent, IProcedure procedure) {
		return new MethodWidget(parent, procedure);
	}
	
	// TODO record
	
	static IDeclarationWidget getDeclarationWidget(IVariableDeclaration varDeclaration) {
		return (IDeclarationWidget) EditorWidget.map.get(varDeclaration);
	}
	
	static IWidget getWidget(IProgramElement element) {
		return EditorWidget.map.get(element);
	}
	
	
}
