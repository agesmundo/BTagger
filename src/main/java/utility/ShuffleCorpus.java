package utility;

import java.io.FileNotFoundException;

import corpus.Sentences;

public final class ShuffleCorpus {
	public static void main(String arg[]) {
		String usage = "USAGE: java ShuffleCorpus <in_file> <out_file> <seed>";
		if (arg.length != 3)
			System.out.println(usage);
		else {
			String inFileName = arg[0];
			String outFileName = arg[1];
			long seed = Long.parseLong(arg[2]);
			try {
				Sentences sentences = new Sentences(inFileName);
				sentences.shuffle(seed);
				sentences.saveFile(outFileName);
			} catch (FileNotFoundException e) {
				System.out.println(e);
			}
		}
		
	}
}
