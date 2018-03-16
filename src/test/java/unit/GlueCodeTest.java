package unit;

import com.coveros.GlueCode;
import com.coveros.exception.MalformedGlueCode;
import com.coveros.exception.MalformedMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GlueCodeTest {

    @Test
    public void processLineImportTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("java.io.IOException");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("import java.io.IOException;");
        Assert.assertEquals(glueCode.getEnumInfo().getClassIncludes(), list);
    }

    @Test
    public void processLineDuplicateImportTest() throws IOException {
        String line = "import java.io.IOException;";
        List<String> list = new ArrayList<>();
        list.add("java.io.IOException");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine(line);
        glueCode.processLine(line);
        Assert.assertEquals(glueCode.getEnumInfo().getClassIncludes(), list);
    }

    @Test
    public void processLineNoImportTest() throws IOException {
        List<String> list = new ArrayList<>();
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("imprt java.io.IOException;");
        Assert.assertEquals(glueCode.getEnumInfo().getClassIncludes(), list);
    }

    @Test
    public void processLineImportStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("import java.io.IOException;");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineGivenStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@Given(\"^I have a user$\")");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineWhenStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@When(\"^I have a user$\")");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineThenStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@Then(\"^I have a user$\")");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineGivenMethodStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("testSteps.push( new step( \"I have a user\" ) );");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@Given(\"^I have a user$\")");
        glueCode.processLine("public void haveUser()");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineWhenMethodStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("testSteps.push( new step( \"I have a user\" ) );");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@When(\"^I have a user$\")");
        glueCode.processLine("public void haveUser()");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineThenMethodStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("testSteps.push( new step( \"I have a user\" ) );");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@Then(\"^I have a user$\")");
        glueCode.processLine("public void haveUser()");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test
    public void processLineMultipleMethodStepsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("testSteps.push( new step( \"I have a user\" ) );");
        list.add("testSteps.push( new step( \"I have a user\" ) );");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("@Given(\"^I have a user$\")");
        glueCode.processLine("public void haveUser()");
        glueCode.processLine("@Then(\"^I have a user$\")");
        glueCode.processLine("public void haveUser()");
        Assert.assertEquals(glueCode.getGlueCodeSteps(), list);
    }

    @Test(expectedExceptions = MalformedGlueCode.class)
    public void checkStepValidityNotCarotTest() throws IOException {
        String given = "@Given(\"I have a new registered user$\")";
        new GlueCode().getStep(given);
    }

    @Test(expectedExceptions = MalformedGlueCode.class)
    public void checkStepValidityNotDollarTest() throws IOException {
        String given = "@Given(\"^I have a new registered user\")";
        new GlueCode().getStep(given);
    }

    @Test(expectedExceptions = MalformedGlueCode.class)
    public void checkStepValidityBadCarotDollarTest() throws IOException {
        String given = "@Given(\"$I have a new registered user^\")";
        new GlueCode().getStep(given);
    }

    @Test
    public void getStepSimpleTest() throws IOException {
        String given = "@Given(\"^I have a new registered user$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "I have a new registered user");
    }

    @Test
    public void getStepAnyTest() throws IOException {
        String given = "@Given(\"^(?:I'm logged|I log) in as an admin user$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "<span class='any'>...</span> in as an admin user");
    }

    @Test
    public void getStepMatchTest() throws IOException {
        String given = "@Given(\"^I have (d+) users$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "I have XXXX users");
    }

    @Test
    public void getStepOptionalTest() throws IOException {
        String given = "@Given(\"^I have [(d+)]? users$\")";
        Assert.assertEquals(new GlueCode().getStep(given), "I have <span class='opt'>XXXX</span> users");
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void checkMethodVariablesValidityNoOpenParenTest() throws IOException {
        String method = "public void myMethod)";
        new GlueCode().getMethodVariables(method);
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void checkMethodVariablesValidityNoCloseParenTest() throws IOException {
        String method = "public void myMethod(";
        new GlueCode().getMethodVariables(method);
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void checkMethodVariablesValidityBadParenTest() throws IOException {
        String method = "public void myMethod)(";
        new GlueCode().getMethodVariables(method);
    }

    @Test
    public void getMethodVariablesNoParamsTest() throws IOException {
        String method = "public void myMethod()";
        Assert.assertEquals(new GlueCode().getMethodVariables(method), new ArrayList<>());
    }

    @Test
    public void getMethodVariablesSingleParamsTest() throws IOException {
        String method = "public void myMethod(String var1)";
        List<String> list = new ArrayList<>();
        list.add("String var1");
        Assert.assertEquals(new GlueCode().getMethodVariables(method), list);
    }

    @Test
    public void getMethodVariablesMultipleParamsTest() throws IOException {
        String method = "public void myMethod(String var1, int 123)";
        List<String> list = new ArrayList<>();
        list.add("String var1");
        list.add(" int 123");
        Assert.assertEquals(new GlueCode().getMethodVariables(method), list);
    }

    @Test
    public void getStepVariablesNoParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        Assert.assertEquals(new GlueCode().getStepVariables(list), "");
    }

    @Test
    public void getStepVariablesListStringsParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("List<String> inputs");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputsList\", \"text\" )");
    }

    @Test
    public void getStepVariablesListIntsParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("List<int> inputs");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputsList\", \"number\" )");
    }

    @Test
    public void getStepVariablesListCustomParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("List<MyEnums> inputs");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputsList\", MyEnums )");
    }

    @Test
    public void getStepVariablesLongParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Long input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"number\" )");
    }

    @Test
    public void getStepVariablesIntParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("int input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"number\" )");
    }

    @Test
    public void getStepVariablesIntegerParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Integer input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"number\" )");
    }

    @Test
    public void getStepVariablesStringParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("String input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesCharParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Char input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesDoubleParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Double input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesBooleanParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Boolean input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesObjectParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Object input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", Object )");
    }

    @Test
    public void getStepVariablesDateParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("Date input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"date\" )");
    }

    @Test
    public void getStepVariablesCustomParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("MyEnum input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", MyEnum )");
    }

    @Test
    public void getStepVariablesTransformerTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("@Transform(GherkinDateConverter.class) Date input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"date\" )");
    }

    @Test
    public void getStepVariablesDelimiterTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("@Delimiter(\";\") List<Object> input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"inputList\", Object )");
    }

    @Test
    public void getStepVariablesMultipleParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("List<String> inputs");
        list.add("Long input");
        list.add("String input");
        list.add("Object input");
        Assert.assertEquals(new GlueCode().getStepVariables(list),
                ", new keypair( \"inputsList\", \"text\" ), new keypair( \"input\", \"number\" ), new keypair( \"input\", \"text\" ), new keypair( \"input\", Object )");
    }

    @Test
    public void getStepVariablesPreSpacingParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("  String input");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepVariablesPostSpacingParamsTest() throws IOException {
        List<String> list = new ArrayList<>();
        list.add("String input  ");
        Assert.assertEquals(new GlueCode().getStepVariables(list), ", new keypair( \"input\", \"text\" )");
    }

    @Test
    public void getStepEnumerationsDefaultTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        List<String> list = new ArrayList<>();
        glueCode.getStepVariables(list);
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), list);
    }

    @Test
    public void getStepEnumerationsStringTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("String input");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListStringTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("List<String> inputs");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsObjectTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("Object input");
        listOut.add("Object");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListObjectTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("List<Object> input");
        listOut.add("Object");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListObjectMultipleTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        List<String> listIn = new ArrayList<>();
        List<String> listOut = new ArrayList<>();
        listIn.add("String input");
        listIn.add("List<String> inputs");
        listIn.add("Object input");
        listIn.add("List<Object> input");
        listOut.add("Object");
        glueCode.getStepVariables(listIn);
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), listOut);
    }

    @Test
    public void getStepEnumerationsListObjectMultiple2Test() throws IOException {
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
        Assert.assertEquals(glueCode.getEnumInfo().getGlueCodeEnumerations(), listOut);
    }

    @Test
    public void isListWrongTest() throws IOException {
        Assert.assertFalse(new GlueCode().isList("List"));
    }

    @Test
    public void isListStartTest() throws IOException {
        Assert.assertFalse(new GlueCode().isList("List<"));
    }

    @Test
    public void isListEndTest() throws IOException {
        Assert.assertFalse(new GlueCode().isList(">"));
    }

    @Test
    public void isListFullTest() throws IOException {
        Assert.assertFalse(new GlueCode().isList("List<>"));
    }

    @Test
    public void isListFullValueTest() throws IOException {
        Assert.assertTrue(new GlueCode().isList("List<S>"));
    }

    @Test
    public void isTextStringTest() throws IOException {
        Assert.assertTrue(new GlueCode().isText("String"));
    }

    @Test
    public void isTextCharTest() throws IOException {
        Assert.assertTrue(new GlueCode().isText("Char"));
    }

    @Test
    public void isTextDoubleTest() throws IOException {
        Assert.assertTrue(new GlueCode().isText("Double"));
    }

    @Test
    public void isTextBooleanTest() throws IOException {
        Assert.assertTrue(new GlueCode().isText("Boolean"));
    }

    @Test
    public void isTextLongTest() throws IOException {
        Assert.assertFalse(new GlueCode().isText("Long"));
    }

    @Test
    public void isTextIntegerTest() throws IOException {
        Assert.assertFalse(new GlueCode().isText("Integer"));
    }

    @Test
    public void isTextIntTest() throws IOException {
        Assert.assertFalse(new GlueCode().isText("Int"));
    }

    @Test
    public void isTextOtherTest() throws IOException {
        Assert.assertFalse(new GlueCode().isText("Other"));
    }

    @Test
    public void isNumberStringTest() throws IOException {
        Assert.assertFalse(new GlueCode().isNumber("String"));
    }

    @Test
    public void isNumberCharTest() throws IOException {
        Assert.assertFalse(new GlueCode().isNumber("Char"));
    }

    @Test
    public void isNumberDoubleTest() throws IOException {
        Assert.assertFalse(new GlueCode().isNumber("Double"));
    }

    @Test
    public void isNumberBooleanTest() throws IOException {
        Assert.assertFalse(new GlueCode().isNumber("Boolean"));
    }

    @Test
    public void isNumberLongTest() throws IOException {
        Assert.assertTrue(new GlueCode().isNumber("Long"));
    }

    @Test
    public void isNumberIntegerTest() throws IOException {
        Assert.assertTrue(new GlueCode().isNumber("Integer"));
    }

    @Test
    public void isNumberIntTest() throws IOException {
        Assert.assertTrue(new GlueCode().isNumber("Int"));
    }

    @Test
    public void isNumberOtherTest() throws IOException {
        Assert.assertFalse(new GlueCode().isNumber("Other"));
    }
}