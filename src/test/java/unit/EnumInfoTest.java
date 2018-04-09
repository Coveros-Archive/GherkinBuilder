package unit;

import com.coveros.EnumInfo;
import com.coveros.GlueCode;
import com.coveros.exception.MalformedMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnumInfoTest {

    public enum Sample {HELLO, WORLD}

    public enum ComplexSample {
        HELLO("123"), WORLD("456");

        ComplexSample(String count) {
        }
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void getEnumFileNullTest() throws IOException {
        new EnumInfo(null).getEnumFile("IOException");
    }

    @Test(expectedExceptions = MalformedMethod.class)
    public void getEnumFileTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        glueCode.addBaseDirectory("/path/to/file/");
        glueCode.processLine("import java.io.IOException;");
        glueCode.getEnumInfo().getEnumFile("IOException");
    }

    @Test
    public void buildEnumTest() throws IOException {
        String enumValue;
        try (BufferedReader br = new BufferedReader(new FileReader("./src/test/java/unit/EnumInfoTest.java"));) {
            enumValue = new EnumInfo(null).buildEnum(br, "Sample");
        }
        Assert.assertEquals(enumValue, "var Sample = new Array(\"HELLO\",\"WORLD\");");
    }

    @Test
    public void buildEnumBadTest() throws IOException {
        String enumValue;
        try (BufferedReader br = new BufferedReader(new FileReader("./src/test/java/unit/EnumInfoTest.java"));) {
            enumValue = new EnumInfo(null).buildEnum(br, "Sample1");
        }
        Assert.assertNull(enumValue);
    }

    @Test
    public void getStepEnumerationsEmptyTest() throws IOException {
        GlueCode glueCode = new GlueCode();
        glueCode.addBaseDirectory("./src/test/java/");
        glueCode.processLine("import unit.Sample;");
        Assert.assertEquals(glueCode.getEnumInfo().getStepEnumerations(), new ArrayList<>());
    }

    @Test
    public void getStepEnumerationsMismatchTest() throws IOException {
        String given = "@Given(\"^I have [\\w+] [(\\d+)]$\")";
        String method = "public void myMethod(String var1, int 123)";
        GlueCode glueCode = new GlueCode();
        glueCode.addBaseDirectory("./src/test/java/");
        glueCode.processLine("import unit.Sample;");
        glueCode.processLine(given);
        glueCode.processLine(method);
        Assert.assertEquals(glueCode.getEnumInfo().getStepEnumerations(), new ArrayList<>());
    }

    @Test
    public void getStepEnumerationsMatchTest() throws IOException {
        String given = "@Given(\"^I have [\\w+] [(\\d+)]$\")";
        String method = "public void myMethod(Sample sample)";
        GlueCode glueCode = new GlueCode();
        glueCode.addBaseDirectory("./src/test/java/");
        glueCode.processLine(given);
        glueCode.processLine(method);
        glueCode.processLine("import unit.EnumInfoTest.Sample;");
        List<String> list = new ArrayList<>();
        list.add("var Sample = new Array(\"HELLO\",\"WORLD\");");
        Assert.assertEquals(glueCode.getEnumInfo().getStepEnumerations(), list);
    }

    @Test
    public void getStepEnumerationsComplexMatchTest() throws IOException {
        String given = "@Given(\"^I have [\\w+] [(\\d+)]$\")";
        String method = "public void myMethod(ComplexSample sample)";
        GlueCode glueCode = new GlueCode();
        glueCode.addBaseDirectory("./src/test/java/");
        glueCode.processLine(given);
        glueCode.processLine(method);
        glueCode.processLine("import unit.EnumInfoTest.ComplexSample;");
        List<String> list = new ArrayList<>();
        list.add("var ComplexSample = new Array(\"HELLO\",\"WORLD\");");
        Assert.assertEquals(glueCode.getEnumInfo().getStepEnumerations(), list);
    }

    @Test
    public void formatEnumValuesBadValueTest() {
        Assert.assertEquals(new EnumInfo(null).formatEnumValues("someVeryBadValue"), "var alue = new Array(\"alue\");");
    }

    @Test
    public void formatEnumValuesSimpleEnumTest() {
        Assert.assertEquals(new EnumInfo(null).formatEnumValues("public enum Simple { YES, NO }"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesSimpleEnum2Test() {
        Assert.assertEquals(new EnumInfo(null).formatEnumValues("public enum Simple { YES, NO };"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesComplexEnumTest() {
        Assert.assertEquals(new EnumInfo(null).formatEnumValues("public enum Simple { YES(\"hello\"), NO(\"world\") };"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesComplexEnum2Test() {
        Assert.assertEquals(new EnumInfo(null).formatEnumValues("public enum Simple { YES(\"hello\"), NO(\"world\");"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesComplexEnumNestedTest() {
        Assert.assertEquals(new EnumInfo(null)
                        .formatEnumValues("public enum Simple { YES(\"hello(there)\"), NO(\"world" + "(earth)\");"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }
}