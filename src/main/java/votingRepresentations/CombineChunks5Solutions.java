package votingRepresentations;

import java.io.FileNotFoundException;
import java.util.Vector;

import corpus.ChunkData;
import corpus.ChunksCoords;
import corpus.Sentences;

/**
 * Join 5 chunk representation solutions with majority voting The inputs must
 * use the I+C representation. The output use the I+C representation. using
 * majority vote
 * 
 * arg[0] number of solutions containing the chunks to pick [3,5] arg[1] tags
 * column ID (starting from 0) arg[2] input file N1 arg[3] input file N2 arg[4]
 * input file N3 arg[5] input file N4 arg[6] input file N5 arg[7] output file
 * 
 */
public class CombineChunks5Solutions
{

	public static void main(String arg[])
	{
		String use = "USE:   java JoinSolutions5 <vote_threshold> <column_ID> <source_file1> <source_file2> <source_file3> <source_file4> <source_file5> <output_file> ";
		if (arg.length != 8)
			System.out.println(use);
		else {
			try {
				combineSolutions5(Integer.parseInt(arg[0]), Integer
						.parseInt(arg[1]), arg[2], arg[3], arg[4], arg[5],
						arg[6], arg[7]);
			} catch (NumberFormatException e) {
				System.out.println(use);
			}
		}
	}

	public static void combineSolutions5(int voteThereshold, int column,
			String source1, String source2, String source3, String source4,
			String source5, String outFile)
	{

		// check input values
		if (column < 0) {
			System.out.println("column ID shall be bigger than 1");
		} else if (voteThereshold > 5 || voteThereshold < 1)
			System.out
					.println("vote threshold shall be a value between 1 and 5");

		// join files
		else {
			try {
				// read source files
				Sentences sentences1 = new Sentences(source1);
				Sentences sentences2 = new Sentences(source2);
				Sentences sentences3 = new Sentences(source3);
				Sentences sentences4 = new Sentences(source4);
				Sentences sentences5 = new Sentences(source5);

				// compute chunk coordinates
				Vector<ChunksCoords> chunkCoords = new Vector<ChunksCoords>();
				chunkCoords.add(sentences1.getChunkCoords(column));
				chunkCoords.add(sentences2.getChunkCoords(column));
				chunkCoords.add(sentences3.getChunkCoords(column));
				chunkCoords.add(sentences4.getChunkCoords(column));
				chunkCoords.add(sentences5.getChunkCoords(column));

				// combine 5 sources
				ChunksCoords combinedCoords = combine(chunkCoords,
						voteThereshold);

				// save results
				Sentences combinedSentences = new Sentences(source1,
						combinedCoords, column);
				combinedSentences.saveFile(outFile);
				System.out.println("\nOUT: "+outFile);
			} catch (FileNotFoundException e) {
				System.out.println("source file not found");
			}
		}
	}

	public static ChunksCoords combine(Vector<ChunksCoords> ChunkCoordsVec,
			int voteThereshold)
	{
		ChunksCoords ret = new ChunksCoords(ChunkCoordsVec.get(0).size());
		int n = ChunkCoordsVec.size() - voteThereshold + 1;
		int[] stats = new int[n];
		for (int i = 0; i < ret.size(); i++) { // loop on the sentences
			
			// combined coords of the current sentence
			Vector<ChunkData> currentCoords = new Vector<ChunkData>(); 

			for (int j = 0; j < n;) { // loop on the sources
				if (ChunkCoordsVec.get(j).coords.get(i).size() == 0) {
					j++;
					continue;
				}
				ChunkData currentChunk = ChunkCoordsVec.get(j).coords.get(i)
						.get(0);
				ChunkCoordsVec.get(j).removeChunk(i, currentChunk);
				int vote = 1;
				for (int k = j + 1; k < ChunkCoordsVec.size(); k++) { // loop on the
					// sources not
					// already processed
					// and different
					// from the current
					if (ChunkCoordsVec.get(k).hasChunk(i, currentChunk)) {
						vote++;
						ChunkCoordsVec.get(k).removeChunk(i, currentChunk);
					}
				}
				if (vote >= voteThereshold) {
					currentCoords.add(currentChunk);
					stats[vote - voteThereshold]++;
				}
			}
			ret.set(i, currentCoords);
		}
		for (int l = 0; l < stats.length; l++) {
			System.out.println("chunks with " + (l + voteThereshold)
					+ " votes = " + stats[l]);
		}

		return ret;
	}
}
