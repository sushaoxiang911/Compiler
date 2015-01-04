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
import java.util.Vector;

import cs132.util.IndentPrinter;
import cs132.util.ProblemException;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.parser.VaporParser;


public class VM2M {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File testFile = new File("test/test1.vaporm");
		FileInputStream in;
		
		try {
			in = new FileInputStream(testFile.getAbsoluteFile());
			VaporProgram program = parseVaporm(in, System.out);
			
			compile(program);
			
			if (program == null) {
				System.exit(0);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void compile(VaporProgram program) throws IOException {
		Writer outWriter = new PrintWriter(System.out);
		IndentPrinter printer = new IndentPrinter(outWriter, "  ");
		// first print .data
		printer.println(".data");
		printer.println();
		// print all the segments
		VDataSegment[] segmentList = program.dataSegments;
		for (VDataSegment segment : segmentList) {
			printer.println(segment.ident + ":");
			// indent
			printer.indent();
			for (VOperand.Static function : segment.values) {
				// here we have to eliminate the first :
				printer.println(function.toString().substring(1));
			}
			printer.dedent();
			printer.println();
		}
		printer.flush();
		
		// now we print .text
		printer.println(".text");
		printer.println();
		
		// print the program entrance code
		printer.indent();
		printer.println("jal Main");
		printer.println("li $v0 10");
		printer.println("syscall");
		printer.dedent();
		printer.println();
		printer.flush();
		
		// now we can go through all of the functions
		// for every function, keep track of the in out and local where in might not be useful
		VFunction[] functionList = program.functions;
		
		for (VFunction function : functionList) {
			compileFunction(function, printer);
			printer.println();
			printer.flush();
		}
		
		// print some builtin functions and values
		// _print 
		printer.println("_print:");
		printer.indent();
		printer.println("li $v0 1");
		printer.println("syscall");
		printer.println("la $a0 _newline");
		printer.println("li $v0 4");
		printer.println("syscall");
		printer.println("jr $ra");
		printer.dedent();
		printer.println();
		
		//_error
		printer.println("_error:");
		printer.indent();
		printer.println("li $v0 4");
		printer.println("syscall");
		printer.println("li $v0 10");
		printer.println("syscall");
		printer.dedent();
		printer.println();
		
		//_heapAlloc
		printer.println("_heapAlloc:");
		printer.indent();
		printer.println("li $v0 9");
		printer.println("syscall");
		printer.println("jr $ra");
		printer.dedent();
		printer.println();
		
		printer.println(".data");
		printer.println(".align 0");
		printer.println("_newline: .asciiz \"\\n\"");
		printer.println("_str0: .asciiz \"null pointer\\n\"");
		printer.println("_str1: .asciiz \"array index out of bounds\\n\"");
		printer.flush();
	}
	
	
	public static void compileFunction(VFunction function, IndentPrinter printer) throws IOException {
		String functionName = function.ident;
		VFunction.Stack stack = function.stack;
		int in = stack.in;
		int out = stack.out;
		int local = stack.local;
		
		// print the function name as the label
		printer.println(functionName + ":");
		
		printer.indent();
		// assign stack for this function
		// save the old fp
		printer.println("sw $fp -8($sp)");
		// move fp to sp place from here we can check the in values(caller's out)
		printer.println("move $fp $sp");
		// calculate the stack we have to remain(out + local)
		int stackNeeded = out + local;
		// move sp to 4 * (out + local) + 8 upward
		printer.println("subu $sp $sp " + String.valueOf(stackNeeded * 4 + 8));
		// save the return address
		printer.println("sw $ra -4($fp)");
		
		// now we can go through all the instruction
		InstrVisitor instrVisitor = new InstrVisitor(in, out, local, printer);
		
		
		VInstr[] body = function.body;
		Vector<VCodeLabel> labelList = new Vector<VCodeLabel> (Arrays.asList(function.labels));
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
		
		// load back ra
		printer.println("lw $ra -4($fp)");
		// load back fp
		printer.println("lw $fp -8($fp)");
		// put back sp
		printer.println("addu $sp $sp " + String.valueOf(stackNeeded * 4 + 8));
		// jump to return address
		printer.println("jr $ra");
		printer.dedent();
		printer.flush();
		
	}
	
	public static VaporProgram parseVaporm(InputStream in, PrintStream err) throws IOException {
		Op[] ops = {Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
			    Op.PrintIntS, Op.HeapAllocZ, Op.Error};
		boolean allowLocals = false;
		String[] registers = {
				"v0", "v1",
				"a0", "a1", "a2", "a3",
				"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8",
				"s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
		};
		boolean allowStack = true;
		
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
