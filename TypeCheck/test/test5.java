class Main { 
	public static void main (String [] Args) {
		int r_val;
		A a;
		a = new A();
		r_val = a.B_fun(1);//no TE
	}
}


class A extends B {
}

class B {	
	public int B_fun(int a) {
		return a;
	}

}

