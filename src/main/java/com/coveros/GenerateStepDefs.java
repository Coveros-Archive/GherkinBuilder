package com.coveros;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenerateStepDefs {

    private static Logger log = Logger.getLogger("GherkinBuilder");
    private static final String STEPS = "public/js/steps.js";

    private GenerateStepDefs() {
    }

    public static void main(String[] args) throws Exception {
        GlueCode glueCode = new GlueCode();

        File fileDef = Outputs.checkInputs(args);
        List<String> fileDefs = Outputs.listFilesForFolder(fileDef);
        System.setProperty("baseDirectory",
                fileDef.getAbsolutePath().substring(0, fileDef.getAbsolutePath().indexOf("src/main/java/") + 14));

        // parse through our step definitions
        for (String file : fileDefs) {
            String line;
            try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr);) {
                while ((line = br.readLine()) != null) {
                    glueCode.processLine(line);
                }
            }
        }
        // write out to our steps file
        try (BufferedWriter buffer = new BufferedWriter(new FileWriter(STEPS))) {
            // write our enumerations
            buffer.write("//our enumerations\n");
            for (String enumeration : glueCode.getEnumInfo().getStepEnumerations()) {
                if (enumeration != null) {
                    buffer.write(enumeration);
                    buffer.write("\n");
                }
            }
            buffer.write("\n");
            // write our old lines
            buffer.write("//our steps\n");
            for (String step : glueCode.getGlueCodeSteps()) {
                buffer.write(step);
                buffer.write("\n");
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Some error occurred writing to '" + STEPS + "'", e);
        }
    }
}