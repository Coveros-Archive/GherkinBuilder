/*
 * Copyright 2018 Coveros, Inc.
 *
 * This file is part of Gherkin Builder.
 *
 * Gherkin Builder is licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.coveros;

import java.io.*;
import java.util.ArrayList;
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

        List<File> stepDirs = Outputs.checkInputs(args);
        List<String> fileDefs = new ArrayList<>();
        for (File stepDir : stepDirs ) {
            fileDefs.addAll( Outputs.listFilesForFolder(stepDir) );
            glueCode.addBaseDirectory(stepDir.getAbsolutePath().substring(0, stepDir.getAbsolutePath().indexOf
                    ("src/main/java/") + 14));
        }

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