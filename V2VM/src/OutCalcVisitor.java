import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VInstr.VisitorR;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;


public class OutCalcVisitor extends VisitorR<Integer, RuntimeException> {

	@Override
	public Integer visit(VAssign arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer visit(VCall arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return arg0.args.length;
	}

	@Override
	public Integer visit(VBuiltIn arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer visit(VMemWrite arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer visit(VMemRead arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer visit(VBranch arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer visit(VGoto arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer visit(VReturn arg0) throws RuntimeException {
		// TODO Auto-generated method stub
		return 0;
	}

}
