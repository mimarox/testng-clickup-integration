package com.matthiasrothe.testng.clickup.internal;

import java.util.List;
import java.util.Map;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.xml.XmlSuite;

public class TestListenerReporterAdapter implements IReporter {
	private final ITestListener testListener;

	public TestListenerReporterAdapter(ITestListener testListener) {
		this.testListener = testListener;
	}

	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		for (int i = 0; i < xmlSuites.size(); i++) {
			Map<String, ISuiteResult> results = suites.get(i).getResults();
			results.values().forEach(this::processSuiteResult);
		}
	}

	private void processSuiteResult(ISuiteResult result) {
		ITestContext context = result.getTestContext();
		testListener.onStart(context);
		
		processTestResults(context.getPassedTests(),
				testResult -> testListener.onTestSuccess(testResult));
		
		processTestResults(context.getFailedButWithinSuccessPercentageTests(),
				testResult -> testListener.onTestFailedButWithinSuccessPercentage(testResult));
		
		processTestResults(context.getFailedTests(),
				testResult -> testListener.onTestFailure(testResult));

		processTestResults(context.getSkippedTests(),
				testResult -> testListener.onTestSkipped(testResult));
		
		testListener.onFinish(context);
	}

	private void processTestResults(IResultMap testResults, ResultProcessor resultProcessor) {
		testResults.getAllResults().forEach(resultProcessor);
	}
}
