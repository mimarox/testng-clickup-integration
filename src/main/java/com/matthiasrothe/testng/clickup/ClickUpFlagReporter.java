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

/**
 * This reporter checks or unchecks a check box on each task that has at
 * least been referenced once as the taskId on a test method annotated with
 * &#64;{@link ClickUp}. It checks it if all invocations of test methods so
 * annotated with the same taskId were executed successfully. Otherwise it
 * unchecks the check box.
 * <p>
 * The check box MUST be a custom field on the ClickUp task named &quot;Tests
 * Succeeded&quot; of type &quot;checkbox&quot;.
 * <p>
 * This reporter expects a parameter named click_up_api_key to be configured
 * at the suite level with the value set to the API key provided by the ClickUp
 * app.
 * 
 * @author Matthias Rothe
 */
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
