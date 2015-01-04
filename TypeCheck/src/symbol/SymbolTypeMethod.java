package symbol;

import java.util.Hashtable;
import java.util.Vector;

public class SymbolTypeMethod extends SymbolType {
	String returnType;
	Hashtable<String, String> methodArgumentVariable;
	
	Vector<String> argumentList;
	
	public void addArgument(String argumentType) {
		argumentList.add(argumentType);
	}
	
	public Vector<String> getArgument() {
		return argumentList;
	}
	
	public SymbolTypeMethod() {
		methodArgumentVariable = new Hashtable<String, String>();
		argumentList = new Vector<String>();
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	
	
	public void addVariable(String name, String symbolName) {
		methodArgumentVariable.put(name, symbolName);
	}
	
	public String getReturnType() {
		return returnType;
	}

	public Hashtable<String, String> getVariable() {
		return methodArgumentVariable;
	}
	
	public boolean equals(SymbolType m) {
//		System.out.println("this returnType: " + this.returnType + " that return type: " + m.getReturnType());
		return (this.returnType.equals(m.getReturnType()) && this.argumentList.equals(m.getArgument()));
	}
	
	public String toString() {
		String result = "resturn type: " + returnType + "; ";
		result += "argument and variable: ";
		for (String key : methodArgumentVariable.keySet()) {
			result = result + " [" + key + ", " + methodArgumentVariable.get(key) + "], "; 
		}
		
		result += "argument order: ";
		for (String type : argumentList) {
			result = result + type + ", ";
		}
		return result;
	}
}
