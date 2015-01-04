import java.io.IOException;

import cs132.util.IndentPrinter;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;


public class InstrVisitor extends Visitor<IOException>{
	int in;
	int out;
	int local;
	IndentPrinter printer;
	
	public InstrVisitor(int in, int out, int local, IndentPrinter printer) {
		super();
		this.in = in;
		this.out = out;
		this.local = local;
		this.printer = printer;
	}
	boolean isRegister(String str) {
		return str.charAt(0) == '$';
	}
	boolean isLabel(String str) {
		return str.charAt(0) == ':';
	}
	
	@Override
	public void visit(VAssign arg0) throws IOException {
		// here is the place to take care of local, in, and out
		// not here!!!!!! the operation on local, out and in is in memory read and write
		// and there is two version []global or out[] local
		String dest = arg0.dest.toString();
		String source = arg0.source.toString();
		if (isRegister(source))
			// if set dest with another register, use move
			printer.println("move " + dest + " " + source);
		else if (isLabel(source))
			// if set dest with an address label
			printer.println("la " + dest + " " + source.substring(1));
		else
			// if set dest with a constant value, use li
			printer.println("li " + dest + " " + source);
	}

	@Override
	public void visit(VCall arg0) throws IOException {
		String functionAdd = arg0.addr.toString();
		if (isRegister(functionAdd))
			// if the function address is stored in the register, use jalr
			printer.println("jalr " + functionAdd);
		else 
			// if the function address is a pure label
			printer.println("jal " + functionAdd.substring(1));
	}

	@Override
	public void visit(VBuiltIn arg0) throws IOException {
		VBuiltIn.Op op = arg0.op;
		VOperand[] operand = arg0.args;
		if (op == VBuiltIn.Op.Add) {
			String returnPlace = arg0.dest.toString();
			String operand1 = operand[0].toString();
			String operand2 = operand[1].toString();
			if (!isRegister(operand1)) {
				// if operand1 is not in the register, put in t9
				printer.println("li $t9 " + operand1);
				operand1 = "$t9";
			}
			if (!isRegister(operand2)) {
				// if operand2 is not register, use addiu
				printer.println("addiu " + returnPlace + " " + operand1 + " " + operand2);
			} else
				// if operand2 is the register, use addu
				printer.println("addu " + returnPlace + " " + operand1 + " " + operand2);
		} else if (op == VBuiltIn.Op.Sub) {
			String returnPlace = arg0.dest.toString();
			String operand1 = operand[0].toString();
			String operand2 = operand[1].toString();
			if (!isRegister(operand1)) {
				// if operand1 is not in the register, put in t9
				printer.println("li $t9 " + operand1);
				operand1 = "$t9";
			}
			if (!isRegister(operand2)) {
				// if operand2 is not register, use addiu and addiu the opposite number
				printer.println("addiu " + returnPlace + " " + operand1 + " " + "-" + operand2);
			} else
				// if operand2 is the register, use subu
				printer.println("subu " + returnPlace + " " + operand1 + " " + operand2);
		} else if (op == VBuiltIn.Op.MulS) {
			// here we know that operand 2 can be anything but operand 1 must be in register
			// is better to at least put in t9
			String returnPlace = arg0.dest.toString();
			String operand1 = operand[0].toString();
			String operand2 = operand[1].toString();
			if (!isRegister(operand1)) {
				printer.println("li $t9 " + operand1);
				operand1 = "$t9";
			} else if (!isRegister(operand2)) {
				printer.println("li $t9 " + operand2);
				operand2 = "$t9";
			}
			printer.println("mul " + returnPlace + " " + operand1 + " " + operand2);
		} else if (op == VBuiltIn.Op.Eq) {
			String returnPlace = arg0.dest.toString();
			String operand1 = operand[0].toString();
			String operand2 = operand[1].toString();
			if (!isRegister(operand1)) {
				printer.println("li $t9 " + operand1);
				operand1 = "$t9";
			} else if (!isRegister(operand2)) {
				printer.println("li $t9 " + operand2);
				operand2 = "$t9";
			}
			// for all of the instructions, we first check operand1, consider if we need to save into t9
			// ask if there is possiblisty that Eq(1,1) both are constants
			printer.println("seq " + returnPlace + " " + operand1 + " " + operand2);
		} else if (op == VBuiltIn.Op.Lt) { 
			String returnPlace = arg0.dest.toString();
			String operand1 = operand[0].toString();
			String operand2 = operand[1].toString();
			if (!isRegister(operand1)) {
				// if operand1 is not in the register, put in t9
				printer.println("li $t9 " + operand1);
				operand1 = "$t9";
			}
			if (!isRegister(operand2)) {
				// if operand2 is not register, use sltiu
				printer.println("sltiu " + returnPlace + " " + operand1 + " " + operand2);
			} else
				// if operand2 is the register, use sltu
				printer.println("sltu " + returnPlace + " " + operand1 + " " + operand2);
		} else if (op == VBuiltIn.Op.LtS) {
			// here is signed comparison
			String returnPlace = arg0.dest.toString();
			String operand1 = operand[0].toString();
			String operand2 = operand[1].toString();
			if (!isRegister(operand1)) {
				// if operand1 is not in the register, put in t9
				printer.println("li $t9 " + operand1);
				operand1 = "$t9";
			}
			if (!isRegister(operand2)) {
				// if operand2 is not register, use slti
				printer.println("slti " + returnPlace + " " + operand1 + " " + operand2);
			} else
				// if operand2 is the register, use slt
				printer.println("slt " + returnPlace + " " + operand1 + " " + operand2);
		} else if (op == VBuiltIn.Op.PrintIntS) {
			String operand1 = operand[0].toString();
			if (isRegister(operand1)) {
				// if it is register, move the value to a0 and call _print
				printer.println("move $a0 " + operand1);
			} else {
				// if it not register, li the value to a0
				printer.println("li $a0 " + operand1);
			}
			printer.println("jal _print");
		} else if (op == VBuiltIn.Op.HeapAllocZ) {
			String operand1 = operand[0].toString();
			String returnPlace = arg0.dest.toString();
			if (isRegister(operand1))
				printer.println("move $a0 " + operand1);
			else
				printer.println("li $a0 " + operand1);
			printer.println("jal _heapAlloc");
			printer.println("move " + returnPlace + " $v0");
		} else if (op == VBuiltIn.Op.Error) {
			String operand1 = operand[0].toString();
			if (operand1.equals("\"null pointer\""))  
				printer.println("la $a0 _str0");
			else if (operand1.equals("\"array index out of bounds\""))
				printer.println("la $a0 _str1");
			printer.println("j _error");
		} else {
			throw new IOException();
		}
	}

	@Override
	public void visit(VMemWrite arg0) throws IOException {
	
		// operate on the source
		String source = arg0.source.toString();
		String sourceRegister;
		if (isRegister(source))
			// if set dest with another register, use move
			sourceRegister = source;
		else if (isLabel(source)) {
			// if set dest with an address label
			printer.println("la $t9 " + source.substring(1));
			sourceRegister = "$t9";
		} else {
			// if set dest with a constant value, use li
			printer.println("li $t9 " + source);
			sourceRegister = "$t9";
		}	
		
		VMemRef dest = arg0.dest;
		String destRegister;
		int byteOffset;
		if (dest instanceof VMemRef.Global) {
			VMemRef.Global mem = (VMemRef.Global)dest;
			destRegister = mem.base.toString();
			byteOffset = mem.byteOffset;
		} else {
			VMemRef.Stack mem = (VMemRef.Stack)dest;
			if (mem.region == VMemRef.Stack.Region.Out) {
				// write out place
				destRegister = "$sp";
				byteOffset = 4 * mem.index;
			} else if (mem.region == VMemRef.Stack.Region.Local) {
				// write local place
				destRegister = "$sp";
				byteOffset = 4 * (out + mem.index);
			} else {
				// write at in place which should not happen
				// might happen, ask for help
				destRegister = "$fp";
				byteOffset = 4 * mem.index;
			}
		}
		
		printer.println("sw " + sourceRegister + " " + String.valueOf(byteOffset) + "(" + destRegister + ")");
	}

	@Override
	public void visit(VMemRead arg0) throws IOException {
		
		// for the dest, we know that it must be register
		String destRegister = arg0.dest.toString();
		
		VMemRef source = arg0.source;
		String sourceRegister;
		int byteOffset;
		if (source instanceof VMemRef.Global) {
			// [$t1 + 4]
			VMemRef.Global mem = (VMemRef.Global) source;
			sourceRegister = mem.base.toString();
			byteOffset = mem.byteOffset;
		} else {
			// local or in
			VMemRef.Stack mem = (VMemRef.Stack) source;
			if (mem.region == VMemRef.Stack.Region.In) {
				sourceRegister = "$fp";
				byteOffset = 4 * mem.index;
			} else if (mem.region == VMemRef.Stack.Region.Local) {
				sourceRegister = "$sp";
				byteOffset = 4 * (out + mem.index);
			} else {
				// can read from out???
				sourceRegister = "$sp";
				byteOffset = 4 * mem.index;
			}
		}
		
		printer.println("lw " + destRegister + " " + String.valueOf(byteOffset) + "(" + sourceRegister + ")");
	}

	@Override
	public void visit(VBranch arg0) throws IOException {
		// can assume it is in register?
		String register = arg0.value.toString();
		String label = arg0.target.ident;
		if (arg0.positive) {
			// if
			printer.println("bnez " + register + " " + label);
		} else {
			// if0
			printer.println("beqz " + register + " " + label);
		}
	}

	@Override
	public void visit(VGoto arg0) throws IOException {
		String label = arg0.target.toString().substring(1);
		printer.println("j " + label); 
	}

	@Override
	public void visit(VReturn arg0) throws IOException {
		// TODO Auto-generated method stub
		// do nothing here, since in vaporm ret is just ret
		// we have to get back the fp sp and ra
		// and we can do that outside
		// means we have finish a function call
	}
	

}
