package com.matthiasrothe.testng.clickup;

import java.io.IOException;

import org.testng.ITestNGMethod;

import net.sf.jetro.tree.JsonObject;
import retrofit2.Call;
import retrofit2.Response;

public final class ClickUpApiDelegate {

	private ClickUpApiDelegate() {}
	
	public static JsonObject getTask(ClickUpApi api, String taskId) {
		Call<JsonObject> taskCall = api.getTask(taskId);
		Response<JsonObject> response;
		
		try {
			response = taskCall.execute();
		} catch (IOException e) {
			return null;
		}
		
		if (!response.isSuccessful()) {
			return null;
		}
		
		return response.body();
	}

	public static String retrieveTaskId(ITestNGMethod testMethod) {
		ClickUp clickUp = testMethod.getConstructorOrMethod().
				getMethod().getAnnotation(ClickUp.class);
		
		return clickUp != null ? clickUp.taskId() : null;
	}
}
