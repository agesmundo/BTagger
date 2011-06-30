package bTagger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A Sample for Linear Tagging
 * 
 * @author Andrea Gesmundo
 */
public class TagSample
{

	/** words of the sentence */
	public String[] words;

	/** additional tag used as support in the training and prediction */
	public String[][] addTags;

	/** tags used for the tagging */
	public Label[] tags;

	// public Context context;
	// //////////////////////////////////////////////////////////////
	// CONSTRUCTORS

	/**
	 * Init with word strings.
	 */
	public TagSample(List<String> wdstrs) {
		addTags = new String[0][0];
		tags = new Label[wdstrs.size()];
		words = wdstrs.toArray(new String[wdstrs.size()]);
	}

	/**
	 * Init with word strings and tags label strings.
	 */
	public TagSample(List<String> wdstrs, List<Label> tgStrs,
			List<List<String>> addTgStrs) {
		words = wdstrs.toArray(new String[wdstrs.size()]);
		addTags = new String[addTgStrs.size()][wdstrs.size()];
		for (int j = 0; j < addTags.length; j++) {
			addTags[j] = addTgStrs.get(j).toArray(new String[addTgStrs.get(j).size()]);
		}
		tags = tgStrs.toArray(new Label[wdstrs.size()]);
	}

	/**
	 * Init with an array of SWords.
	 */
	public TagSample(String... wds) {
		words = wds;
		tags = new Label[wds.length];
		addTags = new String[0][0];
	}

	/**
	 * init with another TagSemple
	 * 
	 * @param sen
	 *            TagSample instance.
	 */
	public TagSample(TagSample sen) {
		words = sen.words;
		addTags = sen.addTags;
		tags = new Label[sen.words.length];
	}

	// //////////////////////////////////////////////////////////////

	public void display(Appendable sb) throws IOException
	{
		for (int i = 0; i < words.length; i++) {
			if (i > 0)
				sb.append(" ");
			sb.append(words[i] + "(" + tags[i] + ")");
		}
	}

	/**
	 * return a string containing the representation of the sentence in in Conll
	 * format.
	 */
	public String displayConll(Context context)
	{
		String rtn = "";
		int addFeatIndex;
		for (int i = 0; i < words.length; i++) {
			addFeatIndex = 0;
			for (int j = 0; j < context.format.size(); j++) {
				if (j != 0)
					rtn += " ";
				if (context.format.get(j).compareTo("W") == 0) {
					rtn += words[i];
				} else if (context.format.get(j).endsWith("!")) {
					rtn += tags[i];
				} else {
					rtn += addTags[addFeatIndex][i];
					addFeatIndex++;
				}
			}
			rtn += "\n";
		}
		return rtn;
	}

	public String toString()
	{
		String ret = "";
		for (int i = 0; i < words.length; i++) {
			ret += words[i] + " ";
		}
		return ret;
	}

	/**
	 * check if the sentence is composed by all capitalized words
	 * 
	 * @return true if all the words are composed by capitalized letters
	 */
	public boolean isAllCapSentence()
	{
		// flow the words of the sentence
		boolean isAllCapSen = true;
		for (int j = 0; j < words.length; j++) {
			if (!isAllCapWord(words[j])) {
				isAllCapSen = false;
				break;
			}
		}
		return isAllCapSen;
	}

	/**
	 * check if a word has all the letters capitalized
	 * 
	 * @param word
	 *            the word to check
	 * @return true if the word has all the letters all capitalized
	 */
	private boolean isAllCapWord(String word)
	{
		char chars[] = word.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] >= 'a' && chars[i] <= 'z')
				return false;
			// if(!Character.isUpperCase(chars[i]))return false;
		}
		return true;
	}

	// TODO Move when done the class to collect tag samples
	public static void saveTagged(TagSamples sample, String filename,
			Context context)
	{
		PrintWriter out = new PrintWriter(FileUtils.getWriter(filename));
		for (int i = 0; i < sample.size(); i++) {
			out.println(sample.get(i).displayConll(context));
		}
		out.close();
		System.out.println("\nOUT: Tagged results saved in: " + filename);
	}

}
