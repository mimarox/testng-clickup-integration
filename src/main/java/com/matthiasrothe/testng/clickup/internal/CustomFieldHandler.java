package com.matthiasrothe.testng.clickup.internal;

import net.sf.jetro.tree.JsonObject;
import net.sf.jetro.tree.JsonType;

public interface CustomFieldHandler {
	String getCustomFieldName();
	String getCustomFieldType();
	JsonType calculateNewCustomFieldValue(TaskResult taskResult, JsonObject task);
}
