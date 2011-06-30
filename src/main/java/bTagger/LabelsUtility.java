package bTagger;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods class
 * Used to calculate phrase metrics for the IOB tag format used in chunking,
 * Begin tags must start with the character 'B'
 * Inside tags must start with the character 'I'
 * Outside tags must start with the character 'O'
 * On the following characters there are no constraints.
 * Tested examples: 
 * 	NP chunking: [O,I-NP,B-NP]
 *  NER chunking: [O,I-MISC,I-LOC,I-PERI-,ORG,B-MISC,B-LOC,B-PER,B-ORG]*
 *   
 * @author Andrea Gesmundo
 */
public class LabelsUtility {

	public static final String outLabel = "O";
	public static final String beginPrefix = "B-";
	public static final String insidePrefix = "I-";
	public static final String endPrefix = "E-";
	public static final String singlePrefix = "S-";
	public static final String divider = "-";
	/**
	 * Compute B-tag level metrics.
	 * 
	 * @return 	An array containing the B-tag level metrics {predB,goldB,matchB,bTaggedStartingI,startingITaggedB}
	 */
	public static int[]  BMetrics(TagSample gold,TagSample predict){
		int predB=0,goldB=0,matchB=0,bTaggedStartingI=0,startingITaggedB=0;
		//detect a new phrases each B tag or each O-I transition
		for(int j=0; j<gold.words.length; j++){
			if(gold.tags[j].lbl.startsWith(beginPrefix)){
				goldB++;
				if(isClassStart(predict,j)&&!predict.tags[j].lbl.startsWith(beginPrefix))bTaggedStartingI++;
			}
			if(predict.tags[j].lbl.startsWith(beginPrefix)){
				predB++;
				if(gold.tags[j].lbl.startsWith(beginPrefix))matchB++;
				if(isClassStart(gold,j)&&!gold.tags[j].lbl.startsWith(beginPrefix))startingITaggedB++;
			}	
		}
		int ret[]={predB,goldB,matchB,bTaggedStartingI,startingITaggedB};
		return ret;
	}

	/**
	 * Compute phrase level metrics.
	 * 
	 * @return 	An array containing the metrics {predPhrases,goldPhrases,matchPhrases}
	 */
	public static int[]  chunksMetrics(TagSample gold,TagSample predict){
		int predPhrases=0,goldPhrases=0,matchPhrases=0;
		List<Integer> startingPhrasesPositions=new ArrayList<Integer>();
		
		for(int j=0; j<gold.words.length; j++){
			if(isClassStart(predict,j))predPhrases++;
			if(isClassStart(gold,j)){
				goldPhrases++;
				startingPhrasesPositions.add(j); //store the position to compute the number of matches later
			}	
		}

		//compute the number of matches
		int pointer=0;
		boolean flagSuccessfulLoop;
		for(int j=0;j<startingPhrasesPositions.size();j++){
			flagSuccessfulLoop=true;
			pointer=startingPhrasesPositions.get(j);
			//if at this index there is not a start also in the predicted tag sequence, skip
			if(!isClassStart(predict,pointer))continue;
			//if are not of the same type, skip
			if(!isSameClass(predict.tags[pointer].lbl,gold.tags[pointer].lbl))continue;
			//until the end of the chunk is reached in the gold tag sequence
			while(!isClassEnd(gold,pointer)){
				//check that at this position there is not an end also in the predicted tag sequence
				if(!isClassEnd(predict,pointer)
						//of the same NE class
						&&isSameClass(predict.tags[pointer].lbl,gold.tags[pointer].lbl)){
					pointer++;
				}
				//otherwise exit and flag
				else{
					flagSuccessfulLoop=false;
					break;
				}
			}
			//if the previous loop was successful
			if((flagSuccessfulLoop)
					//and there is an end of the also in the predicted tag sequence 
					&& isClassEnd(predict,pointer)
					//and the end is of the same NE category then there is a MATCH!
					&& isSameClass(predict.tags[pointer].lbl,gold.tags[pointer].lbl)){
				matchPhrases++;		
			}
		}
		int ret[]={predPhrases,goldPhrases,matchPhrases};
		return ret;
	}

	/**
	 * Compute chunk level metrics for every single class
	 * 
	 * @return 	An array containing the metrics {predPhrases,goldPhrases,matchPhrases}
	 */
	public static int[]  chunkClassMetrics(TagSample gold,TagSample predict,String className){
		int predPhrases=0,goldPhrases=0,matchPhrases=0;
		List<Integer> startingPhrasesPositions=new ArrayList<Integer>();
		
		for(int j=0; j<gold.words.length; j++){
			if(isClassStart(predict,j)){
				if(isCorrespondentClass(predict.tags[j].lbl,className))predPhrases++;
			}
			if(isClassStart(gold,j)){
				if(isCorrespondentClass(gold.tags[j].lbl,className)){
					goldPhrases++;
					startingPhrasesPositions.add(j); //store the position to calculate the number of matches later
				}
			}	
		}

		//calculate the number of matches
		int pointer=0;
		boolean flagSuccessfulLoop;
		for(int j=0;j<startingPhrasesPositions.size();j++){
			flagSuccessfulLoop=true;
			pointer=startingPhrasesPositions.get(j);
			//if at this index there is not a start also in the predicted tag sequence, skip
			if(!isClassStart(predict,pointer))continue;
			//if are not of the same type, skip
			if(!isSameClass(predict.tags[pointer].lbl,gold.tags[pointer].lbl))continue;
			//until the end of the chunk is reached in the gold tag sequence
			while(!isClassEnd(gold,pointer)){
				//check that at this position there is not an end also in the predicted tag sequence
				if(!isClassEnd(predict,pointer)
						//of the same NE class
						&&isSameClass(predict.tags[pointer].lbl,gold.tags[pointer].lbl)){
					pointer++;
				}
				//otherwise exit and flag
				else{
					flagSuccessfulLoop=false;
					break;
				}
			}
			//if the previous loop was successful
			if((flagSuccessfulLoop)
					//and there is an end of the also in the predicted tag sequence 
					&& isClassEnd(predict,pointer)
					//and the end is of the same NE category then there is a MATCH!
					&& isSameClass(predict.tags[pointer].lbl,gold.tags[pointer].lbl)){
				if(isCorrespondentClass(predict.tags[pointer].lbl,className))matchPhrases++;		
			}
		}
		int ret[]={predPhrases,goldPhrases,matchPhrases};
		return ret;
	}
	
	/**
	 * Used to decide if a tag is at the beginning of a chunk.
	 * 
	 * @param tagSem 	The tag sequence.	
	 * @param pointer	Position of the tag.
	 * @return			True if the tag is at the beginning of a chunk.
	 */
	public static boolean isClassStart(TagSample tagSem,int pointer){
		//if the position indicated is outside the tag sample return false
		if (pointer<0 || pointer>=tagSem.tags.length)return false;

		if(pointer==0){
			return isClassStart(tagSem.tags[pointer].lbl,outLabel);
		}
		return isClassStart(tagSem.tags[pointer].lbl,tagSem.tags[pointer-1].lbl);
	}
	
	public static boolean isClassStart(String labels[],int pointer){
		//if the position indicated is outside the tag sample return false
		if (pointer<0 || pointer>=labels.length)return false;

		if(pointer==0){
			return isClassStart(labels[pointer],outLabel);
		}
		return isClassStart(labels[pointer],labels[pointer-1]);
	}
	
	/**
	 * Used to decide if a tag is at the beginning of a chunk.
	 * 
	 * @param label 		The tag.
	 * @param previousLabel The previous tag.	
	 * @return				True if the tag is at the beginning of a chunk.
	 */
	public static boolean isClassStart(String label,String previousLabel){
		//if the label is an O return false
		if(label.compareTo(outLabel)==0)return false;
		
		//if it is a word label and not a chunk label return true
		if(!label.contains(divider))return true;
		
		//if the previous label is O return true
		if(label.compareTo(outLabel)==0)return true;
		//if the current label is B return true
		if(label.startsWith(beginPrefix)) return true;
		//if the current label is S return true
		if(label.startsWith(singlePrefix)) return true;
		//if the previous label is E return true
		if(previousLabel.startsWith(endPrefix))return true;
		//if the previous label is S return true
		if(previousLabel.startsWith(singlePrefix))return true;
		//if the two label are the same in the same class return false
		if(isSameClass(label,previousLabel)) return false;
		//otherwise return true
		return true; 
	}
	
	/**
	 * Used to decide if a tag is at the end of a chunk.
	 * 
	 * @param tagSem 	The tag sequence.	
	 * @param pointer	Position of the tag.
	 * @return			True if the tag is at the end of a chunk.
	 */
	public static boolean isClassEnd(TagSample tagSem,int pointer){
		//if the position indicated is outside the tag sample return false
		if (pointer<0 || pointer>=tagSem.words.length)return false;

		if(pointer==tagSem.words.length-1){
			return isClassEnd(tagSem.tags[pointer].lbl,outLabel);
		}
		return isClassEnd(tagSem.tags[pointer].lbl,tagSem.tags[pointer+1].lbl);
	}
	
	public static boolean isClassEnd(String labels[],int pointer){
		//if the position indicated is outside the tag sample return false
		if (pointer<0 || pointer>=labels.length)return false;

		if(pointer==labels.length-1){
			return isClassEnd(labels[pointer],outLabel);
		}
		return isClassEnd(labels[pointer],labels[pointer+1]);
	}
	
	/**
	 * Used to decide if a tag is at the end of a chunk.
	 * 
	 * @param label 			The tag.	
	 * @param followingLabel	The next tag.
	 * @return					True if the tag is at the end of a chunk.
	 */
	public static boolean isClassEnd(String label,String followingLabel){
		//if the label is an O return false
		if(label.compareTo(outLabel)==0)return false;
		
		//if it is a word label and not a chunk label return true
		if(!label.contains(divider))return true;
		
		//if the following label is O return true
		if(followingLabel.compareTo(outLabel)==0)return true;
		//if the label is an E return true
		if(label.startsWith(endPrefix))return true;
		//if the label is an S return true
		if(label.startsWith(singlePrefix))return true;
		//if the following label is B return true
		if(followingLabel.startsWith(beginPrefix)) return true;
		//if the following label is S return true
		if(followingLabel.startsWith(singlePrefix)) return true;		
		//if the two label are the same in the same class return false
		if(isSameClass(label,followingLabel)) return false;
		//otherwise return true
		return true; 
	}
	
//	/////////////////////////////////////////////////////////
//	Methods for labels

	public static String getClassName(String label){
		return label.substring(label.indexOf(divider)+1);
	}

	/**
	 * Compare the class of two tags.
	 *
	 * @param tag2  	First tag.
	 * @param tag1 		Second tag.
	 * @return 	 		True if the tags have the same class.
	 */
	public static boolean isSameClass(String tag1,String tag2){
		return getClassName(tag1).compareTo(getClassName(tag2))==0;
	}

	/**
	 * Compare the class of a tag with a class name.
	 *
	 * @param tag  		First tag sequence.
	 * @param className Name of the class.
	 * @return 	 		True if the class specified is the class of the tag.
	 */
	public static boolean isCorrespondentClass(String tag,String className){
		return getClassName(tag).compareTo(className)==0;
	}
	
//	//////////////////////////////////////////////////////////
	
}