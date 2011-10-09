package utility;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bTagger.BTagger;
import corpus.Sentences;


public class CrossValidationPrediction
{
	public static void main(String[] args){
		if(args.length!=3 && args.length!=4){
			System.out.println("java CrossValidation <train_file> <feature_script_file> <segmets> [<BOOL is_split_documents>]");
			return;
		}

		String trainFileName= args [0] ;
		String featureScripFile= args[1];
		int crossValidationSegments=Integer.parseInt(args[2]);
		boolean splitDoc=false;
		if(args.length==4){
			splitDoc=Boolean.parseBoolean(args[3]);
		}

		String taggedFile= trainFileName+"-CV-TAGGED";
		new CrossValidationPrediction().execute(trainFileName,featureScripFile, crossValidationSegments, splitDoc).saveFile(taggedFile);
		System.out.println("OUT: FINAL CV PREDICTED MERGED SAVED IN : "+ taggedFile);
		System.out.println("!!!!!!! FINAL EVALUATION !!!!!!!");
		//<proj_ID> <gold file> <prediction> <feature script>
		double result = new BTagger().evaluate("FINAL_EVAL:", trainFileName, taggedFile, featureScripFile);

		try{
			PrintWriter out = new PrintWriter(new FileWriter("RESULT.txt"));
			out.println(result*100);
			out.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public Sentences execute(String trainFileName,String featureScriptFile, int crossValidationSegments, boolean splitDoc){

		List <List<String>> sectionsFilesNames=divideCorpus(trainFileName, crossValidationSegments,splitDoc);
		List<String> testSectionsFilesNames = sectionsFilesNames.get(0);
		List<String> trainingSectionsFilesNames = sectionsFilesNames.get(1);

		// check number of elements
		if (testSectionsFilesNames.size() != crossValidationSegments
				|| trainingSectionsFilesNames.size() != crossValidationSegments) {
			throw new RuntimeException("Number of subsets of corpus incorrect");
		}

		List<Sentences> outPredicted = new ArrayList<Sentences>();

		// train evaluate predict
		for (int i = 0; i < crossValidationSegments; i++) {
			String testFileName = testSectionsFilesNames.get(i);
			String trainFieleName = trainingSectionsFilesNames.get(i);
			List<Double> results = new BTagger().trainEvaluate(testFileName
					+ "-CV", trainFieleName, testFileName,
					featureScriptFile);
			int indexOfBestRound= results.indexOf( (Double)Collections.max(results));
			//int round=Utility.getMaxRound(featureScriptFile)-1;
			System.err.println(indexOfBestRound);
			new BTagger().predict(testFileName, testFileName, testFileName+"-CV."+indexOfBestRound+".fea", featureScriptFile);
			try{
				outPredicted.add(new Sentences(testFileName+"Tagged.txt"));
			}
			catch(FileNotFoundException e){
				System.out.println(e);
			}
		}
		
		return (new Sentences(outPredicted.toArray(new Sentences[outPredicted.size()])));

	}

	public static List<List<String>> divideCorpus(String corpusFile, int crossValidationSegments, boolean splitDoc)
	{
		Sentences fullCorpus = new Sentences();
		try {
			fullCorpus = new Sentences(corpusFile);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
		List<List<Sentences>> subSetsWithComplements;
		if (splitDoc){
			subSetsWithComplements= fullCorpus.splitWithComplementsOnDocs(crossValidationSegments);
		}
		else{
//			subSetsWithComplements= fullCorpus.splitWithComplements(crossValidationSegments);
			//use this so merged final output has same order as full corpus input
			//so we can call evaluate at end
			//if wanna shuffle use SuffleCorpus in utils in pre processing
			subSetsWithComplements= fullCorpus.splitWithComplementsKeepSequence(crossValidationSegments); 
		}
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

}
