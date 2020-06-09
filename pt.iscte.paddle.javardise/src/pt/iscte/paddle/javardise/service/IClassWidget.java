package pt.iscte.paddle.javardise.service;

import java.util.function.Consumer;

import pt.iscte.paddle.model.IProcedure;

public interface IClassWidget extends IWidget, ICodeElement {

	IWidget getClassName();
	
	IMethodWidget getProcedure(IProcedure procedure);

	void toCode(StringBuffer buffer);
	
	void addSelectionListener(Consumer<IWidget> l);

	
	
}
