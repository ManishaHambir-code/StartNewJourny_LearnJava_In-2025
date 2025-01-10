package com.scp.java.java8Concepts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Employee {
private int id;
private String name;
private int age;
private String region;
private double salary;//belong to same city/
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public int getAge() {
	return age;
}
public void setAge(int age) {
	this.age = age;
}
public String getRegion() {
	return region;
}
public void setRegion(String region) {
	this.region = region;
}
public double getSalary() {
	return salary;
}
public void setSalary(double salary) {
	this.salary = salary;
}
public Employee(int id, String name, int age, String region, double salary) {
	super();
	this.id = id;
	this.name = name;
	this.age = age;
	this.region = region;
	this.salary = salary;
}
@Override
public String toString() {
	return "Employee [id=" + id + ", name=" + name + ", age=" + age + ", region=" + region + ", salary=" + salary + "]";
}

public Employee(){
	
}
public static void main(String[] args) {
	List<Employee> employeeList=new ArrayList <>();
	employeeList.add(new Employee(100,"Sunder",47,"North America",45000));
	employeeList.add(new Employee(200,"Pichai",25,"North America",50000));
	employeeList.add(new Employee(300,"Manisha",35,"India",54000));
	employeeList.add(new Employee(400,"Larry",30,"Aisa",45000));
	
	//collect only employee names into list
	List<String> countNames=employeeList.stream().map(e ->e.getName()).collect(Collectors.toList());
	System.out.println(countNames);
	System.out.println("=================");
	//collect all the regions into set
	Set<String> countRegions=employeeList.stream().map(e -> e.getRegion()).collect(Collectors.toSet());
	System.out.println(countRegions);
	
	countRegions.add("hello");
	System.out.println(countRegions);
	System.out.println("==========================");
	//toUnmodifiableSet() works similar to the toSet() but this set can not be modified
	
	Set<Double> unmodifiableSet=employeeList.stream().map(e -> e.getSalary()).collect(Collectors.toUnmodifiableSet());
	System.out.println(unmodifiableSet);
	}
}

