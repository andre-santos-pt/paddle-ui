package pt.iscte.paddle.javardise.service;

import pt.iscte.paddle.model.IProcedure;

public interface IClassWidget extends IWidget, ICodeElement {

	IWidget getClassName();
	
	IMethodWidget getProcedure(IProcedure procedure);

	void toCode(StringBuffer buffer);

}
