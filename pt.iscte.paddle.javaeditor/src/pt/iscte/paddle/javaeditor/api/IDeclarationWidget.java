package pt.iscte.paddle.javaeditor.api;

import pt.iscte.paddle.javardise.api.IWidget;
import pt.iscte.paddle.model.IProgramElement;

public interface IDeclarationWidget extends IWidget<IProgramElement> {
	IWidget<IProgramElement> getVariableType();
	IWidget<IProgramElement> getVariableName();
}
