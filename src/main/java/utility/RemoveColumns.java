package utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class RemoveColumns
{

	public static void main(String args[])
	{
		String use = "java RemoveColumns <source_file> <out_file> <columns_IDs>";
		if (args.length < 3) {
			System.out.println(use);
		}
		try {
			System.out
					.println("Removing columns from corpora file: " + args[0]);
			String inFile = args[0];
			String outFile = args[1];
			List <Integer> columsIDs = new ArrayList<Integer>();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(inFile));
			} catch (FileNotFoundException e) {
				System.out.println(e);
			}
			for (int i = 0; i < args.length - 2; i++) {
				columsIDs.add(Integer.parseInt(args[i + 2]));
			}
			String result = Utility.removeColumns(reader, columsIDs);
			Utility.printFile(outFile, result);
			System.out
					.println("OUT: Corpora file without columns removed saved in: "
							+ outFile);
		} catch (Exception e) {
			System.out.println(use + "\n");
			System.out.print(e);
		}
	}
}
