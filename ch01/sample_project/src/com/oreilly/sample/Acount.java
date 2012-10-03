package com.oreilly.sample;

import java.util.Date;

public class Acount {
	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public float getMoney() {
		return money;
	}

	public void setMoney(float newMoney) {
		money = newMoney;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date newStartTime) {
		startTime = newStartTime;
	}

	private String name;
	private float money;
	private Date startTime;
}
