package com.matthiasrothe.testng.clickup;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark a test for reporting with ClickUp.
 * 
 * @author Matthias Rothe
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface ClickUp {
	
	/**
	 * Specify the task id of the task this test covers.
	 * 
	 * @return the specified task id
	 */
	String taskId();
}
