package com.coveros;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GenerateStepDefs {

	private static final String INITIALSTEPLOCATION = "public/js/step.js";

	private GenerateStepDefs() {
	}

	private static File checkInputs(String[] inputs) throws IOException {
		// get all of our files for the listing
		if (inputs.length != 1) {
			throw new IOException("Please provide the file location for the step definitions");
		}
		File fileDef = new File(inputs[0]);
		if (!fileDef.exists()) {
			throw new IOException("Step defs file does not exist: " + fileDef);
		}
		return fileDef;
	}

	/**
	 * a method to recursively retrieve all the files in a folder
	 *
	 * @param folder:
	 *            the folder to check for files
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

	public static void main(String[] args) throws Exception {
		File fileDef = checkInputs(args);
		String baseDir = fileDef.getAbsolutePath().substring(0, fileDef.getAbsolutePath().indexOf("\\src\\") + 5);

		List<String> fileDefs = listFilesForFolder(fileDef);
		PrintWriter writer = new PrintWriter(INITIALSTEPLOCATION, "UTF-8");

		List<String> includes = new ArrayList<>();
		List<String> enumerations = new ArrayList<>();

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
						line = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
						if (line.length() > 0) {
							String[] objects = line.split(",");
							for (String object : objects) {
								object = object.trim();
								String[] pieces = object.split(" ");
								String type = "";
								if (pieces[0].startsWith("List<") && pieces[0].endsWith(">")) {
									pieces[0] = pieces[0].substring(5, pieces[0].length() - 1);
									pieces[1] += "List";
								}
								if ("long".equalsIgnoreCase(pieces[0]) || "int".equalsIgnoreCase(pieces[0])) {
									type = "\"number\"";
								} else if ("string".equalsIgnoreCase(pieces[0]) || "char".equalsIgnoreCase(pieces[0])
										|| "Integer".equalsIgnoreCase(pieces[0])
										|| "Double".equalsIgnoreCase(pieces[0])) {
									type = "\"text\"";
								} else {
									type = pieces[0];
									enumerations.add(type);
								}
								step.append(", new keypair( \"" + pieces[1] + "\", " + type + " )");
							}
						}
						step.append(" ) );");
						next = false;
						writer.println(step);
						step.setLength(0);
					}
					line = line.trim();
					if (line.startsWith("@Given") || line.startsWith("@When")) {
						step.append("testSteps.whens.push( new step( \"" + GlueCode.getStep(line) + "\"");
						next = true;
					}
					if (line.startsWith("@Then")) {
						step.append("testSteps.thens.push( new step( \"" + GlueCode.getStep(line) + "\"");
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
		for (String enumeration : enumerations) {
			for (String include : includes) {
				if (include.endsWith("." + enumeration)) {
					include = include.substring(0, include.lastIndexOf("."));
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
