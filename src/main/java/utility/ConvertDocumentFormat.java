package utility;


import java.util.Collections;
import corpus.Sentence;
import corpus.Sentences;
import bTagger.BTagger;
import bTagger.FileUtils;

/**
 * Convert document dvider format from evalita to conll
 * 
 * @author Andrea Gesmundo
 *
 */
public class ConvertDocumentFormat
{

	public static void main (String args []){
		if (args.length!=2){
			System.out.println("java ConvertDocumentFormat <in_file_evalita> <out_file_conll>");
			return;
		}

		String inFileName = args[0];
		String outFilename = args[1];

		new ConvertDocumentFormat().convert(inFileName,outFilename);

	}

	void convert(String inFileName, String outFilename)
	{
		Sentences inSents= new Sentences(FileUtils.getReader(inFileName));
		Sentences outSents = convert(inSents);
		outSents.saveFile(outFilename);
	}

	Sentences convert(Sentences inSents)
	{
		if(inSents.size()==0){
			throw new RuntimeException("Empty set of sentences found");
		}

		//create docstart sentence
		int tokensNum =inSents.getSentence(0).getLine(0).size();
		StringBuffer dsLine = new StringBuffer(BTagger.documentStartString);
		for(int i =1 ; i < tokensNum;i++){
			dsLine.append(" -X-");
		}
		Sentence dsSentence = new Sentence(Collections.singletonList(dsLine.toString())); 


		//create return Sentences set
		Sentences rtn = new Sentences();
		Object docID = null;
		for(int i =0; i< inSents.size(); i++){
			Sentence inSent = inSents.getSentence(i);
			if(docID==null || !docID.equals(inSent.getToken(0, 2))){
				docID=inSent.getToken(0, 2);
				rtn.addSentence(dsSentence);
			}
			rtn.addSentence(inSent);
		}


		return rtn;
	}

}
