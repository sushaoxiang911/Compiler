class Main { 
	public static void main (String [] Args) {
		C c;
		A a;
		int return_val;
		a = new A();
		c = new C();
		return_val = c.C_fun(a); // no TE
	
	}
}

class C {
	public int C_fun(B b) {
		return 1;
	}
}

class A extends B {
}

class B {	
}

