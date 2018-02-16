package unit;

import com.coveros.EnumInfo;
import com.coveros.GlueCode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class EnumInfoTest {

    @Test
    public void getEnumFileNullTest() throws IOException {
        Assert.assertNull(new EnumInfo().getEnumFile("IOException"));
    }

    @Test
    public void getEnumFileTest() throws IOException {
        System.setProperty("baseDirectory", "/path/to/file/");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("import java.io.IOException;");
        Assert.assertEquals(glueCode.getEnumInfo().getEnumFile("IOException"),
                new File("/path/to/file/java/io" + ".java"));
    }

    @Test
    public void getEnumFileMultipleTest() throws IOException {
        System.setProperty("baseDirectory", "/path/to/file/");
        GlueCode glueCode = new GlueCode();
        glueCode.processLine("import java.io.IOException;");
        glueCode.processLine("import java.io.IOException;");
        glueCode.processLine("import java.ia.Exception;");
        Assert.assertEquals(glueCode.getEnumInfo().getEnumFile("IOException"),
                new File("/path/to/file/java/io" + ".java"));
        Assert.assertEquals(glueCode.getEnumInfo().getEnumFile("Exception"),
                new File("/path/to/file/java/ia" + ".java"));
    }

    @Test
    public void formatEnumValuesBadValueTest() {
        Assert.assertEquals(new EnumInfo().formatEnumValues("someVeryBadValue"), "var alue = new Array(\"alue\");");
    }

    @Test
    public void formatEnumValuesSimpleEnumTest() {
        Assert.assertEquals(new EnumInfo().formatEnumValues("public enum Simple { YES, NO }"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesSimpleEnum2Test() {
        Assert.assertEquals(new EnumInfo().formatEnumValues("public enum Simple { YES, NO };"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesComplexEnumTest() {
        Assert.assertEquals(new EnumInfo().formatEnumValues("public enum Simple { YES(\"hello\"), NO(\"world\") };"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesComplexEnum2Test() {
        Assert.assertEquals(new EnumInfo().formatEnumValues("public enum Simple { YES(\"hello\"), NO(\"world\");"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }

    @Test
    public void formatEnumValuesComplexEnumNestedTest() {
        Assert.assertEquals(
                new EnumInfo().formatEnumValues("public enum Simple { YES(\"hello(there)\"), NO(\"world" + "(earth)\");"),
                "var Simple = new Array(\"YES\",\"NO\");");
    }
}