class Main {
public static void main(String[] a){
	System.out.println(1);
	}
}
 
class A {
	int aa;
	public int a() {
		return 0;
	}
 
	public boolean af()
	{
		return false && true;
	}
}
 
class B extends A {
	int bb;
	public int b() {
		aa = 1;
		return 1;
	}
 
	public boolean bf()
	{
		return true && (this.af());
	}
}
 
class C extends B {
	int cc;
	public int c() {
		return 2;
	}
}
 
 
class D extends C {
	int dd;
	public int d(A a, B b)
	{
		B bbb;
		bbb = new D();
		return 1;
	}
 
	public int d1()
	{
		int x;
		int y;
		int z;
		A a;
		B b;
		C c;
		x = this.d (new A(), new B());
		x = this.d (new A(), new C());
		x = this.d (new A(), new D());
		x = this.d (new A(), new F());
		x = this.d (new B(), new B());
		x = this.d (new B(), new C());
		x = this.d (new B(), new D());
		x = this.d (new B(), new F());
		x = this.d (new C(), new B());
		x = this.d (new C(), new C());
		x = this.d (new C(), new D());
		x = this.d (new C(), new F());
		x = this.d (new D(), new B());
		x = this.d (new D(), new C());
		x = this.d (new D(), new D());
		x = this.d (new D(), new F());
		aa = this.b();
		bb = this.c();
		bb = a.a();
		bb = b.a();
		bb = c.c();
		bb = c.a();
		bb = c.b();
		bb = c.c();
		cc = 1;
		dd = 1;
		dd = 1;
		aa = 1;
		bb = 1;
		cc = 1;
 
		return 1;
	}
}
 
class F extends D {
	E e; // no error here
	A a;
	D d;
	int fff;
	int[] ar;
	boolean x;
	public int f()
	{
		x = true;
		x = false;
		x = true && false;
		x = true && true;
		x = false && false;
		x = (this.af()) && (this.bf());
		fff = 1 + 2;
		fff = 3 * 2;
		aa = ar[0];
		aa = ar[(this.a())];
		aa = ar[((new D()).a())];
		fff = this.a();
		fff = this.b();
		fff = this.c();
		fff = this.d1();
		return 1;
	}
 
	public int[] aaarrr()
	{
		int x;
		x = 1;
		return (new int[4]);
	}
}
 
class G extends F{
	int[] a;
	// no error here
	public int g(KK k)
	{
		a = this.aaarrr();
		return (new C()).a();
	}
}