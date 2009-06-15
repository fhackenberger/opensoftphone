package org.acoveo.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TemplateFileGenerator {
	protected HashMap<Pattern, String> replacements;
	protected String separatorString;
	
	public TemplateFileGenerator() {
		this.separatorString = "%%";
		replacements = new HashMap<Pattern, String>();
	}
	
	public TemplateFileGenerator(String separatorString) {
		this.separatorString = separatorString;
		replacements = new HashMap<Pattern, String>();
	}
	
	public void addReplacement(String searchString, String replacementString) throws PatternSyntaxException {
		replacements.put(Pattern.compile(separatorString + searchString + separatorString), replacementString);
	}
	
	public void addReplacementsFromFile(File replacementFile) throws IOException, PatternSyntaxException {
		BufferedReader configFileReader = new BufferedReader(new FileReader(replacementFile));
		Pattern replacementPattern = Pattern.compile("^(\\S+)\\s+(.+)$");
		String replacementFileLine;
		while ((replacementFileLine = configFileReader.readLine()) != null) {
			Matcher matcher = replacementPattern.matcher(replacementFileLine);
			if(matcher.matches()) {
				String searchString = matcher.group(1);
				String replacementString = matcher.group(2);
				if(searchString != null && replacementString != null) {
					addReplacement(searchString, replacementString);
				}
			}
		}
	}
	
	public void generateConfigFiles(File templateFile, File destinationFile, boolean overwriteDestination) throws IOException {
		if(!templateFile.isFile()) {
			throw new IOException(templateFile.getAbsolutePath() + " is not a file.");
		}
		if(!overwriteDestination && destinationFile.exists()) {
			throw new IOException(destinationFile.getAbsolutePath() + " exists.");
		}
		File destinationDirectory = new File(destinationFile.getParent());
		if(!destinationDirectory.exists()) {
			destinationDirectory.mkdirs();
		}
		//TODO This is a very naive (slow) implementation.
		// Improve it by searching for the first occurrence of the separator string before
		// applying the patterns.
		BufferedWriter configFileWriter = new BufferedWriter(new FileWriter(destinationFile));
		BufferedReader configFileReader = new BufferedReader(new FileReader(templateFile));
		String configFileLine;
		while ((configFileLine = configFileReader.readLine()) != null) {
			StringBuilder configFileLineBuilder = new StringBuilder(configFileLine);
			for (Entry<Pattern, String> replacement : replacements.entrySet()) {
				configFileLineBuilder.replace(0, Integer.MAX_VALUE, replacement.getKey().matcher(
						configFileLineBuilder.toString()).replaceAll(replacement.getValue()));
			}
			configFileWriter.write(configFileLineBuilder.toString());
			configFileWriter.newLine();
		}
		configFileWriter.close();
	}
}
