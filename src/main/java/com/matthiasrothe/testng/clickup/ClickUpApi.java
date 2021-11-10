package com.matthiasrothe.testng.clickup;

import net.sf.jetro.tree.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Use this interface with Retrofit to make the REST API calls to ClickUp.
 * The base URL is expected to be "https://api.clickup.com/api/v2/".
 * 
 * @author Matthias Rothe
 */
public interface ClickUpApi {

	@GET("task/{taskId}")
	Call<JsonObject> getTask(@Path("taskId") String taskId);
	
	@POST("task/{taskId}/field/{fieldId}")
	Call<JsonObject> setCustomFieldValue(@Path("taskId") String taskId,
			@Path("fieldId") String fieldId, @Body JsonObject body);
	
	@POST("task/{taskId}/comment")
	Call<JsonObject> createTaskComment(@Path("taskId") String taskId, @Body JsonObject body);
}
