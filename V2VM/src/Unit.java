
public abstract class Unit {
	String name;
	public Unit(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	abstract boolean isRegister();
}
