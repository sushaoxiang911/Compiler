import java.util.Hashtable;
import java.util.Vector;

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
import visitor.GJNoArguDepthFirst;


public class CompileVisitor extends GJNoArguDepthFirst<String>{
	Hashtable<String, ClassType> table;
	int temporaryId;
	int ifId;
	int whileId;
	int andId;
	int nullId;
	int boundId;
	
	boolean useTemporary;
	boolean stringNeeded;
	
	int printLevel;
	
	// keep track of the class that we access
	ClassType currentClass;
	// keep track of the class that the expression brings
	ClassType expressionClass;
	Hashtable<String, String> localVariable;
	
	Vector<String> argList;
	
	String terminalToken;
	public CompileVisitor(Hashtable<String, ClassType> table) {
		this.table = table;
		temporaryId = 0;
		ifId = 0;
		whileId = 0;
		andId = 0;
		nullId = 0;
		boundId = 0;
		printLevel = 0;
	}
	public String printSpace() {
		String result = "";
		for (int i = 0; i < printLevel; ++i)
			result += "  ";
		return result;
	}
	
	public String getTemporary() {
		String result =  "t." + String.valueOf(temporaryId);
		temporaryId++;
		return result;
	}
	
	public String getNullLabel() {
		String result =  "null" + String.valueOf(nullId);
		nullId++;
		return result;
	}
	public String getBoundLabel() {
		String result = "bound" + String.valueOf(boundId);
		boundId++;
		return result;
	}
	public String getIfLabel() {
		String result = "if" + String.valueOf(ifId);
		ifId++;
		return result;
	}
	
	public String getWhileLabel() {
		String result = "while" + String.valueOf(whileId);
		whileId++;
		return result;
	}
	public String getAndLabel() {
		String result = "ss" + String.valueOf(andId);
		andId++;
		return result;
	}
	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
	public String visit(Goal n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		return _ret;
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
	public String visit(MainClass n) {
		// print the main function
		printLevel = 0;
		
		System.out.println("func Main()");
		
		// init a new variableTable for main function
		localVariable = new Hashtable<String, String>();
//		n.f1.accept(this);
//		n.f11.accept(this);
		n.f14.accept(this);
		
		// we have level 1 for the statement in main method
		printLevel++;
		n.f15.accept(this);
		// ret
		System.out.println(printSpace() + "ret");
		System.out.println();
		printLevel--;
		return null;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *       | ClassExtendsDeclaration()
	 */
	public String visit(TypeDeclaration n) {
	   n.f0.accept(this);
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
	public String visit(ClassDeclaration n) {
		// get the current class
		stringNeeded = true;
		n.f1.accept(this);
		String className = terminalToken;
		currentClass = table.get(className);
		
		
//		n.f3.accept(this);
		
		// go into methods
		n.f4.accept(this);
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
	public String visit(ClassExtendsDeclaration n) {
		// get class
		stringNeeded = true;
		n.f1.accept(this);
		String className = terminalToken;
		currentClass = table.get(className);
		
		// don't need extend name
//		n.f3.accept(this);
//		n.f5.accept(this);
		
		// go into methods
		n.f6.accept(this);
		return null;
	}

   	/**
   	 * f0 -> Type()
   	 * f1 -> Identifier()
   	 * f2 -> ";"
   	 */
   	public String visit(VarDeclaration n) {
   		stringNeeded = true;
   		n.f0.accept(this);
   		String typeName = terminalToken;
   		
   		stringNeeded = true;
   		n.f1.accept(this);
   		String variableName = terminalToken;
   		localVariable.put(variableName, typeName);
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
   	public String visit(MethodDeclaration n) {
   		
   		// wo don't need to check the type of the method
//   		n.f1.accept(this);
   		// get the method name
   		stringNeeded = true;
   		n.f2.accept(this);
   		String methodName = terminalToken;
   		
   		// temporary id is 0
   		temporaryId = 0;
   		// print the first line and set level to 0
   		printLevel = 0;
   		// here we just print the "func methodname(this " and other will be print when accessing arguments
   		System.out.print("func " + currentClass.getName() + "." + methodName + "(this");
   		// accept all the parameter and local variable
   		// notice we don't count this here 
   		localVariable = new Hashtable<String, String>();

   		// put in this pointer
   		localVariable.put("this", currentClass.getName());
   		
   		// parameter
   		n.f4.accept(this);
   		
   		// a newline and the parenthese for print
   		System.out.println(")");
   		
   		// local variable
   		n.f7.accept(this);
   		
   		
   		// access the statements in the method
   		printLevel++;
   		n.f8.accept(this);
   	
   		// we have to know which temporary has the return value
   		// use the return value of accept
   		// expression will have the temporary name
   		String temporary = n.f10.accept(this);
   		System.out.println(printSpace() + "ret " + temporary);
   		printLevel--;
   		
   		System.out.println();
   		return null;
   	}

   	/**
   	 * f0 -> FormalParameter()
   	 * f1 -> ( FormalParameterRest() )*
   	 */
   public String visit(FormalParameterList n) {
      n.f0.accept(this);
      // the space for parameters
      
      n.f1.accept(this);
      return null;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public String visit(FormalParameter n) {
	   // get the type and name
	   n.f0.accept(this);
	   String typeName = terminalToken;
	   stringNeeded = true;
	   n.f1.accept(this);
	   String variableName = terminalToken;
	   // put into localVariable table
	   localVariable.put(variableName, typeName);
	   
	   // just use all the localvariable name as the termporary
	   System.out.print(" " + variableName);
	   
	   return null;
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   	public String visit(FormalParameterRest n) {
   		n.f1.accept(this);
   		return null;
   	}

   	/**
   	 * f0 -> ArrayType()
   	 *       | BooleanType()
   	 *       | IntegerType()
   	 *       | Identifier()
   	 */
   	public String visit(Type n) {
   		stringNeeded = true;
   		n.f0.accept(this);
      	return null;
   	}

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public String visit(ArrayType n) {
	   terminalToken = "int[]";
	   return null;
   	}

   	/**
   	 * f0 -> "boolean"
   	 */
   	public String visit(BooleanType n) {
   		terminalToken = "boolean";
   		return null;
   	}

   	/**
   	 * f0 -> "int"
   	 */
   	public String visit(IntegerType n) {
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
   	public String visit(Statement n) {
   		n.f0.accept(this);
   		return null;
   	}

   	/**
   	 * f0 -> "{"
   	 * f1 -> ( Statement() )*
   	 * f2 -> "}"
   	 */
   	public String visit(Block n) {
   		// interpret as many statements together
   		n.f1.accept(this);
   		return null;
   	}

   
   	
	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
   	public String visit(AssignmentStatement n) {
   		useTemporary = false;
   		stringNeeded = false;
   		String identifierTemporary = n.f0.accept(this);
   		String returnTemporary = n.f2.accept(this);
   		
   		System.out.println(printSpace() + identifierTemporary + " = " + returnTemporary);
   		return null;
   	}

   	public void checkNull(String temporary) {
   		String nullLabel = getNullLabel();
   		System.out.println(printSpace() + "if " + temporary + " goto :" + nullLabel);
   		// increment space
   		printLevel++;
   		System.out.println(printSpace() + "Error(\"null pointer\")");
   		printLevel--;
   		// print label
   		System.out.println(printSpace() + nullLabel + ":");
   	}
   	
   	public void checkBound(String tempTemporary, String arrayTemporary, String indexTemporary) {
   		// print lengthCheckTemporary = [arrayTemporary] -> get length
   		System.out.println(printSpace() + tempTemporary + " = " + "[" + arrayTemporary + "]");
   		
   		// print lengthCheckTemporary = Lt(indexTemporary lengthCheckTemporary)
   		System.out.println(printSpace() + tempTemporary + " = " + "Lt(" + indexTemporary + " " + tempTemporary + ")");
   		
   		String boundLabel = getBoundLabel();
   		System.out.println(printSpace() + "if " + tempTemporary + " goto :" + boundLabel);
   		printLevel++;
   		System.out.println(printSpace() + "Error(\"array index out of bounds\")");
   		printLevel--;
   		// print label
   		System.out.println(printSpace() + boundLabel + ":");
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
   	public String visit(ArrayAssignmentStatement n) {
   		useTemporary = true;
   		stringNeeded = false;
   		String arrayTemporary = n.f0.accept(this);
//   		
//   		// first get the temporary and save into a new temporary, increment id
//   		String arrayTemporary = getTemporary();
//   		System.out.println(printSpace() + arrayTemporary + " = " + identifierTemporary);
   		
   		// check if arrayTemporary is null
   		checkNull(arrayTemporary);
   		
   		// index temporary found from the expression
   		String indexTemporary = n.f2.accept(this);
   		// save the length of the array into the temporary
   		String assignTemporary = getTemporary();
   		
   		checkBound(assignTemporary, arrayTemporary, indexTemporary);
   		
   		// print assignTemporary = MulS(indexTemporary 4)
   		System.out.println(printSpace() + assignTemporary + " = " + "MulS(" + indexTemporary + " " + "4" + ")");
   		// print assignTemporary = Add(assignTemporary arrayTemporary)
   		System.out.println(printSpace() + assignTemporary + " = " + "Add(" + assignTemporary + " " + arrayTemporary + ")");
   		
   		String returnTemporary = n.f5.accept(this);
   		// print [assignTemporary+4] = returnTemporary
   		System.out.println(printSpace() + "[" + assignTemporary + "+4] = " + returnTemporary);
   		
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
   	public String visit(IfStatement n) {
   		String ifLabel = getIfLabel();
   		// get the return value of expression
   		// >0 go then, 0 else
   		String conditionTemporary = n.f2.accept(this);
   		
   		// if0 conditionTemporary goto :ifLabel_else
   		System.out.println(printSpace() + "if0 " + conditionTemporary + " goto :" + ifLabel + "_else"); 
   		printLevel++;
   		n.f4.accept(this);
   		// goto :ifLabel_end
   		System.out.println(printSpace() + "goto :" + ifLabel + "_end");
   		printLevel--;
   		// ifLabel_else:
   		System.out.println(printSpace() + ifLabel + "_else:");
   		printLevel++;
   		n.f6.accept(this);
   		printLevel--;
   		
   		System.out.println(printSpace() + ifLabel + "_end:");
   		return null;
   	}

   	/**
   	 * f0 -> "while"
   	 * f1 -> "("
   	 * f2 -> Expression()
   	 * f3 -> ")"
   	 * f4 -> Statement()
   	 */
   	public String visit(WhileStatement n) {
   		String whileLabel = getWhileLabel();
   		// whileLabel_top:
   		System.out.println(printSpace() + whileLabel + "_top:");
   		// get the return value of the expression
   		// >0 go, 0 end label
   		String conditionTemporary = n.f2.accept(this);
   		// if0 conditionTemporary goto :whileLabel_end
   		System.out.println(printSpace() + "if0 " + conditionTemporary + " goto :" + whileLabel + "_end");
   		printLevel++;
   		n.f4.accept(this);
   		// goto :whileLabel_top
   		System.out.println(printSpace() + "goto :" + whileLabel + "_top");
   		printLevel--;
   		//whileLabel_end:
   		System.out.println(printSpace() + whileLabel + "_end:");
   		return null;
   }

   	/**
   	 * f0 -> "System.out.println"
   	 * f1 -> "("
   	 * f2 -> Expression()
   	 * f3 -> ")"
   	 * f4 -> ";"
   	 */
   	public String visit(PrintStatement n) {
   		String printTemporary = n.f2.accept(this);
   		// PrintIntS(printTemporary)
   		System.out.println(printSpace() + "PrintIntS(" + printTemporary + ")");
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
   	public String visit(Expression n) {
   		return n.f0.accept(this);
   	}

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "&&"
   	 * f2 -> PrimaryExpression()
   	 */
   	public String visit(AndExpression n) {
   		String returnTemporary = getTemporary();
   		String andLabel = getAndLabel();
   		
   		String conditionTemporary1 = n.f0.accept(this);
   		// if0 conditionTemporary1 goto :andLabel_else
   		System.out.println("if0 " + conditionTemporary1 + " goto :" + andLabel + "_else");
   		printLevel++;
   		String conditionTemporary2 = n.f2.accept(this);
   		// returnTemporary = conditionTemporary2;
   		System.out.println(returnTemporary + " = " + conditionTemporary2);
   		// goto :andnLabel_end
   		System.out.println("goto :" + andLabel + "_end");
   		printLevel--;
   		
   		System.out.println(andLabel + "_else:");
   		printLevel++;
   		System.out.println(returnTemporary + " = 0");
   		printLevel--;
   		
   		System.out.println(andLabel + "_end:");
   		
   		return returnTemporary;
   	}

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "<"
   	 * f2 -> PrimaryExpression()
   	 */
   	public String visit(CompareExpression n) {
   		String compareTemporary1 = n.f0.accept(this);
   		String compareTemporary2 = n.f2.accept(this);
   		String returnTemporary = getTemporary();
   		// returnTemporary = LtS(compareTemporary1 compareTemporary2)
   		System.out.println(printSpace() + returnTemporary + " = " + "LtS(" + compareTemporary1 + " " + compareTemporary2 + ")");
   		return returnTemporary;
   	}

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "+"
   	 * f2 -> PrimaryExpression()
   	 */
   	public String visit(PlusExpression n) {
   		String addTemporary1 = n.f0.accept(this);
   		String addTemporary2 = n.f2.accept(this);
   		String returnTemporary = getTemporary();
   		// returnTemporary = Add(addTemporary1 addTemporary2)
   		System.out.println(printSpace() + returnTemporary + " = " + "Add(" + addTemporary1 + " " + addTemporary2 + ")");
   		
   		return returnTemporary;
   	}

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "-"
   	 * f2 -> PrimaryExpression()
   	 */
   	public String visit(MinusExpression n) {
   		
   		
   		String subTemporary1 = n.f0.accept(this);
   		String subTemporary2 = n.f2.accept(this);
   		String returnTemporary = getTemporary();
   		// returnTemporary = Sub(subTemporary1 subTemporary2)
   		System.out.println(printSpace() + returnTemporary + " = " + "Sub(" + subTemporary1 + " " + subTemporary2 + ")");
   		return returnTemporary;
   	}	

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "*"
   	 * f2 -> PrimaryExpression()
   	 */
   	public String visit(TimesExpression n) {
   		String timeTemporary1 = n.f0.accept(this);
   		String timeTemporary2 = n.f2.accept(this);
   		String returnTemporary = getTemporary();
   		// returnTemporary = MulS(timeTemporary1 timeTemporary2)
   		System.out.println(printSpace() + returnTemporary + " = " + "MulS(" + timeTemporary1 + " " + timeTemporary2 + ")");
   		
   		return returnTemporary;
   	}

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "["	
   	 * f2 -> PrimaryExpression()
   	 * f3 -> "]"
   	 */
   	public String visit(ArrayLookup n) {
   		String arrayTemporary = n.f0.accept(this);
   		String indexTemporary = n.f2.accept(this);
   		
   		// check null
   		checkNull(arrayTemporary);
   		// check bound
   		// need a temporary here
   		String tempTemporary = getTemporary();
   		checkBound(tempTemporary, arrayTemporary, indexTemporary);
   		
   		// tempTemporary = MulS(indexTemporary 4)
   		System.out.println(printSpace() + tempTemporary + " = " + "MulS(" + indexTemporary + " " + "4)");
   		// tempTemporary = Add(tempTemporary arrayTemporary)
   		System.out.println(printSpace() + tempTemporary + " = " + "Add(" + tempTemporary + " " + arrayTemporary + ")");
   		// save the result to that temporary
   		String returnTemporary = getTemporary();
   		// tempTemporary = [tempTemporary+4]
   		System.out.println(printSpace() + returnTemporary + " = " + "[" + tempTemporary + "+4]");
   		
   		return returnTemporary;
   	}	

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "."
   	 * f2 -> "length"
   	 */
   	public String visit(ArrayLength n) {
   		String arrayTemporary = n.f0.accept(this);
   		// check null
   		checkNull(arrayTemporary);
   		
   		String returnTemporary = getTemporary();
   		// returnTemporary = [arrayTemporary]
   		System.out.println(printSpace() + returnTemporary + " = " + "[" + arrayTemporary + "]");
   		return returnTemporary;
   	}	

   	/**
   	 * f0 -> PrimaryExpression()
   	 * f1 -> "."
   	 * f2 -> Identifier()
   	 * f3 -> "("
   	 * f4 -> ( ExpressionList() )?
   	 * f5 -> ")"
   	 */
   	public String visit(MessageSend n) {
   		String objTemporary = n.f0.accept(this);
   		// check null
   		if (!objTemporary.equals("this"))
   			checkNull(objTemporary);
   		
   		// make sure that there is no print in the identifier
   		// here we just need the string of the identifier
   		useTemporary = false;
   		stringNeeded = true;
   		n.f2.accept(this);
   		String methodName = terminalToken;

   		// first get the funciton pointer
   		// here we have to get the function pointer then we can call
   		// first get the virtualTable
   		String vtableTemporary = getTemporary();
   		// vtableTemporary = [objTemporary]
   		System.out.println(printSpace() + vtableTemporary + " = " + "[" + objTemporary + "]");
   		
   		// get the function table
   		VirtualTable virtualTable = expressionClass.getTable();
   		int index = virtualTable.findMethod(methodName);
   		
   		// get the pointer of the function and save into the vtableTemporary
   		// vtableTemporary = [vtableTemporary+index]
   		System.out.println(printSpace() + vtableTemporary + " = " + 
   					"[" + vtableTemporary + "+" + String.valueOf(index * 4) + "]");
   		
   	
   		// then consider every arguments
   		// get all of the temporary and save into list
   		argList = new Vector<String>();
//   		// first put the objTemporary inside
//   		argList.add(objTemporary);
   		// here we directly print the objTemporary in the call
   		
   		// here we need all the temporary format
   		useTemporary = true;
   		n.f4.accept(this);
   		
   		
   		
   		String returnTemporary = getTemporary();
   		// call the function with the argument temporaries
   		// returnTemporary = call vtableTemporary(argList[0] argList[1] ...)
   		System.out.print(printSpace() + returnTemporary + " = " + "call " + vtableTemporary + "(" + objTemporary);
   		for (String temp : argList) {
   			System.out.print(" " + temp);
   		}
   		System.out.println(")");
   		
   		return returnTemporary;
   	}

   	/**
   	 * f0 -> Expression()
   	 * f1 -> ( ExpressionRest() )*
   	 */
   	public String visit(ExpressionList n) {
   		String temporary = n.f0.accept(this);
   		argList.add(temporary);
   		n.f1.accept(this);
   		return null;
   	}

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   	public String visit(ExpressionRest n) {
   		String temporary = n.f1.accept(this);
   		argList.add(temporary);
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
   	public String visit(PrimaryExpression n) {
   		// here we know that we should take a temporary to carry the identifier value
   		useTemporary = true;
   		stringNeeded = false;
   		return n.f0.accept(this);
   	}

   	/**
   	 * f0 -> <INTEGER_LITERAL>
   	 */
   	public String visit(IntegerLiteral n) {
   		return n.f0.toString();
   	}

	/**
   	 * f0 -> "true"
   	 */
   	public String visit(TrueLiteral n) {
   		return String.valueOf(1);
   	}

   	/**
   	 * f0 -> "false"
   	 */
   	public String visit(FalseLiteral n) {
   		return String.valueOf(0);
   	}

   /**
    * f0 -> <IDENTIFIER>
    */
   	public String visit(Identifier n) {
   		String identifierName = n.f0.toString();
   		terminalToken = identifierName;
   		if (stringNeeded)
   			return null;
   		
   	// check the name is in the class or the local
   		if (localVariable.containsKey(identifierName)) {
   			// get the type of the local variable
   			String className = localVariable.get(identifierName);
   			expressionClass = table.get(className);
   			
   			return identifierName;
   		} else {
   			// find the variable from the class
   			
   			// get the type of the class variable
   			Hashtable<String, String> symbolTable = currentClass.getSymbolTable();
   			String className = symbolTable.get(identifierName);
   			expressionClass = table.get(className);
   			
   			// find the variable place
   			Vector<String> classVariable = currentClass.getVariable();
   		
   			int index = classVariable.indexOf(identifierName);
   			if (index == -1) {
   				System.out.println("error, didn't find the variable from class, should not happen");
   				return null;
   			}
   			// get the data from this pointer
   			// it is in the "this+4*(i+1)"
   			// and we don't need the temporary actually to do that
   			int offset = 4 * (index + 1);
   			String reference = "[this+" + String.valueOf(offset) + "]";
   			if (useTemporary) {
   				String returnTemporary = getTemporary();
   				System.out.println(printSpace() + returnTemporary + " = " + reference);
   				return returnTemporary;
   			} else {
   				return reference;
   			}
   		}
   	}

   	/**
   	 * f0 -> "this"
   	 */
   	public String visit(ThisExpression n) {
   		// so the expression brings currentType as the expressionType
   		expressionClass = currentClass;
   		return "this";
   	}	

   	/**
   	 * f0 -> "new"
   	 * f1 -> "int"
   	 * f2 -> "["
   	 * f3 -> Expression()
   	 * f4 -> "]"
   	 */
   	public String visit(ArrayAllocationExpression n) {
   		String sizeTemporary = n.f3.accept(this);
   		
   		String tempTemporary = getTemporary();
   		// calculate the size
   		// tempTemporary = MulS(sizeTemporary 4)
   		System.out.println(printSpace() + tempTemporary + " = " + "MulS(" + sizeTemporary + " 4)");
   		// tempTemporary = Add(tempTemporary 4)
   		System.out.println(printSpace() + tempTemporary + " = " + "Add(" + tempTemporary + " 4)");
   		
   		String returnTemporary = getTemporary();
   		// returnTemporary = HeapAllocZ(tempTemporary)
   		System.out.println(printSpace() + returnTemporary + " = " + "HeapAllocZ(" + tempTemporary + ")");
   		
   		// set size
   		// [returnTemporary] = sizeTemporary
   		System.out.println(printSpace() + "[" + returnTemporary + "]" +  " = " + sizeTemporary);
   		return returnTemporary;
   	}

   	/**
   	 * f0 -> "new"
   	 * f1 -> Identifier()
   	 * f2 -> "("
   	 * f3 -> ")"
   	 */
   	public String visit(AllocationExpression n) {	
   		useTemporary = false;
   		stringNeeded = true;
   		n.f1.accept(this);
   		String className = terminalToken;
   		expressionClass = table.get(className);
   		
   		Vector<String> classVariable = expressionClass.getVariable();
   		VirtualTable virtualTable = expressionClass.getTable();
   		
   		// calculate the size of the class
   		int allocSize = 4 + classVariable.size() * 4;
   		// get the virtualTable name
   		String vtableName = virtualTable.getName();
   		
   		String returnTemporary = getTemporary(); 
   		
   		// returnTemporary = HeapAllocZ(allocSize)
   		System.out.println(printSpace() + returnTemporary + " = " + "HeapAllocZ(" + String.valueOf(allocSize) + ")");
   		// set vtable
   		// [returnTemporary] = :vtableName
   		System.out.println(printSpace() + "[" + returnTemporary + "]" + " = :" + vtableName); 
   		return returnTemporary;
   	}	

   	/**
   	 * f0 -> "!"
   	 * f1 -> Expression()
   	 */
   	public String visit(NotExpression n) {
   		String boolTemporary = n.f1.accept(this);
   		String returnTemporary = getTemporary();
   		// returnTemporary = Sub(1 boolTemporary)
   		System.out.println(printSpace() + returnTemporary + " = " + "Sub(1 " + boolTemporary + ")");
   		
   		return returnTemporary;
   	}	

   	/**
   	 * f0 -> "("
   	 * f1 -> Expression()
   	 * f2 -> ")"
   	 */
   	public String visit(BracketExpression n) {
   		return n.f1.accept(this);
   	}	
}	
