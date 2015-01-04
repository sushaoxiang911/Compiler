import java.io.IOException;

import cs132.util.IndentPrinter;
import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VVarRef;


public class InstrVisitor extends Visitor<IOException> {
	Allocator allocator;
	IndentPrinter printer;
	
	public InstrVisitor(Allocator allocator, IndentPrinter printer) {
		super();
		this.allocator = allocator;
		this.printer = printer;
	}

	@Override
	public void visit(VAssign arg0) throws IOException {
		// TODO Auto-generated method stub
		String temporary = arg0.dest.toString();
		// here a source can be unit or a constant
		String sourceName = arg0.source.toString();
		if (!allocator.temporaryExist(temporary))
			allocator.allocate(temporary);
		Unit unit = allocator.getUnit(temporary);
		// get the unit
		// memory or a register
		// if the sourceName is not a temporary
		if (!allocator.temporaryExist(sourceName)) {
			printer.println(unit.getName() + " = " + sourceName);
		} else {
			Unit sourceUnit = allocator.getUnit(sourceName);
			// only the senario that both of the units are in memory
			if (!sourceUnit.isRegister() && !unit.isRegister()) {
				// use v1
				printer.println("$v1 = " + sourceUnit.getName());
				printer.println(unit.getName() + " = $v1");
			} else {
				printer.println(unit.getName() + " = " + sourceUnit.getName());
			}
		}
//		System.out.println(arg0.dest.toString());
//		System.out.println(arg0.source.toString());
	}

	@Override
	public void visit(VCall arg0) throws IOException {
		// TODO Auto-generated method stub
		VOperand[] args = arg0.args;
		// put the value into a0 - a4 and out
		for (int i = 0; i < args.length; i++) {
			String argName = args[i].toString();
			// if it is a constant value
			if (!allocator.temporaryExist(argName)) {
				if (i <= 3)
					printer.println("$a" + i + " = " + argName);
				else {
					int outId = i - 4;
					printer.println("out[" + outId + "] = " + argName);
				}
				continue;
			}
			Unit argUnit = allocator.getUnit(argName);
			// here use register
			// no matter the value is in memory or in 
			if (i <= 3) {
				printer.println("$a" + i + " = " + argUnit.getName());
			} else {
				// if the unit is register
				// save directly
				int outId = i - 4;
				if (argUnit.isRegister()) {
					printer.println("out[" + outId + "] = " + argUnit.getName());
				} else {
					// not a register but in local, put in v1 first and in out than
					printer.println("$v1 = " + argUnit.getName());
					printer.println("out[" + outId + "] = $v1");
				}
			}
		}
		// the address of the function
		// can be a temporary or a label
		String functionCall = arg0.addr.toString();
		// if it is a temporary
		if (allocator.temporaryExist(functionCall)) {
			Unit functionUnit = allocator.getUnit(functionCall);
			if (functionUnit.isRegister()) {
				functionCall = functionUnit.getName();
			} else {
				printer.println("$v1 = " + functionUnit.getName());
				functionCall = "$v1";
			}
		}
		// call the function
		printer.println("call " + functionCall);
		// save back the return value
		String returnTemporary = arg0.dest.toString();
		if (!allocator.temporaryExist(returnTemporary))
			allocator.allocate(returnTemporary);
		Unit returnUnit = allocator.getUnit(returnTemporary);
		printer.println(returnUnit.getName() + " = $v0");
		
	}

	@Override
	public void visit(VBuiltIn arg0) throws IOException {
		// TODO Auto-generated method stub
		VBuiltIn.Op op = arg0.op;
		VOperand[] operand = arg0.args;
		if (op == VBuiltIn.Op.Error) {
			printer.println(op.name + "(" + operand[0].toString() + ")");
		} else if (op == VBuiltIn.Op.PrintIntS) {
			String argValue = operand[0].toString();
			if (allocator.temporaryExist(argValue) && !allocator.getUnit(argValue).isRegister()) {
				// the only scenario is the argValue is in the memory
				printer.println("$v1 = " + allocator.getUnit(argValue).getName());
				argValue = "$v1";
			} else if (allocator.temporaryExist(argValue) && allocator.getUnit(argValue).isRegister()) {
				argValue = allocator.getUnit(argValue).getName();
			}
			printer.println(op.name +  "(" + argValue + ")");
		} else if (op == VBuiltIn.Op.HeapAllocZ) {
			String argValue = operand[0].toString();
			if (allocator.temporaryExist(argValue) && !allocator.getUnit(argValue).isRegister()) {
				// the only scenario is the argValue is in the memory
				printer.println("$v1 = " + allocator.getUnit(argValue).getName());
				argValue = "$v1";
			} else if (allocator.temporaryExist(argValue) && allocator.getUnit(argValue).isRegister()) {
				argValue = allocator.getUnit(argValue).getName();
			}
			
			// save back the return value
			String returnTemporary = arg0.dest.toString();
			if (!allocator.temporaryExist(returnTemporary))
					allocator.allocate(returnTemporary);
			Unit returnUnit = allocator.getUnit(returnTemporary);
			if (returnUnit.isRegister()) {
				// if it is register for return value
				printer.println(returnUnit.getName() + " = " + op.name + "(" + argValue + ")");
			} else {
				printer.println("$v0" + " = " + op.name + "(" + argValue + ")");
				printer.println(returnUnit.getName() + " = $v0");
			}			
		} else {
			// two arguments op
			String arg0Value = operand[0].toString();
			String arg1Value = operand[1].toString();
			if (allocator.temporaryExist(arg0Value) && !allocator.getUnit(arg0Value).isRegister()) {
				// the only scenario is the argValue is in the memory
				printer.println("$v0 = " + allocator.getUnit(arg0Value).getName());
				arg0Value = "$v0";
			} else if (allocator.temporaryExist(arg0Value) && allocator.getUnit(arg0Value).isRegister())
				arg0Value = allocator.getUnit(arg0Value).getName();
			
			if (allocator.temporaryExist(arg1Value) && !allocator.getUnit(arg1Value).isRegister()) {
				// the only scenario is the argValue is in the memory
				printer.println("$v1 = " + allocator.getUnit(arg1Value).getName());
				arg1Value = "$v1";
			} else if (allocator.temporaryExist(arg1Value) && allocator.getUnit(arg1Value).isRegister())
				arg1Value = allocator.getUnit(arg1Value).getName();
			
			// save back the return value
			String returnTemporary = arg0.dest.toString();
			if (!allocator.temporaryExist(returnTemporary))
				allocator.allocate(returnTemporary);
			Unit returnUnit = allocator.getUnit(returnTemporary);
			if (returnUnit.isRegister()) {
				// if it is register for return value
				printer.println(returnUnit.getName() + " = " + op.name + "(" + arg0Value + " " + arg1Value + ")");
			} else {
				printer.println("$v0" + " = " + op.name + "(" + arg0Value + " " + arg1Value + ")");
				printer.println(returnUnit.getName() + " = $v0");
			}
		}
		
	}

	@Override
	public void visit(VMemWrite arg0) throws IOException {
		// TODO Auto-generated method stub
		String sourceTemporary = arg0.source.toString();
		String rvalue = sourceTemporary;
		// can be a function label
		if (allocator.temporaryExist(sourceTemporary)) {
			Unit sourceUnit = allocator.getUnit(sourceTemporary);
			String sourceRegister = sourceUnit.getName();
			if (!sourceUnit.isRegister()) {
				// $v0 cache source
				printer.println("$v0 = " + sourceUnit.getName());
				sourceRegister = "$v0";
			}
			rvalue = sourceRegister;
		}
		
		// can be an address that is [temporary+1]
		VMemRef.Global memRef = (Global) arg0.dest;
		String baseTemporary = memRef.base.toString();
		int offset = memRef.byteOffset;
		
		Unit destUnit = allocator.getUnit(baseTemporary);
		String baseRegister = destUnit.getName();
		if (!destUnit.isRegister()) {
			printer.println("$v1 = " + destUnit.getName());
			baseRegister = "$v1";
		}
		// memory write
		if (offset > 0) 
			printer.println("[" + baseRegister + "+" + offset + "] = " + rvalue);
		else
			printer.println("[" + baseRegister + "] = " + rvalue);
	}

	@Override
	public void visit(VMemRead arg0) throws IOException {
		// TODO Auto-generated method stub
		VMemRef.Global memRef = (Global) arg0.source;
		String baseTemporary = memRef.base.toString();
		int offset = memRef.byteOffset;
		Unit baseUnit = allocator.getUnit(baseTemporary);
		String baseRegister = baseUnit.getName();
		if (!baseUnit.isRegister()) {
			printer.println("$v1 = " + baseUnit.getName());
			baseRegister = "$v1";
		}
		
		String destTemporary = arg0.dest.toString();
		if (!allocator.temporaryExist(destTemporary)) {
			allocator.allocate(destTemporary);
		}
		Unit destUnit = allocator.getUnit(destTemporary);
		if (!destUnit.isRegister()) {
			if (offset > 0)
				printer.println("$v0 = " + "[" + baseRegister + "+" + offset + "]");
			else 
				printer.println("$v0 = " + "[" + baseRegister + "]");
			printer.println(destUnit.getName() + " = $v0");
		} else 
			if (offset > 0)
				printer.println(destUnit.getName() + " = " +"[" + baseRegister + "+" + offset + "]");
			else
				printer.println(destUnit.getName() + " = " + "[" + baseRegister + "]");
	}
	
	@Override
	public void visit(VBranch arg0) throws IOException {
		// TODO Auto-generated method stub
		String branchValue = arg0.value.toString();
		if (allocator.temporaryExist(branchValue) && !allocator.getUnit(branchValue).isRegister()) {
			// the only scenario is the argValue is in the memory
			printer.println("$v0 = " + allocator.getUnit(branchValue).getName());
			branchValue = "$v0";
		} else if (allocator.temporaryExist(branchValue) && allocator.getUnit(branchValue).isRegister())
			branchValue = allocator.getUnit(branchValue).getName();
		
		String labelGoto = arg0.target.ident;
		
		printer.print("if");
		if (!arg0.positive)
			printer.print("0");
		printer.println(" " + branchValue + " goto :" + labelGoto);
	}

	@Override
	public void visit(VGoto arg0) throws IOException {
		// TODO Auto-generated method stub
		String labelGoto = arg0.target.toString();
		printer.println("goto " + labelGoto);
	}

	@Override
	public void visit(VReturn arg0) throws IOException {
		if (arg0.value == null)
			return;
		String returnValue = arg0.value.toString();
		if (allocator.temporaryExist(returnValue)) {
			Unit returnUnit = allocator.getUnit(returnValue);
			returnValue = returnUnit.getName();
		}
		// cache to $v0
		printer.println("$v0 = " + returnValue);
	}

}
