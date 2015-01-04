import java.util.Hashtable;
import java.util.Vector;


public class ClassType {
	String name;
	String extendName;
	Vector<String> variable;
	Hashtable<String, String> symbolTable;
	VirtualTable table;
	public ClassType(String name, String extendName, Vector<String> variable, 
			Hashtable<String, String> symbolTable, VirtualTable table) {
		super();
		this.name = name;
		this.extendName = extendName;
		this.variable = variable;
		this.symbolTable = symbolTable;
		this.table = table;
	}
	public ClassType(String name, String extendName, VirtualTable table) {
		super();
		this.name = name;
		this.extendName = extendName;
		symbolTable = new Hashtable<String, String>();
		variable = new Vector<String>();
		this.table = table;
	}
	

	public String toString() {
		String result = "";
		result += "className: " + name;
		result += " ";
		result += "extendName: " + extendName;
		result += " ";
		result += "variable: [";
		for (String temp : variable) {
			result = result  + temp + "-" + symbolTable.get(temp) + " ";
		}
		result += "] ";
		result += table.toString();
		return result;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExtendName() {
		return extendName;
	}
	public void setExtendName(String extendName) {
		this.extendName = extendName;
	}
	public Vector<String> getVariable() {
		return variable;
	}
	public void setVariable(Vector<String> variable) {
		this.variable = variable;
	}
	public VirtualTable getTable() {
		return table;
	}
	public void setTable(VirtualTable table) {
		this.table = table;
	}
	
	
	public Hashtable<String, String> getSymbolTable() {
		return symbolTable;
	}
	public void setSymbolTable(Hashtable<String, String> symbolTable) {
		this.symbolTable = symbolTable;
	}
	
}
