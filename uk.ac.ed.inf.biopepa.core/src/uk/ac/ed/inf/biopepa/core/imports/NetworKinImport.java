package uk.ac.ed.inf.biopepa.core.imports;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class NetworKinImport {

	private LinkedList<NetworKinLine> networKlines;
	
	public NetworKinImport (){
		this.networKlines = new LinkedList<NetworKinLine> ();
	}
	
	public List<NetworKinLine> getNetworKinLines (){
		return this.networKlines;
	}
	
	public void importWithReader (Reader contentReader,
				int column1, int column2, int column3) 
			throws IOException{
		 CSVReader reader = new CSVReader(contentReader);
		 List<String[]> csvLines = reader.readAll();
		 
		 /*
		  * We should check and throw an exception if the line is
		  * not as long as the third column (or indeed any column since
		  * we are not enforcing that they are in order).
		  * 
		  * Note we start the index at 1 since the 0th line will
		  * be the column headings.
		  */
		 for (int index = 1; index < csvLines.size(); index++){
			 String[] line = csvLines.get(index);
			 NetworKinLine nline = 
				 new NetworKinLine(line[column1], 
						 line[column2], line[column3]);
			 this.networKlines.addLast(nline);
		 }
	}
	
	public void importFromString (String contents, 
			int c1, int c2, int c3) throws IOException{
		importWithReader(new StringReader(contents), c1, c2, c3);
	}
}
