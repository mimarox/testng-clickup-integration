package com.matthiasrothe.testng.clickup.internal;

public class TaskResult {
	private final String taskId;
	private final boolean succeeded;
	private int succeededCount;
	private int failedCount;
	
	public TaskResult(String taskId, boolean succeeded, int succeededCount, int failedCount) {
		this.taskId = taskId;
		this.succeeded = succeeded;
		this.succeededCount = succeededCount;
		this.failedCount = failedCount;
	}

	public String getTaskId() {
		return taskId;
	}
	
	public boolean getSucceeded() {
		return succeeded;
	}
	
	public int getRating(int maxRating) {
		double successPercentage = (double) succeededCount / (succeededCount + failedCount);
		int rating = (int) Math.floor(successPercentage * maxRating);
		return rating;
	}
}
