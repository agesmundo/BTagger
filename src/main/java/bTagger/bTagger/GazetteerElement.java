package bTagger;

/**
 * <P>
 * Elements in the gazetteers that contain that word the element is represented
 * with two segments of words the first segment contain the words to the left of
 * the requested word (disposed from left to right) the second segment contain
 * the words to the right of the requested word (disposed from right to left).
 * </P>
 * 
 * Example, from the phrase "Bank of United States of America" and the position
 * 3:
 * 
 * <pre>
 *                  |
 * Bank of United States of America
 * </pre>
 * 
 * <code>leftSegment = [United,of,Bank]</code><BR>
 * <code>rightSegment = [of, America]</code>
 * 
 * @author Andrea Gesmundo
 */
public class GazetteerElement
{
	/**
	 * contain the words that appear in the element of the gazetteers to the
	 * left of the requested word
	 */
	public final String[] leftSegment;

	/**
	 * contain the words that appear in the element of the gazetteers to the
	 * right of the requested word
	 */
	public final String[] rightSegment;

	public GazetteerElement(final String[] words, final int wordIndex) {

		// write left segment from right to left
		leftSegment = new String[wordIndex];
		for (int i = 0; i < wordIndex; i++) {
			leftSegment[i] = words[wordIndex - 1 - i];
		}

		// write right segment from left to right
		rightSegment = new String[words.length - wordIndex - 1];
		for (int i = 0; i < rightSegment.length; i++) {
			rightSegment[i] = words[wordIndex + i + 1];
		}

	}

	/**
	 * Check correspondence between this {@link GazetteerElement} and the
	 * strings in the array
	 * 
	 * @param words
	 *            an array of {@link String}s
	 * @param posi
	 *            an index into <code>words</code> where the correspondence will
	 *            be checked
	 * @return <code>true</code> just in case there is correspondence.
	 */
	public boolean correspond(final String[] words, final int posi)
	{
		// check boundaries of the sentence
		if ((posi < leftSegment.length)
				|| (words.length - posi - 1 < rightSegment.length)) {
			return false;
		}

		// flow left segment
		for (int i = 0; i < leftSegment.length; i++) {
			if (!leftSegment[i].equals(words[posi - 1 - i])) {
				return false;
			}
		}
		// flow right segment
		for (int i = 0; i < rightSegment.length; i++) {
			if (!rightSegment[i].equals(words[posi + 1 + i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check correspondence between this {@link GazetteerElement} and the words
	 * in the array. The correspondence will be checked ignoring any differences
	 * in case.
	 * 
	 * @param words
	 *            an array of {@link String}s
	 * @param posi
	 *            an index into <code>words</code> where the correspondence will
	 *            be checked, ignoring case
	 * @return <code>true</code> just in case there is correspondence.
	 */
	public boolean correspondIgnoreCase(final String[] words, final int posi)
	{
		// check boundaries of the sentence
		if ((posi < leftSegment.length)
				|| (words.length - posi - 1 < rightSegment.length)) {
			return false;
		}

		// flow left segment
		for (int i = 0; i < leftSegment.length; i++) {
			if (!leftSegment[i].equalsIgnoreCase(words[posi - 1 - i])) {
				return false;
			}
		}
		// flow right segment
		for (int i = 0; i < rightSegment.length; i++) {
			if (!rightSegment[i].equalsIgnoreCase(words[posi + 1 + i])) {
				return false;
			}
		}

		return true;
	}
}
