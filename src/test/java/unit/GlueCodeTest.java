package unit;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.coveros.GlueCode;
import com.coveros.exception.MalformedGlueCode;
import com.coveros.exception.MalformedMethod;

public class GlueCodeTest {

    @Test(expectedExceptions = MalformedGlueCode.class)
    public void checkStepValidityNotCarotTest() throws MalformedGlueCode {
        String given = "@Given(\"I have a new registered user$\")";
        new GlueCode().getStep(given);
    }

    @Test(expectedExceptions = MalformedGlueCode.class)
    public void checkStepValidityNotDollarTest() throws MalformedGlueCode {
        String given = "@Given(\"^I have a new registered user\")";
        new GlueCode().getStep(given);
    }

    @Test(expectedExceptions = MalformedGlueCode.class)
    public void checkStepValidityBadCarotDollarTest() throws MalformedGlueCode {
        String given = "@Given(\"$I have a new registered user^\")";
        new GlueCode().getStep(given);
    }

    @Test
    public void getStepSimpleTest() throws MalformedGlueCode {
        String given = "@Given(\"^I have a new registered user$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "I have a new registered user");
    }

    @Test
    public void getStepAnyTest() throws MalformedGlueCode {
        String given = "@Given(\"^(?:I'm logged|I log) in as an admin user$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "<span class='any'>...</span> in as an admin user");
    }

    @Test
    public void getStepMatchTest() throws MalformedGlueCode {
        String given = "@Given(\"^I have (d+) users$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "I have XXXX users");
    }

    @Test
    public void getStepOptionalTest() throws MalformedGlueCode {
        String given = "@Given(\"^I have [(d+)]? users$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "I have <span class='opt'>XXXX</span> users");
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void checkMethodVariablesValidityNoOpenParenTest() throws MalformedMethod {
        String method = "public void myMethod)";
        new GlueCode().getMethodVariables(method);
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void checkMethodVariablesValidityNoCloseParenTest() throws MalformedMethod {
        String method = "public void myMethod(";
        new GlueCode().getMethodVariables(method);
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void checkMethodVariablesValidityBadParenTest() throws MalformedMethod {
        String method = "public void myMethod)(";
        new GlueCode().getMethodVariables(method);
    }

    @Test
    public void getMethodVariablesNoParamsTest() throws MalformedMethod {
        String method = "public void myMethod()";
        Assert.assertEquals(new GlueCode().getMethodVariables(method), new ArrayList<>());
    }

    @Test
    public void getMethodVariablesSingleParamsTest() throws MalformedMethod {
        String method = "public void myMethod(String var1)";
        List<String> list = new ArrayList<>();
        list.add("String var1");
        Assert.assertEquals(new GlueCode().getMethodVariables(method), list);
    }

    @Test
    public void getMethodVariablesMultipleParamsTest() throws MalformedMethod {
        String method = "public void myMethod(String var1, int 123)";
        List<String> list = new ArrayList<>();
        list.add("String var1");
        list.add(" int 123");
        Assert.assertEquals(new GlueCode().getMethodVariables(method), list);
    }

    @Test
    public void getStepVariablesNoParamsTest() {
        List<String> list = new ArrayList<>();
        Assert.assertEquals(new GlueCode().getStepVariables(list), "");
    }

    @Test
    public void getStepVariablesListStringsParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("List<String> inputs");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputsList\", \"text\" )");
    }

    @Test
    public void getStepVariablesListIntsParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("List<int> inputs");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputsList\", \"number\" )");
    }

    @Test
    public void getStepVariablesListCustomParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("List<MyEnums> inputs");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputsList\", MyEnums )");
    }

    @Test
    public void getStepVariablesLongParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("Long input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"number\" )");
    }

    @Test
    public void getStepVariablesIntParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("int input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"number\" )");
    }

    @Test
    public void getStepVariablesIntegerParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("Integer input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"number\" )");
    }

    @Test
    public void getStepVariablesStringParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("String input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesCharParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("Char input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesDoubleParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("Double input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesBooleanParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("Boolean input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesObjectParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("Object input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", Object )");
    }

    @Test
    public void getStepVariablesCustomParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("MyEnum input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", MyEnum )");
    }

    @Test
    public void getStepVariablesMultipleParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("List<String> inputs");
        list.add("Long input");
        list.add("String input");
        list.add("Object input");
        Assert.assertEquals(new GlueCode().getStepVariables(list),
                ", new keypair( \"inputsList\", \"text\" ), new keypair( \"input\", \"number\" ), new keypair( \"input\", \"text\" ), new keypair( \"input\", Object )");
    }

    @Test
    public void getStepVariablesPreSpacingParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("  String input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesPostSpacingParamsTest() {
        List<String> list = new ArrayList<>();
        list.add("String input  ");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepEnumerationsDefaultTest() {
        GlueCode glueCode = new GlueCode();
        List<String> list = new ArrayList<>();
        glueCode.getStepVariables(list);
        Assert.assertEquals(glueCode.getStepEnumerations(), list);
    }

    @Test
    public void getStepEnumerationsStringTest() {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("String input");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getStepEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListStringTest() {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("List<String> inputs");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getStepEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsObjectTest() {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("Object input");
        listOut.add("Object");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getStepEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListObjectTest() {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("List<Object> input");
        listOut.add("Object");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getStepEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListObjectMultipleTest() {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("String input");
        listIn.add("List<String> inputs");
        listIn.add("Object input");
        listIn.add("List<Object> input");
        listOut.add("Object");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getStepEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListObjectMultiple2Test() {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("Object input");
        listIn.add("List<Object> input");
        listIn.add("MyEnum input");
        listIn.add("List<MyEnum> input");
        listOut.add("Object");
        listOut.add("MyEnum");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getStepEnumerations(), listOut);
    }

    @Test
    public void isListWrongTest() {
        Assert.assertFalse(new GlueCode().isList("List"));
    }

    @Test
    public void isListStartTest() {
        Assert.assertFalse(new GlueCode().isList("List<"));
    }

    @Test
    public void isListEndTest() {
        Assert.assertFalse(new GlueCode().isList(">"));
    }

    @Test
    public void isListFullTest() {
        Assert.assertFalse(new GlueCode().isList("List<>"));
    }

    @Test
    public void isListFullValueTest() {
        Assert.assertTrue(new GlueCode().isList("List<S>"));
    }

    @Test
    public void isTextStringTest() {
        Assert.assertTrue(new GlueCode().isText("String"));
    }

    @Test
    public void isTextCharTest() {
        Assert.assertTrue(new GlueCode().isText("Char"));
    }

    @Test
    public void isTextDoubleTest() {
        Assert.assertTrue(new GlueCode().isText("Double"));
    }

    @Test
    public void isTextBooleanTest() {
        Assert.assertTrue(new GlueCode().isText("Boolean"));
    }

    @Test
    public void isTextLongTest() {
        Assert.assertFalse(new GlueCode().isText("Long"));
    }

    @Test
    public void isTextIntegerTest() {
        Assert.assertFalse(new GlueCode().isText("Integer"));
    }

    @Test
    public void isTextIntTest() {
        Assert.assertFalse(new GlueCode().isText("Int"));
    }

    @Test
    public void isTextOtherTest() {
        Assert.assertFalse(new GlueCode().isText("Other"));
    }

    @Test
    public void isNumberStringTest() {
        Assert.assertFalse(new GlueCode().isNumber("String"));
    }

    @Test
    public void isNumberCharTest() {
        Assert.assertFalse(new GlueCode().isNumber("Char"));
    }

    @Test
    public void isNumberDoubleTest() {
        Assert.assertFalse(new GlueCode().isNumber("Double"));
    }

    @Test
    public void isNumberBooleanTest() {
        Assert.assertFalse(new GlueCode().isNumber("Boolean"));
    }

    @Test
    public void isNumberLongTest() {
        Assert.assertTrue(new GlueCode().isNumber("Long"));
    }

    @Test
    public void isNumberIntegerTest() {
        Assert.assertTrue(new GlueCode().isNumber("Integer"));
    }

    @Test
    public void isNumberIntTest() {
        Assert.assertTrue(new GlueCode().isNumber("Int"));
    }

    @Test
    public void isNumberOtherTest() {
        Assert.assertFalse(new GlueCode().isNumber("Other"));
    }
}