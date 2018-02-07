package com.coveros;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GenerateStepDefs {

    private static final String INITIALSTEPLOCATION = "public/js/step.js";

    private GenerateStepDefs() {
    }

    public static void main(String[] args) throws Exception {
        GlueCode glueCode = new GlueCode();

        File fileDef = Outputs.checkInputs(args);
        String baseDir = fileDef.getAbsolutePath().substring(0, fileDef.getAbsolutePath().indexOf("\\src\\") + 5);

        List<String> fileDefs = Outputs.listFilesForFolder(fileDef);
        PrintWriter writer = new PrintWriter(INITIALSTEPLOCATION, "UTF-8");

        List<String> includes = new ArrayList<>();

        for (String file : fileDefs) {
            String line = "";
            boolean next = false;
            StringBuilder step = new StringBuilder();
            try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr);) {
                while ((line = br.readLine()) != null) {
                    // grab any imports that might be useful
                    if (line.startsWith("import ")) {
                        includes.add(line.substring(7, line.length() - 1));
                    }
                    // if our previous line was just a Given, When or Then, next
                    // will be set, to indicate this line contains parameters
                    if (next) {
                        step.append(glueCode.getStepVariables(glueCode.getMethodVariables(line)));
                        step.append(" ) );");
                        next = false;
                        writer.println(step);
                        step.setLength(0);
                    }
                    line = line.trim();
                    if (line.startsWith("@Given") || line.startsWith("@When")) {
                        step.append("testSteps.whens.push( new step( \"" + glueCode.getStep(line) + "\"");
                        next = true;
                    }
                    if (line.startsWith("@Then")) {
                        step.append("testSteps.thens.push( new step( \"" + glueCode.getStep(line) + "\"");
                        next = true;
                    }
                }
            }
        }
        // close our file
        writer.close();

        // to prepend, we must copy, then unlink our old file
        writer = new PrintWriter("public/js/steps.js", "UTF-8");
        // write our enumerations
        writer.println("//our enumerations");
        for (String enumeration : glueCode.getStepEnumerations()) {
            for (String include : includes) {
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
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(INITIALSTEPLOCATION));) {
            while ((line = br.readLine()) != null) {
                writer.println(line);
            }
        }
        // close our file
        writer.close();
        new File(INITIALSTEPLOCATION).delete();
    }
}
