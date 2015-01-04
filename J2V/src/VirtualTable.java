import java.util.Vector;


public class VirtualTable {
	String name;
	Vector<String> method;
	public VirtualTable(String name, Vector<String> method) {
		super();
		this.name = createVtableName(name);
		this.method = method;
	}
	public VirtualTable(String name) {
		super();
		this.name = createVtableName(name);
		method = new Vector<String>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Vector<String> getMethod() {
		return method;
	}
	public void setMethod(Vector<String> method) {
		this.method = method;
	}
	public boolean isEmpty() {
		return method.isEmpty();
	}
	
	public String createVtableName(String className) {
		return "vmt_" + className;
	}
	
	public String toString() {
		String result = "";
		result = result + name + ": [";
		for (String temp : method) {
			result = result + temp + " ";
		}
		result += "] ";
		return result;
	}
	
	public int findMethod(String methodName) {
		for (int i = 0; i < method.size(); ++i) {
			String temp = method.get(i);
			if (methodName.equals(temp.substring(temp.lastIndexOf('.') + 1)))
				return i;
		}
		return -1;
	}
	
	public String formatting() {
		String result = "";
		result = result + "const " + name + '\n';
		for (String temp : method) {
			result = result + "  :" + temp + '\n'; 
		}
		return result;
	}
	
}
