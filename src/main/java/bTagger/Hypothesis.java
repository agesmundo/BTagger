package bTagger;

import java.util.ArrayList;
import java.util.List;


/**
   A hypothesis
*/
public class Hypothesis{

  public static double MARGIN_RATE;
  public static boolean training = false;

  public Island island;
    
  public Label lastLabel; //label of the last action?

  public int socketIDFromLeft; //left context socket
  public int socketIDFromRight;//right context socket

  private double labelScore;
  private double contextScore;
  private double hypoScore;

  public boolean isgold = false;
  public int mistake = 0;

  public List<String> features;

  /**
     Initiate with island and label
  */
  public Hypothesis(Island i, Label l){	
	island = i;	
	lastLabel = l;
	features = new ArrayList<String>();
  }
    
  /**
     Computer labelScore and hypoScore
  */
  public void compLblTtlScores(FeatLib featlib){
	labelScore = featlib.getScore(features);
	hypoScore = labelScore + contextScore;
  }

  public void setContextScore(double score){
	contextScore = score;
  }

  public double getLabelScore(){
	return labelScore;
  }

  public double getHypoScore(){
	return hypoScore;
  }

  public double getLabelScoreMGN(){
	if (training){
      //double islandlen = island.rightBoundPosi - island.leftBoundPosi + 1;
//      return labelScore + MARGIN_RATE * (mistake + mistake / islandlen);
      // CONSERVATIVE negative sample selection
      return labelScore + MARGIN_RATE;
    }
	return labelScore;
  }

  public double getHypoScoreMGN(){

    /*
      A      |
       \g1   V
        ---pqx (step t)
       /h1      
      B      

      A
       \g2
        ----qx (step t+1)
       /h2
      B

      score(g2) = score(g1) + score(x|pq)
      score(h2) = score(h1) + score(x|pq)
      score(g1) >= score(h1) + m(t)
      socre(g2) > score(h2) + m(t+1)
      
      => m(t+1) < m(t) => mistake/islandlen is introduced
    */
    if (training){
      double islandlen = island.rightBoundPosi - island.leftBoundPosi + 1;
      return hypoScore + MARGIN_RATE * (mistake + mistake / islandlen);
    }
	return hypoScore;
  }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(contextScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		temp = Double.doubleToLongBits(hypoScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (isgold ? 1231 : 1237);
		result = prime * result + ((island == null) ? 0 : island.hashCode());
		temp = Double.doubleToLongBits(labelScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((lastLabel == null) ? 0 : lastLabel.hashCode());
		result = prime * result + mistake;
		result = prime * result + socketIDFromLeft;
		result = prime * result + socketIDFromRight;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Hypothesis other = (Hypothesis) obj;
		if (Double.doubleToLongBits(contextScore) != Double
				.doubleToLongBits(other.contextScore)) {
			return false;
		}
		if (features == null) {
			if (other.features != null) {
				return false;
			}
		} else if (!features.equals(other.features)) {
			return false;
		}
		if (Double.doubleToLongBits(hypoScore) != Double
				.doubleToLongBits(other.hypoScore)) {
			return false;
		}
		if (isgold != other.isgold) {
			return false;
		}
		if (island == null) {
			if (other.island != null) {
				return false;
			}
		} else if (!island.equals(other.island)) {
			return false;
		}
		if (Double.doubleToLongBits(labelScore) != Double
				.doubleToLongBits(other.labelScore)) {
			return false;
		}
		if (lastLabel == null) {
			if (other.lastLabel != null) {
				return false;
			}
		} else if (!lastLabel.equals(other.lastLabel)) {
			return false;
		}
		if (mistake != other.mistake) {
			return false;
		}
		if (socketIDFromLeft != other.socketIDFromLeft) {
			return false;
		}
		if (socketIDFromRight != other.socketIDFromRight) {
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "Hypothesis[" //
				+ "lastLabel=" + lastLabel //
				+ ",socketIDFromLeft=" + socketIDFromLeft //
				+ ",socketIDFromRight=" + socketIDFromRight //
				+ ",isgold=" + isgold //
				+ ",mistake=" + mistake //
				+ ",hypoScore=" + getHypoScore() //
				+ ",labelScore=" + getLabelScore() //
				+ ",contextScore=" + contextScore //
				+ ",features=" + features + "]";
	}
}

