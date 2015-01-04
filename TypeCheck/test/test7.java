class Main { 
	public static void main (String [] Args) {
		B b;
		b = new A();	// no TE
	}
}


class A extends B {
}

class B {
}