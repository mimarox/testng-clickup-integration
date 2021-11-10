package com.matthiasrothe.testng.clickup;

import java.io.IOException;
import java.util.function.Supplier;

import com.matthiasrothe.retrofit.jetro.JetroConverterFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public final class ClickUpApiProvider {
	static final String API_KEY_PARAMETER_NAME = "click_up_api_key";
	
	private static ClickUpApi api;
	private static Supplier<String> apiKeySupplier;
	
	private ClickUpApiProvider() {}
	
	public static synchronized ClickUpApi provideApi(Supplier<String> apiKeySupplier) {
		if (apiKeySupplier != null) {
			ClickUpApiProvider.apiKeySupplier = apiKeySupplier;
		} else {
			ClickUpApiProvider.apiKeySupplier = () -> "";
		}
		
		if (api == null) {
			buildApi();
		}
		
		return api;
	}
	
	private static void buildApi() {
		OkHttpClient client = new OkHttpClient.Builder().
				addInterceptor(ClickUpApiProvider::authenticationInterceptor).build();
		
		Retrofit retrofit = new Retrofit.Builder().
				baseUrl("https://api.clickup.com/api/v2/").
				addConverterFactory(JetroConverterFactory.create()).
				client(client).
				build();
		
		api = retrofit.create(ClickUpApi.class);
	}
	
	private static Response authenticationInterceptor(Interceptor.Chain chain) throws IOException {
		Request request = chain.request();
		Request newRequest;
		
		String apiKey = apiKeySupplier.get();
		
		if (!(apiKey == null || "".equals(apiKey))) {
			 newRequest = request.newBuilder().
						addHeader("Authorization", apiKey).
						build();
		} else {
			newRequest = request;
		}
		
		return chain.proceed(newRequest);
	}
}
