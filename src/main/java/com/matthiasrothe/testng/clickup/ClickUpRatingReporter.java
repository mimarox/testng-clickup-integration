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

/**
 * This reporter sets a rating on a rating bar on each task that has at
 * least been referenced once as the taskId on a test method annotated with
 * &#64;{@link ClickUp}. It sets the rating according to the percentage of
 * successful test invocations with the same taskId scaled to the number of
 * steps configured on the rating bar.
 * <p>
 * So if, for example, the rating bar has 5 steps configured and 60% to 79% of
 * tests with a given taskId were executed successfully the rating will be 3.
 * <p>
 * The rating bar MUST be a custom field on the ClickUp task named &quot;Code
 * Quality&quot; of type &quot;emoji&quot;.
 * <p>
 * This reporter expects a parameter named click_up_api_key to be configured
 * at the suite level with the value set to the API key provided by the ClickUp
 * app.
 * 
 * @author Matthias Rothe
 */
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
