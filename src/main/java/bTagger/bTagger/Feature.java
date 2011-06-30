package bTagger;

import java.util.List;
import java.util.ArrayList;


/**
 * Representation of a feature.
 * 
 * @author Andrea Gesmundo
 *
 */
public class Feature {
	FeatureDetail details[];
	int leftEdge;
	int rightEdge;
	Context context ;

//	/////////////////////////////////////////////////////////////////
//	CONSTRUCTORS

	public Feature(FeatureDetail d[], Context context){
		this.context=context;
		details=d;
		computeEdges();//compute left and right edges
		order();//order the details to have an unambiguous representation of the feature
	}
	
	/**
	 * Initialize the instance using a feature matrix.
	 * 
	 * @param fm	the feature matrix with the info about which detail belong to the feature.
	 */
	public Feature(FeatureMatrix fm, Context context){
		this.context=context;
		int range=context.NGRAM-1;
		List <FeatureDetail> det=new ArrayList<FeatureDetail>();
		//check bits relative to left side of mainTagDetails
		for(int i=0;i<range;i++){
			if(fm.mainTagDetails[i]==true)det.add(new FeatureDetail(context.mainTag,-1*(i+1),null,context));
		}
		//check bits relative to right side of mainTagDetails
		for(int i=0;i<range;i++){
			if(fm.mainTagDetails[i+range]==true)det.add(new FeatureDetail(context.mainTag,(i+1),null,context));
		}
		//check bit relative to the center of addTagDetails
		if(fm.addTagDetails[0]==true)det.add(new FeatureDetail(context.researchTag,0,null,context));
		//check bits relative to addTagDetails
		for(int i=1;i<=range;i++){
			if(fm.addTagDetails[i]==true)det.add(new FeatureDetail(context.researchTag,-1*i,null,context));
		}
		//check bits relative to addTagDetails
		for(int i=1;i<=range;i++){
			if(fm.addTagDetails[i+range]==true)det.add(new FeatureDetail(context.researchTag,i,null,context));
		}
		details=det.toArray(new FeatureDetail[det.size()]);
		computeEdges();
	}

//	///////////////////////////////////////////////////////////////////////

	/**
	 * compute the edges of the area of the window needed by this feature.
	 */
	public void computeEdges(){
		leftEdge=0;
		rightEdge=0;
		for (int i=0;i<details.length;i++){
			if(details[i].isOnLeft && details[i].distance>leftEdge)leftEdge=details[i].distance;
			if(details[i].isOnRight && details[i].distance>rightEdge)rightEdge=details[i].distance;
		}
	}

	/**
	 * order the details to have an unambiguous representation of the feature
	 * priority on tags: mainTag!, W, additional tags in lexicographic order
	 * among same tags used priority on position: 0,-1...-n,1...n
	 */
	public void order(){
		FeatureDetail tmp;
		List <FeatureDetail> orderedDetails=new ArrayList<FeatureDetail>();
		String currentName="";
		//flow the names
		for(int j=0;j<context.orderedAddTagsNames.size()+2;j++){
			if(j==0)currentName=context.mainTag;
			else if(j==1)currentName="W";
			else currentName=context.orderedAddTagsNames.get(j-2);
			//flow all possible positions and check if there is the corresponding detail
				//center
				tmp=getDetail(currentName,0,false);
				if(tmp!=null)orderedDetails.add(tmp);
				//left
				for(int k=1;k<context.NGRAM;k++){
					tmp=getDetail(currentName,k,true);
					if(tmp!=null)orderedDetails.add(tmp);
				}
				//right
				for(int k=1;k<context.NGRAM;k++){
					tmp=getDetail(currentName,k,false);
					if(tmp!=null)orderedDetails.add(tmp);
				}
		}
		details=orderedDetails.toArray(new FeatureDetail[orderedDetails.size()]);
	}
	
	/**
	 * Return a detail with the requested FDetail if exist.
	 * 
	 * @param name 		The name of the requested FDetail.
	 * @param offset	The offset of the requested FDetail.
	 * @param isOnLeft 	true if the requested FDetail is on the left.
	 * @return			null if the requested FDetail doesn't exist, the instance otherwise.
	 */
	private FeatureDetail getDetail(String name,int offset, boolean isOnLeft){
		for(int i=0;i<details.length;i++){
			if(details[i].isOnLeft==isOnLeft && details[i].distance==offset && details[i].tagName.compareTo(name)==0)return details[i];
		}
		return null;
	}
	
	public String toString(){
		String ret="[";
		for(int i=0;i<details.length;i++){
			if(i>0)ret+=",";
			ret+=details[i].tagName+":";
			if(details[i].isOnLeft)ret+=-1*details[i].distance;
			else ret+=details[i].distance;
		}
		return ret+"]";
	}
	
}
