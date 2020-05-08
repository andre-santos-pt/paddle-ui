package pt.iscte.paddle.javardise.tests;

import pt.iscte.paddle.model.IModule;
import pt.iscte.paddle.model.IProcedure;

public abstract class Test {
	
	IModule module;
	IProcedure procedure;
	
	public IModule getModule() {
		return module;
	}
	
	public IProcedure getProcedure() {
		return procedure;
	}
	
}
