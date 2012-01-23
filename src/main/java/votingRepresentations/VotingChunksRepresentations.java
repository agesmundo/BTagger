package votingRepresentations;

import java.io.File;
import java.util.Date;

import utility.ConvertChunkRepresentation;
import utility.GenerateChunkRepresentations;
import utility.Graph;
import utility.Utility;
import bTagger.BTagger;

/**
 * 
 * @author Andrea Gesmundo
 */
public class VotingChunksRepresentations
{
	public static final String[] chunkRepNames = { "IOB1", "IOB2", "IOE1",
		"IOE2", "O+C" };

	public static void main(String args[])
	{
		String use = "Train: java VotingChunksRepresentations -t <proj_ID> <train_file> <feature script>"
			+ "   \nTrain Evaluate: java VotingChunksRepresentations -te <proj_ID> <train_file> <test_file> <feature script>"
			+ "	\nPredict: java VotingChunksRepresentations -p <proj_ID> <test_file> <feature_script> <features IOB1> <features IOB2> <features IOE1> <features IOE2> <features O+C>"
			+ "\nFull Experiment Train Evaluate: java VotingChunksRepresentations -fte <projID> <train_file> <test_file> <feature_script>"
			+ "\nFull Experiment Train Predict: java VotingChunksRepresentations -ftp <projID> <train_file> <test_untagged_file> <feature_script>"

			;

		if (args.length == 0) {
			System.out.println(use);
			return;
		}

		// save start time
		long startTime = System.currentTimeMillis();
		System.out.println("\nStarting time: " + new Date(startTime));
		String openingMessage = "CHUNKS-VOTING";

		try {

			// select operation
			if (args[0].equals("-t") && args.length == 4) {
				System.out.println("=====================\nTRAIN "
						+ openingMessage + "\n=====================");
				new VotingChunksRepresentations().train(args[1], args[2], args[3]);
			} else if (args[0].equals("-te") && args.length == 5) {
				System.out
				.println("==============================\nTRAIN-EVALUATE "
						+ openingMessage
						+ "\n==============================");
				new VotingChunksRepresentations().trainEvaluate(args[1], args[2], args[3], args[4]);
			} else if (args[0].equals("-pe") && args.length == 9) {
				System.out.println("=======\nPREDICT " + openingMessage
						+ "\n=======");
				String weightFiles[] = { args[4], args[5], args[6], args[7],
						args[8] };
				new VotingChunksRepresentations().predictEvaluate(args[1], args[2], args[3], weightFiles);
			} else if (args[0].equals("-fte") && args.length == 5) {
				System.out.println("=======\nFULL EXPERIMENT TRAIN EVALUATE " + openingMessage
						+ "\n=======");
				new VotingChunksRepresentations().fullExperimentTrainEvaluate(args[1], args[2], args[3], args[4]);
			} else if (args[0].equals("-ftp") && args.length == 5) {
				System.out.println("=======\nFULL EXPERIMENT TRAIN PREDICT" + openingMessage
						+ "\n=======");
				new VotingChunksRepresentations().fullExperimentTrainPredict(args[1], args[2], args[3], args[4]);

			} else {
				System.out.println(use);
				return;
			}

		} catch (NumberFormatException e) {
			System.out.println(use);
		}

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

	private void predictEvaluate(String proj, String testFile,
			String featureScriptFile, String[] weightFiles, boolean evaluate)
	{
		int columnID = Utility.getColumnFromScript(featureScriptFile);

		// executing 5 predictions
		for (int i = 0; i < chunkRepNames.length; i++) {
			System.out.println("\n\n===========\nPREDICT " + chunkRepNames[i]
			                                                               + "\n");

			new BTagger().predict(appendFormatName(proj,i), testFile,
					weightFiles[i], featureScriptFile);

			// convert all the tagged to O+C format
			ConvertChunkRepresentation.convertChunkRepresentation(proj + "_"
					+ chunkRepNames[i] + "Tagged.txt", columnID, proj
					+ "_Tagged" + (i + 1), 5);
		}

		//loop on the merge score threshold  
		double res[]= new double [chunkRepNames.length];
		for (int k=0;k<res.length;k++){

			System.out.println("\n\n===========\nMERGE WITH "+(k+1)+" VOTES\n");
			// join 5 solutions
			CombineChunks5Solutions.combineSolutions5((k+1), columnID, proj
					+ "_Tagged1", proj + "_Tagged2", proj + "_Tagged3", proj
					+ "_Tagged4", proj + "_Tagged5", proj + "_Combined_"+(k+1));


			if(evaluate){
				// evaluate
				res[k] = new BTagger().evaluate(proj+"_"+(k+1), testFile , proj
						+ "_Combined_"+(k+1), featureScriptFile);
			}
		}

		if(evaluate){
			//print results
			System.out.println("\n\nRESULTS:\n\nvotes number - F1 score");
			for (int k=0;k<res.length;k++){
				System.out.println((k+1)+ " :  "+res[k]);
			}
		}
	}

	private void predict(String proj, String testFile,
			String featureScriptFile, String[] weightFiles)
	{
		predictEvaluate(proj, testFile,
				featureScriptFile, weightFiles, false);
	}
	
	private void predictEvaluate(String proj, String testFile,
			String featureScriptFile, String[] weightFiles)
	{
		predictEvaluate(proj, testFile,
				featureScriptFile, weightFiles, true);
	}
	
	public void train(String proj, String trainFile,
			String featureScriptFile)
	{
		// get the number of rounds
		 int rounds = Utility.getMaxRound(featureScriptFile);
		 
		// generate 5 chunk representations for training file
		System.out
		.println("\nGenerating chunk representation of the train file:");
		GenerateChunkRepresentations.generate5ChunkRepresentations(trainFile,
				Utility.getColumnFromScript(featureScriptFile));

		// executing 5 training
		for (int i = 0; i < chunkRepNames.length; i++) {
			
			boolean needTrain = false;

			for (int j = 0; j < rounds; j++) {
				File weightsFile = new File(getFeatFileName(i,j,proj));
				if (!weightsFile.exists()) {
					needTrain = true;
					break;
				}
			}
			
			System.out.println("\n\n==========\nTRAIN " + chunkRepNames[i] + "\n");
			if (needTrain) {
				// execute the training
				new BTagger().train(appendFormatName(proj ,i), appendFormatName(trainFile,i), featureScriptFile);
			} else {

				System.out.println("weight files found\n");
			}
			
		}

	}

	public double[][] trainEvaluate(String proj, String trainFile,
			String testFile, String featureScriptFile)
	{
		// generate 5 chunk representations for test file
		System.out
		.println("\nGenerating chunk representation of the test file:");
		GenerateChunkRepresentations.generate5ChunkRepresentations(testFile,
				Utility.getColumnFromScript(featureScriptFile));
		
		// get the number of rounds
		int rounds = Utility.getMaxRound(featureScriptFile);
		 
		// store F-metrics
		double metrics[][] = new double[5][rounds];

		//train
		train( proj,  trainFile, featureScriptFile);
		
		// for each chunk representation
		for (int i = 0; i < chunkRepNames.length; i++) {

			// evaluate for each round
			for (int j = 0; j < rounds; j++) {
				metrics[i][j] = new BTagger().predictEvaluate(proj, appendFormatName(testFile,i), getFeatFileName(i,j,proj), featureScriptFile);
			}

		}

		Graph graph = new Graph(metrics, chunkRepNames);
		graph.print(proj + "_graph");

		return metrics;
	}

	public void fullExperimentTrainEvaluate(String proj, String trainFile,
			String testFile, String featureScriptFile)
	{
		double[][] metrics = trainEvaluate(proj,trainFile, testFile, featureScriptFile);
		int[]idexesHighestScores=  getIndexesHighestScores(metrics);

		//prepare weight file names
		String weightFiles[]=new String [chunkRepNames.length];
		for (int i=0;i<chunkRepNames.length;i++){
			weightFiles[i]=getFeatFileName(i,idexesHighestScores[i],proj);
		}

		predictEvaluate(proj, testFile, featureScriptFile, weightFiles);
	}

	public void fullExperimentTrainPredict(String proj, String trainFile,
			String testFile, String featureScriptFile)
	{
		train(proj,trainFile, featureScriptFile);
		int round=Utility.getMaxRound(featureScriptFile)-1;

		//prepare weight file names
		String weightFiles[]=new String [chunkRepNames.length];
		for (int i=0;i<chunkRepNames.length;i++){
			weightFiles[i]=getFeatFileName(i,round,proj);
		}

		predict(proj, testFile, featureScriptFile, weightFiles);
	}

	private static String appendFormatName(String file, int formatID){
		return file + "_"	+ chunkRepNames[formatID];
	}

	private static String getFeatFileName(int formatID, int roundID, String proj){
		return proj + "_" + chunkRepNames[formatID] + "."	+ roundID + ".fea";
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

}
