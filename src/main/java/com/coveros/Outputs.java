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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Outputs {

    private static Logger log = Logger.getLogger("Outputs");

    private Outputs() {
    }

    /**
     * Checks the provided inputs to see if an input location was specified, and
     * if the location actually exists or not
     *
     * @param inputs - the provided program parameters
     * @return File - the File object of the provided input
     * @throws IOException
     */
    public static List<File> checkInputs(String[] inputs) throws IOException {
        // get all of our files for the listing
        if (inputs.length < 1) {
            String error = "Please provide the file location for the step definitions";
            log.log(Level.SEVERE, error);
            throw new IOException(error);
        }
        List<File> stepDirs = new ArrayList<>();
        for( String input : inputs ) {
            File stepDir = new File(input);
            if (!stepDir.exists()) {
                String error = "Step defs file does not exist: " + stepDir;
                log.log(Level.SEVERE, error);
                throw new IOException(error);
            }
            stepDirs.add(stepDir);
        }
        return stepDirs;
    }

    /**
     * a method to recursively retrieve all the files in a folder
     *
     * @param folder: the folder to check for files
     * @return ArrayList<String>: an ArrayList with the of multiple files
     * @throws IOException
     */
    public static List<String> listFilesForFolder(File folder) {
        List<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(listFilesForFolder(fileEntry));
            } else {
                files.add(fileEntry.getPath());
            }
        }
        return files;
    }
}