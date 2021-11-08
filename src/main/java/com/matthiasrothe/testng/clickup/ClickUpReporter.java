package com.matthiasrothe.testng.clickup;

import java.io.IOException;
import java.util.Optional;

import org.testng.ITestListener;
import org.testng.ITestResult;

import com.matthiasrothe.retrofit.jetro.JetroConverterFactory;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.JsonArray;
import net.sf.jetro.tree.JsonObject;
import net.sf.jetro.tree.JsonString;
import net.sf.jetro.tree.JsonType;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ClickUpReporter implements ITestListener {
	private final ClickUpApi api;
	
	public ClickUpReporter() {
		OkHttpClient client = new OkHttpClient.Builder().
				addInterceptor(this::authenticationInterceptor).build();
		
		Retrofit retrofit = new Retrofit.Builder().
				baseUrl("https://api.clickup.com/api/v2/").
				addConverterFactory(JetroConverterFactory.create()).
				client(client).
				build();
		
		api = retrofit.create(ClickUpApi.class);
	}
	
	private okhttp3.Response authenticationInterceptor(Interceptor.Chain chain) throws IOException {
		Request request = chain.request();
		
		Request newRequest = request.newBuilder().
				addHeader("Authorization", "pk_36404531_BWL3ZZPWP1CNITOB0EBQ4OCEQM8JI7Z8").
				build();
		
		return chain.proceed(newRequest);
	}
	
	public void onTestSuccess(ITestResult result) {
		setTestSucceededFlag(result, true);
	}

	public void onTestFailure(ITestResult result) {
		setTestSucceededFlag(result, true);
	}
	
	private void setTestSucceededFlag(ITestResult result, boolean testSucceeded) {
		String taskId = retrieveTaskId(result);
		
		if (taskId == null || "".equals(taskId)) {
			return;
		}
		
		String fieldId = retrieveFieldId(taskId);
		
		if (fieldId == null || "".equals(fieldId)) {
			return;
		}
		
		setTestSucceededFlag(taskId, fieldId, testSucceeded);
	}

	private String retrieveTaskId(ITestResult result) {
		ClickUp clickUp = result.getMethod().getConstructorOrMethod().
				getMethod().getAnnotation(ClickUp.class);
		
		return clickUp != null ? clickUp.taskId() : null;
	}
	
	private String retrieveFieldId(String taskId) {
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
		
		JsonObject task = response.body();
		
		JsonPath customFieldsPath = JsonPath.compile("$.custom_fields");
		
		Optional<JsonType> customFieldsOptional = task.getElementAt(customFieldsPath);
		
		if (customFieldsOptional.isPresent()) {
			JsonObject testSucceededCustomField = retrieveTestSucceededCustomField(
					(JsonArray) customFieldsOptional.get());
			
			if (testSucceededCustomField == null) {
				return null;
			}
			
			return ((JsonString) testSucceededCustomField.get("id")).getValue();
		} else {
			return null;
		}
	}

	private JsonObject retrieveTestSucceededCustomField(JsonArray customFields) {
		for (int i = 0; i < customFields.size(); i++) {
			JsonObject customField = (JsonObject) customFields.get(i);
			
			if ("Test Succeeded".equals(((JsonString) customField.get("name")).getValue())
					&& "checkbox".equals(((JsonString) customField.get("type")).getValue())) {
				return customField;
			}
		}
		
		return null;
	}
	
	private void setTestSucceededFlag(String taskId, String fieldId, boolean testSucceeded) {
		JsonObject body = new JsonObject();
		body.asMap().put("value", testSucceeded);
		
		Call<JsonObject> setFlagCall = api.setCustomFieldValue(taskId, fieldId, body);
		
		try {
			setFlagCall.execute();
		} catch (IOException e) {
		}
	}
}
