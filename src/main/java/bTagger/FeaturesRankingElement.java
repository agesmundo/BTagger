package bTagger;
/**
 * Class used to store the priority queue with the ordered list of the 
 * results of the automatic research of new features.
 * 
 * @author Andrea Gesmundo
 */
public class FeaturesRankingElement implements Comparable<FeaturesRankingElement>{
	Feature feature;
	double score;
	
	public FeaturesRankingElement(Feature feat, double scr){
		feature=feat;
		score=scr;
	}
	
	public int compareTo(FeaturesRankingElement otherFRE){
		if (this.score>otherFRE.score)return -1;
		if (this.score<otherFRE.score)return 1;
		return 0;
	}
	public String toString(){
		return feature+", score: "+score;
	}
}
