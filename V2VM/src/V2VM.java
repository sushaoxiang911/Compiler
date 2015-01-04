import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import cs132.util.IndentPrinter;
import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VInstr.VisitorR;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VVarRef;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;


public class V2VM {

	private static int REGISTER_AVAILABLE = 17;
	private static String MAIN_NAME = "Main";
	
	private static int CALLEE_REGISTER_NUM = 8;
	private static int CALLER_REGISTER_NUM = 9;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File testFile = new File("test/test1.vapor");
		FileInputStream in;
		
		try {
			in = new FileInputStream(testFile.getAbsoluteFile());
			VaporProgram program = parseVapor(in, System.out);
			
			if (program == null) {
				System.exit(0);
			}
			compile(program);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void compile(VaporProgram program) throws IOException {
		// segment
		Writer outWriter = new PrintWriter(System.out);
		IndentPrinter printer = new IndentPrinter(outWriter, "  ");
		VDataSegment[] segmentList = program.dataSegments;
		for (VDataSegment segment : segmentList) {
			printer.println("const " + segment.ident);
			// indent
			printer.indent();
			for (VOperand.Static function : segment.values) {
				printer.println(function.toString());
			}
			printer.dedent();
			printer.println();
		}
		printer.println();
		printer.flush();
		
		VFunction[] functionList = program.functions;
		
		for (VFunction function : functionList) {
			// calculate in num
			int in = calcIn(function);
			// calculate out num
			int out = calcOut(function);
			// calculate local num
			int local;
			// check the function name
			String functionName = function.ident;
			if (function.ident.equals(MAIN_NAME))
				local = calcLocalMain(function);
			else
				local = calcLocal(function);
			compileFunction(function, in, out, local, printer);
		}
	}
	
	public static void compileFunction(VFunction function, int in, int out, int local, IndentPrinter printer) 
			throws IOException {
		String functionName = function.ident;
		VInstr[] body = function.body;
		VVarRef.Local[] parameters = function.params;
		Vector<VCodeLabel> labelList = new Vector<VCodeLabel> (Arrays.asList(function.labels));
//		for (int i = 0; i < labelList.size(); ++i)
//			System.out.println(labelList.elementAt(i).ident);
		// print the function name
		printer.println("func " + functionName + " [in " + in + ", out " + out + ", local " + local + "]");
		// start the instruction
		printer.indent();
		// first check if it is main or not
		// main does not need callee save
		// $s0 -- local[0]
		// $s1 -- local[1]
		//  ......
		// $s7 -- local[7]
		// $t0 -- local[8]
		// $t1 -- local[9]
		//  ......
		// $t8 -- local[16]
		
		// write the start position of the local stack
		int localPos = 0;
		if (!functionName.equals(MAIN_NAME)) {
			for (int i = 0; i < CALLEE_REGISTER_NUM; ++i) 
				printer.println("local[" + i + "] = $s" + i);
			for (int i = 0; i < CALLER_REGISTER_NUM; ++i) {
				int localNum = i + CALLEE_REGISTER_NUM;
				printer.println("local[" + localNum + "] = $t" + i);
			}
			localPos = REGISTER_AVAILABLE;
		}
		Allocator allocator = new Allocator(printer, localPos);
		allocator.allocateParameter(parameters);
		
		InstrVisitor instrVisitor = new InstrVisitor(allocator, printer);
		
		printer.flush();
		
		// init currentPos to insert label
		int currentPos = function.sourcePos.line;
		for (int i = 0; i < body.length; ++i) {
			VInstr instr = body[i];
			// if there the pos is not difference by one
			// insert the labels
			int posDiff = instr.sourcePos.line - currentPos;
			if (posDiff > 1) {
				printer.dedent();
				for (int j = 1; j < posDiff; ++j) {
					VCodeLabel label = labelList.firstElement();
					printer.println(label.ident + ":");
					labelList.remove(0);
				}
				printer.indent();
			}
			currentPos = instr.sourcePos.line;
			instr.accept(instrVisitor);
			printer.flush();
		}
		
		if (!functionName.equals(MAIN_NAME)) {
			for (int i = 0; i < CALLEE_REGISTER_NUM; ++i) 
				printer.println("$s" + i + " = local[" + i + "]");
			for (int i = 0; i < CALLER_REGISTER_NUM; ++i) {
				int localNum = i + CALLEE_REGISTER_NUM;
				printer.println("$t" + i + " = local[" + localNum + "]");
			}
		}
		printer.println("ret");
		printer.dedent();
		printer.println();
		printer.flush();
	}
	
	public static int calcIn(VFunction function) {
		VVarRef.Local[] parameters = function.params;
		// beside the 4 argument registers to use
		if (parameters.length > 4)
			return parameters.length - 4;
		else
			return 0;
	}
	
	public static int calcOut(VFunction function) {
		VInstr[] body = function.body;
		int callArguNum = 0;
		OutCalcVisitor outCalcVisitor = new OutCalcVisitor();
		for (VInstr instruction : body) {
			try {
				int temp = instruction.accept(outCalcVisitor);
				if (temp > callArguNum)
					callArguNum = temp;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		// beside the 4 argument registers to use
		if (callArguNum > 4)
			return callArguNum - 4;
		else
			return 0;
	}
	
	// calculate the local used for other functions
	public static int calcLocal(VFunction function) {
		String[] vars = function.vars;
		int temporaryNum = vars.length;
		// calculate the extraLocalNeeded for the function temporary
		// if we need more or not
		int extraLocalNeeded = temporaryNum > REGISTER_AVAILABLE ? temporaryNum - REGISTER_AVAILABLE : 0;
		// Add the local needed for callee save
		return extraLocalNeeded + REGISTER_AVAILABLE;
	}
	
	// calculate the local used for main function
	public static int calcLocalMain(VFunction function) {
		String[] vars = function.vars;
		int temporaryNum = vars.length;
		int extraLocalNeeded = temporaryNum > REGISTER_AVAILABLE ? temporaryNum - REGISTER_AVAILABLE : 0;
		return extraLocalNeeded;
	}
	public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
		Op[] ops = {Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
			    Op.PrintIntS, Op.HeapAllocZ, Op.Error};
		boolean allowLocals = true;
		String[] registers = null;
		boolean allowStack = false;
		
		VaporProgram program;
		try {
			program = VaporParser.run(new InputStreamReader(in), 1, 1, 
					java.util.Arrays.asList(ops), allowLocals, registers, allowStack);
		} catch (ProblemException ex) {
			err.println(ex.getMessage());
			return null;
		}
		return program;
	}
	
}
