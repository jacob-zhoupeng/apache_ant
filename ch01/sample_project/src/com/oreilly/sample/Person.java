package com.oreilly.sample;

public class Person {
	public int getAge() {
		return age;
	}

	public void setAge(int newAge) {
		age = newAge;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String newFirstName) {
		firstName = newFirstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String newLastName) {
		lastName = newLastName;
	}

	private int age;
	private String firstName;
	private String lastName;
}
