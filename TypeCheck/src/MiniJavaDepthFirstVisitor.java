

import java.util.Hashtable;

import symbol.SymbolType;
import symbol.SymbolTypeAbstract;
import symbol.SymbolTypeConst;
import symbol.SymbolTypeMain;
import symbol.SymbolTypeMethod;
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
import visitor.DepthFirstVisitor;

public class MiniJavaDepthFirstVisitor extends DepthFirstVisitor {
	
	   // User-generated visitor methods below
	   //
	Hashtable<String, SymbolType> symbolTable;
	SymbolType recentAccessed;
	String terminalToken;
	
	public Hashtable<String, SymbolType> getSymbolTable() {
		return symbolTable;
	}
	
	public void typeError() {
		System.out.println("Type error!");
		System.exit(0);
	}
	
	
	public MiniJavaDepthFirstVisitor() {
		symbolTable = new Hashtable<String, SymbolType>();
		recentAccessed = null;
		terminalToken = "";
	}
	
	
	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
	public void visit(Goal n) {
		// put basic type into the symbolTable
		symbolTable.put("int", new SymbolTypeConst("int"));
		symbolTable.put("int[]", new SymbolTypeConst("int[]"));
		symbolTable.put("boolean", new SymbolTypeConst("boolean"));
		n.f0.accept(this);
		n.f1.accept(this);
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
	public void visit(MainClass n) {
		SymbolType symbolTypeMain = new SymbolTypeMain();

		n.f1.accept(this);
		String className = terminalToken;
		if (symbolTable.containsKey(className)) {
			System.out.println("visit main class");
			typeError();
		}
		symbolTypeMain.setName(className);
		
		// since we can ignore the String[] type, just take it as an int
		n.f11.accept(this);
		String mainArgument = terminalToken;
		symbolTypeMain.addVariable(mainArgument, "int");
		
		recentAccessed = symbolTypeMain;
		n.f14.accept(this);
		
		// handle the main argument
		symbolTable.put(className, symbolTypeMain);
	}

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public void visit(TypeDeclaration n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public void visit(ClassDeclaration n) {
	   SymbolType symbolTypeAbstract = new SymbolTypeAbstract();
	   // check if the name of type is existed
      n.f1.accept(this);
      String className = terminalToken;
      if (symbolTable.containsKey(className)) {
    	  System.out.println("visit ClassDeclaration: " + className);
    	  typeError();
      }
      symbolTypeAbstract.setName(className);
      
      // put "this" first
      symbolTypeAbstract.getVariable().put("this", className);
      
      recentAccessed = symbolTypeAbstract;
      n.f3.accept(this);
      recentAccessed = symbolTypeAbstract;
      n.f4.accept(this);
            
      symbolTable.put(className, symbolTypeAbstract);
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
   public void visit(ClassExtendsDeclaration n) {
	  SymbolType symbolTypeAbstract = new SymbolTypeAbstract();
	  
	  // check if the name of type is existed
	  n.f1.accept(this);
	  String className = terminalToken;
	  if (symbolTable.containsKey(className)) {
		  System.out.println("visit ClassExtendsDeclaration: " + className);
    	  typeError();
      }
      symbolTypeAbstract.setName(className);
	  
      n.f3.accept(this);
      String extendName = terminalToken;
      symbolTypeAbstract.setExtendClass(extendName);
      
      // put "this" first
      symbolTypeAbstract.getVariable().put("this", className);
      
      recentAccessed = symbolTypeAbstract;
      n.f5.accept(this);
      recentAccessed = symbolTypeAbstract;
      n.f6.accept(this);
      
      symbolTable.put(className, symbolTypeAbstract);
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public void visit(VarDeclaration n) {
      Hashtable<String, String> currentHash = recentAccessed.getVariable();
	  n.f0.accept(this);
	  String typeName = terminalToken;
      n.f1.accept(this);
      String varName = terminalToken;
      
      if (currentHash.containsKey(varName)) {
    	  System.out.println("visit VarDeclaration: " + varName);
    	  typeError();
      }
      currentHash.put(varName, typeName);
      
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
   public void visit(MethodDeclaration n) {
	   // keep track of the current class that is going to include the method
	  SymbolType symbolTypeClass = recentAccessed;
	  
	  // we start to create the method
	  SymbolType symbolTypeMethod = new SymbolTypeMethod();
	  
	  // get the return type token
      n.f1.accept(this);
      String returnTypeName = terminalToken; 
      symbolTypeMethod.setReturnType(returnTypeName);
      
      // get the method name
      n.f2.accept(this);
      String methodName = terminalToken;
      // check if the current class has the method or not
      if (symbolTypeClass.getClassMethod().containsKey(methodName)) {
    	  System.out.println("visit MethodDeclaration, repeat function name");
    	  typeError();
      }
      symbolTypeMethod.setName(methodName);
      
      // get the arguments and the variables
      recentAccessed = symbolTypeMethod;
      n.f4.accept(this);
      n.f7.accept(this);
      
      // do this at last, update the environment should before the definition on the functions
//      // have to add all the class variables to the method
//      // notice that if the method already has the identifier just ignore the class variable
//      // since they are shadowed
//      Hashtable<String, String> classVariable = symbolTypeClass.getVariable();
//      Hashtable<String, String> methodVariable = symbolTypeMethod.getVariable();
//      for (String key : classVariable.keySet()) {
//    	  if (!methodVariable.containsKey(key)) {
//    		  methodVariable.put(key, classVariable.get(key));
//    	  }
//      }
      
      // set to the class
      symbolTypeClass.addClassMethod(methodName, symbolTypeMethod);
      recentAccessed = symbolTypeClass;
   }

   /**
* f0 -> FormalParameter()
* f1 -> ( FormalParameterRest() )*
*/
   public void visit(FormalParameterList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
* f0 -> Type()
* f1 -> Identifier()
*/
   public void visit(FormalParameter n) {
	   Hashtable<String, String> currentHash = recentAccessed.getVariable();
	   n.f0.accept(this);
	   String typeName = terminalToken;
	   n.f1.accept(this);
	   String varName = terminalToken;
      
	   if (currentHash.containsKey(varName)) {
		   System.out.println("visit Formal Parameter, duplicate");
		   typeError();
	   }
	   currentHash.put(varName, typeName);
	   
	   // add to the vector in order for overloading and call check
	   recentAccessed.addArgument(typeName);
   }

   /**
* f0 -> ","
* f1 -> FormalParameter()
*/
   public void visit(FormalParameterRest n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
* f0 -> ArrayType()
*       | BooleanType()
*       | IntegerType()
*       | Identifier()
*/
   public void visit(Type n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public void visit(ArrayType n) {

	   terminalToken = "int[]";
   }

   /**
* f0 -> "boolean"
*/
   public void visit(BooleanType n) {
      terminalToken = "boolean";
   }

   /**
* f0 -> "int"
*/
   public void visit(IntegerType n) {
      terminalToken = "int";
   }

   /**
* f0 -> Block()
*       | AssignmentStatement()
*       | ArrayAssignmentStatement()
*       | IfStatement()
*       | WhileStatement()
*       | PrintStatement()
*/
   public void visit(Statement n) {
      n.f0.accept(this);
   }

   /**
* f0 -> "{"
* f1 -> ( Statement() )*
* f2 -> "}"
*/
   public void visit(Block n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> Identifier()
* f1 -> "="
* f2 -> Expression()
* f3 -> ";"
*/
   public void visit(AssignmentStatement n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
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
   public void visit(ArrayAssignmentStatement n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
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
   public void visit(IfStatement n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
      n.f6.accept(this);
   }

   /**
* f0 -> "while"
* f1 -> "("
* f2 -> Expression()
* f3 -> ")"
* f4 -> Statement()
*/
   public void visit(WhileStatement n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
   }

   /**
* f0 -> "System.out.println"
* f1 -> "("
* f2 -> Expression()
* f3 -> ")"
* f4 -> ";"
*/
   public void visit(PrintStatement n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
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
   public void visit(Expression n) {
      n.f0.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "&&"
* f2 -> PrimaryExpression()
*/
   public void visit(AndExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "<"
* f2 -> PrimaryExpression()
*/
   public void visit(CompareExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "+"
* f2 -> PrimaryExpression()
*/
   public void visit(PlusExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "-"
* f2 -> PrimaryExpression()
*/
   public void visit(MinusExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "*"
* f2 -> PrimaryExpression()
*/
   public void visit(TimesExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "["
* f2 -> PrimaryExpression()
* f3 -> "]"
*/
   public void visit(ArrayLookup n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "."
* f2 -> "length"
*/
   public void visit(ArrayLength n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

   /**
* f0 -> PrimaryExpression()
* f1 -> "."
* f2 -> Identifier()
* f3 -> "("
* f4 -> ( ExpressionList() )?
* f5 -> ")"
*/
   public void visit(MessageSend n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
      n.f5.accept(this);
   }

   /**
* f0 -> Expression()
* f1 -> ( ExpressionRest() )*
*/
   public void visit(ExpressionList n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
* f0 -> ","
* f1 -> Expression()
*/
   public void visit(ExpressionRest n) {
      n.f0.accept(this);
      n.f1.accept(this);
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
   public void visit(PrimaryExpression n) {
      n.f0.accept(this);
   }

   /**
* f0 -> <INTEGER_LITERAL>
*/
   public void visit(IntegerLiteral n) {
      n.f0.accept(this);
   }

   /**
* f0 -> "true"
*/
   public void visit(TrueLiteral n) {
      n.f0.accept(this);
   }

   /**
* f0 -> "false"
*/
   public void visit(FalseLiteral n) {
      n.f0.accept(this);
   }

   /**
* f0 -> <IDENTIFIER>
*/
   public void visit(Identifier n) {
//      n.f0.accept(this);
	   String name = n.f0.toString();
	   terminalToken = name;
   }

   /**
* f0 -> "this"
*/
   public void visit(ThisExpression n) {
      n.f0.accept(this);
   }

   /**
* f0 -> "new"
* f1 -> "int"
* f2 -> "["
* f3 -> Expression()
* f4 -> "]"
*/
   public void visit(ArrayAllocationExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
      n.f4.accept(this);
   }

   /**
* f0 -> "new"
* f1 -> Identifier()
* f2 -> "("
* f3 -> ")"
*/
   public void visit(AllocationExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
      n.f3.accept(this);
   }

   /**
* f0 -> "!"
* f1 -> Expression()
*/
   public void visit(NotExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
   }

   /**
* f0 -> "("
* f1 -> Expression()
* f2 -> ")"
*/
   public void visit(BracketExpression n) {
      n.f0.accept(this);
      n.f1.accept(this);
      n.f2.accept(this);
   }

}
