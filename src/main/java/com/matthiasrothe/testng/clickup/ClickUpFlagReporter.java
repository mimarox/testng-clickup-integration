package com.matthiasrothe.testng.clickup;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.testng.IReporter;
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

public class ClickUpFlagReporter implements IReporter {
	private static class TaskResult {
		private final String taskId;
		private final boolean succeeded;
		
		private TaskResult(String taskId, boolean succeeded) {
			this.taskId = taskId;
			this.succeeded = succeeded;
		}
		
		String getTaskId() {
			return taskId;
		}
		
		boolean getSucceeded() {
			return succeeded;
		}
	}
	
	private static class TaskResults implements Iterable<TaskResult> {
		 private Map<String, Set<Boolean>> taskResults = new HashMap<>();

		public void addTaskResult(String taskId, boolean succeeded) {
			if (isValid(taskId)) {
				if (taskResults.containsKey(taskId)) {
					taskResults.get(taskId).add(succeeded);
				} else {
					Set<Boolean> values = new HashSet<>();
					values.add(succeeded);
					taskResults.put(taskId, values);
				}
			}
		}

		@Override
		public Iterator<TaskResult> iterator() {
			Iterator<Entry<String, Set<Boolean>>> inner = taskResults.entrySet().iterator();
			
			return new Iterator<TaskResult>() {

				@Override
				public boolean hasNext() {
					return inner.hasNext();
				}

				@Override
				public TaskResult next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					
					Entry<String, Set<Boolean>> next = inner.next();
					
					String taskId = next.getKey();
					boolean succeeded;
					
					if (next.getValue().contains(false)) {
						succeeded = false;
					} else {
						succeeded = true;
					}
					
					return new TaskResult(taskId, succeeded);
				}
				
			};
		}
	}
	
	private ClickUpApi api;
	
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
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
		results.values().forEach(result -> processSuiteResult(result, new TaskResults()));
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
		taskResults.forEach(this::processTaskResult);
	}

	private void processTaskResult(TaskResult taskResult) {
		String taskId = taskResult.getTaskId();
		boolean testsSucceeded = taskResult.getSucceeded();
		
		JsonObject task = ClickUpApiDelegate.getTask(api, taskId);
		
		if (task == null) {
			return;
		}
		
		String fieldId = retrieveFieldId(task);
		
		if (!isValid(fieldId)) {
			return;
		}
		
		setTestsSucceededFlag(taskId, fieldId, testsSucceeded);
	}
	
	private String retrieveFieldId(JsonObject task) {
		JsonPath customFieldsPath = JsonPath.compile("$.custom_fields");
		
		Optional<JsonType> customFieldsOptional = task.getElementAt(customFieldsPath);
		
		if (customFieldsOptional.isPresent()) {
			JsonObject testsSucceededCustomField = retrieveTestsSucceededCustomField(
					(JsonArray) customFieldsOptional.get());
			
			if (testsSucceededCustomField == null) {
				return null;
			}
			
			return ((JsonString) testsSucceededCustomField.get("id")).getValue();
		} else {
			return null;
		}
	}

	private JsonObject retrieveTestsSucceededCustomField(JsonArray customFields) {
		for (int i = 0; i < customFields.size(); i++) {
			JsonObject customField = (JsonObject) customFields.get(i);
			
			if ("Tests Succeeded".equals(((JsonString) customField.get("name")).getValue())
					&& "checkbox".equals(((JsonString) customField.get("type")).getValue())) {
				return customField;
			}
		}
		
		return null;
	}
	
	private void setTestsSucceededFlag(String taskId, String fieldId, boolean testsSucceeded) {
		JsonObject body = new JsonObject();
		body.asMap().put("value", testsSucceeded);
		
		Call<JsonObject> setFlagCall = api.setCustomFieldValue(taskId, fieldId, body);
		
		try {
			setFlagCall.execute();
		} catch (IOException e) {
		}
	}

	private static boolean isValid(String value) {
		return !(value == null || "".equals(value));
	}
}
