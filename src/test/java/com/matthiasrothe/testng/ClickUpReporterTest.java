package com.matthiasrothe.testng;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.matthiasrothe.testng.clickup.ClickUp;

public class ClickUpReporterTest {

	@Test(dataProvider = "dp")
	@ClickUp(taskId = "1r52qwe")
	public void testIsBetween(int value, int lowerBound, int upperBound, boolean expected) {
		assertEquals(isBetween(value, lowerBound, upperBound), expected);
	}

	@DataProvider(name = "dp")
	public Object[][] isBetweenDataProvider() {
		final int lowerBound = 5;
		final int upperBound = 10;
		
		return new Object[][] {
			{ 0, lowerBound, upperBound, false},
			{ 5, lowerBound, upperBound, true},
			{ 7, lowerBound, upperBound, true},
			{10, lowerBound, upperBound, true},
			{20, lowerBound, upperBound, false},
		};
	}
	
	private boolean isBetween(int value, int lowerBound, int upperBound) {
		return value >= lowerBound && value <= upperBound;
	}
}
