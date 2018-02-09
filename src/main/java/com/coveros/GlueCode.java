package com.coveros;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coveros.exception.MalformedGlueCode;
import com.coveros.exception.MalformedMethod;

public class GlueCode {

    private Logger log = Logger.getLogger("Glue Code");

    private List<String> includes;
    private List<String> enumerations;
    private Boolean next = false;

    private List<String> steps;
    StringBuilder step;

    public GlueCode() {
        includes = new ArrayList<>();
        enumerations = new ArrayList<>();

        steps = new ArrayList<>();
        step = new StringBuilder();
    }

    /**
     * Runs through the provided line, and determines how to parse it
     * 
     * @param line
     *            - a provided line from a Cucumber Glue Code path
     * @return String - a step to be consumed by the gherkin builder class as js
     */
    public void processLine(String line) throws IOException {
        // grab any imports that might be useful
        if (line.startsWith("import ")) {
            String imprt = line.substring(7, line.length() - 1);
            if (!includes.contains(imprt)) {
                includes.add(imprt);
            }
        }
        // if our previous line was just a Given, When or Then, next
        // will be set, to indicate this line contains parameters
        if (next) {
            step.append(getStepVariables(getMethodVariables(line)));
            step.append(" ) );");
            next = false;
            steps.add(step.toString());
            step.setLength(0);
        }
        String ln = line.trim();
        if (ln.startsWith("@Given") || ln.startsWith("@When")) {
            step.append("testSteps.whens.push( new step( \"" + getStep(ln) + "\"");
            next = true;
        }
        if (ln.startsWith("@Then")) {
            step.append("testSteps.thens.push( new step( \"" + getStep(ln) + "\"");
            next = true;
        }
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
    public String getStep(String glueCode) throws MalformedGlueCode {
        // check for valid formatted glue code
        int start = glueCode.indexOf('^');
        int end = glueCode.indexOf('$');
        if (start < 0 || end < 0 || start > end) {
            String error = "There is a problem with your glue code. It is expected to"
                    + " start with '^' and end with '$'. Examine the expression '" + glueCode + "'";
            log.log(Level.SEVERE, error);
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

    /**
     * Retrieves the list of parameters from a given method declaration
     * 
     * @param method
     *            - the string representation of a method
     * @return List - a list of paramters from the method, as strings. It will
     *         list object, then the object name
     * @throws MalformedMethod
     */
    public List<String> getMethodVariables(String method) throws MalformedMethod {
        // check for valid formatted java method
        int start = method.indexOf('(');
        int end = method.indexOf(')');
        if (start < 0 || end < 0 || start > end) {
            String error = "There is a problem with your method declaration. It does not contain"
                    + " a proper parameter definition. Examine the declaration '" + method + "'";
            log.log(Level.SEVERE, error);
            throw new MalformedMethod(error);
        }
        // define where our parameters are captured
        String params = method.substring(start + 1, end);
        // if any parameters exist
        if (params.length() > 0) {
            // grab an array of them
            return Arrays.asList(params.split(","));
        }
        // if no parameters, ok to return empty
        return new ArrayList<>();
    }

    /**
     * Determines if the provided string is a list or not, with a specific
     * object typed
     * 
     * @param input
     *            - a string interpretation of an object
     * @return Boolean - is it a properly identified list
     */
    public Boolean isList(String input) {
        return input.startsWith("List<") && input.endsWith(">") && input.length() > 6;
    }

    /**
     * Determines if the provided string is a text element or not. A text
     * element is considered a string, character, double or boolean
     * 
     * @param input
     *            - a string interpretation of an object
     * @return Boolean - is it a text element
     */
    public Boolean isText(String input) {
        return "string".equalsIgnoreCase(input) || "char".equalsIgnoreCase(input) || "double".equalsIgnoreCase(input)
                || "boolean".equalsIgnoreCase(input);
    }

    /**
     * Determines if the provided string is a number element or not. A number
     * element is considered a long, or int
     * 
     * @param input
     *            - a string interpretation of an object
     * @return Boolean - is it a number element
     */
    public Boolean isNumber(String input) {
        return "long".equalsIgnoreCase(input) || "int".equalsIgnoreCase(input) || "integer".equalsIgnoreCase(input);
    }

    /**
     * Takes a list of paramters and converts it into step variables
     * 
     * @param parameters
     *            - a list of paramters, as strings. It should list object, then
     *            the object name
     * @return String - a keypair definition to be added to the step definition
     */
    public String getStepVariables(List<String> parameters) {
        StringBuilder params = new StringBuilder();
        for (String parameter : parameters) {
            // remove any surrounding whitespace
            parameter = parameter.trim();
            String type;
            String object = parameter.split(" ")[0];
            String name = parameter.split(" ")[1];

            // are we dealing with a list of elements
            if (isList(object)) {
                object = object.substring(5, object.length() - 1);
                name += "List";
            }
            // are we dealing with a whole number
            if (isNumber(object)) {
                type = "\"number\"";
            } else if (isText(object)) {
                type = "\"text\"";
            } else {
                type = object;
                if (!enumerations.contains(type)) {
                    enumerations.add(type);
                }
            }
            params.append(", new keypair( \"" + name + "\", " + type + " )");
        }
        return params.toString();
    }

    /**
     * Returns the identified enumerations while parsing through the method
     * parameters
     * 
     * @return List - a list of enumerations
     */
    public List<String> getStepEnumerations() {
        return enumerations;
    }

    /**
     * Returns the identified includes while parsing through the method
     * parameters
     * 
     * @return List - a list of includes
     */
    public List<String> getClassIncludes() {
        return includes;
    }

    /**
     * Returns the identified steps while parsing through the method parameters
     * 
     * @return List - a list of steps
     */
    public List<String> getGlueCodeSteps() {
        return steps;
    }
}