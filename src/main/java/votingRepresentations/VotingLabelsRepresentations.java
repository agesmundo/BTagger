package votingRepresentations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utility.ConvertChunkRepresentation;
import utility.GenerateChunkRepresentations;
import utility.Graph;
import utility.Utility;
import bTagger.BTagger;
import bTagger.Context;
import bTagger.FileUtils;
import corpus.Sentences;

/**
 * 
 * @author Andrea Gesmundo
 */
public class VotingLabelsRepresentations
{
	public static final String[] chunkRepNames = { "IOB1", "IOB2", "IOE1",
			"IOE2" };

	/** used to solve parity votes cases */
	public static final int mainRepresentationID = 1;

	public static final int crossValidationSegments = 5;

	public static void main(String args[])
	{
		String use = "Train: java VotingLabelsRepresentations -t <proj_ID> <train_file> <feature_script>"
				+ "\nTrain Evaluate: java BTagger -te <proj_ID> <train_file> <test_file> <feature script>"
				+ "\nFull Experiment: java VotingLabelsRepresentations -f <projID> <train_file> <test_file> <feature_script_train> <feature_script_weights>"

		// + " \nPredict: java BTagger -p <proj_ID> <test_file>
		// <feature_script>
		// <features IOB1> <features IOB2> <features IOE1> <features IOE2>
		// <features O+C>"
		;

		// save start time
		long startTime = System.currentTimeMillis();
		System.out.println("\nStarting time: " + new Date(startTime));
		String openingMessage = "LABELS-VOTING";
		// try {

		// select operation
		if (args[0].equals("-t") && args.length == 4) {
			System.out.println("=====================\nTRAIN " + openingMessage
					+ "\n=====================");
			train(args[1], args[2], args[3]);
		} else if (args[0].equals("-f") && args.length == 6) {
			System.out
					.println("==============================\nTRAIN-EVALUATE "
							+ openingMessage
							+ "\n==============================");
			fullExperiment(args[1], args[2], args[3], args[4], args[5]);
		} else if (args[0].equals("-te") && args.length == 5) {
			System.out
					.println("==============================\nTRAIN-EVALUATE REPRESENTATIONS\n==============================");
			trainEvaluate(args[1], args[2], args[3], args[4]);
		}
		// else if (args[0].equals("-p") && args.length == 9){
		// System.out.println("=======\nPREDICT\n=======");
		// String weightFiles[]={args[4], args[5], args[6], args[7],
		// args[8]};
		// predict(args[1], args[2], args[3], weightFiles);
		// }
		else {
			System.out.println(use);
			return;
		}

		// } catch (Exception e) {
		// System.out.println(use);
		// }

		// print time
		long endTime = System.currentTimeMillis();
		System.out.println("\nStarting time: " + new Date(startTime));
		System.out.println("Ending time: " + new Date(endTime));
		long milliSec = endTime - startTime;
		int hours = (int) (milliSec / 3600000);
		int min = (int) (milliSec / 60000 - hours * 60);
		int sec = (int) (milliSec / 1000 - hours * 3600 - min * 60);
		System.out.println("Elapsed time: " + hours + " hours, " + min
				+ " min, " + sec + " s");
	}

	// private static void predict(String proj, String testFile, String
	// featureScripFile, String[] weightFiles) {
	// int columnID=Utility.getColumnFromScript(featureScripFile);
	//
	// //generate 5 chunk representations for test file
	// System.out.println("\nGenerating chunk representation of the test
	// file:");
	// GenerateChunkRepresentations.generateChunkRepresentations(testFile,
	// Utility.getColumnFromScript(featureScripFile));
	//
	// //executing 5 predictions
	// for(int i=0;i<chunkRepNames.length;i++){
	// System.out.println("\n\n===========\nPREDICT "+chunkRepNames[i]+"\n");
	//
	// BTagger.predict(proj+"_"+chunkRepNames[i],testFile,weightFiles[i],featureScripFile);
	//
	// //convert all the tagged to O+C format
	// ConvertChunkRepresentation.convertChunkRepresentation(proj+"_"+chunkRepNames[i]+"Tagged.txt",columnID,proj+"_Tagged"+(i+1),5);
	// }
	//
	// //join 5 solutions
	// CombineChunks5Solutions.combineSolutions5(3, columnID, proj+"_Tagged1",
	// proj+"_Tagged2", proj+"_Tagged3", proj+"_Tagged4", proj+"_Tagged5",
	// proj+"_Combined");
	//
	// //evaluate
	// double res=BTagger.evaluate(proj, testFile+"_O+C",
	// proj+"_Combined",featureScripFile);
	//
	// System.out.println(res);
	// }

	private static void fullExperiment(String projName, String trainFile,
			String testfile, String featureScriptFile,
			String featureScripFileWeghits)
	{
		System.out
				.println("//////////////////////////////////\n//TRAIN EVALUATE start");
		double trainEvaluateResults[][] = trainEvaluate(projName, trainFile,
				testfile, featureScriptFile);
		System.out
				.println("//////////////////////////////////\n//TRAIN EVALUATE end");

		// column ID of the main tag
		int clumnID = Utility.getColumnFromScript(featureScriptFile);

		// method to get the indexes of the table with highest score
		int idexesHighestScores[] = getIndexesHighestScores(trainEvaluateResults);

		// for each index write prediction and get 4 files to merge
		System.out
				.println("//////////////////////////////////\n//WRITE PREDICTIONS start");
		String filesToMerge[] = new String[idexesHighestScores.length];
		for (int j = 0; j < idexesHighestScores.length; j++) {
			String predProjName = projName + "_Prediction_" + chunkRepNames[j];
			filesToMerge[j] = predProjName + "Tagged.txt";
			new BTagger().predict(predProjName, testfile + "_" + chunkRepNames[j],
					projName + "_" + chunkRepNames[j] + "."
							+ idexesHighestScores[j] + ".fea",
					featureScriptFile);
			System.out.println("\nFILES TO MERGE: " + filesToMerge[j]);
		}
		System.out
				.println("//////////////////////////////////\n//WRITE PREDICTIONS end");

		// get weights
		// for uniform weighting
		double uniformWeights[] = { 1, 1, 1, 1 };
		// for cross validation
		System.out
				.println("//////////////////////////////////\n//COMPUTE CROSS VALIDATION WEIGHTS start");
		double crossValidationWeights[] = getCrossValidationWeights(trainFile,
				featureScripFileWeghits);

		for (double value : crossValidationWeights) {
			System.out.println("\nCROSS VALIDATION WEIGHT : " + value);
		}
		System.out
				.println("//////////////////////////////////\n//COMPUTE CROSS VALIDATION WEIGHTS end");

		// loop on representation to use as base
		System.out
				.println("//////////////////////////////////\n//VOTING ON LABELS start");
		for (int i = 0; i < chunkRepNames.length; i++) {
			
			String filesToMergeOnRepresentation[] = new String[chunkRepNames.length];
			// create base on the current representation for each output
			for (int j = 0; j < filesToMerge.length; j++) {
				filesToMergeOnRepresentation[j] = filesToMerge[j]
						+ "_ToMergeOnRepresentation_" + chunkRepNames[i];
				ConvertChunkRepresentation.convertChunkRepresentation(
						filesToMerge[j], Utility
								.getColumnFromScript(featureScriptFile),
						filesToMergeOnRepresentation[j], i + 1);
			}

			// out merged files names
			String outUniFile = trainFile + "_MergedUniforn_"
					+ chunkRepNames[i];
			String outCVFile = trainFile + "_MergedCV_" + chunkRepNames[i];

			// combine files to merge
			CombineLabels4Solutions.combineSolutions4(mainRepresentationID,
					clumnID, filesToMergeOnRepresentation, uniformWeights,
					outUniFile);
			CombineLabels4Solutions.combineSolutions4(mainRepresentationID,
					clumnID, filesToMergeOnRepresentation,
					crossValidationWeights, outCVFile);

			// evaluate
			double uniResult = new BTagger().evaluate(projName + "_EVAL_Uniform_"
					+ chunkRepNames[i], testfile + "_" + chunkRepNames[i],
					outUniFile, featureScriptFile);
			double cvResult = new BTagger().evaluate(projName + "_EVAL_CrossVal_"
					+ chunkRepNames[i], testfile + "_" + chunkRepNames[i],
					outCVFile, featureScriptFile);

			// print result
			System.out.println("\nRESULTS FOR " + chunkRepNames[i]
					+ ":\nUniform: " + uniResult + "\nCrossValidation: "
					+ cvResult);
		}
		System.out
				.println("//////////////////////////////////\n//VOTIN ON LABELS end");
	}

	private static double[] getCrossValidationWeights(String trainFile,
			String featureScripFileWeghits)
	{
		double weights[] = new double[chunkRepNames.length];
		// generate 4 chunk representations for training file
		System.out
				.println("\nGenerating chunk representation of the train file:");
		GenerateChunkRepresentations.generate4ChunkRepresentations(trainFile,
				Utility.getColumnFromScript(featureScripFileWeghits));

		for (int i = 0; i < weights.length; i++) {
			weights[i] = getCrossValidationWeight(trainFile + "_"
					+ chunkRepNames[i], featureScripFileWeghits);

		}
		return weights;
	}

	private static double getCrossValidationWeight(String corpusFile,
			String featureScripFileWeghits)
	{
		double sumWeights = 0;
		// method to break in 5 section the training
		List<List<String>> sectionsFilesNames = divideCorpus(corpusFile);
		List<String> testSectionsFilesNames = sectionsFilesNames.get(0);
		List<String> trainingSectionsFilesNames = sectionsFilesNames.get(1);

		// check number of elements
		if (testSectionsFilesNames.size() != crossValidationSegments
				|| trainingSectionsFilesNames.size() != crossValidationSegments) {
			throw new RuntimeException("Number of subsets of corpus incorrect");
		}

		// get the weights
		for (int i = 0; i < crossValidationSegments; i++) {
			String testFieleName = testSectionsFilesNames.get(i);
			String trainFieleName = trainingSectionsFilesNames.get(i);
			List<Double> results = new BTagger().trainEvaluate(testFieleName
					+ "CV-Weights-Compute", trainFieleName, testFieleName,
					featureScripFileWeghits);
			sumWeights += results.get(results.size() - 1);
		}

		double averageWeights = sumWeights / (crossValidationSegments);
		return averageWeights;
	}

	public static List<List<String>> divideCorpus(String corpusFile)
	{
		Sentences fullCorpus = new Sentences();
		try {
			fullCorpus = new Sentences(corpusFile);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
		List<List<Sentences>> subSetsWithComplements = fullCorpus
				.splitWithComplements(crossValidationSegments);
		List<Sentences> subSets = subSetsWithComplements.get(0);
		List<Sentences> complementsSets = subSetsWithComplements.get(1);

		// save subsets
		List<String> subSetsFilesNames = new ArrayList<String>();
		int index = 1;
		for (Sentences sentences : subSets) {
			String subSetFileName = corpusFile + "_SubSet-" + index;
			subSetsFilesNames.add(subSetFileName);
			sentences.saveFile(subSetFileName);
			System.out.println("\nOUT:  sub-set file saved in: "
					+ subSetFileName);
			index++;
		}

		// save complements
		List<String> complementSetsFilesNames = new ArrayList<String>();
		index = 1;
		for (Sentences sentences : complementsSets) {
			String compSetFileName = corpusFile + "_CompSet-" + index;
			complementSetsFilesNames.add(compSetFileName);
			sentences.saveFile(compSetFileName);
			System.out.println("\nOUT:  compelmentary set file saved in: "
					+ compSetFileName);
			index++;
		}

		// create return object
		List<List<String>> setsFilesNames = new ArrayList<List<String>>();
		setsFilesNames.add(subSetsFilesNames);
		setsFilesNames.add(complementSetsFilesNames);
		return setsFilesNames;
	}

	private static int[] getIndexesHighestScores(double[][] valuesMatrix)
	{
		int idexesHighestScores[] = new int[valuesMatrix.length];
		for (int i = 0; i < valuesMatrix.length; i++) {
			idexesHighestScores[i] = getIndexeHighestScore(valuesMatrix[i]);
		}
		return idexesHighestScores;
	}

	private static int getIndexeHighestScore(double[] valueVector)
	{
		int index = -1;
		double bestValue = Double.MIN_VALUE;
		for (int i = 0; i < valueVector.length; i++) {
			if (bestValue < valueVector[i]) {
				bestValue = valueVector[i];
				index = i;
			}
		}
		System.out.println("\nINDEX BEST VALUE: " + index);
		return index;

	}

	public static void train(String proj, String trainFile,
			String featureScripFile)
	{

		// generate 4 chunk representations for training file
		System.out
				.println("\nGenerating chunk representation of the train file:");
		GenerateChunkRepresentations.generate4ChunkRepresentations(trainFile,
				Utility.getColumnFromScript(featureScripFile));

		// executing 4 training
		for (int i = 0; i < chunkRepNames.length; i++) {
			System.out.println("\n\n==========\nTRAIN " + chunkRepNames[i]
					+ "\n");
			new BTagger().train(proj + "_" + chunkRepNames[i] + "", trainFile + "_"
					+ chunkRepNames[i] + "", featureScripFile);
		}

	}

	public static double[][] trainEvaluate(String proj, String trainFile,
			String testFile, String featureScriptFile)
	{
		// generate 4 chunk representations for training file
		System.out
				.println("\nGenerating chunk representation of the train file:");
		GenerateChunkRepresentations.generate4ChunkRepresentations(trainFile,
				Utility.getColumnFromScript(featureScriptFile));

		// generate 4 chunk representations for test file
		System.out
				.println("\nGenerating chunk representation of the test file:");
		GenerateChunkRepresentations.generate4ChunkRepresentations(testFile,
				Utility.getColumnFromScript(featureScriptFile));

		// get the number of rounds
		String maxRound = Utility.getParamFromScript(featureScriptFile,
				"MAXROUND");
		int rounds = 0;
		if (maxRound.compareTo("") == 0) {
			rounds = new Context(FileUtils.getReader(featureScriptFile)).MAXROUND;//TODO repleace the getParamFromScript with this line
		} else {
			rounds = Integer.parseInt(maxRound);
		}

		// store F-metrics
		double metrics[][] = new double[4][rounds];

		// for each chunk representation
		for (int i = 0; i < chunkRepNames.length; i++) {
			boolean needTrain = false;

			for (int j = 0; j < rounds; j++) {
				File weightsFile = new File(proj + "_" + chunkRepNames[i] + "."
						+ j + ".fea");
				if (!weightsFile.exists()) {
					needTrain = true;
					break;
				}
			}

			if (needTrain) {
				// execute the training
				System.out.println("\n\n==========\nTRAIN " + chunkRepNames[i]
						+ "\n");
				new BTagger().train(proj + "_" + chunkRepNames[i], trainFile + "_"
						+ chunkRepNames[i] + "", featureScriptFile);
			} else {
				System.out.println("\n\n==========\nTRAIN " + chunkRepNames[i]
						+ "\n");
				System.out.println("weight files found\n");
			}

			// evaluate for each round
			for (int j = 0; j < rounds; j++) {
				metrics[i][j] = new BTagger().predictEvaluate(proj, testFile + "_"
						+ chunkRepNames[i], proj + "_" + chunkRepNames[i] + "."
						+ j + ".fea", featureScriptFile);
			}

		}

		Graph graph = new Graph(metrics, chunkRepNames);
		graph.print(proj + "_graph");

		return metrics;

	}

}
