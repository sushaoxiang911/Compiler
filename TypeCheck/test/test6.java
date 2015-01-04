class Main { 
	public static void main (String [] Args) {
	}
}

class C {
}


class A extends B {
	public int overload_fun(int A_a, int[] A_b, C A_c) { // TE
		return 1;
	}
}

class B {	
	public int[] overload_fun(int B_a, int B_b, C B_c) {
		return 1;
	}
}

