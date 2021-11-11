package com.matthiasrothe.testng.clickup;

import java.util.List;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

import com.matthiasrothe.testng.clickup.internal.TestListenerReporterAdapter;

/**
 * Use this reporter to run all reporters in the TestNG ClickUp Plug-In.
 * 
 * @author Matthias Rothe
 */
public class ClickUpFullReporter implements IReporter {
	private final ClickUpFlagReporter flagReporter = new ClickUpFlagReporter();
	private final ClickUpRatingReporter ratingReporter = new ClickUpRatingReporter();
	private final TestListenerReporterAdapter commentReporter =
			new TestListenerReporterAdapter(new ClickUpCommentReporter());
			
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		flagReporter.generateReport(xmlSuites, suites, outputDirectory);
		ratingReporter.generateReport(xmlSuites, suites, outputDirectory);
		commentReporter.generateReport(xmlSuites, suites, outputDirectory);
	}
}
