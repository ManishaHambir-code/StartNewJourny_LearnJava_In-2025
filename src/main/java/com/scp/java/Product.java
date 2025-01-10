package com.scp.java;

import java.util.ArrayList;
import java.util.List;

public class Product {
private int id;
private String name;
private String price;
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
public String getprice() {
	return price;
}
public void setprice(String price) {
	this.price = price;
}
public Product(int id, String name, String price) {
	super();
	this.id = id;
	this.name = name;
	this.price = price;
}
@Override
public String toString() {
	return "Product [id=" + id + ", name=" + name + ", price=" + price + "]\n";
}

public Product(){
	
}

public static void main(String[] args) {
	List<Product> list=new ArrayList<Product>();
	list.add(new Product(11, "Book", "400"));
	list.add(new Product(12, "HearPhone", "200"));
	list.add(new Product(13, "Computer", "40000"));
	list.add(new Product(14, "Pen", "100"));
	System.out.println(list);
	
	System.out.println("All method of List");
	list.add(3, new Product(16, "Pencils", "320"));
	System.out.println("add new element: " +list);
	System.out.println("Size of list :"+list.size());
	System.out.println("use contains method :"+list.contains(list));
	
}
}
