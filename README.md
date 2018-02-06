# Gherkin Builder
The Gherkin Builder provides a simple structuring and auto-completion for writing 
Gherkin tests around PMI program

 * Create a new Feature, or add tests to an existing Feature in JIRA
 * Suggests test steps based on already implemented test steps
 * Write a Scenario and easily turn it into a Scenario Outline

## Structure
Gherkin Builder code base consists of two parts
 * Glue Code Parser
   * Maven project
   * Scans provided folder for regular expressions in Java glue code
   * Builds javascript file containing possible test steps to be consumed
   * Support for multiple input types
 * Web App
   * PHP Project
   * Front end builder, using jquery to build Feature files
   * APIs for interacting with JIRA APIs and ZAPI

## Installation
Use the Maven project to generate the required javascript containing test steps, extracted from
the Gherkin glue code. After building the project with Maven (`mvn clean install`), execute the jar,
and provide it the location of the Java glue code to be examined. A `steps.js` file will be automatically
created in the `public/js/` folder. For example:
```
java -jar target/gherkin.builder.jar ../automation/src/main/java/com/coveros/steps/
```
It is suggested to set this up as part of your CI process, so that each time new glue code is committed,
the test steps are re-generated. A sample test steps file might look like:
`steps.js`
```
testSteps.whens.push( new step( "I close the form" ) );
testSteps.whens.push( new step( "I resume filling out the form" ) );
testSteps.whens.push( new step( "I select prefer not to answer" ) );
testSteps.whens.push( new step( "I click through the form using \"XXXX\"", new keypair( "options", "text" ) ) );
testSteps.whens.push( new step( "I navigate to the next page" ) );
testSteps.thens.push( new step( "I see the \"XXXX\" of type \"XXXX\"", new keypair( "placeholder", "text" ), new keypair( "fieldType", "text" ) ) );
testSteps.thens.push( new step( "the continue button is disabled" ) );
testSteps.thens.push( new step( "I can replay the video" ) );
```

Ensure the `public` folder is hosted on a php server. Then, simply accessing the base URL will give access
to the Gherkin Builder

### JIRA Integration
To enable JIRA integration, simply fill out the two properties files in the base public directory.
Setting a JIRA `base` link will enable JIRA integration. In order for the integration to properly work,
fill out all of the fields under `[jira]` in `props.ini`. Additionally, if any custom fields need to be set
for a feature or scenario, add those under the `[feature]` and `[scenario]` sections. For example
```
[jira]
base = "https://my.jira.domain.org/jira"
project = "HW"
epic_name_field = "customfield_10004"
epic_link_field = "customfield_10001"

[feature]
customfield_12301[] = "QA"
customfield_12201[] = "Android"
customfield_12201[] = "iOS"
customfield_12201[] = "Web"

[scenario]
customfield_12301[] = "QA"
```

Additionally, populate `props.js` with the same information
```
jiraOptions = {
    project : "HW",
    base : "https://my.jira.domain.org/jira"
}
```

## Usage
### Features
 * Provide Tags for each Feature indicating functionality of the Test Suite
   * Tags should start with an ‘@’ and have no spaces in them
   * By convention, tags should be all lowercase, and have dashes (-) separating multiple words
 * Enter a descriptive title for the Testing Suite into the Feature title
 * Provide a user story in the form of “As a <role>, I want <feature> so that <reason>.”
### Background Steps
 * Background Steps are optional, only needed if there are common Givens or Whens for every Scenario in the Feature
 * Even when Background Steps are used, a title and description are optional
 * For each Given and When step desired
   * Click the ‘Add Background Step’ button
   * Select whether you want a ‘Given’ or ‘When’ Statement
   * Select your test step from the input
     * This input will attempt to autocomplete, based on steps that currently exist
     * Note that any ‘blanks’ cannot be turned into Scenario Outlines, and must be filled out
### Scenarios
 * Provide Tags for each Scenario indicating functionality of the Test Suite
   * Tags should start with an ‘@’ and have no spaces in them
   * By convention, tags should be all lowercase, and have dashes (-) separating multiple words
 * If the testing suite involves functionality being created from a JIRA issue, use the link icon to enter in the JIRA issue
 * Enter a descriptive title for the Test Case into the Scenario title
 * If desired, enter a description into the Scenario Description
 * For each Given, When, and Then step desired
   * Click the ‘Add Test Step’ button
   * Select whether you want a ‘Given’ or ‘When’ Statement
   * Select your test step from the input
     * This input will attempt to autocomplete, based on steps that currently exist
     * Note that any ‘blanks’ cannot be turned into Scenario Outlines, and must be filled out
     * If a new test step is desired, simply type it in
       * It will appear in italic red text
     * If selecting a test step with ‘XXXX’ in the text, an input will appear in its place
       * By default that input will turn the Scenario into a Scenario Outline
       * If there is a specific value to be executed, write it in, otherwise this becomes an input parameter
   * If that test steps needs to be edited after its initial selection, use the edit icon
### Scenario Outlines
 * When input parameters are left in test steps, the Scenario automatically turns into a Scenario Outline
   * An Example table will automatically appear
   * This can be filled in to parameterize the test run
 * If desired, provide Tags for each Example indicating functionality of the Test Suite
   * It is not typical to provide Tags for Examples
   * Tags should start with an ‘@’ and have no spaces in them
     * By convention, tags should be all lowercase, and have dashes (-) separating multiple words
 * Fill out the Example table with desired inputs, using the ‘Add Data Row’ button to add additional data sets

## JIRA Integration
### Features
 * If an existing Feature is in JIRA, use the edit icon to enter in the JIRA issue
 * If the testing suite involves functionality being created from a JIRA issue, use the link icon to enter in the JIRA issue

## Additional Integration
 * Providing a list of tags allows autocompletion of existing tags
 * Simply add tags.js to public/js/
`tags.js`
```
tags = [];
tags.push( "tag1" );
tags.push( "tag2" );
tags.push( "tag3" );
tags.push( "tag4" );
```