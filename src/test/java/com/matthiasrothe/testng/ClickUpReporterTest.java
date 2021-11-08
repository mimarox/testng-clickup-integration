package com.matthiasrothe.testng;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.matthiasrothe.testng.clickup.ClickUp;

public class ClickUpReporterTest {

	@Test
	@ClickUp(taskId = "1r52qwe")
	public void shouldSucceed() {
		assertTrue(true);
	}
}
