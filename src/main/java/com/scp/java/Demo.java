package com.scp.java;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Demo {
//List<Integer>square each no is greater than 100 find the average
	
	public static void main(String[] args) {
		findAverage();
	}

private static void findAverage() {
	// TODO Auto-generated method stub
	List<Integer> num=Arrays.asList(1,5,25,30,15);
	//num.stream().filter(n-> n*5 >100).collect(Collectors.averagingInt());
int maxNumber=num.stream().max(Comparator.naturalOrder()).get();
System.out.println(maxNumber);
int minNumber=num.stream().min(Comparator.naturalOrder()).get();
}
}
