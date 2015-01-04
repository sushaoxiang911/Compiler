// EXT:ISC
class Main {
	
	public static void main(String[] args){
		Test test;
		Base base;
		Extends extend;
		boolean result;
		test = new Test();
		base = new Extends(); // ok
		result = test.testParameters(extend); // not ok
	}

}

class Test {

	public boolean testParameters(Base e){
		return true;
	}

}


class Base {



}

class Extends extends Base {

}


