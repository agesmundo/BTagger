package bTagger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DocumentGazetteers
{
	Map <String, Gazetteer> lblToGazet;
	final boolean csMainWord;
	final boolean csContextWords;
	int size=1;

	public DocumentGazetteers(boolean csmw, boolean cscw){
		lblToGazet=new HashMap<String, Gazetteer>();
		csMainWord=csmw;
		csContextWords=cscw;
	}

	public void clear(){
		lblToGazet=new HashMap<String, Gazetteer>();
	}

	public void addEntries(String [] words, String [] labes){
		Gazetteer gazet;
		int srcPos;
		int length;
		String chunkWords[];
		for (int i =0; i<words.length;i++){
			if (/*labes[i].equals(LabelsUtility.outLabel) ||*/ LabelsUtility.isClassStart(labes,i)){
				gazet =getLblGazet(LabelsUtility.getClassName(labes[i]));
				srcPos = i;
				length=1;
				
				while(!(/*labes[i].equals(LabelsUtility.outLabel) ||*/ LabelsUtility.isClassEnd(labes, i))){
					length++;
					i++;
				}
				
				chunkWords=new String[length];
				System.arraycopy(words, srcPos, chunkWords, 0, length);
				gazet.addEntry(chunkWords);
			}
		}
	}

	public Gazetteer getLblGazet(String lbl){
		Gazetteer rtn = lblToGazet.get(lbl);
		if(rtn!=null){
			return rtn;
		}
		Gazetteer gazet = new Gazetteer(csMainWord,csContextWords);
		lblToGazet.put(lbl, gazet);
		return gazet;
	}
	
	public Set<String> keySet(){
		return lblToGazet.keySet();
	}

}
