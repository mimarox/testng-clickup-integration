package com.matthiasrothe.testng.clickup.internal;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.Callable;

import org.testng.ITestNGMethod;

import com.matthiasrothe.testng.clickup.ClickUp;

import net.sf.jetro.tree.JsonObject;
import retrofit2.Call;
import retrofit2.Response;

public final class ClickUpApiDelegate {
	private static final class BackOffStrategy {
		private int currentRetry = 0;
		private int maxRetries;
		
		BackOffStrategy(int maxRetries) {
			this.maxRetries = maxRetries;
		}
		
		void backOff() {
			try {
				Thread.sleep(fibunacciOf(currentRetry) * 1000);
			} catch (InterruptedException e) {
			}
		}
		
		private int fibunacciOf(int index) {
			int fibunacci = 0;
			
			if (index == 0 || index == 1) {
				fibunacci = 1;
			} else {
				fibunacci = fibunacciOf(index - 1) + fibunacciOf(index - 2);
			}
			
			return fibunacci;
		}

		boolean hasMoreRetries() {
			return currentRetry < maxRetries;
		}
		
		BackOffStrategy next() {
			currentRetry++;
			return this;
		}
	}
	
	private static final int MAX_RETRIES = 5;
	
	private ClickUpApiDelegate() {}
	
	public static JsonObject getTask(ClickUpApi api, String taskId) {
		return executeCall(() -> api.getTask(taskId));
	}

	public static String retrieveTaskId(ITestNGMethod testMethod) {
		ClickUp clickUp = testMethod.getConstructorOrMethod().
				getMethod().getAnnotation(ClickUp.class);
		
		return clickUp != null ? clickUp.taskId() : null;
	}
	
	public static <R> R executeCall(Callable<Call<R>> callable) {
		return executeCall(callable, new BackOffStrategy(MAX_RETRIES));
	}
	
	private static <R> R executeCall(Callable<Call<R>> callable, BackOffStrategy backOffStrategy) {
		try {
			Response<R> response = callable.call().execute();
			
			if (!response.isSuccessful()) {
				return null;
			}
			
			return response.body();
		} catch (ConnectException e) {
			return handleConnectException(callable, backOffStrategy);
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static <R> R handleConnectException(Callable<Call<R>> callable,
			BackOffStrategy backOffStrategy) {
		if (backOffStrategy.hasMoreRetries()) {
			backOffStrategy.backOff();
			return executeCall(callable, backOffStrategy.next());
		} else {
			return null;
		}
	}
}
