
public class UnitMemory extends Unit{
	int memId;
	public UnitMemory(String name, int id) {
		super(name);
		memId = id;
	}
	@Override
	boolean isRegister() {
		return false;
	}
}
