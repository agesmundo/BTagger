package utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Graph {
	public double metrics[][];
	public String functionNames[];

	public Graph(double _metrics[][], String _functionNames[]){
		metrics=_metrics;
		functionNames=new String[metrics.length];
		for(int i=0;i<metrics.length;i++){
			if (i>=_functionNames.length){
				functionNames[i]="";
			}
			else{
				functionNames[i]=_functionNames[i];
			}
		}

	}

	public void print (String fileName) {
		try{
			PrintWriter out = new PrintWriter (new FileOutputStream(fileName+"_data"));//write out file
			out.write(this.getTable());
			out.close();
			out = new PrintWriter (new FileOutputStream(fileName+"_script"));//write out file
			out.write(this.getGnuScript(fileName));
			out.close();
		}catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	private String getGnuScript(String fileName) {
		String rtn= "set term postscript eps	" +
		"\nset output \""+fileName+"_plot.eps\" " +
		"\nset key 1,"+metrics[0][metrics[0].length-1]+" box " +
		"\nset xlabel \"Numero di iterazioni sul training set\"" +
		"\nset ylabel \"F-measure (%)\"" +
		"\nplot ";
		for(int i=0;i<metrics.length;i++){
			rtn+="\""+fileName+"_data\" using 1:"+(i+2)+" title '"+functionNames[i]+"' with lines"+(i+1)+"";
			if(i!=metrics.length-1)rtn+=", \\\n";
		}
		return rtn;
	}

	private String getTable() {
		String ret="#round";

		//print names
		for(int j=0;j<functionNames.length;j++){
			ret+=" "+functionNames[j];
		}


		//print metrics for each round
		for(int i=0;i<metrics[0].length;i++){
			ret+="\n"+(i+1);
			for(int j=0;j<metrics.length;j++){
				ret+=" "+metrics[j][i];
			}
		}
		return ret;
	}

}
