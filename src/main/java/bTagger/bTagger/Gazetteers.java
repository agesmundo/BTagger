package bTagger;

import java.util.ArrayList;
import java.util.List;

public class Gazetteers
{
	List<Gazetteer> gazetteers;
	
	public Gazetteers(){
		gazetteers = new ArrayList<Gazetteer>();
	}
	
	public Gazetteers(List<Gazetteer> gazets){
		gazetteers = gazets;
	}
	
	public void add(Gazetteer gazet){
		gazetteers.add(gazet);
	}
	
	public Gazetteer get(int index){
		return gazetteers.get(index);
	}
	
	public int size(){
		return gazetteers.size();
	}
}
