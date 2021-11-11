package com.matthiasrothe.testng.clickup.internal;

import java.util.function.Consumer;

import org.testng.ITestResult;

@FunctionalInterface
public interface ResultProcessor extends Consumer<ITestResult> {
}
