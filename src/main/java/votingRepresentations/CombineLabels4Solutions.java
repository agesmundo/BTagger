package votingRepresentations;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import corpus.Sentence;
import corpus.Sentences;

public class CombineLabels4Solutions
{

	public static void combineSolutions4(int mainRepresentationID,
			int columnID, String[] filesToMerge, double[] weights,
			String outFileName)
	{

		// check input
		if (columnID < 0) {
			System.out.println("column ID shall be bigger than 1");
		} else if (filesToMerge.length != weights.length) {
			System.out
					.println("filesToMerge and weights shuold be the same number of elements");
		} else {
			// print paramenters
			System.out
					.println("\n~~~~~~~~~~~~~~~~~~~~~~\nCOMBINE ON LABELS 4 SOLUTIONS\n~~~~~~~~~~~~~~~~~~~~~~\n");
			System.out.println("mainRepresentationID= " + mainRepresentationID);
			System.out.println("column= " + columnID);
			for (int i = 0; i < filesToMerge.length; i++) {
				System.out.println("fileToMerge " + i + "= " + filesToMerge[i]
						+ " ; with weight = " + weights[i]);
			}
			System.out.println("outFileName= " + outFileName);

			// elaborate

			// read the input files
			Sentences sentencesToMerge[] = new Sentences[filesToMerge.length];
			for (int j = 0; j < sentencesToMerge.length; j++) {
				try {
					sentencesToMerge[j] = new Sentences(filesToMerge[j]);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			// create merged sentences
			Sentences combinedSentences = new Sentences();

			int tokensPerLine = sentencesToMerge[0].getSentence(0).getLine(0)
					.size();

			// sentence by sentence
			{
				// for each sentence
				for (int sentenceIndex = 0; sentenceIndex < sentencesToMerge[0]
						.size(); sentenceIndex++) {
					Sentence combinedSentence = new Sentence();
					// for each line
					for (int lineIndex = 0; lineIndex < sentencesToMerge[0]
							.getSentence(sentenceIndex).size(); lineIndex++) {
						List<String> tokens = new ArrayList<String>();
						// for each token
						for (int tokenIndex = 0; tokenIndex < tokensPerLine; tokenIndex++) {
							if (tokenIndex == columnID) {
								List<String> candidateTokens = new ArrayList<String>();
								// for each group of sentences
								for (int sentencesGroupIndex = 0; sentencesGroupIndex < sentencesToMerge.length; sentencesGroupIndex++) {
									candidateTokens
											.add(sentencesToMerge[sentencesGroupIndex]
													.getToken(sentenceIndex,
															lineIndex,
															tokenIndex));
								}
								tokens.add(computeBestLabel(candidateTokens,
										weights, mainRepresentationID));
							} else {
								tokens.add(sentencesToMerge[0].getToken(
										sentenceIndex, lineIndex, tokenIndex));
							}
						}
						combinedSentence.addLine(tokens);
					}
					combinedSentences.addSentence(combinedSentence);
				}
			}

			combinedSentences.saveFile(outFileName);

			System.out.println("\nOUT: merged file saved in: " + outFileName);

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
		}

	}

	private static String computeBestLabel(List<String> candidateTokens,
			double[] weights, int mainRepresentationID)
	{
		List<String> mergedTokens = new ArrayList<String>();
		List<Double> mergedWeights = new ArrayList<Double>();
		List<List<Integer>> sourceCandidateIDs = new ArrayList<List<Integer>>();
		boolean matched = false;
		for (int i = 0; i < candidateTokens.size(); i++) {
			matched = false;
			for (int j = 0; j < mergedTokens.size(); j++) {
				if (candidateTokens.get(i).compareTo(mergedTokens.get(j)) == 0) {
					double updateWeight = mergedWeights.get(j) + weights[i];
					mergedWeights.set(j, new Double(updateWeight));
					sourceCandidateIDs.get(j).add(new Integer(i));
					matched = true;
					break;
				}
			}
			if (matched == false) {
				mergedTokens.add(candidateTokens.get(i));
				mergedWeights.add(weights[i]);
				List<Integer> sourceCandidateID = new ArrayList<Integer>();
				sourceCandidateID.add(new Integer(i));
				sourceCandidateIDs.add(sourceCandidateID);
			}
		}

		// get higher Score
		double bestScore = Double.MIN_VALUE;
		for (double mergedWeight : mergedWeights) {
			if (bestScore < mergedWeight) {
				bestScore = mergedWeight;
			}
		}

		// get all the labels with the highest score
		List<String> bestMergedTokens = new ArrayList<String>();
		List<List<Integer>> bestSourceCandidateIDs = new ArrayList<List<Integer>>();
		for (int k = 0; k < mergedTokens.size(); k++) {
			if (mergedWeights.get(k) == bestScore) {
				bestMergedTokens.add(mergedTokens.get(k));
				bestSourceCandidateIDs.add(sourceCandidateIDs.get(k));
			}
		}

		// if there is only one best return
		if (bestMergedTokens.size() == 1) {
			return bestMergedTokens.get(0);
		}

		else {
			// find the token from mainRepresentationID
			for (int j = 0; j < bestSourceCandidateIDs.size(); j++) {
				for (int sourceCandID : bestSourceCandidateIDs.get(j)) {
					if (sourceCandID == mainRepresentationID) {
						return bestMergedTokens.get(j);
					}
				}
			}
		}
		throw new RuntimeException("CANNOT SELECT THE BEST TOKEN");
	}
}
