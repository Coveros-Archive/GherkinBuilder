package com.coveros;

import org.testng.log4testng.Logger;

public class GlueCode {

	private static Logger log = Logger.getLogger(GlueCode.class);

	private GlueCode() {
	}

	/**
	 * Extracts the regular expression from the cucumber given, when or then
	 * annotation
	 * 
	 * @param glueCode
	 *            - cucumber given, when or then annotation. Example:
	 *            \@Given("^I have a new registered user$") \@When("^I
	 *            (.*)login$") \@Then("^I see the login error message
	 *            \"([^\"]*)\"$")
	 * @return String - a formatted string to be consumer by the gherkin builder
	 * @throws MalformedGlueCode
	 */
	public static String getStep(String glueCode) throws MalformedGlueCode {
		// check for valid formatted glue code
		int start = glueCode.indexOf('^');
		int end = glueCode.indexOf('$');
		if (start < 0 || end < 0 || start > end) {
			String error = "There is a problem with your glue code. It is expected to"
					+ " start with '^' and end with '$'. " + "Examine the expression '" + glueCode + "'";
			log.error(error);
			throw new MalformedGlueCode(error);
		}
		// get just the regex from the annotation
		String regex = glueCode.substring(start + 1, end);
		// denote a non-capturing match
		regex = regex.replaceAll("\\(\\?:.*?\\)", "<span class='any'>...</span>");
		// capture any generic matches
		regex = regex.replaceAll("\\(.*?\\)", "XXXX");
		// capture any optional matches
		regex = regex.replaceAll("\\[(.*?)\\]\\?", "<span class='opt'>$1</span>");
		return regex;
	}
}