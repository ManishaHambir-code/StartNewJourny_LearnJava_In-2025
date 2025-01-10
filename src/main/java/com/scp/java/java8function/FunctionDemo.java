package com.scp.java.java8function;
import java.util.function.Function;
public class FunctionDemo {
public static void main(String[] args) {
	Function<Integer,Double> halfvalue=i -> i/2.0;
	Double result=halfvalue.apply(10);
	System.out.println(result);
	
	//Requirement : Given String should be converted to UpperCase
	
Function<String, String> func1ToUppercase= s-> s.toUpperCase();
// use method references instead of lambda expression 
Function<String, String> convertedToUppercase=String::toUpperCase;
System.out.println(convertedToUppercase.apply("Manisha Hambir"));//user name-ManishaHambir-code//maniSha@123
}
}
