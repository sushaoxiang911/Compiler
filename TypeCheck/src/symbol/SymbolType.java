package symbol;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;


public class SymbolType {
	String typeName;
	
	public void typeError() {
		System.out.println("Not the required type: " + typeName);
		System.out.println("Type Error!");
		System.exit(0);
	}
	
	
	public void setName(String name) {  
		typeName = name;
	}
	public String getName() {
		return typeName;
	}
	
	
	// for SymbolTypeAbstract
	public void setExtendClass (String className) {
		typeError();
	}
	
	public void addClassMethod(String methodName, SymbolType symbolType) {
		typeError();
	}
	public String getExtendClass() {
		typeError();
		return null;
	}
	
	public Hashtable<String, SymbolType> getClassMethod() {
		typeError();
		return null;
	}
	
	public HashSet<String> getParentClass() {
		typeError();
		return null;
	}
	
	
	// for method
	public void setReturnType(String returnType) {
		typeError();
	}
	public String getReturnType() {
		typeError();
		return null;
	}
	
	public void addArgument(String argumentType) {
		typeError();
	}
	
	public Vector<String> getArgument() {
		typeError();
		return null;
	}
	
	
	
	// for method, class and main
	public void addVariable(String varName, String typeName) {
		typeError();
	}
	public Hashtable<String, String> getVariable () {
		typeError();
		return null;
	}
	
	// equals
	public boolean equals(SymbolType t) {
		return true;
	}
	
	
	// for test
	public String toString() {
		return "";
	}
	
	
	
}
