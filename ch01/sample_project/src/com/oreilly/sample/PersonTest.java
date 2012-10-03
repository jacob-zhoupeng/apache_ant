package com.oreilly.sample;

// import junit.framework.TestCast;
// import junit.framework.Assert;

public class PersonTest /* extends TestCase */ {
	protected void setUp() throws Exception {
		// super.setUp();
		System.out.println("initialzating ...");
	}

	protected void tearDown() throws Exception {
		System.out.println("tear down ...");
		// super.tearDown();
	}

	public void testGetAndSetFirstName() {
		Person p = new Person();
		p.setFirstName("Zhou Peng");
		String actual = p.getFirstName();
		String expected = "Zhou Peng";
		// Assert.assertEquals(expected, actual);
	}

	public void testGetAndSetLastName() {
		Person p = new Person();
		p.setLastName("Zhou Peng");
		String actual = p.getLastName();
		String expected = "Zhou Peng";
		// Assert.assertEquals(expected, actual);
	}
}
