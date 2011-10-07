package utility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class KeepClass
{

	/**
	 * arg[0] source file
	 * 
	 * arg[1] destination file
	 * 
	 * arg[2] coluumn ID (startin from 0)
	 * 
	 * arg[3+] names of the classes to keep
	 * 
	 * keep the NP tags from the second column supposed this format FORMAT:W POS
	 * PARSING NE!
	 */
	public static void main(String arg[])
	{
		String use = "USE:   java KeepClass <input_file> <output_file> <column_ID> <class_names>";
		try {
			if (arg.length < 4) {
				System.out.println(use);
			} else {
				BufferedReader in = new BufferedReader(new FileReader(arg[0]));
				PrintWriter out = new PrintWriter(new FileOutputStream(arg[1]));
				int columnID = Integer.parseInt(arg[2]);
				Vector<String> classNames = new Vector<String>();
				for (int i = 3; i < arg.length; i++) {
					classNames.add(arg[i]);
				}
				boolean lastWasEmpty = true; // used to avoid multiple empty
				// lines

				String line = in.readLine();
				String[] arr;

				while (line != null) {
					line = line.trim();
					if (line.length() > 0) {
						arr = line.split("\\s+");
						boolean foundClass = false;
						for (String className : classNames) {
							if (arr[columnID].endsWith(className)) {
								foundClass = true;
								break;
							}
						}
						if (foundClass) {
							out.println(line);
						} else {
							for (int i = 0; i < arr.length; i++) {
								if (i > 0) {
									out.print(" ");
								}
								if (i == columnID) {
									out.print("O");
								} else {
									out.print(arr[i]);
								}
							}
							out.println();
						}
						lastWasEmpty = false;
					} else {
						if (!lastWasEmpty) {// remove consecutive empty lines
							out.println();
							lastWasEmpty = true;
						}
					}
					line = in.readLine();
				}

				in.close();
				out.close();
				System.out.println("OUT: Corpora file without classes removed saved in: "+arg[1]);
			}
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (IOException e) {
			System.out.println("IO exception");
		}
	}
}
