package corpus;

import java.util.ArrayList;
import java.util.List;

public class Sentence
{
	List<Line> lines = new ArrayList<Line>();
	
	final public static int WORDS_COLUMN_ID  = 0;

//	///////////////////////////////////////////////////////
//	CONSTRUCTORS
	
	public Sentence() {
		lines = new ArrayList<Line>();
	}

	public Sentence(List<String> words) {
		for(String word : words){
			lines.add(new Line(word));
		}
	}

// ///////////////////////////////////////////////////////////

	public int size()
	{
		return lines.size();
	}

	public Line getLine(int j)
	{
		if (j < 0 || j >= lines.size())
			return null;
		return lines.get(j);
	}

	public List<Line> getLines()
	{
		return lines;
	}

	public void addLine(Line newLine)
	{
		lines.add(newLine);
	}

	public void setAllOut(int columnID)	{
		for (int lineID = 0; lineID < lines.size(); lineID++) {
			lines.get(lineID).tokens.set(columnID, "O");
		}

	}

	public void addLine(List<String> tokens)
	{
		this.lines.add(new Line(tokens));

	}

	public String getToken(int lineIndex, int tokenIndex)
	{
		return this.lines.get(lineIndex).getToken(tokenIndex);
	}
	
	public void addColumns(int columnNumber)
	{
		for (Line line : this.lines) {
			line.addColumns(columnNumber);
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// toString methods
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0){
				ret.append("\n");
			}
			ret.append(lines.get(i).toString());
		}
		return ret.toString();
	}
	
	/**
	 * Convert the Sentence to String ignoring some columns.
	 * 
	 * @param columnsID		Columns ID of the columns to ignore.
	 * @return				A String representing the Sentence with the columns not ignored.
	 */
	public String toStringDelColumns(List<Integer> columnsID)
	{
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0){
				ret.append("\n");
			}
			ret.append(lines.get(i).toStringDelColumns(columnsID));
		}
		return ret.toString();
	}
	
	/**
	 * Convert the Sentence to String ignoring a column.
	 * 
	 * @param columnsID		Column ID of the column to ignore.
	 * @return				A String representing the Sentence with the columns not ignored.
	 */
	public String toStringDelColumn(int columnID)
	{
		List<Integer> columnsID= new ArrayList<Integer>();
		columnsID.add(new Integer(columnID));
		return this.toStringDelColumns(columnsID);
	}
	
	/**
	 * Convert the Sentence to String printing only some columns.
	 * 
	 * @param columnsID 	Columns ID of the columns to print.
	 * @return				A String representing the Sentence with the selected columns.
	 */
	public String toStringSelColumns(List<Integer> columnsID){
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0){
				ret.append("\n");
			}
			ret.append(lines.get(i).toStringSelColumns(columnsID));
		}
		return ret.toString();
	}
	
	/**
	 * Convert the Sentence to String printing only a column.
	 * 
	 * @param columnsID		Column ID of the column to print.
	 * @return				A String representing the column of the Sentence to print.
	 */
	public String toStringSelColumn(int columnID)
	{
		List<Integer> columnsID= new ArrayList<Integer>();
		columnsID.add(new Integer(columnID));
		return this.toStringSelColumns(columnsID);
	}
	
	/**
	 * Print the words of the sentence as plain text.
	 * 
	 * @return 	The words of the sentence as plain text
	 */
	public String toStringPlainText()
	{
		return this.toStringSelColumn(WORDS_COLUMN_ID).replaceAll("\n", " ");
	}

	/**
	 * Print the words of a chunk.
	 * 
	 * @param chunk 	The chunk we wants the plain text.
	 * @return 	The words of the sentence as plain text
	 */
	public String toStringPlainText(ChunkData chunk)
	{
		return toStringPlainText(chunk.getChunkStart(), chunk.getChunkEnd());
	}

	/**
	 * Print the words from a range of lines.
	 * 
	 * @param startLine 	Index of the starting line.
	 * @param endLine		Index of the ending line.
	 * @return				A String containing the words in the range of lines.
	 */
	public String toStringPlainText(int startLine, int endLine)
	{
		StringBuilder chunkPlainText= new StringBuilder();
		for(int lineIndex=startLine;lineIndex<=endLine;lineIndex++){
			if(lineIndex>startLine){
				chunkPlainText.append(" ");
			}
			chunkPlainText.append(getToken(lineIndex , WORDS_COLUMN_ID));
		}
		return chunkPlainText.toString();
	}

	public void setToken(int lineIndex, int tokenIndex, String newToken)
	{
		this.lines.get(lineIndex).setToken(tokenIndex,newToken);
	}

	public List<String> getColumn(int columnID)
	{
		List <String> column = new ArrayList<String>();
		for (Line line: lines){
			column.add(line.getToken(columnID));
		}
		return column;
	}

	public boolean isDocStart()
	{
		if(lines.size()==1 && getToken(0, 0).equals("-DOCSTART-")){
			return true;
		}
		return false;
	}
	
////////////////////////////////////////////////////////////////////////////////


}
