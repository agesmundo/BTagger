package utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import bTagger.LabelsUtility;

import corpus.Sentences;

public class ConvertChunkRepresentation
{

	/**
	 * Convert chunk representation
	 * 
	 * arg[0] source file
	 * 
	 * arg[1] column ID in the source file (starting from 0)
	 * 
	 * arg[2] destination file
	 * 
	 * arg[3] destination chunk representation ID
	 * (IOB1:1,IOB2:2,IOE1:3,IOE2:4,O-C:5)
	 */
	public static void main(String arg[])
	{
		String use = "USE:   java Convert <source_file> <column> <destination_file> <destination_chunk_format>";
		if (arg.length != 4)
			System.out.println(use);
		else {
			try {
				convertChunkRepresentation(arg[0], Integer.parseInt(arg[1]),
						arg[2], Integer.parseInt(arg[3]));
			} catch (NumberFormatException e) {
				System.out.println(use);
			}
		}
	}

	public static void convertChunkRepresentation(String sourceFile,
			int column, String destinationFile, int destinationRepID)
	{
		String tmpFileName = "TMP";

		try {
			Sentences sentences = new Sentences(sourceFile);
			int sourceRepID = sentences.getChunkFormat(column);
			if (sourceRepID==0){
				throw new RuntimeException ("Unable to detect chunk representation");
			}
			// TODO don't work on files but work on Sentences Objs.

			BufferedReader in = new BufferedReader(new FileReader(sourceFile));// read
			// in
			// file
			PrintWriter out = new PrintWriter(new FileOutputStream(
					destinationFile));// write
			// out
			// file
			PrintWriter outTmp = new PrintWriter(new FileOutputStream(
					tmpFileName));// write
			// tmp
			// file
			BufferedReader inTmp;// read tmp file

			boolean lastWasEmpty = true; // used to avoid multiple empty
											// lines
			boolean exitCondition = false; // condition to exit the lines
			// reader loops
			String[] nextLine = "".split("\\s+"), currentLine = ""
					.split("\\s+"), previousLine = "".split("\\s+"); // strore
			// lines

			// convert from source format to IOB2
			String line = in.readLine();
			while (!exitCondition) {
				if (line == null) {
					line = "";
					exitCondition = true;
				}
				line = line.trim();
				previousLine = currentLine;
				currentLine = nextLine;
				nextLine = line.split("\\s+");

				if(!isEmptyLine(nextLine) && nextLine.length<=column){
					throw new RuntimeException("missing column at line:\n\t"+line);
				}
				
				if (currentLine == null) {
					line = in.readLine();
					continue;
				}
				// if currentLine is not an empty line
				if (!isEmptyLine(currentLine)) {

					for (int k = 0; k < currentLine.length; k++) {
						if (k > 0) {
							outTmp.print(" ");
						}
						if (k == column) {

							if (sourceRepID == 1) {
								if (checkPrefix("O", currentLine[column])) {
									outTmp.print("O");
								} else if (checkPrefix("B", currentLine[column])) {
									outTmp.print(currentLine[column]);
								} else if (checkPrefix("I", currentLine[column])) {
									if (isEmptyLine(previousLine)
											|| !sameClass(previousLine[column],
													currentLine[column])) {
										outTmp.print(changePrefix("B",
												currentLine[column]));
									} else {
										outTmp.print(currentLine[column]);
									}
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

							} else if (sourceRepID == 2) {
								outTmp.print(currentLine[k]);

							} else if (sourceRepID == 3 || sourceRepID == 4) {

								if (checkPrefix("O", currentLine[column])) {
									outTmp.print("O");
								} else if (checkPrefix("E", currentLine[column])
										|| checkPrefix("I", currentLine[column])) {
									if (!isEmptyLine(previousLine)
											&& sameClass(previousLine[column],
													currentLine[column])
											&& checkPrefix("I",
													previousLine[column])) {
										outTmp.print(changePrefix("I",
												currentLine[column]));
									} else {
										outTmp.print(changePrefix("B",
												currentLine[column]));
									}
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

								// }else if(sourceFormat==4){

							} else if (sourceRepID == 5) {

								if (checkPrefix("O", currentLine[column])) {
									outTmp.print("O");
								} else if (checkPrefix("B", currentLine[column])) {
									outTmp.print(currentLine[column]);
								} else if (checkPrefix("I", currentLine[column])) {
									outTmp.print(currentLine[column]);
								} else if (checkPrefix("S", currentLine[column])) {
									outTmp.print(changePrefix("B",
											currentLine[column]));
								} else if (checkPrefix("E", currentLine[column])) {
									outTmp.print(changePrefix("I",
											currentLine[column]));
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

							} else {
								System.out.println("Wrong source format ID!!!");
							}
						} else {
							outTmp.print(currentLine[k]);
						}
					}
					if (!exitCondition)
						outTmp.println();
					lastWasEmpty = false;
				} else {
					if (!lastWasEmpty) {
						outTmp.println("");
						lastWasEmpty = true;
					}
				}
				line = in.readLine();
			}
			outTmp.close();

			// convert from IOB2 to destination format
			inTmp = new BufferedReader(new FileReader(tmpFileName));
			nextLine = "".split("\\s+");
			currentLine = "".split("\\s+");
			previousLine = "".split("\\s+");
			line = inTmp.readLine();
			lastWasEmpty = true;
			exitCondition = false;
			while (!exitCondition) {
				if (line == null) {
					line = "";
					exitCondition = true;
				}
				line = line.trim();
				previousLine = currentLine;
				currentLine = nextLine;
				nextLine = line.split("\\s+");
				if (currentLine == null) {
					line = inTmp.readLine();
					continue;
				}
				if (!isEmptyLine(currentLine)) {// if currentLine is not an
					// empty line
					for (int k = 0; k < currentLine.length; k++) {
						if (k > 0) {
							out.print(" ");
						}
						if (k == column) {

							if (destinationRepID == 1) {
								if (checkPrefix("O", currentLine[column])) {
									out.print("O");
								} else if (checkPrefix("I", currentLine[column])) {
									out.print(currentLine[column]);
								} else if (checkPrefix("B", currentLine[column])) {
									if (!isEmptyLine(previousLine)
											&& sameClass(previousLine[column],
													currentLine[column])) {
										out.print(currentLine[column]);
									} else {
										out.print(changePrefix("I",
												currentLine[column]));
									}
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

							} else if (destinationRepID == 2) {
								out.print(currentLine[k]);

							} else if (destinationRepID == 3) {

								if (checkPrefix("O", currentLine[column])) {
									out.print("O");
								} else if (checkPrefix("I", currentLine[column])
										|| checkPrefix("B", currentLine[column])) {
									if (isEmptyLine(nextLine)
											|| checkPrefix("O",
													nextLine[column])) {
										out.print(changePrefix("I",
												currentLine[column]));
									} else if (checkPrefix("I",
											nextLine[column])
											&& sameClass(nextLine[column],
													currentLine[column])) {
										out.print(changePrefix("I",
												currentLine[column]));
									} else if (checkPrefix("B",
											nextLine[column])
											&& !sameClass(nextLine[column],
													currentLine[column])) {
										out.print(changePrefix("I",
												currentLine[column]));
									} else {
										out.print(changePrefix("E",
												currentLine[column]));
									}
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

							} else if (destinationRepID == 4) {

								if (checkPrefix("O", currentLine[column])) {
									out.print("O");
								} else if (checkPrefix("I", currentLine[column])
										|| checkPrefix("B", currentLine[column])) {
									if (!isEmptyLine(nextLine)
											&& checkPrefix("I",
													nextLine[column])
											&& sameClass(nextLine[column],
													currentLine[column])) {
										out.print(changePrefix("I",
												currentLine[column]));
									} else {
										out.print(changePrefix("E",
												currentLine[column]));
									}
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

							} else if (destinationRepID == 5) {

								if (checkPrefix("O", currentLine[column])) {
									out.print("O");
								} else if (checkPrefix("I", currentLine[column])) {
									if (!isEmptyLine(nextLine)
											&& checkPrefix("I",
													nextLine[column])) {
										out.print(changePrefix("I",
												currentLine[column]));
									} else {
										out.print(changePrefix("E",
												currentLine[column]));
									}
								} else if (checkPrefix("B", currentLine[column])) {
									if (!isEmptyLine(nextLine)
											&& checkPrefix("I",
													nextLine[column])) {
										out.print(changePrefix("B",
												currentLine[column]));
									} else {
										out.print(changePrefix("S",
												currentLine[column]));
									}
								} else
									System.err.println("Unknow prefix:"
											+ currentLine[column]);

							} else {
								System.out
										.println("Wrong destination format ID!!!");
							}
						} else {
							out.print(currentLine[k]);
						}
					}
					if (!exitCondition)
						out.println();
					lastWasEmpty = false;
				} else {
					if (!lastWasEmpty) {
						out.println("");
						lastWasEmpty = true;
					}
				}
				line = inTmp.readLine();
			}

			// close buffers
			in.close();
			out.close();
			inTmp.close();
			outTmp.close();
			System.out.println("\nOUT: Chunk conversion saved in: "
					+ destinationFile);

			Utility.removeFile(tmpFileName);

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("IO exception");
		}
	}

	public static boolean sameClass(String tagA, String tagB)
	{
		tagA = tagA.trim();
		tagB = tagB.trim();
		if (tagA.length() == 1 && tagB.length() == 1)
			return true;
		if (tagA.length() == 1 || tagB.length() == 1)
			return false;
		return LabelsUtility.getClassName(tagA).compareTo(LabelsUtility.getClassName(tagB)) == 0;
	}

	public static boolean checkPrefix(String prefix, String tag)
	{
		return prefix.compareTo(tag.split(LabelsUtility.divider)[0]) == 0;
	}

	public static String changePrefix(String newPrefix, String tag)
	{
		return newPrefix + LabelsUtility.divider + LabelsUtility.getClassName(tag);
	}

// USE MetricsUtility.getClassName
//	public static String getClass(String tag)
//	{
//		return tag.split(MetricsUtility.divider)[1];
//	}

	public static boolean isEmptyLine(String line[])
	{
		return !(line.length != 1 || line[0].compareTo("") != 0);
	}
}
