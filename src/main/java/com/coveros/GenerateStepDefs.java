package com.coveros;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;

public class GenerateStepDefs {

    private GenerateStepDefs() {
    }

    public static void main(String[] args) throws Exception {
        GlueCode glueCode = new GlueCode();

        File fileDef = Outputs.checkInputs(args);
        String baseDir = fileDef.getAbsolutePath().substring(0, fileDef.getAbsolutePath().indexOf("\\src\\") + 5);

        List<String> fileDefs = Outputs.listFilesForFolder(fileDef);

        for (String file : fileDefs) {
            String line;
            try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr);) {
                while ((line = br.readLine()) != null) {
                    glueCode.processLine(line);
                }
            }
        }
        // close our file

        // to prepend, we must copy, then unlink our old file
        PrintWriter writer = new PrintWriter("public/js/steps.js", "UTF-8");
        // write our enumerations
        writer.println("//our enumerations");
        for (String enumeration : glueCode.getStepEnumerations()) {
            for (String include : glueCode.getClassIncludes()) {
                if (include.endsWith("." + enumeration)) {
                    include = include.substring(0, include.lastIndexOf('.'));
                    include = include.replaceAll("\\.", "\\\\");
                    String line = "";
                    try (BufferedReader br = new BufferedReader(new FileReader(baseDir + include + ".java"));) {
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("public enum " + enumeration)) {
                                String array = "var " + line.substring(12);
                                array = array.replace("{ ", " = new Array( \"");
                                array = array.replace(" }", "\" )");
                                array = array.replace(", ", "\", \"");
                                writer.println(array);
                            }
                        }
                    }
                }
            }
        }
        writer.println("");
        // write our old lines
        writer.println("//our steps");
        for (String step : glueCode.getGlueCodeSteps()) {
            writer.println(step);
        }
        // close our file
        writer.close();
    }
}
