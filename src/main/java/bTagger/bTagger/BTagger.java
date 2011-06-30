package bTagger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Bidirectional tagging with Guided Learning
 * 
 * @author Andrea Gesmundo
 */
public class BTagger
{

	public TagSamples train = new TagSamples();

	public TagSamples untagged = new TagSamples();

	public TagSamples gold = new TagSamples();

	public TagSamples tagged = new TagSamples();

	final static public String documentStartString = "-DOCSTART-";

	public static void main(String[] args)
	{
		final String usage = "Train: java BTagger -t <proj_ID> <training file> <feature script>"
			+ "\nPredict: java BTagger -p <proj_ID> <test file> <weights> <feature script>"
			+ "\nEvaluate: java BTagger -e <proj_ID> <gold file> <prediction> <feature script>"
			+ "\nPredict & Evaluate: java BTagger -pe <proj_ID> <gold file> <weights> <feature script>"
			+ "\nTrain & Evaluate: java BTagger -te <proj_ID> <training file> <gold file> <feature script>"
			+ "\nReorder train: java BTagger -r <proj_ID> <training file> <feature script>"
			+ "\nFeatures Selection: java BTagger -s <proj_ID> <training file> <gold file> <feature script> <tag name>";

		if (args.length == 0) {
			System.out.println(usage);
			return;
		}

		long startTime = System.currentTimeMillis();
		System.out.println("\nStarting time: " + new Date(startTime));

		if (args[0].equals("-t") && args.length == 4) {
			new BTagger().train(args[1], args[2], args[3]);
		} else if (args[0].equals("-p") && args.length == 5) {
			new BTagger().predict(args[1], args[2], args[3], args[4]);
		} else if (args[0].equals("-e") && args.length == 5) {
			new BTagger().evaluate(args[1], args[2], args[3], args[4]);
		} else if (args[0].equals("-pe") && args.length == 5) {
			new BTagger().predictEvaluate(args[1], args[2], args[3], args[4]);
		} else if (args[0].equals("-te") && args.length == 5) {
			new BTagger().trainEvaluate(args[1], args[2], args[3], args[4]);
		} else if (args[0].equals("-r") && args.length == 4) {
			new BTagger().reorderTrain(args[1], args[2], args[3]);
			return;
		} else if (args[0].equals("-s") && args.length == 6) {
			new BTagger().researchFeatures(args[1], args[2], args[3], args[4], args[5]);
			return;
		} else {
			System.out.println(usage);
			return;
		}

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

	/**
	 * To reorder the training data It is run offline
	 * 
	 * @param prev
	 *            File containing the training set to reorder.
	 * @param seed
	 *            Seed prime number used to generate the new order.
	 * @author andrea 07-07-09 the previous code reordered TD in an uncorrect
	 *         way.
	 */
	public void reorderTrain(String seed, String prev,
			String featureScriptFile){
		System.out.println("-------------\nREORDER TRAIN\n-------------");

		LabelLib labels= new LabelLib();

		Context context = new Context(FileUtils.getReader(featureScriptFile));

		int prime = Integer.parseInt(seed);
		// java.util.Random rnd=new Random();
		// int primeNumbers[] =
		// {29,71,113,173,229,281,349,409,463,541,601,659,733,809,863,941,1013,1069};
		// prime = primeNumbers[rnd.nextInt(17)];
		// prime=rnd.nextInt(100000);
		loadTrainingData(prev,labels, context);
		int sizePlusOne = train.size() + 1;

		if (sizePlusOne % prime == 0 || prime % sizePlusOne == 0) {
			System.err.println("Bad pair (" + sizePlusOne + "," + prime + ")");
			System.err
			.println("Insert a number not multiple of " + sizePlusOne);
			return;
		} else {
			System.err.println("Good pair (" + sizePlusOne + "," + prime + ")");
		}

		boolean read[] = new boolean[train.size()];
		int id = 0;
		for (int i = 0; i < train.size(); i++) {
			id = (((i + 1) * prime) % sizePlusOne);
			if (id > 0)
				id--;
			TagSample sample = train.get(id);

			System.out.println(sample.displayConll(context)); // display in Conll
			// format

			read[id] = true;
		}

		for (int i = 0; i < read.length; i++) {
			if (read[i] == false) {
				System.err.println("" + i + " is missing");
			}
		}
	}

	/**
	 * Learn the weight vector
	 */
	public void train(String projName, String trnfile,
			String featureScriptFile){
		System.out.println("-----\nTRAIN\n-----");
		LabelLib labels= new LabelLib();

		String proj = projName;

		System.out.println("Opening Feature Script File: "+featureScriptFile);
		Context context = new Context(FileUtils.getReader(featureScriptFile));
		loadTrainingData(trnfile,labels, context);		

		FeatLib feat = new FeatLib();

		labels.loadLexicon(context.LEXICON);

		TagLearn learner = new TagLearn(proj, feat,labels, context);
		learner.train(train);

		labels.saveLabels(proj + ".tag");
	}

	/**
	 * Execute prediction on a test file.
	 * 
	 * @param untaggedFile
	 *            The test file name.
	 * @param weightfile
	 *            The feature file name.
	 * @param labelfile
	 *            The label file name.
	 */
	public String predict(String projName, String untaggedFile,
			String weightfile, String featureScriptFile){
		System.out.println("-------\nPREDICT\n-------");
		LabelLib labels= new LabelLib();

		String proj = projName;

		System.out.println("Opening Feature Script File: "+featureScriptFile);
		Context context = new Context(FileUtils.getReader(featureScriptFile));
		loadUntagged(untaggedFile, context);

		FeatLib feat = new FeatLib();
		loadFeatTable(weightfile,feat,labels);

		labels.loadLexicon(context.LEXICON);

		TagLearn learner = new TagLearn(proj, feat,labels,context);
		TagSample.saveTagged(learner.predict(untagged), proj + "Tagged.txt",context);
		return proj + "Tagged.txt";
	}

	/**
	 * Evaluation
	 */
	public double evaluate(String projName, String goldFile,
			String taggedFile, String featureScriptFile){
		System.out.println("--------\nEVALUATE\n--------");
		LabelLib labels= new LabelLib();

		String proj = projName;

		System.out.println("Opening Feature Script File: "+featureScriptFile);
		Context context = new Context(FileUtils.getReader(featureScriptFile));
		FeatLib feat = new FeatLib();

		loadGoldStandard(goldFile,labels, context);
		loadTagged(taggedFile,labels, context);

		labels.loadLexicon(context.LEXICON);

		TagLearn learner = new TagLearn(proj, feat,labels,context);
		return learner.evaluate(tagged, gold);
	}

	/**
	 * Prediction & Evaluation
	 */
	public double predictEvaluate(String projName, String goldFile,
			String weightsFile, String featureScriptFile){
		System.out.println("----------------\nPREDICT-EVALUATE\n----------------");
		LabelLib labels= new LabelLib();

		String proj = projName;

		System.out.println("Opening Feature Script File: "+featureScriptFile);
		Context context = new Context(FileUtils.getReader(featureScriptFile));
		loadGoldStandard(goldFile,labels, context);


		FeatLib feat = new FeatLib();
		loadFeatTable(weightsFile,feat,labels);

		labels.loadLexicon(context.LEXICON);

		TagLearn learner = new TagLearn(proj, feat,labels, context);
		return learner.predictEvaluate(gold);
	}

	/**
	 * Train and Evaluate
	 */
	public List<Double> trainEvaluate(String projName, String trnfile,
			String goldfile, String featureScriptFile){
		System.out
		.println("--------------\nTRAIN-EVALUATE\n--------------");
		LabelLib labels= new LabelLib();

		String proj = projName;

		System.out.println("Opening Feature Script File: "+featureScriptFile);
		Context context = new Context(FileUtils.getReader(featureScriptFile));
		loadTrainingData(trnfile,labels, context);
		loadGoldStandard(goldfile,labels, context);

		FeatLib feat = new FeatLib();

		labels.loadLexicon(context.LEXICON);

		TagLearn learner = new TagLearn(proj, feat ,labels,context);
		List<Double> results = learner.trainEval(gold, train);

		labels.saveLabels(proj + ".tag");

		return results;
	}

	/**
	 * Research new features
	 */
	public void researchFeatures(String projName, String trnfile,
			String goldfile, String featureScriptFile, String resTag)
	{
		System.out.println("-----------------\nFEATURES SELECTION\n-----------------");

		LabelLib labels= new LabelLib();

		String proj = projName;

		System.out.println("Opening Feature Script File: "+featureScriptFile);
		Context context = new Context(FileUtils.getReader(featureScriptFile));
		context.researchTag = resTag;//TODO put this parameter in the script?
		System.out.println("research target: " + resTag);

		loadTrainingData(trnfile,labels, context);
		loadGoldStandard(goldfile,labels, context);

		FeatLib feat = new FeatLib();

		labels.loadLexicon(context.LEXICON);

		TagLearn learner = new TagLearn(proj, feat, labels, context);
		learner.dispEval = false;
		learner.researchFeat(gold, train);

		// DELETE ME!feat.saveWeight(proj+".fea", learner.inner);
		// DELETE ME!LabelLib.saveLabels(proj+".tag");
	}


	//	//////////////////////////////////////////////////////////////
	//	LOAD
	//	TODO create wrapper classes for the files
	//	TODO load and write specifying the charset

	/**
	 * Load the Training Data.
	 * 
	 * @param file
	 *            The file containing the training data.
	 */
	public void loadTrainingData(String file, LabelLib labels, Context context)
	{
		train.clear();
		System.out.println("TRAIN FILE:\n\t" + file);
		loadData(file, train, false, labels, context);
	}

	/**
	 * Load the gold standard data
	 */
	public void loadGoldStandard(String file, LabelLib labels, Context context){
		gold.clear();
		System.out.println("GOLDSTANDARD FILE:\n\t" + file);
		loadData(file, gold, false, labels, context);
	}

	/**
	 * Load the test data
	 */
	public void loadUntagged(String file, Context context){
		untagged.clear();
		System.out.println("UNTAGGED FILE:\n\t" + file);
		loadData(file, untagged, true, null, context);
	}

	/**
	 * Load the tagged data
	 */
	public void loadTagged(String file, LabelLib labels, Context context){
		tagged.clear();
		System.out.println("TAGGED FILE:\n\t" + file);
		loadData(file, tagged, false, labels, context);
	}



	/**
	 * Load data in CoNLL's style. One word per data, followed by tags
	 * 
	 * @param file
	 *            Name of the file where there are the data to load.
	 * @param samples
	 *            List where are stored the data.
	 */
	public void loadData(String file, TagSamples samples,
			boolean isUntagged, LabelLib labels, Context context){

		try {
			BufferedReader in = new BufferedReader(FileUtils.getReader(file));

			String line = in.readLine();
			ArrayList<String> fileFormat = new ArrayList<String> (context.format);

			//remove the main tag symbol from the file format if the file is untagged
			//			if (isUntagged){
			//				for (int i = 0; i < fileFormat.size(); i++){
			//					if (fileFormat.get(i).endsWith("!")){
			//						fileFormat.remove(i);
			//					}
			//				}
			//			}

			ArrayList<String> word = new ArrayList<String>();
			ArrayList<Label> mainTags = new ArrayList<Label>();

			// count the number of additional tag sets
			int count = 0;
			for (int j = 0; j < fileFormat.size(); j++) {
				if (fileFormat.get(j).compareToIgnoreCase("W") != 0
						&& !fileFormat.get(j).endsWith("!"))
					count++;
			}

			List<List<String>> addTags = new ArrayList<List<String>>(count);
			for (int j = 0; j < count; j++){
				addTags.add(new ArrayList<String>());
			}

			while (line != null) {
				line = line.trim();

				//catch document start

				if(line.startsWith(documentStartString)){
					if(context.DOCUMENTFEAT){
						samples.addDocumentStartID();
						line="";				
					}
					else{
						System.err.println("FOUND DOCUMENT DIVIDER, BUT DOCUMENT FEATURES ARE DISABLED");
						//						System.err.println("SENTENCE REMOVED!");
						//						line="";
					}
				}

				// if empty line store the finished sentence
				if (line.equals("")) {
					if (word.size() > 0) {
						TagSample newone = new TagSample(word, mainTags,
								addTags);
						samples.add(newone);
						word.clear();
						mainTags.clear();
						for (int j = 0; j < addTags.size(); j++) {
							addTags.get(j).clear();
						}
					}
				} else { // otherwise extract the word and the tag and store
					// in a tmp buffer
					String[] arr = line.split("\\s+");
					int y = 0;
					try{
						int i=0;
						for (String formatID : fileFormat) {
							if (formatID.compareToIgnoreCase("W") == 0) {
								word.add(arr[i++]);
							} else if (formatID.endsWith("!")) {
								if(!isUntagged){
									mainTags.add(labels.getLabel(arr[i++]));
								}
								else{
									if (arr.length>=fileFormat.size()){
										i++;
									}
								}
							} else {
								addTags.get(y++).add(arr[i++]);
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e){
						throw new RuntimeException("Input file malformed at line: \n"+line);
					}
				}

				line = in.readLine();
			}
			// store the last sentence
			if (word.size() > 0) {
				TagSample newone = new TagSample(word, mainTags, addTags);
				samples.add(newone);
			}

			in.close();
		} catch (FileNotFoundException e) {
			System.err.println(e.toString());
		} catch (IOException e) {
			System.err.println(e.toString());
		}

		System.out.println("\t\t" + samples.size()
				+ " sentences loaded in this file");

		if(context.DOCUMENTFEAT){
			System.out.println("\t\t"+ samples.documentsNumber()+ " document loaded in this file");

			//compile document feat gazetteers
			samples.documentGazetteers = new ArrayList<List<DocumentGazetteers>>();

			List <DocumentGazetteers> dgs = new ArrayList<DocumentGazetteers>(context.DOCUMENTCOLUMNS.size());
			for(int k=0;k<context.DOCUMENTCOLUMNS.size();k++){
				dgs.add(new DocumentGazetteers(context.DOC_GAZETTEERS_CASESENSITIVE_MAINWORD, context.DOC_GAZETTEERS_CASESENSITIVE_CONTEXTWORDS));
			}

			//iterate sentences
			for (int i = 0; i < samples.size(); i++) {
				if(i!=0 && samples.documentsStartID.contains(i)){
					samples.documentGazetteers.add(dgs);
					dgs = new ArrayList<DocumentGazetteers>(context.DOCUMENTCOLUMNS.size());
					for(int k=0;k<context.DOCUMENTCOLUMNS.size();k++){
						dgs.add(new DocumentGazetteers(context.DOC_GAZETTEERS_CASESENSITIVE_MAINWORD, context.DOC_GAZETTEERS_CASESENSITIVE_CONTEXTWORDS));
					}
				}
				//iterate columns
				for(int h=0;h<context.DOCUMENTCOLUMNS.size();h++){
					int indexColumn = context.addTagsNames.indexOf(context.DOCUMENTCOLUMNS.get(h));
					dgs.get(h).addEntries(samples.get(i).words, samples.get(i).addTags[indexColumn]);
				}
			}
			//add last doc
			samples.documentGazetteers.add(dgs);
			dgs = new ArrayList<DocumentGazetteers>(context.DOCUMENTCOLUMNS.size());
			for(int k=0;k<context.DOCUMENTCOLUMNS.size();k++){
				dgs.add(new DocumentGazetteers(context.DOC_GAZETTEERS_CASESENSITIVE_MAINWORD, context.DOC_GAZETTEERS_CASESENSITIVE_CONTEXTWORDS));
			}
		}



	}

	/**
	 * Load a feature table from a file.
	 * 
	 * @param featFile 	Feature file name.
	 */
	public void loadFeatTable(String featFile, FeatLib features, LabelLib labels){//TODO create wrapper class for file     
		System.out.println("FEATURE TABLE:\n\t"+featFile);
		loadFeatTable( FileUtils.getReader(featFile),  features,  labels);
	}

	/**
	 * Load a feature table from a file.
	 * 
	 * @param reader, it will be closed after the execution.
	 */
	public void loadFeatTable(Reader reader, FeatLib features, LabelLib labels){//TODO create wrapper class for file
		try {
			BufferedReader in = new BufferedReader ( reader);             

			String line = in.readLine();
			String featureName;
			while ( line != null) {
				String[] arr = line.split(" ");		
				//record label
				featureName=arr[arr.length-2];
				labels.getLabelID(featureName.substring(0,featureName.indexOf('|')));
				//add the feature
				int id = features.regFeat(featureName);
				//set the weight
				features.setWeight(id, Double.parseDouble(arr[arr.length-1]));

				line = in.readLine();
			} // end of each line

			in.close();
		} catch (FileNotFoundException e){
			System.err.println(e.toString());
		} catch (IOException e) {
			System.err.println(e.toString());
		}

		features.trimToSize();
		System.out.println("\t\t" + features.size() + " features loaded");
		System.out.println("\t\t" + labels.getSize()+" labels loaded");
	}


	//	//////////////////////////////////////////////////////////////

} // end of class BTagger

