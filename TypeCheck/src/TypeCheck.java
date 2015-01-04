import symbol.SymbolType;
import symbol.SymbolTypeAbstract;
import syntaxtree.Goal;
import visitor.DepthFirstVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class TypeCheck {
    public static void main(String[] Args) {
        File testFile = new File("test/test19.java"); // create a folder called tests, and put the tests in it
        MiniJavaParser parse = null;

        FileInputStream in;
		try {
			in = new FileInputStream(testFile.getAbsoluteFile());
			
			parse = new MiniJavaParser(in);
	        Goal g = MiniJavaParser.Goal();
	        MiniJavaDepthFirstVisitor buildVisitor = new MiniJavaDepthFirstVisitor();
	        buildVisitor.visit(g);
	        Hashtable<String, SymbolType> hashtable = buildVisitor.getSymbolTable();

	        handleClassHierarchy(hashtable);
	        for (String key : hashtable.keySet()) {
	        	System.out.println(hashtable.get(key).toString());
	        }
	        
	        MiniJavaGJDepthFirst checkVisitor = new MiniJavaGJDepthFirst(hashtable);
	        checkVisitor.visit(g, null);
	        
	        System.out.println("No error!");
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        /*
        for (final File fileEntry : testDir.listFiles()) {
            if (fileEntry.isFile()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(fileEntry.getAbsoluteFile());
                }
                catch (FileNotFoundException err) {
                    err.printStackTrace();
                }

                try {
                    System.out.println("Processing: " + fileEntry.getName());
                    if (null == parse) {
                        parse = new MiniJavaParser(in);
                    }
                    else {
                        parse.ReInit(in);
                    }
                    Goal g = MiniJavaParser.Goal();
                    MiniJavaDepthFirstVisitor buildVisitor = new MiniJavaDepthFirstVisitor();
                    buildVisitor.visit(g);
                    Hashtable<String, SymbolType> hashtable = buildVisitor.getSymbolTable();
    
                    handleClassHierarchy(hashtable);
                    for (String key : hashtable.keySet()) {
                    	System.out.println(hashtable.get(key).toString());
                    }
                    
                    MiniJavaGJDepthFirst checkVisitor = new MiniJavaGJDepthFirst(hashtable);
                    checkVisitor.visit(g, null);
                }
                catch (ParseException e) {
                    System.out.println(e.toString());
                }
            }
        }*/
    }
    static void handleClassHierarchy(Hashtable<String, SymbolType> hashtable) {
    	Hashtable<String, SymbolType> classTable = new Hashtable<String, SymbolType>();
    	for (String key : hashtable.keySet()) {
    		if (!key.equals("int[]") && !key.equals("int") && !key.equals("boolean")){
    			classTable.put(key, hashtable.get(key));
    		}
    	}
    	checkCycle(classTable);
    	checkOverloading(classTable);
    	variableInheritance(classTable);
    	buildClassHierarchy(classTable);
    }
    
    
    static void checkCycleHelp(Hashtable<String, SymbolType> hashtable, 
    		Set<String> currentRecord, SymbolType type) {
    	String className = type.getName();
    	String extendName = type.getExtendClass();
    	if (extendName.equals("")) {
    		return;
    	}
    	if (currentRecord.contains(extendName)) {
    		System.out.println("cycle");
    		typeError();
    	}
    	// currentRecord add accessed type
    	currentRecord.add(className);
    	if (!hashtable.containsKey(extendName)) {
    		System.out.println("no extend class found " + extendName);
    		typeError();
    	}
    	// get the extend type
    	SymbolType extendType = hashtable.get(extendName);
    	checkCycleHelp(hashtable, currentRecord, extendType);
    	
    }
    
    // this function checks extend class existence and cycle
    static void checkCycle(Hashtable<String, SymbolType> hashtable) {
    	for (String key : hashtable.keySet()) {
    		SymbolType currentType = hashtable.get(key);
    		Set<String> currentRecord = new HashSet<String>();
    		checkCycleHelp(hashtable, currentRecord, currentType);
    	}
    }
    
    static Hashtable<String, SymbolType> checkOverloadingHelp(Hashtable<String, SymbolType> hashtable, SymbolType type) {
    	String className = type.getName();
    	String extendName = type.getExtendClass();
    	if (extendName.equals(""))
    		return type.getClassMethod();
    	
    	Hashtable<String, SymbolType> currentMethodTable = type.getClassMethod();
    	SymbolType extendType = hashtable.get(extendName);
    	Hashtable<String, SymbolType> parentMethodTable = checkOverloadingHelp(hashtable, extendType);
    	for (String key : parentMethodTable.keySet()) {
    		// check if current table contains the key
    		if (!currentMethodTable.containsKey(key)) {
    			// not exist
    			// not need to copy???? I think so
    			currentMethodTable.put(key, parentMethodTable.get(key));
    		} else {
    			// exist
    			// check overload
    			
    			SymbolType currentMethodType = currentMethodTable.get(key);
    			SymbolType parentMethodType = parentMethodTable.get(key);
    			if (!currentMethodType.equals(parentMethodType)) {
    				System.out.println("overloading");
    				typeError();
    			}
    		}
    	}
    	return currentMethodTable;
    }
    
    // check overloading and put parent functions into the field
    static void checkOverloading(Hashtable<String, SymbolType> hashtable){
    	for (String key : hashtable.keySet()) {
    		SymbolType currentType = hashtable.get(key);
    		checkOverloadingHelp(hashtable, currentType);
    	}
    }
    
    
    
    static Hashtable<String, String> variableInheritanceHelp(Hashtable<String, SymbolType> hashtable, SymbolType type) {
    	String className = type.getName();
    	String extendName = type.getExtendClass();
    	if (extendName == "")
    		return type.getVariable();
    	Hashtable<String, String> currentVariableTable = type.getVariable();
    	SymbolType extendType = hashtable.get(extendName);
    	Hashtable<String, String> parentVariableTable = variableInheritanceHelp(hashtable, extendType);
    	
    	for (String key : parentVariableTable.keySet()) {
    		// not exist in the current field
    		// change the method should be at last
    		if (!currentVariableTable.containsKey(key)) {
    			currentVariableTable.put(key, parentVariableTable.get(key));
//    			// add it to the method field
//    			Hashtable<String, SymbolType> currentTypeMethodTable = type.getClassMethod();
//    			for (SymbolType method : currentTypeMethodTable.values()) {
//    				Hashtable<String, String> currentMethodVariableTable = method.getVariable();
//    				if (!currentMethodVariableTable.containsKey(key)) {
//    					currentMethodVariableTable.put(key, parentVariableTable.get(key));
//    				}
//    			}
    		}
    	}
    	
    	return currentVariableTable;
    }
    static void variableInheritance(Hashtable<String, SymbolType> hashtable) {
    	for (String key : hashtable.keySet()) {
    		SymbolType currentType = hashtable.get(key);
    		variableInheritanceHelp(hashtable, currentType);
    	}
    }
    static HashSet<String> buildClassHierarchyHelp(Hashtable<String, SymbolType> hashtable, SymbolType type) {
    	String className = type.getName();
    	String extendName = type.getExtendClass();
    	
    	HashSet<String> parentClass = type.getParentClass();
    	
    	// if the current class has the set set up(not zero)
    	if (!type.getParentClass().isEmpty()) {
    		return parentClass;
    	}
    	// if base
    	// add the type name of self
    	if (extendName.equals("")) {
    		parentClass.add(className);
    		return parentClass;
    	}
    	// else
    	SymbolType extendType = hashtable.get(extendName);
    	HashSet<String> extendParentClass = buildClassHierarchyHelp(hashtable, extendType);
    	// add to this class
    	for (String typeName : extendParentClass) {
    		parentClass.add(typeName);
    	}
    	// add it self
    	parentClass.add(className);
    	return parentClass;
    }
    
    static void buildClassHierarchy(Hashtable<String, SymbolType> hashtable) {
    	for (String key : hashtable.keySet()) {
    		SymbolType currentType = hashtable.get(key);
    		buildClassHierarchyHelp(hashtable, currentType);
    	}
    }
    
    
    static void typeError() {
    	System.out.println("Type error!");
		System.exit(0);
    }
}