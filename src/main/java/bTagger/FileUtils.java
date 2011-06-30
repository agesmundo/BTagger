package bTagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to load ad save files,
 * handle the problem of the charset selection if not specified.
 * 
 * @author Andrea Gesmundo
 */
public class FileUtils
{
	public static final Charset defaultCharset= Charset.forName("UTF-8");

//////////////////////////////////////////////////////////////////////////////
//	READERS

	public static Reader getReader(String fileName, Charset cs){
		Reader reader=null;
		try{
			reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName),cs));
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		return reader;
	}

	public static Reader getReader(String fileName){
		return getReader(fileName, defaultCharset);
	}
	
	public static Reader getReader(File file, Charset cs){
		Reader reader=null;
		try{
			reader=getReader(new FileInputStream(file), cs);
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		return reader;
	}
	
	public static Reader getReader(File file){
		return getReader(file,defaultCharset);
	}
	
	public static Reader getReader(URL url, Charset cs){
		Reader reader=null;
		try{
			reader=getReader(url.openStream(), cs);
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e ){
			e.printStackTrace();
		}
		return reader;
	}
	
	public static Reader getReader(URL url){
		return getReader(url, defaultCharset);
	}

	public static Reader getReader(InputStream inStream, Charset cs){
		Reader reader=null;
		reader=new BufferedReader(new InputStreamReader(inStream,cs));
		return reader;
	}
	
	public static Reader getReader(InputStream inStream){
		return getReader(inStream,defaultCharset);
	}

///////////////////////////////////////////////////////////////////////////////
// 	WRITERS
	
	public static Writer getWriter(String fileName, Charset cs){
		OutputStreamWriter writer=null;
		try{
			return new OutputStreamWriter(new FileOutputStream(fileName),cs);
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		return writer;
	}

	public static Writer getWriter(String fileName){
		return getWriter(fileName, defaultCharset);
	}

///////////////////////////////////////////////////////////////////////////////
	
	public static List<String> getListString(String fileName) {
		return getListString(FileUtils.getReader(fileName));
	}
	
	public static List<String> getListString(URL url) {
		return getListString(FileUtils.getReader(url));
	}
	
	public static List<String> getListString(Reader reader) {
		List <String> lines=new ArrayList<String>();
		try{
			BufferedReader in = new BufferedReader ( reader );
			String line=in.readLine();
			while(line!=null){
				lines.add(line);
				line=in.readLine();
			}
			in.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return lines;
	}

	public static void saveToFile(String fileName, List<String> lines){
		StringBuilder text=new StringBuilder();
		for(int i=0; i<lines.size();i++){
			if(i>0){
				text.append("\n");
			}
			text.append(lines.get(i));
		}
		saveToFile(fileName, text.toString());
	}

	public static void saveToFile(String fileName, String text){
		PrintWriter out= new PrintWriter(FileUtils.getWriter(fileName));
		out.print(text);
		out.close();
	}

}
