package corpus;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Line
{
	public List<String> tokens;

// ///////////////////////////////////////////////////////////////////
//	CONSTRUCTORS
	
	Line(String line) {
		this.tokens = new Vector<String>(Arrays.asList(line.trim()
				.split("\\s+")));
	}

	Line(List<String> tokensArg) {
		this.tokens = tokensArg;
	}

// ///////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		String ret = "";
		for (int i = 0; i < tokens.size(); i++) {
			if (i > 0)
				ret += " ";
			ret += tokens.get(i);
		}
		return ret;
	}

	/**
	 * Convert the Line to String ignoring some columns.
	 * 
	 * @param columnsID		Columns ID of the columns to ignore.
	 * @return				A String representing the Line with only the columns not ignored.
	 */
	public String toStringDelColumns(List<Integer> columnsID)
	{
		StringBuilder ret = new StringBuilder();
		boolean isNotFirst=false;
		for (int i = 0; i < tokens.size(); i++) {

			if (!columnsID.contains(i)) {
				if (isNotFirst) {
					ret.append(" ");
				}
				ret.append(tokens.get(i));
				isNotFirst=true;
			}
		}
		return ret.toString();
	}

	/**
	 * Convert the Line to String printing only some columns.
	 * 
	 * @param columnsID 	Columns ID of the columns to print.
	 * @return				A String representing the Line with only the selected columns.
	 */
	public String toStringSelColumns(List<Integer> columnsID)
	{
		StringBuilder ret = new StringBuilder();
		boolean isNotFirst=false;
		for (int i = 0; i < tokens.size(); i++) {
			if (columnsID.contains(i)) {
				if (isNotFirst) {
					ret.append(" ");
				}
				ret.append(tokens.get(i));
				isNotFirst=true;
			}
		}
		return ret.toString();
	}

	public String getToken(int columnID)
	{
		return tokens.get(columnID);
	}

	/**
	 * Removing the token correspondent to a column.
	 * 
	 * @param columnID
	 *            The column ID of the token to remove.
	 */
	public void removeColumn(int columnID)
	{
		tokens.remove(columnID);
	}

	public int size()
	{
		return tokens.size();
	}

	public void addColumns(int columnNumber)
	{
		for(int i=0 ; i<columnNumber;i++){
			tokens.add(new String());
		}
	}

	public void setToken(int tokenIndex, String newToken)
	{
		tokens.set(tokenIndex, newToken);
	}

}