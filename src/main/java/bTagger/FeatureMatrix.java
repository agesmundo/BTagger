package bTagger;
/**
 * a matrix that represent a feature
 * ex [NP:0,NP:-1,NE:-1]
 * 				index:	-2	-1 	0	1	2		
 * mainTagDetails  	NE	F	T	x	F 	F
 * addTagDetails	NP	F	T	T	F	F
 * 
 * index map for mainTagDetails: 0->-1,1->-2,2->1,3->2
 * index map for addTagDetails: 0->0,1->-1,2->-2,3->1,4->2
 * 
 * @author Andrea Gesmundo
 *
 */
public class FeatureMatrix {
	boolean mainTagDetails[];
	boolean addTagDetails[];    
	
//	////////////////////////////////////////////////////////////////////
//	CONSTRUCTORS

	public FeatureMatrix(int seed, int ngram){
		int range=ngram-1;
		int bitSelector=1;
		mainTagDetails=new boolean[2*range];
		addTagDetails=new boolean[2*range+1];
		//check bits relative to mainTagDetails
		for(int i=0;i<2*range;i++){
			if((bitSelector&seed)!=0)mainTagDetails[i]=true;
			bitSelector<<=1;
		}
		//check bits relative to addTagDetails
		for(int i=0;i<2*range+1;i++){
			if((bitSelector&seed)!=0)addTagDetails[i]=true;
			bitSelector<<=1;
		}
	}
	
//	/////////////////////////////////////////////////////////////////////
}
