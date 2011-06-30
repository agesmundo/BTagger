package utility;

import java.io.FileNotFoundException;

import corpus.Sentences;

public class PlainText
{

	public static void main(String args[])
	{
		String use = "java PlainText <source_file> <out_file>";
		if (args.length < 2) {
			System.out.println(use);
			return;
		}

		System.out
		.println("Reading text from corpora file: " + args[0]);
		String inFile = args[0];
		String outFile = args[1];
		Sentences sentences=new Sentences();
		try{
			sentences = new Sentences(inFile);
		}
		catch (FileNotFoundException e){
			System.out.println("input file: "+inFile+" doesn't exist");
		}
		sentences.saveFilePlainText(outFile);
		System.out
		.println("OUT: Plain text saved in: "
				+ outFile);
	}
}
