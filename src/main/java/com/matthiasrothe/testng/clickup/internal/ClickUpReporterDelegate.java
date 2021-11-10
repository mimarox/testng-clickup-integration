package com.matthiasrothe.testng.clickup.internal;

import static com.matthiasrothe.testng.clickup.internal.Util.isValid;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.JsonArray;
import net.sf.jetro.tree.JsonObject;
import net.sf.jetro.tree.JsonString;
import net.sf.jetro.tree.JsonType;
import retrofit2.Call;

public class ClickUpReporterDelegate {
	private static final JsonPath CUSTOM_FIELDS_PATH = JsonPath.compile("$.custom_fields");

	private CustomFieldHandler customFieldHandler;
	private ClickUpApi api;
	
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
			CustomFieldHandler customFieldHandler) {
		this.customFieldHandler = customFieldHandler;
		
		for (int i = 0; i < xmlSuites.size(); i++) {
			processSuite(xmlSuites.get(i), suites.get(i));
		}
	}

	private void processSuite(XmlSuite xmlSuite, ISuite suite) {
		String apiKey = xmlSuite.getParameter(ClickUpApiProvider.API_KEY_PARAMETER_NAME);
		
		if (isValid(apiKey)) {
			assureApi(() -> apiKey);
		} else {
			return;
		}

		Map<String, ISuiteResult> results = suite.getResults();
		results.values().forEach(result ->
		processSuiteResult(result, new TaskResults()));
	}

	private void assureApi(Supplier<String> apiKeySupplier) {
		api = ClickUpApiProvider.provideApi(apiKeySupplier);
	}

	private void processSuiteResult(ISuiteResult result, TaskResults taskResults) {
		ITestContext context = result.getTestContext();
		
		processTestResults(context.getPassedTests(), true, taskResults);
		processTestResults(context.getFailedButWithinSuccessPercentageTests(), true, taskResults);
		processTestResults(context.getFailedTests(), false, taskResults);
		
		processTaskResults(taskResults);
	}
	
	private void processTestResults(IResultMap testResults, boolean succeeded,
			TaskResults taskResults) {
		for (ITestResult result : testResults.getAllResults()) {
			String taskId = ClickUpApiDelegate.retrieveTaskId(result.getMethod());
			
			if (isValid(taskId)) {
				taskResults.addTaskResult(taskId, succeeded);
			}
		}
	}
	
	private void processTaskResults(TaskResults taskResults) {
		taskResults.forEach(taskResult -> processTaskResult(taskResult));
	}

	private void processTaskResult(TaskResult taskResult) {
		String taskId = taskResult.getTaskId();
		JsonObject task = ClickUpApiDelegate.getTask(api, taskId);
		
		if (task == null) {
			return;
		}
		
		String fieldId = retrieveCustomFieldId(task, customFieldHandler.getCustomFieldName(),
				customFieldHandler.getCustomFieldType());
		
		if (!isValid(fieldId)) {
			return;
		}
		
		setCustomFieldValue(api, taskId, fieldId,
				customFieldHandler.calculateNewCustomFieldValue(taskResult, task));
	}
	
	private String retrieveCustomFieldId(JsonObject task, String fieldName, String fieldType) {
		JsonObject customField = getCustomField(task, fieldName, fieldType);
		
		if (customField == null) {
			return null;
		}
		
		return ((JsonString) customField.get("id")).getValue();
	}
	
	public JsonObject getCustomField(JsonObject task, String fieldName, String fieldType) {
		Optional<JsonType> customFieldsOptional = task.getElementAt(CUSTOM_FIELDS_PATH);
		
		if (customFieldsOptional.isPresent()) {
			JsonObject customField = retrieveCustomFieldByNameAndType(
					(JsonArray) customFieldsOptional.get(), fieldName, fieldType);
			
			return customField;
		} else {
			return null;
		}
	}

	private JsonObject retrieveCustomFieldByNameAndType(JsonArray customFields,
			String fieldName, String fieldType) {
		for (int i = 0; i < customFields.size(); i++) {
			JsonObject customField = (JsonObject) customFields.get(i);
			
			if (fieldName.equals(((JsonString) customField.get("name")).getValue())
					&& fieldType.equals(((JsonString) customField.get("type")).getValue())) {
				return customField;
			}
		}
		
		return null;
	}
	
	private void setCustomFieldValue(ClickUpApi api, String taskId, String fieldId,
			JsonType value) {
		JsonObject body = new JsonObject();
		body.asMap().put("value", value);
		
		Call<JsonObject> setCustomFieldValueCall = api.setCustomFieldValue(taskId, fieldId, body);
		
		try {
			setCustomFieldValueCall.execute();
		} catch (IOException e) {
		}
	}
}
