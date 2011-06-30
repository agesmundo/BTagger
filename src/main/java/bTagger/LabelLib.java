package bTagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Library of labels
 * 
 * @author Andrea Gesmundo
 * 
 */
public class LabelLib
{

	// constants
	private final int LABEL_HASH_INIT = 50;

	// variables
	private Map<String, Integer> label2id;

	private List<Label> id2label;

	// lexicon
	private Map<String, List<Label>> wordsToLabels;

	// ///////////////////////////////////////////////////////////////
	// COSTRUCTORS

	public LabelLib() {
		label2id = new HashMap<String, Integer>(LABEL_HASH_INIT);
		id2label = new ArrayList<Label>();
		wordsToLabels = new HashMap<String, List<Label>>();
	}

	// ///////////////////////////////////////////////////////////////

	/**
	 * return true if the label is contained in the LabelLib
	 */
	public boolean containsLabel(String label)
	{
		if (label2id.get(label) == null) {
			return false;
		}
		return true;
	}

	/**
	 * get the Label ID with a string new label would be inserted into LabelLib
	 */
	public int getLabelID(String label)
	{
		Integer id = (Integer) label2id.get(label);
		if (id != null) {
			return id.intValue();
		}

		id = new Integer(id2label.size());
		label2id.put(label, id);
		id2label.add(new Label(label, id));
		return id.intValue();
	}

	/**
	 * get the SLabel with a string new label would be inserted into LabelLib
	 */
	public Label getLabel(String str)
	{
		return id2label.get(getLabelID(str));
	}

	/**
	 * get SLabel with ID
	 */
	public Label getLabel(int id)
	{
		return id2label.get(id);
	}

	/**
	 * get the Size of the Label set
	 */
	public int getSize()
	{
		return id2label.size();
	}

	public String listAll()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < id2label.size(); i++) {
			sb.append("" + i + " " + id2label.get(i) + "\n");
		}
		return sb.toString();
	}

	/**
	 * Write the labels in a file.
	 * 
	 * @param filename
	 *            The file name.
	 */
	public void saveLabels(String filename)
	{
		PrintWriter out = new PrintWriter(FileUtils.getWriter(filename));
		for (int i = 0; i < id2label.size(); i++) {
			out.println("" + id2label.get(i));
		}
		out.close();
		System.out.println("\nOUT: Tag set saved in: " + filename);
	}

	public void loadLabels(String filename)
	{
		try {
			BufferedReader in = new BufferedReader(FileUtils
					.getReader(filename));
			System.err.println("Open Label File : " + filename);

			String line = in.readLine();
			while (line != null) {
				String str = line.trim();
				String[] arr = str.split("\\s+");
				getLabel(arr[0]);

				line = in.readLine();
			} // end of each line

			in.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	public List<Label> getCandidateLabels()
	{
		return id2label;
	}

	public List<Label> getCandidateLabelsFromLexicon(String word)
	{
		List<Label> rtn = wordsToLabels.get(word);
		if (rtn == null) {
			return id2label;
		}
		return rtn;
	}

	public void loadLexicon(String lexicon)
	{
		if (lexicon == null) {
			return;
		}
		loadLexicon(FileUtils.getReader(lexicon));
	}

	public void loadLexicon(Reader lexiconReader)
	{
		List<String> lexiconLines = FileUtils.getListString(lexiconReader);
		for (String line : lexiconLines) {
			line = line.trim();
			if (line.compareTo("") == 0) {
				continue;
			}
			String tokens[] = line.split("\\s+");
			if (tokens.length < 2) {
				throw new RuntimeException("malformed Lexicon line: " + line);
			}
			List<Label> lables = new ArrayList<Label>();
			for (int i = 1; i < tokens.length; i++) {
				if (!containsLabel(tokens[i])) {
					System.err
							.println("WARNING: Lexicon contain label not included in the library: '"
									+ tokens[i] + "'");
					continue;
				}
				lables.add(getLabel(tokens[i]));
			}
			if (lables.size() == 0) {
				continue;
			}
			if (wordsToLabels.get(tokens[0]) != null) {
				throw new RuntimeException(
						"Lexicon contains twice the same word: '" + tokens[0]
								+ "'");
			}
			wordsToLabels.put(tokens[0], lables);
		}
	}
	
	
	List<String> classesNames;
	public List<String> getClassesNames()
	{
		//if already computed return it
		if(classesNames!=null){
			return classesNames;
		}
		
		classesNames = new ArrayList<String>();
		// look for the name of the classes.
		List<Label> lbl = getCandidateLabels();
		String currentLbl = "";
		for (int j = 0; j < lbl.size(); j++) {// loop all the labels
			currentLbl = lbl.get(j).lbl;
			if (currentLbl.compareTo("O") == 0) {
				continue;
			}
			// extract the class
//			String currentLblTokens[] = currentLbl.split("-");//TODO split on the first -, create an object to represent the labels for the splitting
//			= currentLblTokens[currentLblTokens.length - 1];
			currentLbl = LabelsUtility.getClassName(currentLbl);
			
			if (!classesNames.contains(currentLbl))
				classesNames.add(currentLbl);// insert
			// if
			// new
		}
		return classesNames;
	}


}
