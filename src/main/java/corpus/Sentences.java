package corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import bTagger.BTagger;
import bTagger.FileUtils;

import utility.ConvertChunkRepresentation;

public class Sentences
{
	private List<Sentence> sentences = new ArrayList<Sentence>();

	// ///////////////////////////////////////////////////////////
	// Constructors

	/**
	 * Create an empty Sentences instance.
	 * 
	 */
	public Sentences() {
		sentences = new ArrayList<Sentence>();
	}

	/**
	 * Create a Sentences instance from a file name.
	 * 
	 * @param file
	 *            Name of the file to use.
	 * @throws FileNotFoundException
	 *             Thrown if the file does not exists.
	 */
	public Sentences(String file) throws FileNotFoundException {
		this(FileUtils.getReader(file));
	}

	/**
	 * Create a Sentences instance from a file name.
	 * 
	 * @param file
	 *            Name of the file to use.
	 * @param cs
	 *            Char set to use.
	 * @throws FileNotFoundException
	 *             Thrown if the file does not exists.
	 */
	public Sentences(String file, Charset cs) throws FileNotFoundException {
		this(FileUtils.getReader(file, cs));
	}

	/**
	 * Create a Sentences from a Reader.
	 * 
	 * @param reader
	 *            Reader to read to create the instance
	 */
	public Sentences(Reader reader) {
		BufferedReader in = new BufferedReader(reader);
		boolean lastWasEmpty = true; // used to avoid multiple empty lines
		List<String> sentence = new ArrayList<String>();
		try {
			String line = in.readLine();
			while (line != null) {
				line = line.trim();
				if (line.compareTo("") == 0) {
					if (lastWasEmpty != true && sentence.size() > 0) {
						sentences.add(new Sentence(sentence));
						sentence = new ArrayList<String>();
					}
					lastWasEmpty = true;
				} else {
					sentence.add(line);
					lastWasEmpty = false;
				}
				line = in.readLine();
			}
			if (lastWasEmpty != true && sentence.size() > 0) {
				sentences.add(new Sentence(sentence));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Sentences(Sentences... sentencesList) {
		this();
		for (Sentences sentencesCurrent : sentencesList) {
			this.sentences.addAll(sentencesCurrent.getSentences());
		}
	}

	public Sentences(String source1, ChunksCoords combinedCoords, int columnID)
			throws FileNotFoundException {
		this(source1);
		this.setColumnWithChunksCoords(combinedCoords, columnID);
	}

	public Sentences(List<List<String>> sentencesWords) {
		for (List<String> sentenceWords : sentencesWords) {
			sentences.add(new Sentence(sentenceWords));
		}
	}

	// ///////////////////////////////////////////////////////////////////

	public void setColumnWithChunksCoords(ChunksCoords combinedCoords,
			int columnID)
	{
		for (int i = 0; i < sentences.size(); i++) {
			sentences.get(i).setAllOut(columnID);
			for (int j = 0; j < combinedCoords.coords.get(i).size(); j++) {
				ChunkData currentChunk = combinedCoords.coords.get(i).get(j);
				if (currentChunk.getChunkEnd() == currentChunk.getChunkStart()) {
					sentences.get(i).lines.get(currentChunk.getChunkEnd()).tokens
							.set(columnID, "S-" + currentChunk.getChunkClass());
				} else {
					for (int k = currentChunk.getChunkStart() + 1; k < currentChunk
							.getChunkEnd(); k++) {
						sentences.get(i).lines.get(k).tokens.set(columnID, "I-"
								+ currentChunk.getChunkClass());
					}
					sentences.get(i).lines.get(currentChunk.getChunkStart()).tokens
							.set(columnID, "B-" + currentChunk.getChunkClass());
					sentences.get(i).lines.get(currentChunk.getChunkEnd()).tokens
							.set(columnID, "E-" + currentChunk.getChunkClass());
				}
			}
		}
	}

	public void setColumnWithTagStrings(List<List<String>> tags, int columnID)
	{
		for (int i = 0; i < tags.size(); i++) {
			if(tags.get(i).size()!=sentences.get(i).size()){
				throw new RuntimeException("Failed attempt to insert column in a sentences group. The corresponding sentence differ in length.");
			}
			for (int j = 0; j < tags.get(i).size(); j++) {
				setToken(i, j, columnID, tags.get(i).get(j));
			}
		}
	}

	public Sentences setAllOut(int columnID)
	{
		for (int i = 0; i < sentences.size(); i++) {
			sentences.get(i).setAllOut(columnID);
		}
		return this;
	}

	// ///////////////////////////////////////////////////////////
	// toString methods

	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < sentences.size(); i++) {
			if (i > 0)
				ret.append("\n\n");
			ret.append(sentences.get(i));
		}
		return ret.toString();
	}

	public String toStringDelColumns(List<Integer> columnsID)
	{
		StringBuilder ret = new StringBuilder("");
		for (int i = 0; i < sentences.size(); i++) {
			if (i > 0) {
				ret.append("\n\n");
			}
			ret.append(sentences.get(i).toStringDelColumns(columnsID));
		}
		return ret.toString();
	}

	public String toStringSelColumns(List<Integer> columnsID)
	{
		StringBuilder ret = new StringBuilder("");
		for (int i = 0; i < sentences.size(); i++) {
			if (i > 0) {
				ret.append("\n\n");
			}
			ret.append(sentences.get(i).toStringSelColumns(columnsID));
		}
		return ret.toString();
	}

	/**
	 * Print the the sentences as plain text. One sentence per line.
	 * 
	 * @return The words of the sentence as plain text
	 */
	public String toStringPlainText()
	{
		StringBuilder ret = new StringBuilder("");
		for (int i = 0; i < sentences.size(); i++) {
			if (i > 0) {
				ret.append("\n");
			}
			ret.append(sentences.get(i).toStringPlainText());
		}
		return ret.toString();
	}

	// ////////////////////////////////////////////////////////
	// SAVE

	public void saveFile(String fileName)
	{
		saveFile(fileName, Charset.defaultCharset());
	}

	public void saveFile(String fileName, Charset cs)
	{
		try {
			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(fileName), cs);// write
			out.write(this.toString());
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void saveFilePlainText(String fileName)
	{
		saveFilePlainText(fileName, Charset.defaultCharset());
	}

	public void saveFilePlainText(String fileName, Charset cs)
	{
		try {
			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(fileName), cs);// write
			out.write(this.toStringPlainText());
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void saveFileWords(String fileName)
	{
		saveFileWords(fileName, Charset.defaultCharset());
	}

	public void saveFileWords(String fileName, Charset cs)
	{
		try {
			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(fileName), cs);// write
			List<Integer> colId = new ArrayList<Integer>();
			colId.add(Sentence.WORDS_COLUMN_ID);
			out.write(this.toStringSelColumns(colId));
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public int getColumnsNumber(){
		return sentences.get(0).lines.get(0).size();
	}

	public List<List<String>> getColumn(int columnID)
	{
		List<List<String>> column = new ArrayList<List<String>>();
		for(Sentence sentence:sentences){
			column.add(sentence.getColumn(columnID));
		}
		return column;
	}

	public ChunksCoords getChunkCoords(int columnID)
	{
		return new ChunksCoords(this, columnID);
	}

	public int size()
	{
		return sentences.size();
	}

	public Sentence getSentence(int index)
	{
		return sentences.get(index);
	}

	public int getChunkFormat(int columnID)
	{
		boolean isIOB1 = true, isIOB2 = true, isIOE1 = true, isIOE2 = true, isOC = true;
		for (int i = 0; i < sentences.size(); i++) {
			Sentence currentSentence = sentences.get(i);
			for (int j = 0; j < currentSentence.size(); j++) {
				Line prevLine = currentSentence.getLine(j - 1);
				Line currentLine = currentSentence.getLine(j);
				Line nextLine = currentSentence.getLine(j + 1);

				if (ConvertChunkRepresentation.checkPrefix("S", currentLine
						.getToken(columnID))) {
					isIOB1 = false;
					isIOB2 = false;
					isIOE1 = false;
					isIOE2 = false;
				} else if (ConvertChunkRepresentation.checkPrefix("I",
						currentLine.getToken(columnID))) {
					if (prevLine != null
							&& !ConvertChunkRepresentation.sameClass(prevLine
									.getToken(columnID), currentLine
									.getToken(columnID))) {
						isIOB2 = false;
						isOC = false;
						if (ConvertChunkRepresentation.checkPrefix("I",
								prevLine.getToken(columnID))) {
							isIOE2 = false;
						}
					}
					if (nextLine != null
							&& !ConvertChunkRepresentation.sameClass(nextLine
									.getToken(columnID), currentLine
									.getToken(columnID))) {
						isIOE2 = false;
						isOC = false;
						if (ConvertChunkRepresentation.checkPrefix("I",
								nextLine.getToken(columnID))) {
							isIOB2 = false;
						}
					}
				} else if (ConvertChunkRepresentation.checkPrefix("B",
						currentLine.getToken(columnID))) {
					isIOE1 = false;
					isIOE2 = false;
					if (prevLine != null
							&& !ConvertChunkRepresentation.sameClass(prevLine
									.getToken(columnID), currentLine
									.getToken(columnID))) {
						isIOB1 = false;
					}
				} else if (ConvertChunkRepresentation.checkPrefix("E",
						currentLine.getToken(columnID))) {
					isIOB1 = false;
					isIOB2 = false;
					if (nextLine != null
							&& !ConvertChunkRepresentation.sameClass(nextLine
									.getToken(columnID), currentLine
									.getToken(columnID))) {
						isIOE1 = false;
					}
				} else if (ConvertChunkRepresentation.checkPrefix("O",
						currentLine.getToken(columnID))) {
					continue;
				} else {
					break;
				}

				// control if the representation is detected
				if (isIOB2 == false && isIOE1 == false && isIOE2 == false
						&& isOC == false) {// IOB1
					return 1;
				} else if (isIOB1 == false && isIOE1 == false
						&& isIOE2 == false && isOC == false) {// IOB2
					return 2;
				} else if (isIOB1 == false && isIOB2 == false
						&& isIOE2 == false && isOC == false) {// IOE1
					return 3;
				} else if (isIOB1 == false && isIOB2 == false
						&& isIOE1 == false && isOC == false) {// IOE2
					return 4;
				} else if (isIOB1 == false && isIOB2 == false
						&& isIOE1 == false && isIOE2 == false) {// O+C
					return 5;
				}
			}
		}

		// in the case there are only I- prefix it could be IOB1 or IOE1
		if (isIOB1 == true && isIOB2 == false && isIOE1 == true
				&& isIOE2 == false && isOC == false) {// IOB1
			return 1;
		}

		// in the case there are only B- and I- prefix it could be IOB2 or O+C
		if (isIOB1 == false && isIOB2 == true && isIOE1 == false
				&& isIOE2 == false && isOC == true) {// IOB1
			return 2;
		}

		// in the case there are only E- and I- prefix it could be IOE2 or O+C
		if (isIOB1 == false && isIOB2 == false && isIOE1 == false
				&& isIOE2 == true && isOC == true) {// IOB1
			return 4;
		}

		System.out.println("Unable to detect chunk representation");
		return 0;
	}

	/**
	 * Remove a column from the corpus
	 * 
	 * @param columnID
	 *            The ID of the column to remove.(starting from 0)
	 */
	public void removeColumns(int columnID)
	{
		for (Sentence sentence : sentences) {
			for (Line line : sentence.lines) {
				line.removeColumn(columnID);
			}
		}
	}

	/**
	 * Add the space for new columns in the corpus
	 * 
	 * @param columnNumber
	 *            The number of columns to be added
	 */
	public void addColumns(int columnNumber)
	{
		for (Sentence sentence : sentences) {
			sentence.addColumns(columnNumber);
		}
	}

	public void addColumn(ChunksCoords chunksCoords)
	{
		addColumns(1);
		setColumnWithChunksCoords(chunksCoords, sentences.get(0).getLine(0)
				.size() - 1);
	}

	public void addColumn(List<List<String>> tags)
	{
		addColumns(1);
		setColumnWithTagStrings(tags, sentences.get(0).getLine(0).size() - 1);
	}

	/**
	 * Split Sentences set in subsets
	 * 
	 * @param subsetsNumber
	 *            The number of subsets.
	 * @return Subsection of the original sentences set.
	 */
	public List<Sentences> split(int subsetsNumber)
	{
		// initialize returned object
		List<Sentences> subSets = new ArrayList<Sentences>();
		for (int i = 0; i < subsetsNumber; i++) {
			subSets.add(new Sentences());
		}

		// split sentences in subsets
		int i = 0;
		for (Sentence sentence : sentences) {
			subSets.get(i).addSentence(sentence);
			i++;
			i = i % subsetsNumber;
		}
		return subSets;
	}

	/**
	 * Split Sentences set in subsets and get also the complement subset for
	 * each subset.
	 * 
	 * @param subsetsNumber
	 *            The number of subsets.
	 * @return Subsection of the original sentences set and a complement set for
	 *         each subsection.
	 */
	public List<List<Sentences>> splitWithComplements(int subsetsNumber)
	{
		// initialize returned objects
		List<Sentences> subSets = new ArrayList<Sentences>();
		for (int i = 0; i < subsetsNumber; i++) {
			subSets.add(new Sentences());
		}
		List<Sentences> compSets = new ArrayList<Sentences>();
		for (int i = 0; i < subsetsNumber; i++) {
			compSets.add(new Sentences());
		}

		// split sentences in subsets
		int i = 0;
		for (Sentence sentence : sentences) {
			subSets.get(i).addSentence(sentence);
			for (int j = 0; j < compSets.size(); j++) {
				if (j != i) {
					compSets.get(j).addSentence(sentence);
				}
			}
			i++;
			i = i % subsetsNumber;
		}

		List<List<Sentences>> rtn = new ArrayList<List<Sentences>>();
		rtn.add(subSets);
		rtn.add(compSets);
		return rtn;
	}

	/**
	 * Split Sentences set in subsets on docstarts and get also the complement
	 * subset for each subset.
	 * 
	 * @param subsetsNumber
	 *            The number of subsets.
	 * @return Subsection of the original sentences set and a complement set for
	 *         each subsection.
	 */
	public List<List<Sentences>> splitWithComplementsOnDocs(int subsetsNumber)
	{
		// initialize returned objects
		List<Sentences> subSets = new ArrayList<Sentences>();
		for (int i = 0; i < subsetsNumber; i++) {
			subSets.add(new Sentences());
		}
		List<Sentences> compSets = new ArrayList<Sentences>();
		for (int i = 0; i < subsetsNumber; i++) {
			compSets.add(new Sentences());
		}

		// split sentences in subsets
		int subsetId = 0;
		int subsetSize = sentences.size() / subsetsNumber;
		int nextThreshold = ++subsetSize;
		for (int k = 0; k < sentences.size(); k++) {
			Sentence sentence = sentences.get(k);

			// increase the subset id if needed
			if (sentence.getToken(0, 0).startsWith(BTagger.documentStartString)) {
				if (k >= nextThreshold) {
					nextThreshold += subsetSize;
					subsetId++;
					// debug
					if (subsetId >= subsetsNumber) {
						throw new RuntimeException("erwe");
					}
				}
			}

			// add sentence
			subSets.get(subsetId).addSentence(sentence);
			for (int j = 0; j < compSets.size(); j++) {
				if (j != subsetId) {
					compSets.get(j).addSentence(sentence);
				}
			}

		}

		List<List<Sentences>> rtn = new ArrayList<List<Sentences>>();
		rtn.add(subSets);
		rtn.add(compSets);
		return rtn;
	}

	/**
	 * Add sentence to the set.
	 * 
	 * @param sentence
	 *            The sentence to add.
	 */
	public void addSentence(Sentence sentence)
	{
		sentences.add(sentence);
	}

	public List<Sentence> getSentences()
	{
		return this.sentences;
	}

	public String getToken(int sentenceIndex, int lineIndex, int tokenIndex)
	{
		return this.sentences.get(sentenceIndex)
				.getToken(lineIndex, tokenIndex);
	}

	public void setToken(int sentenceIndex, int lineIndex, int tokenIndex,
			String newToken)
	{
		this.sentences.get(sentenceIndex).setToken(lineIndex, tokenIndex,
				newToken);
	}

	public Sentence removeSentence(int sentenceIndex)
	{
		return this.sentences.remove(sentenceIndex);
	}

	public void removeDocStarts()
	{
		List<Sentence> newSentences= new ArrayList <Sentence>();
		for (Sentence sentence:sentences){
			if(!sentence.isDocStart()){
				newSentences.add(sentence);
			}
		}
		sentences=newSentences;
	}

}
