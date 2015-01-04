class Main
{
	public static void main(String[] argu) 
	{
		B b;
		b = new A();
		System.out.println(b.function1());
	}
}

class A extends B {
	public int function2 () {
		return 1;
	
	}
	public int function1 () {
		return 1;
	}	
}


class B {
	public int function1 () {
		return 2;
	}
}