class Main { 
	public static void main (String [] Args) {
	}
}

class A extends B {
	int A_local;
	public int A_fun (int a) {
		A_local = 3;
		B_local = 5;// no error here
		return B_local;
	}
}

class B {
	int B_local;
	
	
}