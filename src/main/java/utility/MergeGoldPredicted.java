package utility;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Usage java MergeGoldPredicted <ID column of the predicted tag> <gold file>
 * <predicted file> [<IDs of columns to add at the output>]
 * 
 * Output [word] [added columns] [gold tag] [predicted tag]
 * 
 * 
 * NB! column numbers start with zero, first column has ID 0, second has ID 1
 * etc...
 * 
 * @author Gesmundo Andrea
 */
public class MergeGoldPredicted
{

	public static void main(String arg[])
	{

		int predictedColumn;
		String goldFile, predictedFile;
		Vector<Integer> addColums = new Vector<Integer>();

		// check parameters
		if (arg.length < 3) {
			System.out
					.println(" Usage: \n java MergeGoldPredicted <column number of the predicted tag> <gold file> <predicted file> [<column numbers to add at the output>]");
			return;
		}
		// read parameters
		predictedColumn = Integer.parseInt(arg[0]);
		goldFile = arg[1];
		predictedFile = arg[2];
		for (int i = 3; i < arg.length; i++) {
			addColums.add(Integer.parseInt(arg[i]));
		}
		final String outFileName="MergedOutput.txt";
		// merge files
		try {
			BufferedReader inGold = new BufferedReader(new FileReader(goldFile));
			BufferedReader inPred = new BufferedReader(new FileReader(
					predictedFile));
			PrintWriter out = new PrintWriter(
					new FileOutputStream(outFileName));
			String lineGold = inGold.readLine();
			String linePredict = inPred.readLine();
			String[] splitGold, splitPredict;
			boolean prevLineIsEmpty = true;
			out: while (lineGold != null) {

				// skip empty lines
				while (lineGold.trim().compareTo("") == 0) {
					if (!prevLineIsEmpty)
						out.println("");
					prevLineIsEmpty = true;
					lineGold = inGold.readLine();
					if (lineGold == null)
						break out;
				}
				while (linePredict.trim().compareTo("") == 0)
					linePredict = inPred.readLine();
				prevLineIsEmpty = false;

				// split lines
				splitGold = lineGold.trim().split(" ");
				splitPredict = linePredict.trim().split(" ");

				// check line correspondence
				if (splitGold[0].compareTo(splitPredict[0]) != 0) {
					System.out.println("ERROR: " + lineGold + " - "
							+ linePredict);
				}

				// write output
				out.print(splitGold[0] + " ");
				for (int i = 0; i < addColums.size(); i++) {
					out.print(splitGold[addColums.get(i)] + " ");
				}
				out.println(splitGold[predictedColumn] + " "
						+ splitPredict[predictedColumn]);

				// red next line
				lineGold = inGold.readLine();
				linePredict = inPred.readLine();
			}

			inGold.close();
			inPred.close();
			out.close();
			System.out.println("OUT:  Merged file saved in :"+outFileName);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
