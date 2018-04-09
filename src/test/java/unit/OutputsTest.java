package unit;

import com.coveros.Outputs;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutputsTest {

    @Test(expectedExceptions = IOException.class)
    public void checkInputsNoneTest() throws IOException {
        Outputs.checkInputs(new String[0]);
    }

    @Test(expectedExceptions = IOException.class)
    public void checkInputsBadLocationTest() throws IOException {
        Outputs.checkInputs(new String[]{"badLocation"});
    }

    @Test
    public void checkInputsGoodLocationTest() throws IOException {
        List files = new ArrayList<>();
        files.add( new File("src/test/java") );
        Assert.assertEquals(Outputs.checkInputs(new String[]{"src/test/java"}).size(), 1);
        Assert.assertEquals(Outputs.checkInputs(new String[]{"src/test/java"}), files);
    }

    @Test
    public void checkInputsGoodLocationsTest() throws IOException {
        List files = new ArrayList<>();
        files.add( new File("src/test/java") );
        files.add( new File("src/main/java") );
        Assert.assertEquals(Outputs.checkInputs(new String[]{"src/test/java","src/main/java"}).size(), 2);
        Assert.assertEquals(Outputs.checkInputs(new String[]{"src/test/java","src/main/java"}), files);
    }

    @Test
    public void listFilesForFolder() {
        Assert.assertEquals(Outputs.listFilesForFolder(new File("src/test/java")).size(), 3);
    }
}