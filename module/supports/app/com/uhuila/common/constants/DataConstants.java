package com.uhuila.common.constants;

public enum DataConstants {
	ZERO(0),
	ONE(1),
	TWO(2),
	THREE(3), 
	FOUR(4);
	private int value;

	DataConstants(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
