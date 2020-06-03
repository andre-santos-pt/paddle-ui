package pt.iscte.paddle.javardise;

import pt.iscte.paddle.model.IProgramElement;

public enum Flag {
	CONSTRUCTOR, INSTANCE, FOR, ELSE;

	public void set(IProgramElement e) {
		e.setProperty(name());
	}
	
	public void setIf(IProgramElement e, boolean test) {
		if(test)
			e.setFlag(name());
	}
	
	public boolean is(String s) {
		return name().equals(s);
	}
	
	public boolean is(IProgramElement ... elements) {
		for(IProgramElement e : elements)
			if(!e.is(name()))
				return false;
		return true;
	}
	
	public boolean isNot(IProgramElement ... elements) {
		for(IProgramElement e : elements)
			if(e.is(name()))
				return false;
		return true;
	}
}
