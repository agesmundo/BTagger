package corpus;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.*;

public class SentenceLib{

	private static int SENTENCE_HASH_INIT = 1000;

	private Hashtable<String, Integer> string2id;
	private Sentences sentences;

	public SentenceLib(){
		sentences=new Sentences();
		string2id = new Hashtable<String, Integer>(SENTENCE_HASH_INIT);
	}

	public SentenceLib(String fileName) throws FileNotFoundException{
		sentences=new Sentences(fileName);
		string2id = new Hashtable<String, Integer>(SENTENCE_HASH_INIT);
		for(int i=0; i <sentences.size();i++){
			String currentSentenceString = sentences.getSentence(i).toStringPlainText();
			if (contains(currentSentenceString)){
				System.out.println("SENTENCE REMOVED BECAUSE ALREADY IN THE SENTENCE LIB:\n"+currentSentenceString);
				sentences.removeSentence(i);
				i--;
			}
			else{
				string2id.put(currentSentenceString, new Integer(i));
			}
		}
	}
	
	/**
	 * Get the Sentence ID with a string
	 * if the Sentence is not in the Sentence Lib return null.
	 * 
	 * @param string	 
	 * @return
	 */
	public Integer getSentenceID(String string){
		Integer id = (Integer) string2id.get(string);
		if (id != null) {
			return id.intValue();
		}
		return null;
	}

	/**
	 * Check if is contained in the Lib a Sentence correspondent to the given String.
	 * 
	 * @param string	String to check.
	 * @return			True if there is a Sentence correspondent to the given String.
	 */
	public boolean contains(String string){
		Integer id = (Integer) string2id.get(string);
		if (id != null) {
			return true;
		}
		return false;
	}

	/**
	 * Add the sentence in the Lib.
	 * 
	 * @param senToAdd  The Sentence to add.
	 * @return			The ID of the sentence if added. null if the sentence is already in the Lib.
	 */
	public Integer addSentence(Sentence senToAdd){
		if (contains(senToAdd.toStringPlainText())){
			return null;
		}
		Integer id = new Integer(sentences.size());
		string2id.put(senToAdd.toStringPlainText(), id);
		sentences.addSentence(senToAdd);
		return id.intValue();
	}
	
	/**
	 * Remove the last sentence inserted in the Lib.
	 * 
	 * @return The sentence removed.
	 */
	public Sentence remLastSentence(){
		int senToRemID= sentences.size()-1;
		Sentence senToRem=sentences.removeSentence(senToRemID);
		string2id.remove(senToRem.toStringPlainText());
		return senToRem;
	}
	
	/**
	 * Remove the last 'sentencesNumber' sentences inserted in the Lib.
	 * 
	 * @param sentencesNumber 	The number of sentences to remove.
	 * @return The sentence removed.
	 */
	public void remLastSentences(int sentencesNumber){
		for(int i=0;i<sentencesNumber;i++){
			remLastSentence();
		}
	}

	/**
	 * Get the Sentence from a String.
	 * 
	 * @param str	A string representative of the Sentence.
	 * @return		The sentence correspondent to the String, or null if there is not such a Sentence in the Lib.
	 */
	public Sentence getSentence(String str){
		Integer sentenceID =getSentenceID(str);
		if (sentenceID==null){
			return null;
		}
		return sentences.getSentence(sentenceID);
	}

	/**
	 * Get Sentence with ID.
	 * 
	 * @param id	ID of the Sentence to get.
	 * @return		The Sentence correspondent of the ID.
	 */
	public Sentence getSentence(int id){
		return sentences.getSentence(id);
	}

	/**
	 * Get the number of sentences in the Lib.
	 * 
	 * @return 	the number of sentences in the Lib
	 */
	public int size(){
		return sentences.size();
	}

	/**
	 * Write the Sentence Lib in a file.
	 * 
	 * @param filename 	The file name.
	 */
	public void saveFile(String fileName, Charset cs){
		this.sentences.saveFile(fileName, cs);
	}
	
	/**
	 * Write the Sentence Lib in a file.
	 * 
	 * @param filename 	The file name.
	 */
//	public void saveFile(String fileName){
//		this.sentences.saveFile(fileName);
//	}

	public Sentences getSentences(){
		return this.sentences;
	}

	public void setColumnWithChunksCoords(ChunksCoords chunksCoords,
			int columnID)
	{
		sentences.setColumnWithChunksCoords(chunksCoords,columnID);
	}

	public Sentence getLastSentence()
	{
		return sentences.getSentence(sentences.size()-1);
	}

}
