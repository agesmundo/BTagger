package bTagger;

import java.util.List;


/**
 * A Socket visible from the left side or the right side
 * 
 * @author Andrea Gesmundo
 */
public class Socket{

	public final List<Label> sktLabel;

	public Socket(List<Label> ss){
		sktLabel = ss;
	}

	/**
	 * Compare the current Socket with the parameter.
	 * 
	 * @param o 	The object to compare.
	 * @return 		True if the object is a BSocket equal to the current.
	 */
	public boolean equals(Object o){
		Socket s = (Socket) o;
		if (sktLabel.size() != s.sktLabel.size()) return false;
		for (int i=0; i<sktLabel.size(); i++){
			if (sktLabel.get(i) != s.sktLabel.get(i)) return false; //? the same ref to Slabel??doesn't compare the fields
		}
		return true;
	}

	/**
	 * Generate an hashcode for the current BSocket.
	 * 
	 * @return 	The generated hashcode
	 */
	public int hashCode(){
		int code = 0;
		for (int i=0; i<sktLabel.size(); i++){
			code = code*64 + sktLabel.get(i).id;
		}
		return code;
	}

	/**
	 * Print the object.
	 * 
	 * @return 	A representation of the object.
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer("<");
		for (int i=0; i<sktLabel.size(); i++){
			if (i > 0) sb.append(", ");
			sb.append(sktLabel.get(i));
		}
		sb.append(">");
		return sb.toString();
	}

}

