package com.matthiasrothe.testng.clickup;

import java.util.List;
import java.util.Optional;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

import com.matthiasrothe.testng.clickup.internal.ClickUpReporterDelegate;
import com.matthiasrothe.testng.clickup.internal.CustomFieldHandler;
import com.matthiasrothe.testng.clickup.internal.TaskResult;

import net.sf.jetro.path.JsonPath;
import net.sf.jetro.tree.JsonNumber;
import net.sf.jetro.tree.JsonObject;
import net.sf.jetro.tree.JsonType;

public class ClickUpRatingReporter implements IReporter {
	private static final JsonPath MAX_RATING_PATH = JsonPath.compile("$.type_config.count");
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		ClickUpReporterDelegate delegate = new ClickUpReporterDelegate();
		delegate.generateReport(xmlSuites, suites, new CustomFieldHandler() {
			
			@Override
			public String getCustomFieldType() {
				return "emoji";
			}
			
			@Override
			public String getCustomFieldName() {
				return "Code Quality";
			}
			
			@Override
			public JsonNumber calculateNewCustomFieldValue(TaskResult taskResult, JsonObject task) {
				int maxRating = retrieveMaxRating(task);
				int rating = taskResult.getRating(maxRating);
				return new JsonNumber(rating);
			}

			private int retrieveMaxRating(JsonObject task) {
				JsonObject customField = delegate.getCustomField(task, getCustomFieldName(),
						getCustomFieldType());
				
				if (customField == null) {
					return 0;
				}
				
				customField = customField.deepCopy();
				customField.recalculateTreePaths();
				
				Optional<JsonType> optionalMaxRating = customField.getElementAt(MAX_RATING_PATH);
				
				if (optionalMaxRating.isPresent()) {
					return ((JsonNumber) optionalMaxRating.get()).getValue().intValue();
				}
				
				return 0;
			}
		});
	}
}
