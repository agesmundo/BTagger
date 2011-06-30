package corpus;

public class ChunkData implements Comparable<ChunkData>{
	private int chunkStart;
	private int chunkEnd;
	private String chunkClass;
	
	public ChunkData(int chunkStartArg,int chunkEndArg,String chunkClassArg){
		if(chunkEndArg<chunkStartArg){
			throw new RuntimeException("cannot create new ChunkData, start is after end");
		}
		this.chunkStart=chunkStartArg;
		this.chunkEnd=chunkEndArg;
		this.chunkClass=chunkClassArg;
	}
	public int getChunkStart() {
		return chunkStart;
	}
	
	public int getChunkEnd() {
		return chunkEnd;
	}
	
	public int getChunkLength(){
		return chunkEnd-chunkStart;
	}
	
	public String getChunkClass(){
		return chunkClass;
	}
	
	public void setChunkClass(String chunkClassArg){
		chunkClass=chunkClassArg;
	}
	
	public int compareTo(ChunkData toCompare)
	{
		return this.getChunkStart() - toCompare.getChunkEnd();
	}
	
	public boolean equals(ChunkData toCompare){
		if(chunkStart==toCompare.getChunkStart() && chunkEnd==toCompare.getChunkEnd() &&chunkClass.compareTo(toCompare.getChunkClass())==0){
			return true;
		}
		return false;
	}
	
	public boolean sameStartEnd(ChunkData toCompare){
		if(chunkStart==toCompare.getChunkStart() && chunkEnd==toCompare.getChunkEnd() ){
			return true;
		}
		return false;
	}
	
//////////////////////////////////////////////////////////
//	multext compare methods
	/**
	 * Return true if the labels are equivalent following the multext definition.
	 * 
	 * @param toCompare The chunk to compare
	 * @return			True if the labels are equivalent following the multext definition
	 */
	public boolean equivalentLabel(ChunkData toCompare)
	{
		if(this.chunkClass.length()!=toCompare.chunkClass.length()){
			return false;
		}
		for(int i =0;i<chunkClass.length();i++){
			if(chunkClass.charAt(i)=='.' ||toCompare.chunkClass.charAt(i)=='.'){
				continue;
			}
			else if(chunkClass.charAt(i)!=toCompare.chunkClass.charAt(i)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Multext method.
	 * Return true if the label of the current chunk is more specific.
	 * Return false if the label of the current chunk in less specific and
	 * if the labels are from different class.
	 * 
	 * @param toCompare 	The chunk to compare
	 * @return				True if the label of the current chunk is more specific.
	 */
	public boolean isMoreSpecificThan(ChunkData toCompare)
	{
		if(this.chunkClass.length()!=toCompare.chunkClass.length()){
			return false;
		}
		for(int i =0;i<chunkClass.length();i++){
			//there is a dot
			if(chunkClass.charAt(i)=='.' ||toCompare.chunkClass.charAt(i)=='.'){
				//both dots, continue
				if(chunkClass.charAt(i)==toCompare.chunkClass.charAt(i)){
					continue;
				}
				//current has a dot, return false
				else if(chunkClass.charAt(i)=='.'){
					return false;
				}
				//toCompare has a dot therefore the current is more specific 
				else {
					return true;
				}
			}
			//the labels are different
			else if(chunkClass.charAt(i)!=toCompare.chunkClass.charAt(i)){
				return false;
			}
		}
		//equal labels
		return false;
	}
	
}
