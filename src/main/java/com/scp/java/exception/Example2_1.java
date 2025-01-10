package com.scp.java.exception;

public class Example2_1 {
public static void main(String[] args) {
	m1();
}
public static void m1() {
	m2();
	System.out.println(1/0);
}
public static void m2() {
	System.out.println("Hello");
}
}
