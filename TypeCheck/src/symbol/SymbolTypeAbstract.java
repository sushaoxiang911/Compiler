package symbol;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

public class SymbolTypeAbstract extends SymbolType {
	String extendClass;
	Hashtable<String, String> classVariable;
	Hashtable<String, SymbolType> classMethod;
	HashSet<String> parentClass;
	
	public SymbolTypeAbstract() {
		classVariable = new Hashtable<String, String> ();
		classMethod = new Hashtable<String, SymbolType> ();
		parentClass = new HashSet<String> ();
		extendClass = "";
	}
	
	public HashSet<String> getParentClass() {
		return parentClass;
	}
	
	public void setExtendClass (String className) {
		extendClass = className;
	}
	
	public void addVariable(String varName, String symbolName) {
		classVariable.put(varName, symbolName);
	}
	
	public void addClassMethod(String methodName, SymbolType symbolType) {
		classMethod.put(methodName, symbolType);
	}
	
	public String getExtendClass() {
		return extendClass;
	}

	public Hashtable<String, String> getVariable () {
		return classVariable;
	}
	public Hashtable<String, SymbolType> getClassMethod() {
		return classMethod;
	}
	
	public String toString() {
		String result = getName() + ": " + "extend: " + extendClass;
		result += " variables: ";
		for (String key : classVariable.keySet()) {
			result = result + " [" + key + ", " + classVariable.get(key) + "], "; 
		}
		result += " methods: ";
		for (String key : classMethod.keySet()) {
			result = result + " [" + key + ", " + classMethod.get(key).toString() + "], ";
		}
		
		result += " parent class >=: ";
		for (String name : parentClass) {
			result = result + name + ", ";
		}
		return result;
	}
	
}
