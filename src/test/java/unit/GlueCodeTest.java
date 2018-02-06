package unit;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.coveros.GlueCode;
import com.coveros.MalformedGlueCode;

public class GlueCodeTest {

	@Test (expectedExceptions = MalformedGlueCode.class)
	public void checkStepValidityNotCarotTest() throws MalformedGlueCode {
		String given = "@Given(\"I have a new registered user$\")";
		GlueCode.getStep(given);
	}
	
	@Test (expectedExceptions = MalformedGlueCode.class)
	public void checkStepValidityNotDollarTest() throws MalformedGlueCode {
		String given = "@Given(\"^I have a new registered user\")";
		GlueCode.getStep(given);
	}
	
	@Test (expectedExceptions = MalformedGlueCode.class)
	public void checkStepValidityBadCarotDollarTest() throws MalformedGlueCode {
		String given = "@Given(\"$I have a new registered user^\")";
		GlueCode.getStep(given);
	}
	
	@Test
	public void checkStepValiditySimpleTest() throws MalformedGlueCode {
		String given = "@Given(\"^I have a new registered user$\")";
		Assert.assertEquals( GlueCode.getStep(given), "I have a new registered user");
	}
	
	@Test
	public void checkStepValidityAnyTest() throws MalformedGlueCode {
		String given = "@Given(\"^(?:I'm logged|I log) in as an admin user$\")";
		Assert.assertEquals( GlueCode.getStep(given), "<span class='any'>...</span> in as an admin user");
	}
	
	@Test
	public void checkStepValidityMatchTest() throws MalformedGlueCode {
		String given = "@Given(\"^I have (d+) users$\")";
		Assert.assertEquals( GlueCode.getStep(given), "I have XXXX users");
	}
	
	@Test
	public void checkStepValidityOptionalTest() throws MalformedGlueCode {
		String given = "@Given(\"^I have [(d+)]? users$\")";
		Assert.assertEquals( GlueCode.getStep(given), "I have <span class='opt'>XXXX</span> users");
	}
}