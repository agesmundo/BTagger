package corpus;

import java.util.ArrayList;
import java.util.List;

import bTagger.LabelsUtility;


public class ChunksCoords {
	public List <List <ChunkData>> coords=new ArrayList <List <ChunkData>>();
	
//	//////////////////////////////////////////////////////
// 	Constructors
	
	public ChunksCoords(int size) {
		setSize(size);
	}

	public ChunksCoords(List <List <ChunkData>> coordsArg){
		this.coords=coordsArg;
	}
	

	public ChunksCoords(SentenceLib sentenceLib, int columnID) {
		this(sentenceLib.getSentences(), columnID);
	}
	
	public ChunksCoords(Sentences sentences, int columnID) {
		setSize(sentences.size());
		for(int i=0;i<sentences.size();i++){ //loop sentences
			Sentence currentSen=sentences.getSentence(i);
			List <ChunkData> currentCoords=new ArrayList <ChunkData>();
			int startIndex=0;
			int j=0;
			Line prevLine=null;
			Line currentLine=null;
			Line nextLine=currentSen.getLine(j);
			boolean exitCondition = false;
			String prevLineLabel="";
			String currentLineLabel="";
			String nextLineLabel="";
//XXX			boolean isFirstLabel;
//			boolean isLastLabel;
			while(!exitCondition){
				
				//get pointers to the context lines
				prevLine=currentLine;
				currentLine=nextLine;
				if(j<currentSen.size()){
					nextLine=currentSen.getLine(j+1);
				}
				else{
					nextLine=null;
					exitCondition=true;
				}
				
				//skip the first round with currentLine==null
				if(currentLine==null){
					continue;
				}
				//get context labels		
				currentLineLabel=currentLine.getToken(columnID);
				if(prevLine!= null){
					prevLineLabel=prevLine.getToken(columnID);
				}
				else {
					prevLineLabel="O";
				}
				if(nextLine!= null){
					nextLineLabel=nextLine.getToken(columnID);
				}
				else {
					nextLineLabel="O";
				}
				
		//TODO test		
				//set isFirstLabel and isLastLabel
				if(j==0){
					prevLineLabel="O";
//					isFirstLabel=true;
				}
//				else{
//					isFirstLabel=false;
//				}
				if(j==currentSen.size()-1){
					nextLineLabel="O";
//					isLastLabel=true;
				}
//				else{
//					isLastLabel=false;
//				}
				
				//check if the current label is at a start
				if(LabelsUtility.isClassStart(currentLineLabel,prevLineLabel )){
					startIndex=j;
				}
				
				//check if the current label is at an end
				if(LabelsUtility.isClassEnd(currentLineLabel,nextLineLabel )){
					currentCoords.add(new ChunkData(startIndex,j, LabelsUtility.getClassName(currentLine.getToken(columnID))));
				}
				j++;
			}
			coords.set(i, currentCoords);
		}
	}
//	//////////////////////////////////////////////////////

	private void setSize(int size) {
		while(coords.size()<size){
			coords.add(new ArrayList<ChunkData>());
		}
	}
	
//	//////////////////////////////////////////////////////
//	GET
	
	public ChunkData getChunkData(int sentenceInx, int chunkInx){
		return coords.get(sentenceInx).get(chunkInx);
	}
	
	/**
	 * Return the first chunk that starts at or after the starting position indicated.
	 * 
	 * @param sentenceIndx  	Index of the sentence.
	 * @param startingPosition 	Starting position of the chunk.
	 * @return					The chunk that starts at or after the starting position indicated
	 */
	public ChunkData getChunkFrom( int sentenceIndx, int startingPosition){
		for (ChunkData candidateChunk : coords.get(sentenceIndx)){
			if(candidateChunk.getChunkStart()>=startingPosition){
				return candidateChunk;
			}
		}
		return null;
	}
	
	public List <List <ChunkData>> getCoords(){
		return coords;
	}
	
	public List <ChunkData> getCoords(int sentenceIndex){
		return coords.get(sentenceIndex);
	}

	public List<String> getAllClassNames(){
		List<String> rtn = new ArrayList<String>();
		for(List <ChunkData> senCoords : coords){
			for(ChunkData cd: senCoords){
				rtn.add(cd.getChunkClass());
			}
		}
		return rtn;
	}
	
//	//////////////////////////////////////////////////////
	
	public int size(){
		return coords.size();
	}
	
	public int size(int index){
		return coords.get(index).size();
	}

	public void set(int i, List<ChunkData> currentCoords){
		coords.set(i, currentCoords);
	}


	public boolean hasChunk(int sentenceID, ChunkData chunkData) {
		List <ChunkData> sentenceCoords = coords.get(sentenceID);
		for(int i=0;i<sentenceCoords.size();i++){
			if(sentenceCoords.get(i).getChunkStart()==chunkData.getChunkStart() && sentenceCoords.get(i).getChunkEnd()==chunkData.getChunkEnd() && sentenceCoords.get(i).getChunkClass().compareTo(chunkData.getChunkClass())==0){
				return true;
			}
		}
		return false;
	}


	public void removeChunk(int sentenceID, ChunkData chunkData) {
		List <ChunkData> sentenceCoords = coords.get(sentenceID);
		for(int i=0;i<sentenceCoords.size();i++){
			if(sentenceCoords.get(i).getChunkStart()==chunkData.getChunkStart() && sentenceCoords.get(i).getChunkEnd()==chunkData.getChunkEnd() && sentenceCoords.get(i).getChunkClass().compareTo(chunkData.getChunkClass())==0){
				sentenceCoords.remove(i);
			}
		}
	}

	public void add(List<ChunkData> chunkData)
	{
		coords.add(chunkData);
	}

	public void remove(int index)
	{
		coords.remove(index);		
	}
	
}