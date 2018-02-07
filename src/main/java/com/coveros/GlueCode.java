package com.coveros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.coveros.exception.MalformedGlueCode;
import com.coveros.exception.MalformedMethod;

public class GlueCode {

    private Logger log = Logger.getLogger("Glue Code");

    private List<String> enumerations;

    public GlueCode() {
        enumerations = new ArrayList<>();
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
     *            - a string interpreation of a list object
     * @return Boolean - is it a properly identified list
     */
    public Boolean isList(String input) {
        return input.startsWith("List<") && input.endsWith(">") && input.length() > 6;
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
            if ("long".equalsIgnoreCase(object) || "int".equalsIgnoreCase(object)
                    || "integer".equalsIgnoreCase(object)) {
                type = "\"number\"";
            } else if ("string".equalsIgnoreCase(object) || "char".equalsIgnoreCase(object)
                    || "double".equalsIgnoreCase(object) || "boolean".equalsIgnoreCase(object)) {
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
}