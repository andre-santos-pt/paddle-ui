package pt.iscte.paddle.javardise.service;

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
	
	IMethodWidget createMethodWidget(Composite parent, IProcedure procedure);
	
	// TODO record
	
	IDeclarationWidget getDeclarationWidget(IVariableDeclaration varDeclaration);
	
	IWidget getWidget(IProgramElement element);
	
	
}
