package com.matthiasrothe.testng.clickup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.JsonArray;
import net.sf.jetro.tree.JsonNumber;
import net.sf.jetro.tree.JsonObject;
import net.sf.jetro.tree.JsonObject.JsonProperties;
import net.sf.jetro.tree.JsonType;
import retrofit2.Call;

public class ClickUpCommentReporter implements ITestListener {
	private static final DateTimeFormatter FORMATTER =
			new DateTimeFormatterBuilder().appendLocalized(FormatStyle.MEDIUM, FormatStyle.MEDIUM).
			toFormatter(Locale.getDefault());
	
	private final ClickUpApi api;
	
	private String apiKey;
	
	public ClickUpCommentReporter() {
		api = ClickUpApiProvider.provideApi(() -> apiKey);
	}

	@Override
	public void onStart(ITestContext context) {
		apiKey = context.getSuite().getParameter(ClickUpApiProvider.API_KEY_PARAMETER_NAME);
	}
	
	@Override
	public void onTestSuccess(ITestResult result) {
		logToClickUp(result, true);
	}
    
	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		logToClickUp(result, true);
	}
	
	@Override
	public void onTestFailure(ITestResult result) {
		logToClickUp(result, false);
	}
	
	private void logToClickUp(ITestResult result, boolean testSucceeded) {
		String taskId = ClickUpApiDelegate.retrieveTaskId(result.getMethod());
		
		if (taskId == null || "".equals(taskId)) {
			return;
		}
		
		JsonObject task = ClickUpApiDelegate.getTask(api, taskId);
		
		if (task == null) {
			return;
		}
		
		JsonNumber assigneeId = retrieveAssigneeId(task);
		
		if (assigneeId == null) {
			return;
		}
		
		createTaskComment(taskId, assigneeId, result, testSucceeded);
	}

	private JsonNumber retrieveAssigneeId(JsonObject task) {
		JsonPath assigneesPath = JsonPath.compile("$.assignees");
		
		Optional<JsonType> assigneesOptional = task.getElementAt(assigneesPath);
		
		if (assigneesOptional.isPresent()) {
			JsonObject assignee = (JsonObject) ((JsonArray) assigneesOptional.get()).get(0);
			return (JsonNumber) assignee.get("id");
		} else {
			return null;
		}
	}

	private void createTaskComment(String taskId, JsonNumber assigneeId,
			ITestResult result,	boolean testSucceeded) {
		JsonProperties body = new JsonObject().asMap();
		body.put("comment_text", buildCommentText(result, testSucceeded));
		body.put("assignee", assigneeId);
		body.put("notify_all", true);
		
		Call<JsonObject> createCommentCall = api.createTaskComment(taskId, body.asJsonObject());

		try {
			createCommentCall.execute();
		} catch (IOException e) {
		}
	}
	
	private String buildCommentText(ITestResult result, boolean testSucceeded) {
		String nowString = LocalDateTime.now().format(FORMATTER);
		String testResultString = testSucceeded ? "succeeded" : "failed";
		
		Object[] parameters = result.getParameters();
		String testDataString;
		
		if (parameters != null && parameters.length > 0) {
			testDataString = toString(parameters);
		} else {
			testDataString = "No test data given";
		}
		
		return "Date: " + nowString + ", Test " + testResultString +
				", Test data: " + testDataString;
	}

	private String toString(Object[] array) {
		StringBuilder value = new StringBuilder();
		
		for (int i = 0; i < array.length - 1; i++) {
			value.append(array[i].toString()).append(", ");
		}
		
		value.append(array[array.length - 1].toString());
		return value.toString();
	}
}
