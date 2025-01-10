package com.scp.java.exception;

public class Example2 {
public static void main(String[] args) {
	m1();
}
public static void m1() {
	m2();
}
public static void m2() {
	m3();
}
public static void m3() {
	System.out.println(1/0);
}
}
