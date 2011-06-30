package bTagger;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * used to store a gazetteer.
 * 
 * @author Andrea Gesmundo
 * 
 */
public class Gazetteer
{

	/**
	 * Map a word to all the elements in the gazetteers that contain that word
	 * the element is represented with two segments of words the first segment
	 * contain the words to the left of the requested word (disposed from left
	 * to right) the second segment contain the words to the right of the
	 * requested word (disposed from right to left)
	 * 
	 * NB, the words are stored and retrieved in lower case version
	 * 
	 * @author Andrea Gesmundo
	 */
	private final Map<String, List<GazetteerElement>> gazetteerElements;

	private final boolean caseSensitiveMainWord;

	private final boolean caseSensitiveContextWords;

	public Gazetteer(final URL gazetURL, final boolean csMainWord,
			final boolean csContextWords) {
		this(FileUtils.getListString(gazetURL), csMainWord, csContextWords);
	}

	public Gazetteer(final boolean csMainWord,final boolean csContextWords){
		this.caseSensitiveMainWord = csMainWord;
		this.caseSensitiveContextWords = csContextWords;
		gazetteerElements = new HashMap<String, List<GazetteerElement>>();
	}
	
	public Gazetteer(final List<String> lines,final boolean csMainWord,final boolean csContextWords ) {
		this(csMainWord,csContextWords);
		for (String line : lines) {
			line = line.trim();
			if (line.length()!=0) {
				final String[] words = line.split(" ");
				addEntry(words);
			}
		}
	}

	public void addEntry(final String[] words){
		for (int j = 0; j < words.length; j++) {
			List<GazetteerElement> elements = getElementsWithMainWord(words[j]);
			if (elements == null) {
				elements = new ArrayList<GazetteerElement>();
				elements.add(new GazetteerElement(words, j));
				putElementsWithMainWord(words[j], elements);
			} else {
				if (
				!(caseSensitiveContextWords ? isInTheGazetteersContextCaseSesitive(
						words, j)
						: isInTheGazetteersNotContextCaseSensitive(
								words, j))) {
					elements.add(new GazetteerElement(words, j));
				}
			}
		}
	}
	
	public boolean isInTheGazetteersSingleWord(final String word)
	{
		return getElementsWithMainWord(word)!=null;
	}

	public int isInTheGazetteersSingleWordCount(final String word)
	{
		return getElementsWithMainWord(word).size();
	}
	
	public int[] isInTheGazetteersGetPositionInLongest(final String word)
	{
		List<GazetteerElement> elements;
		elements = getElementsWithMainWord(word);
		if (elements == null) {
			return null;
		}
		int rtn []={0,0};//Max Length, position
		int currLength=0;
		for (int i = 0; i < elements.size(); i++) {
			currLength=elements.get(i).leftSegment.length + elements.get(i).rightSegment.length;
			if (currLength > rtn[0]) {
				rtn[0] = currLength;
				rtn[1] = elements.get(i).leftSegment.length;
			}
		}
		return rtn;
	}
	
	public boolean isInTheGazetteers(final TagSample sen, final int posi)
	{
		return caseSensitiveContextWords 
		? isInTheGazetteersContextCaseSesitive(sen.words, posi)
				: isInTheGazetteersNotContextCaseSensitive(sen.words, posi);
	}

	private boolean isInTheGazetteersContextCaseSesitive(final String[] words,
			final int posi)
	{
		List<GazetteerElement> elements;
		elements = getElementsWithMainWord(words[posi]);
		if (elements == null) {
			return false;
		}
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).correspond(words, posi)) {
				return true;
			}
		}
		return false;
	}

	private boolean isInTheGazetteersNotContextCaseSensitive(
			final String[] words, final int posi)
	{
		List<GazetteerElement> elements;
		elements = getElementsWithMainWord(words[posi]);
		if (elements == null) {
			return false;
		}
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).correspondIgnoreCase(words, posi)) {
				return true;
			}
		}
		return false;
	}

	private void putElementsWithMainWord(final String mWords,
			final List<GazetteerElement> elements)
	{
		gazetteerElements.put(getMainWord(mWords), elements);
	}

	private List<GazetteerElement> getElementsWithMainWord(final String mWords)
	{
		return gazetteerElements.get(getMainWord(mWords));
	}

	private String getMainWord(final String mWords)
	{
		if (caseSensitiveMainWord) {
			return mWords;
		} else {
			return mWords.toLowerCase();
		}
	}
}
