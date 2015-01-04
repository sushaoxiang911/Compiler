class Main { 
	public static void main (String [] Args) {
		int a;
		int b;
		a = 1;
	}
}

class A extends B {
	int A_var;
	public int fun(int a, int[] b) {
		int[] A_local;
		return a;
	}
}


class B extends C{
	int B_var;
	public int fun(int a, int[]b) {
		int B_local;
		return b;
	}
	
	public int[] foo (int c, int d) {
		return d;
	}
}

class C {
	int C_var;
	public int fun(int a, int[]b) {
		int C_local;
		return a;
	}
	public int boo() {
		return 1;
	}
}

class D extends B {
}
