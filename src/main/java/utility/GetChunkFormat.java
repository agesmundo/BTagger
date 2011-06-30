package utility;

import java.io.FileNotFoundException;

import corpus.Sentences;

public class GetChunkFormat {
	/**
	 * Get the chunk format ID (IOB1:1,IOB2:2,IOE1:3,IOE2:4,O+C:5) Return 0 if
	 * the format is unknown.
	 * 
	 * arg[0] source file.
	 * 
	 * arg[1] column ID in the source file (starting from 0).
	 * 
	 */
	public static void main(String arg[]) {
		String use = "USE:   java GetChunkFormat <source_file> <column_ID>";
		if (arg.length != 2)
			System.out.println(use);
		else {
			try {
				getChunkFormat(arg[0], Integer.parseInt(arg[1]));
			} catch (NumberFormatException e) {
				System.out.println(use);
			}
		}
	}

	public static void getChunkFormat(String fileName, int columnID) {
		try {
			Sentences sentences = new Sentences(fileName);
			System.out.println(sentences.getChunkFormat(columnID));
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
	}

}
