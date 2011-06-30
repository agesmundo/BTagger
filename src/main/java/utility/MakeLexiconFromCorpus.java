package utility;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bTagger.FileUtils;

import corpus.Line;
import corpus.Sentence;
import corpus.SentenceLib;

public class MakeLexiconFromCorpus {

	public static void main(String args []){
		String use ="MakeLexiconFromCorpus <CorpusFile> <WordsColum> <TagsColumn> <threshold> <outFileName>";
		
		//check number of parameter
		if(args.length!=5){
			System.out.println(use);
			return;
		}
		
		//reading parameters
		SentenceLib corpus=null;
		int wordsColumnID=Integer.parseInt(args[1])-1;
		int tagsColumnID=Integer.parseInt(args[2])-1;
		int threshold = Integer.parseInt(args[3]);
		String outFileName=args[4];
		try{
			corpus = new SentenceLib(args[0]);
		}catch(FileNotFoundException e){
			System.out.println("Corpus File "+args[0]+" not found");
			return;
		}
		new MakeLexiconFromCorpus().makeLexiconFromCorpus(corpus, wordsColumnID, tagsColumnID, threshold,outFileName);
	}
	
	public void makeLexiconFromCorpus(SentenceLib corpus,int wordsColumnID, int tagsColumnID, int threshold, String outFileName){
		Table table=new Table();
		
		//fill table
		for(int i=0; i<corpus.size();i++){
			Sentence sentence=corpus.getSentence(i);
			for(int j=0;j<sentence.size();j++){
				Line line=sentence.getLine(j);
				table.add(line.getToken(wordsColumnID), line.getToken(tagsColumnID));
			}
		}
		
		//print and save file
		System.out.println(table.toString(true,threshold));
		FileUtils.saveToFile(outFileName, table.toString(false,threshold));
		
	}
	
	private class Table{
		Map<String, TableEntry> table;
		//configuration
	
		Table(){
			table=new HashMap<String, TableEntry>();
		}
		
		List<TableEntry> values(){
			List<TableEntry> entryList=new ArrayList<TableEntry>();
			for(TableEntry entry:table.values()){
				entryList.add(entry);
			}
			return entryList;
		}
		
		public String toString(boolean printNumbers, int threshold){
			List <TableEntry>entryTable = values();
			Collections.sort(entryTable);
			StringBuilder string=new StringBuilder();
			for(TableEntry entry:entryTable){
				if(entry.counter<threshold){
					break;
				}
				if(printNumbers){
					string.append(entry.counter+" ");
				}
				string.append(entry.word+" ");
				for(int j=0;j<entry.tags.size();j++){
					if(j>0){
						string.append(" ");
					}
					string.append(entry.tags.get(j));
				}
				string.append(" \n");
			}
			return string.toString();
		}
		
		void add(String word, String tag){
			TableEntry entry = table.get(word);
			
			//the first time we see the word
			if(entry==null){
				table.put(word, new TableEntry(word,tag));
			}
			//add the label and increase the counter
			else{
				if(!entry.contains(tag)){
					entry.add(tag);
				}
				entry.increaseCount();
			}
		}
	}
	
	private class TableEntry implements Comparable<TableEntry>{
		int counter;
		List<String>tags;
		String word;
		
		TableEntry(String word, String firstTag){
			counter=1;
			tags=new ArrayList<String>();
			tags.add(firstTag);
			this.word=word;
		}
		
		boolean contains(String tag){
			return tags.contains(tag);
		}
		
		void add(String tag){
			tags.add(tag);
		}

		void increaseCount(){
			counter++;
		}
		
		public int compareTo(TableEntry entry){
			return entry.counter-counter;
		}
	}
}
