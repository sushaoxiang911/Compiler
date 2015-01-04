import syntaxtree.Goal;
import visitor.DepthFirstVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class J2V {
    public static void main(String[] Args) {
        File testFile = new File("test/test1.java"); // create a folder called tests, and put the tests in it
        MiniJavaParser parse = null;

        FileInputStream in;
		try {
			in = new FileInputStream(testFile.getAbsoluteFile());
			parse = new MiniJavaParser(in);
	        Goal g = MiniJavaParser.Goal();
			VtableVisitor buildVisitor = new VtableVisitor();
			g.accept(buildVisitor);
			Hashtable<String, ClassType> table = buildVisitor.getTable();
			handleClassHierarchy(table);
			
//			for (String key : table.keySet()) {
//            	System.out.println(table.get(key).toString());
//            }
//			
			writeVtable(table);
			CompileVisitor  compileVisitor = new CompileVisitor(table);
			g.accept(compileVisitor);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
       
    }
    
    public static void writeVtable(Hashtable<String, ClassType> table) {
    	for (String key : table.keySet()) {
    		ClassType currentType = table.get(key);
    		VirtualTable currentTable = currentType.getTable();
    		if (!currentTable.isEmpty()) {
    			System.out.println(currentTable.formatting());
    		}
    	}
    }
    
    
    public static void handleClassHierarchy(Hashtable<String, ClassType> table) {
    	for (String key : table.keySet()) {
    		ClassType currentType = table.get(key);
    		handleClassHierarchyHelp(currentType, table);
    	}
    }
    
    public static void handleClassHierarchyHelp(ClassType type, Hashtable<String, ClassType> table) {
    	String extendName = type.getExtendName();
    	if (extendName.equals("")) {
    		return;
    	}
    	ClassType extendType = table.get(extendName);
    	handleClassHierarchyHelp(extendType, table);
    	Vector<String> variable = new Vector<String>();
    	Hashtable<String, String> symbolTable = new Hashtable<String, String>();
    	
    	Vector<String> extendVariable = extendType.getVariable();
    	Hashtable<String, String> extendSymbolTable = extendType.getSymbolTable();
    	for (String temp : extendVariable) {
    		variable.add(temp);
    		symbolTable.put(temp, extendSymbolTable.get(temp));
    	}
    	
    	Vector<String> currentVariable = type.getVariable();
    	Hashtable<String, String> currentSymbolTable = type.getSymbolTable();
    	for (int i = 0; i < currentVariable.size(); ++i) {
    		String temp1 = currentVariable.elementAt(i);
    		if (!variable.contains(temp1)) {
    			variable.add(temp1);
    		} 
			symbolTable.put(temp1, currentSymbolTable.get(temp1));
    	}
    	type.setVariable(variable);
    	type.setSymbolTable(symbolTable);
    	
    	Vector<String> method = new Vector<String>();
    	VirtualTable extendTable = extendType.getTable();
    	Vector<String> extendMethod = extendTable.getMethod();
    	for (String temp : extendMethod) {
    		method.add(temp);
    	}
    	VirtualTable currentTable = type.getTable();
    	Vector<String> currentMethod = currentTable.getMethod();
    	for (int i = 0; i < currentMethod.size(); ++i) {
    		String currentMethodName = currentMethod.elementAt(i);
    		currentMethodName = currentMethodName.substring(currentMethodName.lastIndexOf('.') + 1);
//			System.out.println(currentMethodName);
//    		System.out.println(method.toString());
//    		System.out.println(method.get(0));
    		for (int j = 0; j < extendMethod.size(); ++j) {
    			String extendMethodName = method.elementAt(j);
    			extendMethodName = extendMethodName.substring(extendMethodName.lastIndexOf('.') + 1);
//    			System.out.println(currentMethodName);
//    			System.out.println(extendMethodName);
    			if (currentMethodName.equals(extendMethodName)) {
//    				System.out.println("aaaa");
    				method.set(j, currentMethod.elementAt(i));
    			} else {
    				method.add(currentMethod.elementAt(i));
    			}
    		}
//    		System.out.println(method.toString());
    	}
    	type.setTable(new VirtualTable(type.getName(), method));
    }
    
}