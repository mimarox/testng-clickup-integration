package com.matthiasrothe.testng.clickup.internal;

public class Util {
	private Util() {}
	
	public static boolean isValid(String value) {
		return !(value == null || "".equals(value));
	}
}
