package bTagger;

import java.util.ArrayList;
import java.util.List;


/**
 * Group of Tag Sample
 * 
 * @author Andrea Gesmundo
 *
 */
public class TagSamples{
	List<TagSample> samples;
	List<Integer> documentsStartID;
	/** gazetteers for document level features;  docID -> columIDN -> gazet*/
	List<List<DocumentGazetteers>> documentGazetteers;
	
	public TagSamples(){
		samples=new ArrayList<TagSample>();
		documentsStartID=new ArrayList<Integer>();
	}
	
	public TagSamples(List<TagSample> s){
		samples=s;
	}

	public List<TagSample> getSamples(){
		return samples;
	}

	/**
	 * Move the tagged column in an additional tag column
	 */
	public TagSamples moveTaggedColumn(){
		List<TagSample> newSamples=new ArrayList<TagSample>();
		for(int i=0;i<samples.size();i++){
			TagSample currentSample=samples.get(i);
			TagSample newSample =new TagSample(currentSample.words);
			//convert add tag
			newSample.addTags=new String[currentSample.addTags.length+1][];
			for (int j=0;j<currentSample.addTags.length;j++){
				newSample.addTags[j]=currentSample.addTags[j];
			}
			newSample.addTags[newSample.addTags.length-1]=new String [currentSample.words.length];
			for(int k=0;k<currentSample.words.length;k++){
				newSample.addTags[newSample.addTags.length-1][k]=currentSample.tags[k].lbl;
			}
			newSamples.add(newSample);
		}
		return new TagSamples(newSamples);
	}


///////////////////////
//WRAPPER LIST METHOD
	public int size(){
		return samples.size(); 
	}
	
	public TagSample get (int id){
		return samples.get(id);
	}
	
	public void add(TagSample sample){
		samples.add(sample);
	}
	
	public void clear(){
		samples.clear();
		documentsStartID.clear();
	}
	
//
	
	//////////////////////////////////////
	//Document features
	public void addDocumentStartID(){
		documentsStartID.add(samples.size());	
	}
	
	public int documentsNumber()
	{		
		return documentsStartID.size();
	}
	
	public TagSample getUntaggedCopy(int id){
		return new TagSample(samples.get(id));
	}
	//

}
