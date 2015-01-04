import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import cs132.util.IndentPrinter;
import cs132.vapor.ast.VVarRef;


public class Allocator {
	private static int CALLEE_REGISTER_NUM = 8;
	private static int CALLER_REGISTER_NUM = 9;
	
	
	Hashtable<String, Unit> temporaryTable;
	Vector<Unit> availableRegister;
	int localPos;
	
	IndentPrinter printer;
	public Allocator(IndentPrinter printer, int localPos) {
		this.printer = printer;
		
		temporaryTable = new Hashtable<String, Unit>();
		availableRegister = new Vector<Unit>();
		for (int i = 0; i < CALLEE_REGISTER_NUM; ++i)
			availableRegister.add(new UnitRegister("$s" + String.valueOf(i)));
		for (int i = 0; i < CALLER_REGISTER_NUM; ++i)
			availableRegister.add(new UnitRegister("$t" + String.valueOf(i)));
		this.localPos = localPos;
	}
	
	// this function find the allocation place for argument or temporary
	Unit findPlace() {
		if (!availableRegister.isEmpty()) {
			Unit result = availableRegister.firstElement();
			availableRegister.remove(0);
			return result;
		} else {
			int memId = localPos;
			localPos++;
			return new UnitMemory(new String("local[" + String.valueOf(memId) + "]"), memId);
		}
	}
	public void allocateParameter(VVarRef.Local[] parameters) throws IOException {
		for (int i = 0; i < parameters.length; ++i) {
			if (i <= 3) {
				// if the argument in the register
				// find the place for the parameter
				Unit place = findPlace();
				// print the assign line
				printer.println(place.getName() + " = " + "$a" + i);
				// save into the map
				temporaryTable.put(parameters[i].ident, place);
			} else {
				// it is in the in
				// use a v0 to cache and save into the new place
				int inId = i - 4;
				Unit place = findPlace();
				if (!place.isRegister()) {
					printer.println("$v0 = " + "in[" + inId + "]");
					printer.println(place.getName() + " = $v0");
				}else
					printer.println(place.getName() + " = in[" + inId + "]");
				// save into the map
				temporaryTable.put(parameters[i].ident, place);
			}
		}
	}
	
	// if the temporary is existed
	public boolean temporaryExist(String temporary) {
		return temporaryTable.containsKey(temporary);
	}
	
	public void allocate(String temporary) {
		Unit place = findPlace();
		temporaryTable.put(temporary, place);
	}
	
	public Unit getUnit(String temporary) {
		return temporaryTable.get(temporary);
	}
	
}
