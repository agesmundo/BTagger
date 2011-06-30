package bTagger;
/**
 * Class used to store a label and its ID.
 * 
 * @author Andrea Gesmundo
 */
public class Label {

	/** The label form*/
	public String lbl;
	/** The label ID*/
	public int id;		

	/**
	 * Called by SLabelLib.
	 */
	public Label(String l, int i){
		lbl = new String(l);
		id = i;
	}

	public String toString(){
		return lbl;
	}

	/**
	 * To create a temp label.
	 */	
	public Label(String l){
		lbl = new String(l);
		id = -1;
	}
}
