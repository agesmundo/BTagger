package bTagger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Linear Tagging Learner
 */

public class TagLearn
{
	/** In training or not */
	public boolean training;

	/** Feature Lib */
	public FeatLib features;

	/**LabelLib*/
	public LabelLib labels;

	/** project name */
	public String proj;

	/** display results of the evaluate */
	public boolean dispEval = true;

	/** current round of training */
	private int currentRound = -1;

	/** average F-metric */
	private static double averageFM = 0;

	/** current innerRound of training; used for voted Perceptron */
	public int inner = 0;

	public int curSenID = -1;

	public Context context;

	//	///////////////////////////////////////////////////////////////////////////
	//	COSTRUCTORS

	/**
	 * Init BLinTagLearn with samples. Learn the feat weight with sample.
	 */
	public TagLearn(String p, FeatLib f, LabelLib l, Context c) {
		proj = p;
		features = f;
		labels = l;
		context=c;
	}

	//	///////////////////////////////////////////////////////////////////////////

	/**
	 * train
	 */
	public void train(TagSamples sample)
	{
		int docIndex=0;
		training = true;
		Hypothesis.training = true;
		Hypothesis.MARGIN_RATE = context.MARGIN_RATE;

		for (currentRound = 0; currentRound < context.MAXROUND; currentRound++) {
			docIndex=0;
			for (int i = 0; i < sample.size(); i++) {
				// System.err.println("Sentence "+i);

				if(context.DOCUMENTFEAT &&(i==0 || sample.documentsStartID.contains(i))){
					context.documentGazetteers=sample.documentGazetteers.get(docIndex++);
				}
				
				TagSample sentag = sample.get(i);
				List<Island> currentIsland = new ArrayList<Island>();
				List<Island> candIsland = new ArrayList<Island>();

				int length = 0;
				int lastLength = -1;
				int loop = 0;

				// Init candidate Islands with the sentag.words
				initCands(candIsland, sentag);

				while (candIsland.size() > 0) {

					inner++;
					if (length == lastLength) {
						loop++;
						if (loop >= context.MAXLOOP) {
							break;
						}
					} else {
						lastLength = length;
						loop = 0;
					}

					// DEBUG
					// System.err.print("cur:");
					// for (int j=0; j<currentIsland.size(); j++){
					// System.err.print(" "+currentIsland.get(j));
					// }
					// System.err.print("\ncand:");
					// for (int j=0; j<candIsland.size(); j++){
					// System.err.print(" "+candIsland.get(j));
					// }
					// System.err.println("");

					// Search for the candidate whose hypothesis has the highest
					// localScore
					Island selIsland = selectCand(candIsland);

					// check for weight update
					boolean correct = checkCand(selIsland);

					// DEBUG
					// for (int k=0; k<selIsland.leftBoundSocket.size(); k++){
					// System.err.print(" "+selIsland.leftBoundSocket.get(k));
					// }
					// System.err.println("");
					// for (int k=0; k<selIsland.rightBoundSocket.size(); k++){
					// System.err.print(" "+selIsland.rightBoundSocket.get(k));
					// }
					// System.err.println("");

					if (correct) { // no weight update

						// update the current Islands, candidate Islands by
						// applying selIsland
						applyCand(currentIsland, candIsland, selIsland, sentag);
						length++;

					} else { // new weight

						// re-generate candidate Island with the curren weights
						candIsland.clear();
						if (currentIsland.size() == 0) {
							initCands(candIsland, sentag);
						} else {
							genAllCandIslands(candIsland, currentIsland, sentag);
						}
					}

				} // end of all operations

				if (length < sentag.words.length) {
					System.err.println("LOOP: " + i);
					continue;
				}

				// DEBUG : display the results
				// StringBuffer sb = new StringBuffer("GLD: ");
				// sentag.display(sb);
				// sb.append("\nTOP: ");
				// currentIsland.get(0).display(sb);
				// System.err.println(sb.toString());

			} // end of all sentences;

			// save weights at the end of the round
			if (context.PRINT_WEIGHTS_EACH_ROUND) {
				features.saveWeight(proj + "." + currentRound + ".fea", inner);
			}

		} // end of a round

		// save weights at the end of the training
		if (!context.PRINT_WEIGHTS_EACH_ROUND) {
			features.saveWeight(proj + ".fea", inner);
		}
	}

	/**
	 * predict
	 */
	public TagSamples predict(TagSamples sample)
	{
		int docIndex=0;
		training = false;
		Hypothesis.training = false;

		// for each sentence
		for (int i = 0; i < sample.size(); i++) {

			if(context.DOCUMENTFEAT &&(i==0 || sample.documentsStartID.contains(i))){
				context.documentGazetteers=sample.documentGazetteers.get(docIndex++);
			}

			// System.err.println("Sentence "+i);
			TagSample sentag = sample.get(i); // select current
			// sentence
			List<Island> currentIsland = new ArrayList<Island>(); // accepted
			// spans
			List<Island> candIsland = new ArrayList<Island>(); // candidate
			// spans

			// Init candidate Islands with the sentag.words
			initCands(candIsland, sentag);

			while (candIsland.size() > 0) {

				/*
				 * / debug for(int j=0;j<currentIsland.size();j++){
				 * System.out.print(currentIsland.get(j)+","); }
				 * System.out.println(); for(int j=0;j<candIsland.size();j++){
				 * System.out.print(candIsland.get(j)+","); }
				 * System.out.println("\n--");
				 */

				// Search for the candidate whose hypothesis has the highest
				// localScore
				Island selIsland = selectCand(candIsland);

				// update the current Islands, candidate Islands by applying
				// selIsland
				applyCand(currentIsland, candIsland, selIsland, sentag);

			} // end of all operations

			// retrieve the top sequence of tags
			Island finalIsland = currentIsland.get(0);
			TagSample tagspl = sample.get(i);
			finalIsland.retrieve(tagspl.tags, finalIsland.topLeftBoundSktID,
					finalIsland.topRightBoundSktID);

			// output
			/*
			 * for (int j=0; j<sentag.words.length; j++){ if (j>0)
			 * System.out.print(" ");
			 * System.out.print(""+tagspl.words[j]+"_"+tagspl.tags[j]); }
			 * System.out.println("");
			 */

		} // end of all sentences;

		return sample;


	}

	// TODO use predict() and evaluate()
	/**
	 * Predict and Evaluate.
	 * 
	 * @return the value of the F-metric
	 */
	public double predictEvaluate(TagSamples sample)
	{
		int docIndex=0;
 
		training = false;
		Hypothesis.training = false;

		int tagMatch = 0;
		int tagTotal = 0;
		int senmatch = 0;

		int firstTagMatch = 0;

		// B-tag level metrics, only for IOB1 and IOB2
		/*
		 * int goldB=0; int predB=0; int matchB=0; int bTaggedStartingI=0; int
		 * startingITaggedB=0;
		 */

		List<String> classesNames = new ArrayList<String>();
		if (dispEval) {
			classesNames = labels.getClassesNames();
		}
		int[][] classResults = new int[classesNames.size()][3];// for each
		// class
		// store,predPhrases
		// goldPhrases
		// and
		// matchPhrases;
		int goldPhrases = 0;
		int predPhrases = 0;
		int matchPhrases = 0;

		List<TagSample> taggedSample = new ArrayList<TagSample>();
		for (int i = 0; i < sample.size(); i++) {

			// System.out.println("Sentence "+i);

			if(context.DOCUMENTFEAT &&(i==0 || sample.documentsStartID.contains(i))){
				context.documentGazetteers=sample.documentGazetteers.get(docIndex++);
			}

			TagSample sentag = sample.get(i);// current sentence
			List<Island> currentIsland = new ArrayList<Island>();
			List<Island> candIsland = new ArrayList<Island>();
			boolean nomistake = true;

			// Init candidate Islands with the sentag.words
			initCands(candIsland, sentag);

			while (candIsland.size() > 0) {

				// DEBUG
				// System.err.print("cur:");
				// for (int j=0; j<currentIsland.size(); j++){
				// System.err.print(" "+currentIsland.get(j));
				// }
				// System.err.print("\ncand:");
				// for (int j=0; j<candIsland.size(); j++){
				// System.err.print(" "+candIsland.get(j));
				// }
				// System.err.println("");

				// Search for the candidate whose hypothesis has the highest
				// localScore
				Island selIsland = selectCand(candIsland);

				// DEBUG
				// for (int k=0; k<selIsland.leftBoundSocket.size(); k++){
				// System.err.print(" "+selIsland.leftBoundSocket.get(k));
				// }
				// System.err.println("");
				// for (int k=0; k<selIsland.rightBoundSocket.size(); k++){
				// System.err.print(" "+selIsland.rightBoundSocket.get(k));
				// }
				// System.err.println("");

				// update the current Islands, candidate Islands by applying
				// selIsland
				applyCand(currentIsland, candIsland, selIsland, sentag);

			} // end of all operations

			// retrieve the top sequence of tags
			Island finalIsland = currentIsland.get(0);
			TagSample tagspl = new TagSample(finalIsland.sen);
			finalIsland.retrieve(tagspl.tags, finalIsland.topLeftBoundSktID,
					finalIsland.topRightBoundSktID);
			taggedSample.add(tagspl);
			// ?? are always (often) equal to 0?
			//maybe because the best socket is always stored in 0 like hypos?
			// System.out.println(finalIsland.topRightBoundSktID);
			// System.out.println(finalIsland.topLeftBoundSktID);

			//update document features with the result of the tagging of last sentence
			//			if(context.DOCUMENTFEAT){
			//				context.documentGazetteers.addEntries(tagspl);
			//			}

			if (dispEval) {
				// tag level metrics
				tagTotal += tagspl.words.length;
				for (int j = 0; j < tagspl.words.length; j++) {
					if (tagspl.tags[j] == sentag.tags[j]) {
						tagMatch++;
						if (j == 0)
							firstTagMatch++;
					} else {
						nomistake = false;
					}
				}
				if (nomistake)
					senmatch++;

				int[] results;
				// B-tag level metrics, only for IOB1 and IOB2
				/*
				 * results=MetricsUtility.chunkMetrics(sentag,tagspl);
				 * predB+=results[0]; goldB+=results[1]; matchB+=results[2];
				 * bTaggedStartingI+=results[3]; startingITaggedB+=results[4];
				 */

				// Compute phrase level metrics for the different classes.
				for (int j = 0; j < classesNames.size(); j++) {
					results = LabelsUtility.chunkClassMetrics(sentag, tagspl,
							classesNames.get(j));
					classResults[j][0] += results[0];
					classResults[j][1] += results[1];
					classResults[j][2] += results[2];
				}
			}
			// Compute phrase level metrics for the IOB tag format
			int[] results = LabelsUtility.chunksMetrics(sentag, tagspl);
			predPhrases += results[0];
			goldPhrases += results[1];
			matchPhrases += results[2];

			// display results
			/*
			 * StringBuffer sb = new StringBuffer("GLD: "); sentag.display(sb);
			 * sb.append("\nTOP: "); currentIsland.get(0).display(sb);
			 * System.out.println(sb.toString());
			 */
		} // end of all sentences;

		double precision = 1.0 * matchPhrases / predPhrases;
		double recall = 1.0 * matchPhrases / goldPhrases;
		double fMetric = (2 * recall * precision) / (recall + precision);

		if (dispEval) {
			System.out.println("\nTags number: " + tagTotal);
			System.out.println("Tags matches: " + tagMatch);
			double tprecision = 1.0 * tagMatch / tagTotal;
			System.out.println("Tag precision: " + tprecision);

			System.out.println("\nFirst tag matches: " + firstTagMatch);
			System.out.println("First tag precision: " + 1.0 * firstTagMatch
					/ sample.size());

			System.out.println("\nSentences number: " + sample.size());
			System.out.println("Sentences matches: " + senmatch);
			double senprec = 1.0 * senmatch / sample.size();
			System.out.println("Sentence Precisioin: " + senprec);

			// B-tag level metrics, only for IOB1 and IOB2
			/*
			 * System.out.println("\nB-tags number in the gold standard:
			 * "+goldB); System.out.println("B-tags number in the prediction: "+
			 * predB); System.out.println("B-tags matches: "+ matchB);
			 * System.out.println("B-tags tagged as a starting I: "+
			 * bTaggedStartingI);//matchB+bTaggedStartingI=goldB-tagErrors>=0
			 * System.out.println("Starting I tagged as B-tags: "+
			 * startingITaggedB); double bPrecision = 1.0 * matchB/predB;
			 * System.out.println("B-tag precision: "+ bPrecision); double
			 * bRecall = 1.0 * matchB/goldB; System.out.println("B-tag recall:
			 * "+bRecall); System.out.println("B-tag F-METRIC:
			 * "+(2*bRecall*bPrecision)/(bRecall+bPrecision));
			 */

			for (int j = 0; j < classesNames.size(); j++) {
				System.out.println("\nClass " + classesNames.get(j)
						+ " phrases number in the gold standard: "
						+ classResults[j][1]);
				System.out.println("Class " + classesNames.get(j)
						+ " phrases number in the prediction: "
						+ classResults[j][0]);
				System.out.println("Class " + classesNames.get(j)
						+ " phrases matches: " + classResults[j][2]);
				double cPrecision = 1.0 * classResults[j][2]
				                                          / classResults[j][0];
				System.out.println("Class " + classesNames.get(j)
						+ " phrase precision: " + cPrecision);
				double cRecall = 1.0 * classResults[j][2] / classResults[j][1];
				System.out.println("Class " + classesNames.get(j)
						+ " phrase recall: " + cRecall);
				System.out.println("Class " + classesNames.get(j)
						+ " phrase F-METRIC: " + (2 * cRecall * cPrecision)
						/ (cRecall + cPrecision));
			}

			System.out.println("\nPhrases number in the gold standard: "
					+ goldPhrases);
			System.out.println("Phrases number in the prediction: "
					+ predPhrases);
			System.out.println("Phrases matches: " + matchPhrases);
			System.out.println("Phrase precision: " + precision);
			System.out.println("Phrase recall: " + recall);
			System.out.println("Phrase F-METRIC: " + fMetric);

			if (dispEval) {
				averageFM += fMetric;
				// TagSample.saveTagged(taggedSample,
				// proj+""+(currentRound>=0?"."+currentRound:"")+"Tagged.txt");
			}
		}
		return fMetric;
	}

	/**
	 * Evaluate.
	 * 
	 * @return the value of the F-metric
	 */
	public double evaluate(TagSamples tagged, TagSamples gold)
	{

		training = false;
		Hypothesis.training = false;

		int tagMatch = 0;
		int tagTotal = 0;
		int senmatch = 0;

		int firstTagMatch = 0;

		// B-tag level metrics, only for IOB1 and IOB2
		/*
		 * int goldB=0; int predB=0; int matchB=0; int bTaggedStartingI=0; int
		 * startingITaggedB=0;
		 */

		List<String> classesNames = new ArrayList<String>();
		if (dispEval) {
			classesNames = labels.getClassesNames();
		}
		int[][] classResults = new int[classesNames.size()][3];// for each
		// class
		// store,predPhrases
		// goldPhrases
		// and
		// matchPhrases;
		int goldPhrases = 0;
		int predPhrases = 0;
		int matchPhrases = 0;

		for (int i = 0; i < gold.size(); i++) {

			// System.out.println("Sentence "+i);

			TagSample sentag = gold.get(i);// current sentence

			boolean nomistake = true;
			TagSample tagspl = tagged.get(i);
			if (dispEval) {
				// tag level metrics
				tagTotal += tagspl.words.length;
				for (int j = 0; j < tagspl.words.length; j++) {
					if (tagspl.tags[j] == sentag.tags[j]) {
						tagMatch++;
						if (j == 0)
							firstTagMatch++;
					} else {
						nomistake = false;
					}
				}
				if (nomistake)
					senmatch++;

				int[] results;

				// B-tag level metrics, only for IOB1 and IOB2
				/*
				 * results=MetricsUtility.BMetrics(sentag,tagspl);
				 * predB+=results[0]; goldB+=results[1]; matchB+=results[2];
				 * bTaggedStartingI+=results[3]; startingITaggedB+=results[4];
				 */

				// Compute phrase level metrics for the different classes.
				for (int j = 0; j < classesNames.size(); j++) {
					results = LabelsUtility.chunkClassMetrics(sentag, tagspl,
							classesNames.get(j));
					classResults[j][0] += results[0];
					classResults[j][1] += results[1];
					classResults[j][2] += results[2];
				}
			}
			// Compute phrase level metrics for the IOB tag format
			int[] results = LabelsUtility.chunksMetrics(sentag, tagspl);
			predPhrases += results[0];
			goldPhrases += results[1];
			matchPhrases += results[2];

			// display results
			/*
			 * StringBuffer sb = new StringBuffer("GLD: "); sentag.display(sb);
			 * sb.append("\nTOP: "); currentIsland.get(0).display(sb);
			 * System.out.println(sb.toString());
			 */
		} // end of all sentences;

		double precision = 1.0 * matchPhrases / predPhrases;
		double recall = 1.0 * matchPhrases / goldPhrases;
		double fMetric = (2 * recall * precision) / (recall + precision);

		if (dispEval) {
			System.out.println("\nTags number: " + tagTotal);
			System.out.println("Tags matches: " + tagMatch);
			double tprecision = 1.0 * tagMatch / tagTotal;
			System.out.println("Tag precision: " + tprecision);

			System.out.println("\nFirst tag matches: " + firstTagMatch);
			System.out.println("First tag precision: " + 1.0 * firstTagMatch
					/ gold.size());

			System.out.println("\nSentences number: " + gold.size());
			System.out.println("Sentences matches: " + senmatch);
			double senprec = 1.0 * senmatch / gold.size();
			System.out.println("Sentence Precisioin: " + senprec);

			// B-tag level metrics, only for IOB1 and IOB2
			/*
			 * System.out.println("\nB-tags number in the gold standard:
			 * "+goldB); System.out.println("B-tags number in the prediction: "+
			 * predB); System.out.println("B-tags matches: "+ matchB);
			 * System.out.println("B-tags tagged as a starting I: "+
			 * bTaggedStartingI);//matchB+bTaggedStartingI=goldB-tagErrors>=0
			 * System.out.println("Starting I tagged as B-tags: "+
			 * startingITaggedB); double bPrecision = 1.0 * matchB/predB;
			 * System.out.println("B-tag precision: "+ bPrecision); double
			 * bRecall = 1.0 * matchB/goldB; System.out.println("B-tag recall:
			 * "+bRecall); System.out.println("B-tag F-METRIC:
			 * "+(2*bRecall*bPrecision)/(bRecall+bPrecision));
			 */

			for (int j = 0; j < classesNames.size(); j++) {
				System.out.println("\nClass " + classesNames.get(j)
						+ " phrases number in the gold standard: "
						+ classResults[j][1]);
				System.out.println("Class " + classesNames.get(j)
						+ " phrases number in the prediction: "
						+ classResults[j][0]);
				System.out.println("Class " + classesNames.get(j)
						+ " phrases matches: " + classResults[j][2]);
				double cPrecision = 1.0 * classResults[j][2]
				                                          / classResults[j][0];
				System.out.println("Class " + classesNames.get(j)
						+ " phrase precision: " + cPrecision);
				double cRecall = 1.0 * classResults[j][2] / classResults[j][1];
				System.out.println("Class " + classesNames.get(j)
						+ " phrase recall: " + cRecall);
				System.out.println("Class " + classesNames.get(j)
						+ " phrase F-METRIC: " + (2 * cRecall * cPrecision)
						/ (cRecall + cPrecision));
			}

			System.out.println("\nPhrases number in the gold standard: "
					+ goldPhrases);
			System.out.println("Phrases number in the prediction: "
					+ predPhrases);
			System.out.println("Phrases matches: " + matchPhrases);
			System.out.println("Phrase precision: " + precision);
			System.out.println("Phrase recall: " + recall);
			System.out.println("Phrase F-METRIC: " + fMetric);

			if (dispEval) {
				averageFM += fMetric;
				// TagSample.saveTagged(taggedSample,
				// proj+""+(currentRound>=0?"."+currentRound:"")+"Tagged.txt");
			}
		}
		return fMetric;
	}

	/**
	 * train and evaluate
	 */
	public List<Double> trainEval(TagSamples evalsample, TagSamples sample)
	{
		int docIndex=0;

		ArrayList<Double> results = new ArrayList<Double>();
		averageFM = 0;

		for (currentRound = 0; currentRound < context.MAXROUND; currentRound++) {
			docIndex=0;

			System.out.println("\n------------------- ROUND "
					+ (currentRound + 1) + " -------------------");
			System.out.println("Start time: "
					+ new Date(System.currentTimeMillis()));
			training = true;
			Hypothesis.training = true;
			Hypothesis.MARGIN_RATE = context.MARGIN_RATE;


			for (int i = 0; i < sample.size(); i++) {

				// System.err.println("Sentence "+i);

				//TODO move start index in document gazet? implement pointer to save time
				if(context.DOCUMENTFEAT &&(i==0 || sample.documentsStartID.contains(i))){
					context.documentGazetteers=sample.documentGazetteers.get(docIndex++);
				}

				TagSample sentag = sample.get(i);
				List<Island> currentIsland = new ArrayList<Island>();
				List<Island> candIsland = new ArrayList<Island>();

				int length = 0;
				int lastLength = -1;
				int loop = 0;

				// Init candidate Islands with the sentag.words
				initCands(candIsland, sentag);

				while (candIsland.size() > 0) {

					inner++;
					if (length == lastLength) {
						loop++;
						if (loop >= context.MAXLOOP) {
							break;
						}
					} else {
						lastLength = length;
						loop = 0;
					}

					// Search for the candidate whose hypothesis has the highest
					// localScore
					Island selIsland = selectCand(candIsland);

					// check for weight update
					boolean correct = checkCand(selIsland);

					if (correct) { // no weight update

						// update the current Islands, candidate Islands by
						// applying selIsland
						applyCand(currentIsland, candIsland, selIsland, sentag);
						length++;

					} else { // new weight

						// re-generate candidate Island with the curren weights
						candIsland.clear();
						if (currentIsland.size() == 0) {
							initCands(candIsland, sentag);
						} else {
							genAllCandIslands(candIsland, currentIsland, sentag);
						}
					}

				} // end of all operations

				if (length < sentag.words.length) {
					System.err.println("LOOP: " + i);
					continue;
				}

				//update document features with the result of the tagging of last sentence
				//				if(context.DOCUMENTFEAT){
				//					context.documentGazetteers.addEntries(sentag);
				//				}

			} // end of all sentences;
			// feat.saveWeight(proj+"."+currentRound+".fea", inner);

			// evaluate
			training = false;
			Hypothesis.training = false;

			FeatLib orgfeat = features;
			FeatLib roundfeat = new FeatLib(features); // copy the feat;
			roundfeat.useVotedFeat(inner);

			TagLearn evaluator = new TagLearn(proj, roundfeat, this.labels, this.context);
			results.add(evaluator.predictEvaluate(evalsample));
			features = orgfeat;

			// save weights at the end of the round
			if (context.PRINT_WEIGHTS_EACH_ROUND) {
				features.saveWeight(proj + "." + currentRound + ".fea", inner);
			}

		} // end of a round

		// print average F-metric
		System.out.println("\nAverage F-mertic: " + averageFM / context.MAXROUND);
		System.out.println("Max F-mertic: " + (Double)Collections.max(results));
		System.out.println("Max F-mertic at round: " + (results.indexOf((Double)Collections.max(results))+1));

		// save weights at the end of the training
		if (!context.PRINT_WEIGHTS_EACH_ROUND) {
			features.saveWeight(proj + ".fea", inner);
		}

		return results;

	}

	/**
	 * Research new features
	 */
	public void researchFeat(TagSamples evalsample, TagSamples sample)
	{
		double score = 0;
		List<Feature> candidateFeature = new ArrayList<Feature>();
		PriorityQueue<FeaturesRankingElement> FeaturesRanking = new PriorityQueue<FeaturesRankingElement>();
		genAllCandFeatures(candidateFeature);

		// loop on candidate features
		for (int j = 247; j < candidateFeature.size(); j++) {
			features = new FeatLib();

			System.out.print(j + " " + candidateFeature.get(j));
			context.addFeature.add(candidateFeature.get(j));

			// loop on training rounds
			for (currentRound = 0; currentRound < context.MAXROUND; currentRound++) {
				score = 0;
				training = true;
				Hypothesis.training = true;
				Hypothesis.MARGIN_RATE = context.MARGIN_RATE;

				// loop on sentences
				for (int i = 0; i < sample.size(); i++) {

					curSenID = i;

					TagSample sentag = sample.get(i);
					List<Island> currentIsland = new ArrayList<Island>();
					List<Island> candIsland = new ArrayList<Island>();

					int length = 0;
					int lastLength = -1;
					int loop = 0;

					// Initialize candidate Islands with the sentag.words
					initCands(candIsland, sentag);

					while (candIsland.size() > 0) {

						inner++;
						if (length == lastLength) {
							loop++;
							if (loop >= context.MAXLOOP) {
								break;
							}
						} else {
							lastLength = length;
							loop = 0;
						}

						// Search for the candidate whose hypothesis has the
						// highest localScore
						Island selIsland = selectCand(candIsland);

						// check for weight update
						boolean correct = checkCand(selIsland);

						if (correct) { // no weight update

							// update the current Islands, candidate Islands by
							// applying selIsland
							applyCand(currentIsland, candIsland, selIsland,
									sentag);
							length++;

						} else { // new weight

							// re-generate candidate Island with the curren
							// weights
							candIsland.clear();
							if (currentIsland.size() == 0) {
								initCands(candIsland, sentag);
							} else {
								genAllCandIslands(candIsland, currentIsland,
										sentag);
							}
						}

					} // end of all operations

					if (length < sentag.words.length) {
						// System.err.println("LOOP: "+i);
						continue;
					}

				} // end of all sentences;
				// feat.saveWeight(proj+"."+currentRound+".fea", inner);

				// evaluate
				training = false;
				Hypothesis.training = false;

				FeatLib orgfeat = features;
				FeatLib roundfeat = new FeatLib(features); // copy the feat;
				roundfeat.useVotedFeat(inner);

				TagLearn evaluator = new TagLearn(proj, roundfeat, this.labels, this.context);
				evaluator.dispEval=false;
				score += evaluator.predictEvaluate(evalsample);
				features = orgfeat;
			} // end of a round
			context.addFeature.remove(context.addFeature.size() - 1);
			System.out.println(", score: " + score);
			FeaturesRanking.add(new FeaturesRankingElement(candidateFeature
					.get(j), score));
		}// end of measure of a feature

		// write ranking file
		PrintWriter out = new PrintWriter(FileUtils.getWriter(proj
				+ ".rnk"));
		int i = 0;
		while (FeaturesRanking.peek() != null) {
			out.println((++i) + " \t" + FeaturesRanking.poll());
		}
		System.out
		.println("\nOUT: Feature rank saved in: " + proj + ".rnk");
		out.close();

	}

	public void genAllCandFeatures(List<Feature> candFeat)
	{
		int featureNumber = (int) Math.pow(2, (4 * context.NGRAM - 3));// combination
		// of all
		// possible
		// detail
		// excluded
		// [TAG!:0],2^(AREA-1),AREA=2*WindowSize,WindowSize=2*NGRAM-1
		int start = (int) Math.pow(2, 2 * context.NGRAM - 2);// to skip all
		// the
		// features with only
		// main tags
		for (int i = start; i < featureNumber; i++) {
			candFeat.add(new Feature(new FeatureMatrix(i,context.NGRAM),context));
		}
	}

	/**
	 * Initiate candidate Span with empty currentIsland and sentence.words
	 * 
	 * @param cands
	 *            List where the spans are saved.
	 * @param sen
	 *            The sentence used to initialize the set of candidate spans.
	 */
	public void initCands(List<Island> cands, TagSample sen)
	{

		// create a candidate island for each word
		for (int i = 0; i < sen.words.length; i++) {
			Island island = new Island(this, sen, i, training);
			cands.add(island);
		}
	}

	/**
	 * Search for the candidate island one of whose hypotheses has the highest
	 * localScore
	 */
	public Island selectCand(List<Island> candIsland)
	{

		Island topCand = null;
		double topOpScore = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < candIsland.size(); i++) {
			Island island = candIsland.get(i);

			// DEBUG
			// System.err.println(island.lastPosi);
			// for (int j=0; j<island.leftBoundSocket.size(); j++){
			// System.err.print(" "+island.leftBoundSocket.get(j));
			// }
			// System.err.print(" |");
			// for (int j=0; j<island.rightBoundSocket.size(); j++){
			// System.err.print(" "+island.rightBoundSocket.get(j));
			// }
			// System.err.println("");

			if (island.topOpHypo.getLabelScoreMGN() > topOpScore) {
				topOpScore = island.topOpHypo.getLabelScoreMGN();
				topCand = island;
			}
		}
		return topCand;
	}

	/**
	 * Check if the top hypothesis in selIsland is compatible with gold
	 * standard. Update the weight List if it is incompatible
	 */
	public boolean checkCand(Island selIsland)
	{

		// DEBUG
		// if (selIsland.topOpHypo == selIsland.goldHypo)
		// System.err.print("==>");
		// System.err.print(" "+selIsland.lastPosi);
		// System.err.println(" "+selIsland.topOpHypo.lastLabel+"
		// "+selIsland.goldHypo.lastLabel);

		// System.err.print("TOP: "+selIsland.topOpHypo+"
		// "+selIsland.topOpHypo.lastLabel);
		// System.err.println(" "+selIsland.topOpHypo.socketIDFromLeft+"
		// "+selIsland.topOpHypo.socketIDFromRight);
		// System.err.print("GLD: "+selIsland.goldHypo+"
		// "+selIsland.goldHypo.lastLabel);
		// System.err.println(" "+selIsland.goldHypo.socketIDFromLeft+"
		// "+selIsland.goldHypo.socketIDFromRight);

		if (selIsland.topOpHypo == selIsland.goldHypo) {
			return true;
		}

		features.updateFeat(selIsland.goldHypo.features, +1, inner);
		features.updateFeat(selIsland.topOpHypo.features, -1, inner);

		return false;
	}

	/**
	 * Update currentIsland by applying selIsland Update candIsland by applying
	 * selIsland
	 */
	public void applyCand(List<Island> currentIsland,
			List<Island> candIsland, Island selIsland, TagSample sen)
	{

		// 1 Update currentIsland:
		// 1.1 remove child islands of selIsland from currentIsland
		if (selIsland.islandFromLeft != null
				|| selIsland.islandFromRight != null) {
			for (int i = 0; i < currentIsland.size(); i++) {
				Island cur = currentIsland.get(i);
				if (cur == selIsland.islandFromLeft
						|| cur == selIsland.islandFromRight) {
					currentIsland.remove(i);
					i--;
				}
			}
		}

		// 1.2 insert selIsland to currentIsland
		boolean inserted = false;
		for (int i = 0; i < currentIsland.size(); i++) {
			Island cur = currentIsland.get(i);
			if (cur.lastPosi > selIsland.lastPosi) {
				currentIsland.add(i, selIsland);
				inserted = true;
				break;
			}
		}
		if (!inserted)
			currentIsland.add(selIsland);

		// 2. Update candIsland: ( X = selIsland )
		// 2.1 remove selIsland from candIsland
		candIsland.remove(selIsland);

		// 2.2 remove from candidate Islands depending on selIsland's children
		// 2.3 replace them with candidates depending on selIsland
		// cand: A X B
		// / \ / \
		// cur: ==== ===== ====
		//
		// ||
		// \/
		// 
		// cand: A' B'
		// / \ / \
		// cur: ==== ======X ====
		//
		for (int i = 0; i < candIsland.size(); i++) {
			Island cand = candIsland.get(i);

			// Case A :
			if (selIsland.islandFromLeft != null
					&& cand.islandFromRight == selIsland.islandFromLeft) {
				Island newcand = new Island(this, sen, cand.lastPosi,
						cand.islandFromLeft, selIsland, training);
				candIsland.set(i, newcand);
			}

			if (selIsland.islandFromRight != null
					&& cand.islandFromLeft == selIsland.islandFromRight) {
				Island newcand = new Island(this, sen, cand.lastPosi, selIsland,
						cand.islandFromRight, training);
				candIsland.set(i, newcand);
			}

			// Case B :
			if (selIsland.islandFromLeft == null
					&& cand.lastPosi == selIsland.lastPosi - 1) {
				Island newcand = new Island(this, sen, cand.lastPosi,
						cand.islandFromLeft, selIsland, training);
				candIsland.set(i, newcand);
			}

			if (selIsland.islandFromRight == null
					&& cand.lastPosi == selIsland.lastPosi + 1) {
				Island newcand = new Island(this, sen, cand.lastPosi, selIsland,
						cand.islandFromRight, training);
				candIsland.set(i, newcand);
			}
		}

	}

	/**
	 * generate candidate Islands with the current Islands
	 */
	public void genAllCandIslands(List<Island> candIsland,
			List<Island> currentIsland, TagSample sen)
	{

		for (int i = 0; i < sen.words.length; i++) {

			// in currentIsland ?
			if (getDomIsland(i, currentIsland) != null) {
				continue;
			}

			// left and right neighbor islands
			Island leftIsland = getDomIsland(i - 1, currentIsland);
			Island rightIsland = getDomIsland(i + 1, currentIsland);

			// generate new Island
			Island newcand = new Island(this, sen, i, leftIsland, rightIsland,
					training);
			candIsland.add(newcand);
		}

	}

	/**
	 * get the BLinIsland in islands which dominates idx
	 */
	private Island getDomIsland(int idx, List<Island> islands)
	{
		for (int i = 0; i < islands.size(); i++) {
			Island island = islands.get(i);
			if (island.leftBoundPosi <= idx && idx <= island.rightBoundPosi) {
				return island;
			}
		}
		return null;
	}

}
