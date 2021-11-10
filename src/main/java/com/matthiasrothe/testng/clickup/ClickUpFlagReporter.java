package com.matthiasrothe.testng.clickup;

import java.util.List;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

import com.matthiasrothe.testng.clickup.internal.ClickUpReporterDelegate;
import com.matthiasrothe.testng.clickup.internal.CustomFieldHandler;
import com.matthiasrothe.testng.clickup.internal.TaskResult;

import net.sf.jetro.tree.JsonBoolean;
import net.sf.jetro.tree.JsonObject;

public class ClickUpFlagReporter implements IReporter {
	
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		new ClickUpReporterDelegate().generateReport(xmlSuites, suites, new CustomFieldHandler() {
			
			@Override
			public String getCustomFieldType() {
				return "checkbox";
			}
			
			@Override
			public String getCustomFieldName() {
				return "Tests Succeeded";
			}
			
			@Override
			public JsonBoolean calculateNewCustomFieldValue(TaskResult taskResult, JsonObject task) {
				return new JsonBoolean(taskResult.getSucceeded());
			}
		});
	}
}
