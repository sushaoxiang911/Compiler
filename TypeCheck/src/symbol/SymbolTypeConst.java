package symbol;

import java.util.HashSet;

public class SymbolTypeConst extends SymbolType {
	
	HashSet<String> parentClass;
	
	public SymbolTypeConst(String name) {
		setName(name);
		parentClass = new HashSet<String> ();
		parentClass.add(name);
	}
	
	public void setName(String name) {
		if (!name.equals("int[]") && !name.equals("int") && !name.equals("boolean")){
			System.out.println("SymbolTypeConst type error!");
			System.out.println("Type Error!");
			System.exit(0);
		}
		super.setName(name);
	}
	
	public String toString() {
		return getName();
	}

	// for const type check
	public HashSet<String> getParentClass() {
		return parentClass;
	}
}
