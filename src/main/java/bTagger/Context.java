package bTagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Store the values of the variables specified with the configuration script
 * 
 * @author Andrea Gesmundo
 *
 */
public class Context{

	/** use base features */
	public boolean BASEFEAT = true;
	/** use extended features */
	public boolean EXTENDFEAT = true;
	/** use additional features */
	public boolean ADDITIONALFEAT = true;
	/** use gazetteers */
	public boolean GAZETTEERS = false;
	/** use document based features*/
	public boolean DOCUMENTFEAT=false;
	/** use barrier features */
	public boolean BARRIERFEAT=false;
	/** which column is used to exctract doc feats*/
	public List<String> DOCUMENTCOLUMNS=new ArrayList<String>();
	/** use limit character of the sentence "@" */
	public boolean SENTENCE_LIMIT_CHAR = false;
	/** print the weights in a file each training round */
	public boolean PRINT_WEIGHTS_EACH_ROUND = true;
	/** We FIX to the Trigram model */
	public int NGRAM = 3;
	/** We employ k-best socket pairs */
	public int KSOCKET = 3;
	/** We employ k-best hypos for the same sockets training parameters */
	public int KHYPO = 3;
	/** */
	public double TAU = 0.03;
	/** */
	public double RADIUS = 40;
	/** */
	public double MARGIN_RATE = TAU * RADIUS * RADIUS;
	/** Max Loop on the same length */
	public int MAXLOOP = 50;
	/** Round of Training */
	public int MAXROUND = 1;
	/** details of the additional features */
	public List<Feature> addFeature;
	/** names of the additional tags lexicographic ordered */
	public List<String> orderedAddTagsNames; //TODO use collections.sort()
	/** names of the additional tags */
	public List<String> addTagsNames;
	/** name of the main tag */
	public String mainTag;
	/** data format */
	public List<String> format;
	/** name of the tag involved in the automatic research of new features */
	public String researchTag;
	/** set of gazetteers */
	public Gazetteers gazetteers;
	/**compare the context word in the gazetteers ignoring or not the case*/
	public boolean GAZETTEERS_CASESENSITIVE_CONTEXTWORDS=false;
	/**compare the main word in the gazetteers ignoring or not the case*/
	public boolean GAZETTEERS_CASESENSITIVE_MAINWORD= true;
	/**for document features, compare the context word in the gazetteers ignoring or not the case*/
	public boolean DOC_GAZETTEERS_CASESENSITIVE_CONTEXTWORDS=false;
	/**for document features, compare the main word in the gazetteers ignoring or not the case*/
	public boolean DOC_GAZETTEERS_CASESENSITIVE_MAINWORD= true;

	public String LEXICON=null;

	protected Logger logger = Logger.getLogger(getClass().getName());

	List<DocumentGazetteers> documentGazetteers;
	
	//	/////////////////////////////////////////////////////////////////////

	public Context(Reader reader){
		try {
			BufferedReader in = new BufferedReader ( reader);  

			//read script lines
			List<String> lines = new ArrayList <String>(); 
			String scriptLine= in.readLine();
			while (scriptLine != null) {
				lines.add(scriptLine.trim());
				scriptLine = in.readLine();
			}

			// read parameters
			List<String> paramStrn = new ArrayList<String>();
			for (String line : lines) {
				if (line.startsWith("PARAMETER:")) {
					String tmp[] = line.split(":");
					paramStrn.add(tmp[1]);
				}

			}
			loadParameters(paramStrn);

			// read input files format

			mainTag = "";
			format = new ArrayList<String>();
			addTagsNames = new ArrayList<String>();
			orderedAddTagsNames = new ArrayList<String>();
			String formatline = "";
			for (String line : lines) {
				if(line.startsWith("FORMAT:")){
					formatline=line;
					break;
				}
			}

			// read the format if specified
			if (formatline.startsWith("FORMAT:")) {
				String tmp[] = formatline.split(":");
				System.out.println("FORMAT:\n\t" + tmp[1]);
				tmp = tmp[1].split(",");
				for (int i = 0; i < tmp.length; i++) {
					format.add(tmp[i]);
					// retrieve the name of the main feature
					if (tmp[i].endsWith("!")) {
						mainTag = tmp[i].substring(0, tmp[i].indexOf('!'));
					}
					// retrieve the names of the additional features
					if (format.get(i).compareToIgnoreCase("W") != 0
							&& !format.get(i).endsWith("!")) {
						addTagsNames.add(format.get(i));
						// lexicographic order names
						boolean inserted = false;
						for (int j = 0; j < orderedAddTagsNames.size(); j++) {
							if (format.get(i).compareTo(
									orderedAddTagsNames.get(j)) < 0) {
								orderedAddTagsNames.add(j,
										format.get(i));
								inserted = true;
								break;
							}
						}
						if (!inserted)
							orderedAddTagsNames.add(format
									.get(i));
					}
				}
			}
			// otherwise use the default
			else {
				System.out.println("FORMAT:\n\tW,T!");
				format.add("W");
				format.add("T!");
				mainTag = "T";
			}
			// show reordered addTagsNames
			System.out.println("ADDITIONAL TAGS:\n\t"
					+ orderedAddTagsNames);

			// read additional feature if enabled
			if (ADDITIONALFEAT) {
				System.out.println("ADDITIONAL FEATURES:");
				List<String> addFeatStrn = new ArrayList<String>();
				for (String line : lines) {
					line.trim();
					if (line.startsWith("[")) {
						line = line.substring(line.indexOf('[') + 1, line
								.indexOf(']'));
						addFeatStrn.add(line);
						System.out.println("\t" + line);
					}
				}
				loadAdditionalFeatures(addFeatStrn);
			}

			// load the gazetteers if enabled
			if (GAZETTEERS) {
				List<URL> gazettersURLs = new ArrayList<URL>();
				for (String line : lines) {
					line.trim();
					if (line.startsWith("GAZETTEERS:")) {
						String tmp[] = line.split(":");
						gazettersURLs.add(new File(tmp[1]).toURI().toURL());
					}
				}
				loadGazetteers(gazettersURLs);
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//	/////////////////////////////////////////////////////////////////////

	/**
	 * load the parameters values of the learner from a set o string.
	 * 
	 * @param lines
	 *            set of lines containing the values of the parameters.
	 */
	public void loadParameters(List<String> lines){
		String tmp[];
		for (int i = 0; i < lines.size(); i++) {
			tmp = lines.get(i).split("=");
			tmp[0] = tmp[0].trim();
			tmp[1] = tmp[1].trim();
			if (tmp[0].compareToIgnoreCase("KSOCKET") == 0) {
				KSOCKET = Integer.parseInt(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("KHYPO") == 0) {
				KHYPO = Integer.parseInt(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("TAU") == 0) {
				TAU = Double.parseDouble(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("RADIUS") == 0) {
				RADIUS = Double.parseDouble(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("MAXROUND") == 0) {
				MAXROUND = Integer.parseInt(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("SENTENCE_LIMIT_CHAR") == 0) {
				SENTENCE_LIMIT_CHAR = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("BASEFEAT") == 0) {
				BASEFEAT = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("EXTENDFEAT") == 0) {
				EXTENDFEAT = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("ADDITIONALFEAT") == 0) {
				ADDITIONALFEAT = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("GAZETTEERS") == 0) {
				GAZETTEERS = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("DOCUMENTFEAT") == 0) {
				DOCUMENTFEAT = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("DOCUMENTCOLUMN") == 0) {
				DOCUMENTCOLUMNS.add(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("BARRIERFEAT") == 0) {
				BARRIERFEAT = Boolean.parseBoolean(tmp[1]);
			} else if (tmp[0].compareToIgnoreCase("PRINT_WEIGHTS_EACH_ROUND") == 0) {
				PRINT_WEIGHTS_EACH_ROUND = Boolean
				.parseBoolean(tmp[1]);
			} else if (tmp[0]
			               .compareToIgnoreCase("GAZETTEERS_CASESENSITIVE_MAINWORD") == 0) {
				GAZETTEERS_CASESENSITIVE_MAINWORD = Boolean
				.parseBoolean(tmp[1]);
			} else if (tmp[0]
			               .compareToIgnoreCase("GAZETTEERS_CASESENSITIVE_CONTEXTWORDS") == 0) {
				GAZETTEERS_CASESENSITIVE_CONTEXTWORDS = Boolean
				.parseBoolean(tmp[1]);

			} else if (tmp[0]
			               .compareToIgnoreCase("LEXICON") == 0) {
				LEXICON = tmp[1];
			}


		}
		testCosistency();
		System.out.print(printParmeters());
	}

	public void testCosistency(){
		if(DOCUMENTCOLUMNS.size()==0 && DOCUMENTFEAT==true){
			throw new RuntimeException("DOCUMENTFEAT is activated but DOCUMENTCOLUMN is not specified");
		}
		if(DOCUMENTCOLUMNS.size()!=0 && DOCUMENTFEAT==false){
			throw new RuntimeException("DOCUMENTCOLUMN is specified but DOCUMENTFEAT is off");
		}
	}

	/**
	 * Load the gazetteers.
	 * 
	 * @param lines
	 *            Set of strings indicating the gazetteers files.
	 */
	public void loadGazetteers(List<URL> gazetteersURLs){
		gazetteers = new Gazetteers();
		if(gazetteersURLs.size()>0){
			System.out.println("GAZETTEERS:");
			for (int i = 0; i < gazetteersURLs.size(); i++) {
				System.out.println("\t"+gazetteersURLs.get(i).toString());
				gazetteers.add(new Gazetteer(gazetteersURLs.get(i),GAZETTEERS_CASESENSITIVE_MAINWORD, GAZETTEERS_CASESENSITIVE_CONTEXTWORDS));
			}
			System.out.println("\t\t" + gazetteers.size()
					+ " gazetteers loaded");

		}
	}

	/**
	 * Load additional features.
	 * 
	 * @param lines
	 *            string representing the feature to load
	 */
	public void loadAdditionalFeatures(List<String> lines){
		int range = NGRAM - 1;

		// read additional features
		addFeature = new ArrayList<Feature>();
		List<FeatureDetail> featDetails = new ArrayList<FeatureDetail>();
		String arr[];
		String tmp[] = {};
		String tagName;// name of the tag
		String line;
		int offset;
		FeatureProperty fp;
		int preSufLength;
		for (int k = 0; k < lines.size(); k++) {
			line = lines.get(k);
			line.trim();

			arr = line.split(",");
			for (int i = 0; i < arr.length; i++) {
				tmp = arr[i].split(":");
				//check the number of tokens
				if (tmp.length<2 ||tmp.length>3){
					throw new RuntimeException("this feature fromat is wrong (wrong numbero fo ':') : ["
							+ line + "] , this feature is ignored");
				}
				int j=0;

				// compute the name of the tag
				tagName = "";
				if (tmp[j].endsWith("!")){
					tagName = mainTag;
				}
				else if (tmp[j].compareToIgnoreCase("W") == 0){
					tagName = "W";
				}
				else{
					tagName = tmp[j];
				}
				j++;

				//read the prefix-suffix indication if present
				if (tmp.length==3){
					if(tmp[j].startsWith("p")){
						preSufLength = Integer.parseInt(tmp[j].substring(1));
						if(preSufLength<=0){
							throw new RuntimeException("this feature fromat is wrong (prefix length must be a positive integer) : ["
									+ line + "] , this feature is ignored");
						}
						fp=new FeatureProperty(FeaturePropertyType.PREFIX,preSufLength);
					}
					else if(tmp[j].startsWith("s")){
						preSufLength = Integer.parseInt(tmp[j].substring(1));
						if(preSufLength<=0){
							throw new RuntimeException("this feature fromat is wrong (prefix length must be a positive integer) : ["
									+ line + "] , this feature is ignored");
						}
						fp=new FeatureProperty(FeaturePropertyType.SUFFIX,preSufLength);
					}
					else{
						throw new RuntimeException("this feature fromat is wrong (second token should start with 'p' for prefix or 's' for suffix) : ["
								+ line + "] , this feature is ignored");
					}
					j++;
				}
				else{
					fp=null;//used whole form, no property
				}

				//read offset
				offset = Integer.parseInt(tmp[j]);
				// check if the feature is inside the window size
				if (offset > range || (-1 * offset > range)) {
					throw new RuntimeException("this feature is out of the window size: ["
							+ line + "] , this feature is ignored");
				}

				featDetails.add(new FeatureDetail(tagName, offset,fp, this));

			}
			if (!featDetails.isEmpty()) {
				addFeature.add(new Feature(featDetails
						.toArray(new FeatureDetail[featDetails.size()]),this));
			}

			featDetails.clear();
		}

		System.out.println("\t\t" + addFeature.size()
				+ " additional features loaded");
	}

	/**
	 * print the parameters of the learner.
	 */
	public String printParmeters()
	{
		StringBuilder rtn= new StringBuilder();
		rtn.append("PARAMETERS:\n");
		rtn.append("\tKSOCKET = " + KSOCKET+"\n");
		rtn.append("\tKHYPO = " + KHYPO+"\n");
		rtn.append("\tTAU = " + TAU+"\n");
		rtn.append("\tRADIUS = " + RADIUS+"\n");
		rtn.append("\tMARGIN_RATE = " + MARGIN_RATE+"\n");
		rtn.append("\tMAXROUND = " + MAXROUND+"\n");
		rtn.append("\tSENTENCE_LIMIT_CHAR = "
				+ SENTENCE_LIMIT_CHAR+"\n");
		rtn.append("\tBASEFEAT = " + BASEFEAT+"\n");
		rtn.append("\tEXTENDFEAT = " + EXTENDFEAT+"\n");
		rtn.append("\tADDITIONALFEAT = " + ADDITIONALFEAT+"\n");
		rtn.append("\tDOCUMENTFEAT = " + DOCUMENTFEAT+"\n");
		rtn.append("\tDOCUMENTCOLUMN = " + DOCUMENTCOLUMNS+"\n");
		rtn.append("\tBARRIERFEAT = " + BARRIERFEAT+"\n");
		rtn.append("\tGAZETTEERS = " + GAZETTEERS+"\n");

		rtn.append("\tGAZETTEERS_CASESENSITIVE_MAINWORD = "
				+ GAZETTEERS_CASESENSITIVE_MAINWORD+"\n");
		rtn.append("\tGAZETTEERS_CASESENSITIVE_CONTEXTWORDS = "
				+ GAZETTEERS_CASESENSITIVE_CONTEXTWORDS+"\n");
		rtn.append("\tLEXICON = "+LEXICON+"\n");
		return rtn.toString();
	}

	public String toString (){
		StringBuilder rtn = new StringBuilder();

		return rtn.toString();
	}

	//XXX delete old lumina method
	//XXX TODO create a class to handle the script file
	//XXX TODO call this method from the main LOADing, and handle folder specification '/*'
	//XXX TODO uniform the loading system os we don't need special tag LUMINAGAZETTEERS
	//	public static List<String> getGazetteersList(Reader reader){
	//	BufferedReader in = new BufferedReader ( reader);  
	//	List<String> lines = new ArrayList <String>(); 

	//	try{
	//	//read script lines	
	//	String scriptLine= in.readLine();
	//	while (scriptLine != null) {
	//	lines.add(scriptLine.trim());
	//	scriptLine = in.readLine();
	//	}
	//	}catch(IOException e){
	//	e.printStackTrace();
	//	}

	//	//read gazetteers lines
	//	List<String> gazetterNames = new ArrayList<String>();
	//	for (String line : lines) {
	//	line.trim();
	//	if (line.startsWith("LUMINAGAZETTEERS:")) {
	//	String tmp[] = line.split(":");
	//	gazetterNames.add(tmp[1]);
	//	}
	//	}

	//	return gazetterNames;
	//	}


	public void setGazetteers(Gazetteers gazetteers) {
		if(this.GAZETTEERS){
			this.gazetteers = gazetteers;
		}
		else{
			System.err.println("Gazetteers have not been set because their use is disabled");
		}
	} 

	public void setGazetteersFromURL(List <URL> gazettersURL) {
		if(this.GAZETTEERS){
			loadGazetteers(gazettersURL);
		}
		else{
			System.err.println("Gazetteers have not been set because their use is disabled");
		}
	}

}
