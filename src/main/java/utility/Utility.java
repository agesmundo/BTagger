package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

import bTagger.Context;
import bTagger.FileUtils;

import corpus.Sentences;

public class Utility
{

	/**
	 * Remove columns from a corpora file in the CoNLL format
	 * 
	 * @param reader
	 *            Reader where to read the content of the corpora.
	 * @param columnIDs
	 *            Array of columns to remove.
	 * @return The content of the file without the columns.
	 */
	public static String removeColumns(Reader reader, List<Integer> columnIDs)
	{
		Sentences sentences = new Sentences(reader);
		return sentences.toStringDelColumns(columnIDs);
	}

	/**
	 * Return the column ID of the main tags from the feature script If the
	 * column ID is not specified in the script the default ID is 1 (second
	 * column).
	 * 
	 * @param fileName
	 *            Name of the feature script.
	 * @return The column ID.
	 */
	public static int getColumnFromScript(String fileName)
	{

		try {
			BufferedReader in;
			String line;

			// read input files format
			in = new BufferedReader(new FileReader(fileName));
			line = in.readLine();
			while (line != null && !line.startsWith("FORMAT:"))
				line = in.readLine();
			// read the format if specified
			if (line.startsWith("FORMAT:")) {
				String tmp[] = line.split(":");
				tmp = tmp[1].split(",");
				for (int i = 0; i < tmp.length; i++) {
					// retrieve the name of the main feature
					if (tmp[i].endsWith("!")) {
						return i;
					}
				}
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.toString());
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		// otherwise use the default
		return 1;
	}

	/**
	 * Return the value of a parameter contained in the script file.
	 * 
	 * @param fileName
	 *            Name of the script file.
	 * @param pramName
	 *            Name of the parameter.
	 * @return Value of the parameter, if the parameter is not specified in the
	 *         script return an empty string.
	 */
	public static String getParamFromScript(String fileName, String pramName)
	{

		try {
			BufferedReader in;
			String line;

			// read input files format
			in = new BufferedReader(new FileReader(fileName));
			line = in.readLine();
			while (line != null && !line.startsWith("PARAMETER:"))
				line = in.readLine();
			// read the format if specified
			while (line != null) {
				line.trim();
				if (line.startsWith("PARAMETER:")) {
					String tmp[] = line.split(":");
					tmp = tmp[1].split("=");
					tmp[0] = tmp[0].trim();
					tmp[1] = tmp[1].trim();
					if (tmp[0].compareTo(pramName) == 0) {
						return tmp[1];
					}
				}
				line = in.readLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.toString());
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return "";
	}
	
	public static int getMaxRound(String featureScriptFile){
		int rounds;
		String maxRound = Utility.getParamFromScript(featureScriptFile,
		"MAXROUND");

		if (maxRound.compareTo("") == 0) {
			rounds = new Context(FileUtils.getReader(featureScriptFile)).MAXROUND;
		} else {
			rounds = Integer.parseInt(maxRound);
		}
		return rounds;
	}

	/**
	 * Remove a specified file.
	 * 
	 * @param fileName
	 *            name of the file to remove.
	 */
	public static boolean removeFile(String fileName)
	{
		File file = new File(fileName);
		return file.delete();
	}

	/**
	 * Write String to a file.
	 * 
	 * @param fileName
	 *            Name of the output file.
	 * @param toPrint
	 *            String to print.
	 */
	public static void printFile(String fileName, String toPrint)
			throws FileNotFoundException
	{
		PrintWriter out = new PrintWriter(new FileOutputStream(fileName));// write
		out.write(toPrint);
		out.close();
	}
}
