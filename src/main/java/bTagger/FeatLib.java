package bTagger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * Library of Features
 * 
 * @author Andrea Gesmundo
 *
 */
public class FeatLib{

	//constants
	private final int FEAT_HASH_INIT=1000000;

	//variables
	public Map<String, Integer> feat2id;
	public ArrayList<Feat> id2feat;

//	////////////////////////////////////////////////////////////
//	Constructors

	public FeatLib(){
		feat2id = new HashMap<String, Integer>(FEAT_HASH_INIT);
		id2feat = new ArrayList<Feat>(FEAT_HASH_INIT);
	}

	public FeatLib(FeatLib org){
		this();
		for (int i=0; i<org.id2feat.size(); i++){
			Feat feat = org.id2feat.get(i);
			this.id2feat.add(new Feat(feat));
			this.feat2id.put(feat.featstr, new Integer(i));
		}
	}

//	////////////////////////////////////////////////////////////

	public int size(){
		return id2feat.size();
	}
	
	public void trimToSize(){
		id2feat.trimToSize();
	}
	
	/**
	 * Set the weights from the training result.
	 * 
	 * @param id 		The ID of the feature to update.
	 * @param weight	The the weight of the feature.
	 */
	public void setWeight(int id, double weight){
		Feat feat = id2feat.get(id);
		feat.weight = weight;
	}

	/**
	 * Register a set of feature.
	 * 
	 * @param feats 	A List of name of feature.
	 */
	public void regFeat(List<String> feats){
		for (int i=0; i<feats.size(); i++){
			regFeat(feats.get(i));
		}
	}

	/**
	 * Add a feature to the library. or increase the frequency if already exist.
	 * 
	 * @param feat 	Name of the feature.
	 * @return		The index of the feature.
	 */
	public int regFeat(String feat){
		Integer id = (Integer) feat2id.get(feat);
		//if already in the lib
		if (id != null) {
			int idx = id.intValue();
			id2feat.get(idx).freq += 1;//increase the frequency
			return idx;
		}
		//if not in the lib update the HT and the List
		id = new Integer(id2feat.size());
		feat2id.put(feat, id);
		Feat newfeat = new Feat(feat);
		newfeat.freq = 1;
		id2feat.add(newfeat);	
		return id.intValue();
	}

	/**
	 * Get the ID from the feature name.
	 * 
	 * @param feat 	The feature name.
	 * @return		The feature ID, return -1 if the feature doesn't exist.
	 */
	private int getFeatID(String feat){
		Integer id = (Integer) feat2id.get(feat);
		if (id != null) {
			return id.intValue();
		} 
		return -1;
	}

	/**
	 * Get weight from the feature name.
	 *  
	 * @param feat	The feature name.
	 * @return		The feature weight, return 0 if the feature does't exist.
	 */
	public double getWeight(String feat){	
		int feaid = getFeatID(feat);
		if (feaid == -1) 
			return 0;
		return id2feat.get(feaid).weight;
	}

	/**
	 * Get the score (sum of weights) of a set of features.
	 * 
	 * @param feats	 	A List of feature names.
	 * @return			The sum of the weights of the features selected.
	 */
	public double getScore(List<String> feats){
		double score = 0;
		for (int i=0; i<feats.size(); i++){
			int feaid = getFeatID(feats.get(i));
			if (feaid != -1) 
				score += id2feat.get(feaid).weight;
		}
		return score;
	}

	/**
	 * 
	 * 
	 * @param feats
	 * @param allround
	 * @return
	 */
	public double getVotedScore(List<String> feats, int allround){
		double score = 0;
		for (int i=0; i<feats.size(); i++){
			int feaid = getFeatID(feats.get(i));
			if (feaid != -1){
				Feat feat = id2feat.get(feaid);
				score += feat.updateCmlwt(allround);
			}
		}
		return score;
	}

	public void updateFeat(Map<String, Integer> featval, double para, int allround){
		Iterator<String> featenu = featval.keySet().iterator();
		while (featenu.hasNext()){
			String onefeat = featenu.next();
			int val = featval.get(onefeat).intValue();
			if (val != 0){

				int feaid = getFeatID(onefeat);
				if (feaid == -1){
					feaid = regFeat(onefeat);
				}  
				Feat feat = id2feat.get(feaid);
				feat.updateCmlwt(allround);
				feat.weight += val*para;	

			}
		}
	}


	public void updateFeat(List<String> feats, double para, int allround){

		// Debug
		if (para == 0.0){
			System.err.println("*** ZERO UPDATING***");
		}

		for (int i=0; i<feats.size(); i++){
			String onefeat = feats.get(i);
			int feaid = getFeatID(onefeat);
			if (feaid == -1){
				feaid = regFeat(onefeat);
			}  
			Feat feat = id2feat.get(feaid);
			feat.updateCmlwt(allround);
			feat.weight += para;		    
		}
	}


	public void listWeight(){
		System.err.println("list weights :");

		for (int i=0; i<id2feat.size(); i++){
			System.err.println(""+i+" "+id2feat.get(i).featstr+" "+id2feat.get(i).weight);
		}

		// 	for (int i=0; i<id2feat.size(); i++){
		// 	    System.out.println("v"+i+" "+id2feat.get(i).featstr+" "
		// 			       +id2feat.get(i).updateCmlwt(PTrain.allround));
		// 	}
	}


	public void saveWeight(String filename, int round){

//			PrintWriter sout = new PrintWriter (new FileOutputStream(filename+".wt"));
//			for (int i=0; i<id2feat.size(); i++){
//			sout.println(""+i+" "+id2feat.get(i).featstr+" "+id2feat.get(i).weight);
//			}
//			sout.close();

			PrintWriter out = new PrintWriter (FileUtils.getWriter(filename));
			for (int i=0; i<id2feat.size(); i++){
				out.println(/*""+i+" "+*/id2feat.get(i).featstr+" "+
						id2feat.get(i).updateCmlwt(round));
			}
			out.close();
			System.out.println("\nOUT: Feature weights saved in: "+filename);

	}


	public void updateCmlwt(int round){
		for (int i=0; i<id2feat.size(); i++){
			id2feat.get(i).updateCmlwt(round);
		}
	}


	public void useVotedFeat(int round){
		for (int i=0; i<id2feat.size(); i++){
			Feat fea = id2feat.get(i);
			fea.updateCmlwt(round);
			fea.weight = fea.cmlwt;
		}
	}

}

class Feat{

	/** Name of the feature*/
	public String featstr;
	/** Weight of the feature*/
	public double weight;
	/** Frequency in the training data*/
	public int freq;
	/** The last round of updating*/
	public int update;
	/** Cumulative weight*/
	public double cmlwt; // 

	public Feat(String str){
		featstr = str;
		weight = 0;
		freq = 0;
		update = 0;
		cmlwt = 0;
	}

	public Feat(Feat org){
		featstr = org.featstr;
		weight = org.weight;
		freq = org.freq;
		update = org.update;
		cmlwt = org.cmlwt;
	}

	/**
     update cmlwt according to the current allround
	 */
	public double updateCmlwt(int allround){
		cmlwt += (allround - update) * weight;
		update = allround;
		return cmlwt;
	}

}
