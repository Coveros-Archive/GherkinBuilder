package com.coveros;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnumInfo {

    private List<String> includes;
    private List<String> enumerations;

    public enum sample {YES, NO}

    public EnumInfo() {
        includes = new ArrayList<>();
        enumerations = new ArrayList<>();
    }

    /**
     * Returns the identified enumerations while parsing through the method
     * parameters
     *
     * @return List - a list of enumerations
     */
    public List<String> getGlueCodeEnumerations() {
        return enumerations;
    }

    public void addGlueCodeEnumeration(String enumeration) {
        if (!enumerations.contains(enumeration)) {
            enumerations.add(enumeration);
        }
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

    public void addClassInclude(String include) {
        if (!includes.contains(include)) {
            includes.add(include);
        }
    }

    public List<String> getStepEnumerations() throws IOException {
        List<String> enums = new ArrayList<>();
        for (String enumeration : enumerations) {
            try (BufferedReader br = new BufferedReader(new FileReader(getEnumFile(enumeration)));) {
                String line;
                StringBuilder value = new StringBuilder();
                Boolean start = false;
                Boolean end = false;
                while ((line = br.readLine()) != null) {
                    String ln = line.trim();
                    if (start) {
                        value.append(ln);
                    }
                    if (ln.startsWith("public enum " + enumeration)) {
                        start = true;
                        value.append(ln);
                    }
                    if ((start && (ln.endsWith(";") || ln.endsWith("}")))) {
                        end = true;
                    }
                    if (end) {
                        enums.add(formatEnumValues(value.toString()));
                        break;
                    }
                }
            }
        }
        return enums;
    }

    public File getEnumFile(String enumeration) {
        for (String include : getClassIncludes()) {
            if (include.endsWith("." + enumeration)) {
                include = include.substring(0, include.lastIndexOf('.'));
                include = include.replaceAll("\\.", "/");
                return new File(System.getProperty("baseDirectory") + include + ".java");
            }
        }
        return null;
    }

    public String formatEnumValues(String value) {
        String trim = value.substring(12);
        String enumName = trim.split(" ")[0];
        String enumVals = trim.substring(trim.indexOf('{') + 1);
        if (enumVals.endsWith(";") || enumVals.endsWith("}")) {
            enumVals = enumVals.substring(0, enumVals.length() - 1);
        }
        while (enumVals.contains("(")) {
            enumVals = enumVals.replaceAll("(\\([^\\)\\(]+\\))", "");
        }
        enumVals = enumVals.replace(" ", "");
        String array = "var " + enumName + " = new Array( \"";
        array += enumVals.replace(",", "\",\"");
        array += "\");";
        return array;
    }
}