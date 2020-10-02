package pt.iscte.paddle.javaeditor.api;

import java.util.function.Consumer;

import pt.iscte.paddle.javardise.api.ICodeElement;
import pt.iscte.paddle.javardise.api.IWidget;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IRecordType;

public interface IClassWidget extends IWidget<IRecordType>, ICodeElement {

	IWidget getClassName();
	
	IMethodWidget getProcedure(IProcedure procedure);

	void toCode(StringBuffer buffer);
	
	void addSelectionListener(Consumer<IWidget<IRecordType>> l);

	
	
}
