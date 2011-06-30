package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import votingRepresentations.VotingChunksRepresentations;

import bTagger.BTagger;

import corpus.Sentences;

public class ExperimentChunkingVotingDocFreatures
{
	public static void main(String args[]){
		String use = "\nFull Experiment Train Evaluate: java VotingChunksRepresentations -fte <projID> <train_file> <test_file> <base_feature_script> <document_feature_script>"
			+ "\nFull Experiment Train Predict: java VotingChunksRepresentations -ftp <projID> <train_file> <test_untagged_file> <base_feature_script> <document_feature_script>";

		if (args.length == 0) {
			System.out.println(use);
			return;
		}
		String openingMessage=  "CHUNIKIKING WITH VOTING ON REPRESENTATION AND DOCUMENT FEATURES";

		// save start time
		long startTime = System.currentTimeMillis();
		System.out.println("\nStarting time: " + new Date(startTime));
		try {

			// select operation
			if (args[0].equals("-fte") && args.length == 6) {
				System.out.println("=======\nFULL EXPERIMENT TRAIN EVALUATE " + openingMessage
						+ "\n=======");
				fullExperiment(args[1], args[2], args[3], args[4], args[5],true);
			} else if (args[0].equals("-ftp") && args.length == 6) {
				System.out.println("=======\nFULL EXPERIMENT TRAIN PREDICT " + openingMessage
						+ "\n=======");
				fullExperiment(args[1], args[2], args[3], args[4], args[5],false);

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

	private static void fullExperiment(String proj, String trainFile,
			String testFile, String baseFeatureScriptFile, String docFeatureScriptFile, boolean evaluate)
	{

		//create train with DC
		String trainWithDC=trainFile+"-withDC-Tagged.txt";
		if(!new File(trainWithDC).exists()){
			System.out.println("=========CREATE TRAIN WITH DC=======");
			//read corpus files
			Sentences trainSet;
			try{
				trainSet= new Sentences(trainFile);
			}catch (FileNotFoundException e){
				e.printStackTrace();
				return;
			}

			//cross validation on train to create DC column on train
			Sentences docFeatTrainSet= new CrossValidationPrediction().execute(trainFile,baseFeatureScriptFile, 5);

			docFeatTrainSet.addColumn(trainSet.getColumn(Utility.getColumnFromScript(baseFeatureScriptFile)));
			docFeatTrainSet.saveFile(trainWithDC);
			System.out.println("OUT: FINAL CV PREDICTED MERGED TRAIN FILE SAVED IN : "+ trainWithDC);
		}
		else{
			System.out.println("TRAIN-DC ALREADY EXISTS, skipping tagging\n"+trainWithDC );
		}

		//create test with DC
		String testWithDC=testFile+"-withDC-Tagged.txt";
		if(!new File(testWithDC).exists()){
			System.out.println("=========CREATE TEST WITH DC=======");
			new BTagger().train(proj+"_TestDC", trainFile, baseFeatureScriptFile);
			int roundId = Utility.getMaxRound(baseFeatureScriptFile)-1;
			new BTagger().predict(testFile+"-withDC-", testFile, proj+"_TestDC."+roundId+".fea", baseFeatureScriptFile);

			if(evaluate){
				//paste gold column column
				try{
					Sentences taggedTestSet= new Sentences(testWithDC);
					Sentences goldTestSet = new Sentences(testFile);
					taggedTestSet.addColumn(goldTestSet.getColumn(goldTestSet.getColumnsNumber()-1));
					taggedTestSet.saveFile(testWithDC);
				}catch (FileNotFoundException e){
					e.printStackTrace();
					return;
				}

			}

			System.out.println("OUT: FINAL CV PREDICTED MERGED TEST FILE SAVED IN : "+ testWithDC);
		}
		else{
			System.out.println("TEST-DC ALREADY EXISTS, skipping tagging"+testWithDC);
		}

		//tag with voting system
		{
			if (evaluate){
				new VotingChunksRepresentations().fullExperimentTrainEvaluate(proj+"_voting", trainWithDC, testWithDC, docFeatureScriptFile );
			}else{
				new VotingChunksRepresentations().fullExperimentTrainPredict(proj+"_voting", trainWithDC, testWithDC, docFeatureScriptFile );

			}

		}
		

	}

}
