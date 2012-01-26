package utility;

import java.io.FileNotFoundException;
import java.util.List;

import corpus.Sentences;

public final class SplitCorpusWithRatio {
	public static void main(String arg[]) {
		String usage = "USAGE: java SplitCorpusWithRatio <in_file> <ratio> <out_file1> <out_file2>";
		if (arg.length != 4)
			System.out.println(usage);
		else {
			String inFileName = arg[0];
			double ratio = Double.parseDouble(arg[1]);
			String outFileName1 = arg[2];
			String outFileName2 = arg[3];
			try {
				Sentences sentences = new Sentences(inFileName);
				List<Sentences> out = sentences.splitWithRatio(ratio);
				out.get(0).saveFile(outFileName1);
				out.get(1).saveFile(outFileName2);
			} catch (FileNotFoundException e) {
				System.out.println(e);
			}
		}
		
	}
}
