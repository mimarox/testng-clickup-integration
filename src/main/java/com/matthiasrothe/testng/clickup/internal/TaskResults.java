package com.matthiasrothe.testng.clickup.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TaskResults implements Iterable<TaskResult> {
	private static class CountableArrayList<E> extends ArrayList<E> {
		private static final long serialVersionUID = 7099410650246220761L;
	
		public int count(E element) {
			int count = 0;
			
			if (contains(element)) {
				for (E e : this) {
					if (Objects.deepEquals(element, e)) {
						count++;
					}
				}
			}
			
			return count;
		}
	}
	
	private final Map<String, CountableArrayList<Boolean>> taskResults = new HashMap<>();
	
	public void addTaskResult(String taskId, boolean succeeded) {
		if (!(taskId == null || "".equals(taskId))) {
			if (taskResults.containsKey(taskId)) {
				taskResults.get(taskId).add(succeeded);
			} else {
				CountableArrayList<Boolean> values = new CountableArrayList<>();
				values.add(succeeded);
				taskResults.put(taskId, values);
			}
		}
	}

	@Override
	public Iterator<TaskResult> iterator() {
		Iterator<Entry<String, CountableArrayList<Boolean>>> inner =
				taskResults.entrySet().iterator();
		
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
				
				Entry<String, CountableArrayList<Boolean>> next = inner.next();
				
				String taskId = next.getKey();
				CountableArrayList<Boolean> results = next.getValue();
				
				boolean succeeded;
				
				if (results.contains(false)) {
					succeeded = false;
				} else {
					succeeded = true;
				}
				
				int succeededCount = results.count(true);
				int failedCount = results.count(false);
				
				return new TaskResult(taskId, succeeded, succeededCount, failedCount);
			}
		};
	}
}
