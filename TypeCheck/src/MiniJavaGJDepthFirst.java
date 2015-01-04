

import java.util.Hashtable;
import java.util.Vector;

import symbol.SymbolType;
import syntaxtree.AllocationExpression;
import syntaxtree.AndExpression;
import syntaxtree.ArrayAllocationExpression;
import syntaxtree.ArrayAssignmentStatement;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.ArrayType;
import syntaxtree.AssignmentStatement;
import syntaxtree.Block;
import syntaxtree.BooleanType;
import syntaxtree.BracketExpression;
import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.CompareExpression;
import syntaxtree.Expression;
import syntaxtree.ExpressionList;
import syntaxtree.ExpressionRest;
import syntaxtree.FalseLiteral;
import syntaxtree.FormalParameter;
import syntaxtree.FormalParameterList;
import syntaxtree.FormalParameterRest;
import syntaxtree.Goal;
import syntaxtree.Identifier;
import syntaxtree.IfStatement;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.MainClass;
import syntaxtree.MessageSend;
import syntaxtree.MethodDeclaration;
import syntaxtree.MinusExpression;
import syntaxtree.NotExpression;
import syntaxtree.PlusExpression;
import syntaxtree.PrimaryExpression;
import syntaxtree.PrintStatement;
import syntaxtree.Statement;
import syntaxtree.ThisExpression;
import syntaxtree.TimesExpression;
import syntaxtree.TrueLiteral;
import syntaxtree.Type;
import syntaxtree.TypeDeclaration;
import syntaxtree.VarDeclaration;
import syntaxtree.WhileStatement;
import visitor.GJDepthFirst;

public class MiniJavaGJDepthFirst extends
		GJDepthFirst<String, Hashtable<String, String>> {
	Hashtable<String, SymbolType> symbolTable;
	// we still have to remember the class
	// so that we can find the method type
	SymbolType recentAccessed;
	Vector<String> passedValueList;
	String terminalToken = "";
	
	
	public MiniJavaGJDepthFirst(Hashtable<String, SymbolType> symbolTable) {
		super();
		this.symbolTable = symbolTable;
	}
	
	public void typeError() {
		System.out.println("Type error!");
		System.exit(0);
	}
	
	
	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
	public String visit(Goal n, Hashtable<String, String> argu) {
		// goal's environment is empty
		argu = new Hashtable<String, String>();
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> "public"
	 * f4 -> "static"
	 * f5 -> "void"
	 * f6 -> "main"
	 * f7 -> "("
	 * f8 -> "String"
	 * f9 -> "["
	 * f10 -> "]"
	 * f11 -> Identifier()
	 * f12 -> ")"
	 * f13 -> "{"
	 * f14 -> ( VarDeclaration() )*
	 * f15 -> ( Statement() )*
	 * f16 -> "}"
	 * f17 -> "}"
	 */
	public String visit(MainClass n, Hashtable<String, String> argu) {
		n.f1.accept(this, argu);
		String mainClassName = terminalToken;
		n.f14.accept(this, argu);
		
		SymbolType mainType = symbolTable.get(mainClassName);
		argu = mainType.getVariable();
		n.f15.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *       | ClassExtendsDeclaration()
	 */
	public String visit(TypeDeclaration n, Hashtable<String, String> argu) {
		n.f0.accept(this, argu);
		return null;
	}	

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 */
	public String visit(ClassDeclaration n, Hashtable<String, String> argu) {
		// get the identifier name
		n.f1.accept(this, argu);
		String className = terminalToken;
		
		// check the var declaration type is correct
		n.f3.accept(this, argu);
		
		// we have to record the class that we have here for the sub tree use
		recentAccessed = symbolTable.get(className);
		// get the environment
		argu = recentAccessed.getVariable();
		n.f4.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "extends"
	 * f3 -> Identifier()
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 -> "}"
	 */
	public String visit(ClassExtendsDeclaration n, Hashtable<String, String> argu) {
		// get the class name
		n.f1.accept(this, argu);
		String className = terminalToken;
		
		// may be not neccessary
		n.f3.accept(this, argu);
		
		// check the variable type
		n.f5.accept(this, argu);
		
		// get the class
		recentAccessed = symbolTable.get(className);
		// get the environment
		argu = recentAccessed.getVariable();
		n.f6.accept(this, argu);
		return null;
	}	

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n, Hashtable<String, String> argu) {
		// get the type name
		n.f0.accept(this, argu);
		String typeName = terminalToken;
		// check if we have such type
		if (!symbolTable.containsKey(typeName)) {
			System.out.println("varDeclaration type not existed, type: " + typeName);
			typeError();
		}
//		
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( FormalParameterList() )?
	 * f5 -> ")"
	 * f6 -> "{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 -> "return"
	 * f10 -> Expression()
	 * f11 -> ";"
	 * f12 -> "}"
	 */
	public String visit(MethodDeclaration n, Hashtable<String, String> argu) {
		// keep track the class
		SymbolType symbolTypeClass = recentAccessed;
		
		// get the return type
		n.f1.accept(this, argu);
		String returnTypeName = terminalToken;
		// check if the returnTypeName exist
		if (!symbolTable.containsKey(returnTypeName)) {
			System.out.println("methodDeclaration return type not exist");
			typeError();
		}
		// get the method name
		n.f2.accept(this, argu);
		String methodName = terminalToken;
		// for checking argument type 
		n.f4.accept(this, argu);
		// for checking var type
		n.f7.accept(this, argu);
		
		// get the method of the class
		Hashtable<String, SymbolType> methodTable = symbolTypeClass.getClassMethod();
		// get the current method
		SymbolType method = methodTable.get(methodName);
		
		// set the environment for the statement
		Hashtable<String, String> methodEnvir = method.getVariable();
		// merger the class environment;
		for (String key : argu.keySet()) {
			// if not include the variable
			if (!methodEnvir.containsKey(key)) {
				methodEnvir.put(key, argu.get(key));
			}
		}
		// set the environment
		argu = methodEnvir;
		n.f8.accept(this, argu);
		
		// check if the returned expression has the same type name of the method
		// expression return a type
		// only one 
		String tempReturnTypeName = n.f10.accept(this, argu);
		if (!returnTypeName.equals(tempReturnTypeName)){
			System.out.println("methodDeclaration return type not matched");
			typeError();
		}
		return null;
	}	

	/**
	 * f0 -> FormalParameter()
	 * f1 -> ( FormalParameterRest() )*
	 */
	public String visit(FormalParameterList n, Hashtable<String, String> argu) {
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
	public String visit(FormalParameter n, Hashtable<String, String> argu) {
		// check the type name
		n.f0.accept(this, argu);
		String typeName = terminalToken;
		
		
		if (!symbolTable.containsKey(typeName)) {
			System.out.println("FormalParameter type not exists");
			typeError();
		}
		
		// might not necessary
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 */
	public String visit(FormalParameterRest n, Hashtable<String, String> argu) {
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ArrayType()
	 *       | BooleanType()
	 *       | IntegerType()
	 *       | Identifier()
	 */
	public String visit(Type n, Hashtable<String, String> argu) {
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 */
	public String visit(ArrayType n, Hashtable<String, String> argu) {
		terminalToken = "int[]";
		return null;
	}

	/**
    * f0 -> "boolean"
    */
	public String visit(BooleanType n, Hashtable<String, String> argu) {
		terminalToken = "boolean";
		return null;
	}

	/**
	 * f0 -> "int"
	 */
	public String visit(IntegerType n, Hashtable<String, String> argu) {
		terminalToken = "int";
		return null;
	}

	/**
	 * f0 -> Block()
	 *       | AssignmentStatement()
	 *       | ArrayAssignmentStatement()
	 *       | IfStatement()
	 *       | WhileStatement()
	 *       | PrintStatement()
	 */
	public String visit(Statement n, Hashtable<String, String> argu) {
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "{"
	 * f1 -> ( Statement() )*
	 * f2 -> "}"
	 */
	public String visit(Block n, Hashtable<String, String> argu) {
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
	public String visit(AssignmentStatement n, Hashtable<String, String> argu) {
		// get the identifier name
		n.f0.accept(this, argu);
		String lvalue = terminalToken;
		if (!argu.containsKey(lvalue)) {
			System.out.println("AssignmentStatement lvalue not in the environment");
			typeError();
		}
		String lvalueTypeName = argu.get(lvalue);
		
		// 11/5 added we use the identifier, check if the type exist
		if (!symbolTable.containsKey(lvalueTypeName)) {
			System.out.println("Assignment lvalue not have a valid type");
			typeError();
		}
		
		
		// check the type match
		String rvalueTypeName = n.f2.accept(this, argu);
		SymbolType rvalueType = symbolTable.get(rvalueTypeName);
		
		// if the type not match
		if (!rvalueType.getParentClass().contains(lvalueTypeName)) {
			System.out.println("lvalueTypeName: " + lvalueTypeName);
			System.out.println("lvalue: " + lvalue);
			System.out.println("rvalueTypeName: " + rvalueTypeName);
			System.out.println("Assignment type mismatch");
			typeError();
		}
		return null;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "["
	 * f2 -> Expression()
	 * f3 -> "]"
	 * f4 -> "="
	 * f5 -> Expression()
	 * f6 -> ";"
	 */
	public String visit(ArrayAssignmentStatement n, Hashtable<String, String> argu) {
		// lvalueType
		String lvalueTypeName = n.f0.accept(this, argu);
		
		// the index
		String indexTypeName = n.f2.accept(this, argu);

		// rvalueType
		String rvalueTypeName = n.f5.accept(this, argu);

		if (!lvalueTypeName.equals("int[]") || !indexTypeName.equals("int") || !rvalueTypeName.equals("int")) {
			System.out.println("Identifier: " + lvalueTypeName);
			System.out.println("index: " + indexTypeName);
			System.out.println("rightside: " + rvalueTypeName);
			System.out.println("ArrayAssignmentStatement type error");
			typeError();
		}
		
		return null;
	}	

	/**
	 * f0 -> "if"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * f5 -> "else"
	 * f6 -> Statement()
	 */
	public String visit(IfStatement n, Hashtable<String, String> argu) {
		// check condition type
		String conditionTypeName = n.f2.accept(this, argu);
		if (!conditionTypeName.equals("boolean")) {
			System.out.println("IfStatement condition type error");
			typeError();
		}
		n.f4.accept(this, argu);
		n.f6.accept(this, argu);
		return null;
	}	

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	public String visit(WhileStatement n, Hashtable<String, String> argu) {
		// check the condition type
		String conditionTypeName = n.f2.accept(this, argu);
		if (!conditionTypeName.equals("boolean")) {
			System.out.println("WhileStatement condition type error");
			typeError();
		}
		
		n.f4.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	public String visit(PrintStatement n, Hashtable<String, String> argu) {
		// check the output type
		String outputTypeName = n.f2.accept(this, argu);
		if (!outputTypeName.equals("int")) {
			System.out.println("PrintStatement output type error");
			typeError();
		}
		return null;
	}	

	/**
	 * f0 -> AndExpression()
	 *       | CompareExpression()
	 *       | PlusExpression()
	 *       | MinusExpression()
	 *       | TimesExpression()
	 *       | ArrayLookup()
	 *       | ArrayLength()
	 *       | MessageSend()
	 *       | PrimaryExpression()
	 */
	public String visit(Expression n, Hashtable<String, String> argu) {
		// get from low level node of the expression type
		return n.f0.accept(this, argu);
	}	
	
	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(AndExpression n, Hashtable<String, String> argu) {
		// check two type
		String leftsideExpressionTypeName = n.f0.accept(this, argu);
		String rightsideExpressionTypeName = n.f2.accept(this, argu);
		if (!leftsideExpressionTypeName.equals("boolean") || !rightsideExpressionTypeName.equals("boolean")) {
			System.out.println("AndExpression type error");
			typeError();
		}
		// return the boolean type back
		return new String("boolean");
	}	

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(CompareExpression n, Hashtable<String, String> argu) {
		// get the left side
		String lvalueTypeName = n.f0.accept(this, argu);
		// get the right side
		String rvalueTypeName = n.f2.accept(this, argu);
		if (!lvalueTypeName.equals("int") || !rvalueTypeName.equals("int")) {
			System.out.println("CompareExpression type error");
			typeError();
		}
		return new String("boolean");
	}	

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n, Hashtable<String, String> argu) {
		// get the left side
		String lvalueTypeName = n.f0.accept(this, argu);
		// get the right side
		String rvalueTypeName = n.f2.accept(this, argu);
		if (!lvalueTypeName.equals("int") || !rvalueTypeName.equals("int")) {
			System.out.println("PlusExpression type error");
			typeError();
		}
		
		return new String("int");
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(MinusExpression n, Hashtable<String, String> argu) {
		// get the left side	
		String lvalueTypeName = n.f0.accept(this, argu);
		// get the right side
		String rvalueTypeName = n.f2.accept(this, argu);
		if (!lvalueTypeName.equals("int") || !rvalueTypeName.equals("int")) {
			System.out.println("MinusExpression type error");
			typeError();
		}
		return new String("int");
	}	

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(TimesExpression n, Hashtable<String, String> argu) {
		// get the left side
		String lvalueTypeName = n.f0.accept(this, argu);
		// get the right side
		String rvalueTypeName = n.f2.accept(this, argu);
		if (!lvalueTypeName.equals("int") || !rvalueTypeName.equals("int")) {
			System.out.println("TimesExpression type error");
			typeError();
		}
		
		return new String("int");
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	public String visit(ArrayLookup n, Hashtable<String, String> argu) {
		String identifierTypeName = n.f0.accept(this, argu);
		String indexTypeName = n.f2.accept(this, argu);
		
		if (!identifierTypeName.equals("int[]") || !indexTypeName.equals("int")) {
			System.out.println("identifierTypeName: " + identifierTypeName);
			System.out.println("indexTypeName: " + indexTypeName);
			System.out.println("ArrayLookup type error");
			typeError();
		}
		
		return new String ("int");
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	public String visit(ArrayLength n, Hashtable<String, String> argu) {
		String objectTypeName = n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		
		if (!objectTypeName.equals("int[]")) {
			System.out.println("ArrayLength type error");
			typeError();
		}
	
		return new String ("int");
	}	

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 */
	public String visit(MessageSend n, Hashtable<String, String> argu) {
		// the type from expression
		String objectTypeName = n.f0.accept(this, argu);

		// get the method hashtable, error out if it is not a class type
		// it should be in the symbolTable since it is valid expression so it has a type
		// for identifier, if it is in the environment, it's type must be in the symbolTable
		// we have checked it at very beginning
		if (!symbolTable.containsKey(objectTypeName)) {
			System.out.println("MessageSend, object has no valid type. This should not happen");
			typeError();
		}
		SymbolType objectType = symbolTable.get(objectTypeName);
		Hashtable<String, SymbolType> methodTable = objectType.getClassMethod();
		
		// methodName from terminalToken cache by the identifier
		n.f2.accept(this, argu);
		String methodName = terminalToken;
		
		if (!methodTable.containsKey(methodName)) {
			System.out.println("MessageSend the object does not have the method");
			typeError();
		}
		// get the method
		SymbolType methodType = methodTable.get(methodName);
		
		// check if the passedValue type and the argumentType are identical
		// we keep track of a vector of string
		passedValueList = new Vector<String>();
		n.f4.accept(this, argu);
		
		// first check size
		if (methodType.getArgument().size() != passedValueList.size()) {
			System.out.println("MessageSend the passedvalue not mathch the arguments in size");
			System.out.println("passed: " + passedValueList);
			System.out.println("argu: " + methodType.getArgument());
			typeError();
		}
		for (int i = 0; i < passedValueList.size(); ++i) {
			String passedTypeName = passedValueList.elementAt(i);
			// if passedType ! <= function argument type
			if (!symbolTable.containsKey(passedTypeName)) {
				System.out.println("MessageSend symbol table does not contain the type.");
				typeError();
			}
			SymbolType passedType = symbolTable.get(passedTypeName);
			if (!passedType.getParentClass().contains(methodType.getArgument().elementAt(i))) {
				System.out.println("MessageSend the passedvalue not mathch the arguments in ith type " + i);
				System.out.println("passed: " + passedValueList);
				System.out.println("argu: " + methodType.getArgument());
				typeError();
			}
		}
		
	
		return new String (methodType.getReturnType());
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public String visit(ExpressionList n, Hashtable<String, String> argu) {
		// add the first type inside
		String currentTypeName = n.f0.accept(this, argu);
		passedValueList.add(currentTypeName);
		
		// the remain in the list, add them
		n.f1.accept(this, argu);
		
		return null;
	}	

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public String visit(ExpressionRest n, Hashtable<String, String> argu) {
		passedValueList.add(n.f1.accept(this, argu));
		return null;
	}	

	/**
	 * f0 -> IntegerLiteral()
	 *       | TrueLiteral()
	 *       | FalseLiteral()
	 *       | Identifier()
	 *       | ThisExpression()
	 *       | ArrayAllocationExpression()
	 *       | AllocationExpression()
	 *       | NotExpression()
	 *       | BracketExpression()
	 */
	public String visit(PrimaryExpression n, Hashtable<String, String> argu) {
		// if return a null, means we cannot find a type for the identifier or other...
		//check here
		String expressionTypeName = n.f0.accept(this, argu);
		if (expressionTypeName == null) {
			System.out.println("the identifier is not in the environment");
			typeError();
		}
		// check here to see if the expression has a valid type
		if (!symbolTable.containsKey(expressionTypeName)) {
			System.out.println("the type of the identifier is not exist");
			typeError();
		}
		
		
		return expressionTypeName;
	}	

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n, Hashtable<String, String> argu) {
		return new String("int");
	}	

	/**
	 * f0 -> "true"
	 */
	public String visit(TrueLiteral n, Hashtable<String, String> argu) {
		return new String ("boolean");
	}	

	/**
	 * f0 -> "false"
	 */
	public String visit(FalseLiteral n, Hashtable<String, String> argu) {
		return new String ("boolean");
	}

	/**
    * f0 -> <IDENTIFIER>
    */
	public String visit(Identifier n, Hashtable<String, String> argu) {
		// we still set terminalToken for method name use
		// meanwhile we still return the identifier type
		// if we cannot find one, just return null for the upper layer return error
		// since that might be a name of method that will be used somewhere
		// or it is a true error.
		String name = n.f0.toString();
		terminalToken = name;
		// we find the type of the identifier
		if (argu.containsKey(name)) {
			return argu.get(name);
		}
		return null;
	}

	/**
	 * f0 -> "this"
	 */
	public String visit(ThisExpression n, Hashtable<String, String> argu) {
		if (!argu.containsKey("this")) {
			System.out.println("ThisExpression type error, should not happen");
			typeError();
		}
		return argu.get("this");
	}	

	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 */
	public String visit(ArrayAllocationExpression n, Hashtable<String, String> argu) {
		// check expression
		String lengthTypeName = n.f3.accept(this, argu);
		if (!lengthTypeName.equals("int")) {
			System.out.println("ArrayAllocationExpression length type error");
			typeError();
		}
		return new String ("int[]");
	}	

	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 */
	public String visit(AllocationExpression n, Hashtable<String, String> argu) {
		n.f1.accept(this, argu);
		String typeName = terminalToken;
		return new String(typeName);
	}	

	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public String visit(NotExpression n, Hashtable<String, String> argu) {
		String expressionType = n.f1.accept(this, argu);
		if (!expressionType.equals("boolean")) {
			System.out.println("NotExpression type error");
			typeError();
		}
		return new String("boolean");
	}	
	
	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */
	public String visit(BracketExpression n, Hashtable<String, String> argu) {
		return n.f1.accept(this, argu);
	}

	
	
}
