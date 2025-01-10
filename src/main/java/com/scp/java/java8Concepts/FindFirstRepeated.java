package com.scp.java.java8Concepts;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.*;
public class FindFirstRepeated {
public static void main(String[] args) {
	String str="Hello Java";
	Map<String,Long> input=Arrays.stream(str.split(""))
			.collect(Collectors.groupingBy(Function.identity(),LinkedHashMap::new,Collectors.counting()));
	String firstRepeat=input.entrySet().stream().filter(entry ->entry.getValue()>1)
			.map(entry -> entry.getKey()).findFirst().get();
	System.out.println(firstRepeat);
}
}
