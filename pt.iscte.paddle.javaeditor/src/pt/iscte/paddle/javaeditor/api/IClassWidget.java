package pt.iscte.paddle.javaeditor.api;

import java.util.function.Consumer;

import pt.iscte.paddle.javardise.api.IWidget;
import pt.iscte.paddle.model.IProcedure;
import pt.iscte.paddle.model.IRecordType;

public interface IClassWidget extends IWidget {

	IWidget getClassName();
	
	IMethodWidget getProcedure(IProcedure procedure);

	void addSelectionListener(Consumer<IWidget> l);
}
