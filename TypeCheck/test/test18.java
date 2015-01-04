class Main { 
	public static void main (String [] Args) {
		A a;
		B b;
		int r;
		a = new A();
		r = a.a(1, b);	// TE
	}
}



class A {
	public int a (int a, B b) {
		return 1;
	}
}