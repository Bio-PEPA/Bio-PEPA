package uk.ac.ed.inf.biopepa.core.sba.export;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.FileStringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;

public class SBRMLResultExport {

	private SBAModel sbaModel;
	public SBRMLResultExport (SBAModel model){
		this.sbaModel = model;
	}
	private String modelName;
	public void setModelName (String name){
		this.modelName = name;
	}

	private class FileStringIndenter extends FileStringConsumer {
		private int indent;
		public FileStringIndenter(String filename){
			super(filename);
			this.indent = 0;
		}
		
		public void indentLine() throws IOException {
			for (int i = 0; i < indent; i++){
				this.append("  "); // this.append("\t");
			}
		}
		public void indentedLine(String s) throws IOException {
			indentLine();
			this.appendLine(s);
		}
		public void indentedString (String s) throws IOException {
			indentLine ();
			this.append(s);
		}
		
		public void increaseIndent(){
			this.indent++;
		}
		public void decreaseIndent(){
			this.indent--;
		}
	}
	
	
	public void exportResults (String filename, Result results) 
		throws IOException{
		FileStringIndenter fs = new FileStringIndenter(filename);
		
		fs.openStringConsumer();
		Calendar c = new GregorianCalendar();
	    Date s =  c.getTime();
	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    String dateString = formatter.format(s);

	
		fs.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		fs.append("<sbrml xmlns=\"http://www.sbrml.org/sbrml/level1/version1\" ");
		fs.append("version=\"1\" level=\"1\" creationDate=\"" + dateString + "\">");
		fs.endLine();
		fs.increaseIndent();
		
		fs.indentedLine("<ontologyTerms>");
		fs.increaseIndent();
		
		fs.indentedLine("<ontologyTerm id=\"term1\" term=\"Temporal Behaviour\" " +
				"sourceTermId=\"TEDDY_0000107\" " + 
				"ontologyURI=\"http://teddyontology.sourceforge.net/teddy/rel-2007-09-03/ontology/teddy.owl\"/>");
		
		/*
		fs.increaseIndent();
		fs.indentedLine("<!--");
		fs.indentedLine("    Note here (term2) that there is no ontology URI given and ");
		fs.indentedLine("    also that we assume stochastic simulation ");
		fs.indentedLine("    when in fact it could have been due to an ODE result.");
		fs.indentedLine("    We should define our own ontology terms but");
		fs.indentedLine("    I'm not sure how to do this.");
		fs.indentedLine("-->");
		fs.decreaseIndent();
		*/
		
		/* fs.indentedLine("<ontologyTerm id=\"term2\" term=\"Stochastic Simulation\" />"); */
		
		fs.indentedLine("<ontologyTerm id=\"term3\" term=\"time\" " +
				"sourceTermId=\"SBO:0000345\" ontologyURI=\"http://www.ebi.ac.uk/sbo/\"/>");
		fs.indentedLine("<ontologyTerm id=\"term4\" term=\"concentration\" "
				+ "sourceTermId=\"SBO:0000196\" ontologyURI=\"http://www.ebi.ac.uk/sbo/\"/>");
		fs.indentedLine("<ontologyTerm id=\"term5\" term=\"particle numbers\" " +
				 "sourceTermId=\"SBRML:00002\" ontologyURI=\"urn:sbrml:ontologyterms\" />");
		
		
		fs.indentedLine("</ontologyTerms>");
		fs.decreaseIndent();
		
		fs.indentedLine("<model name = \"" + modelName + "\" />");
		
		fs.indentedLine("<operations>");
		fs.increaseIndent();
		fs.indentedLine("<operation id=\"op1\" name=\"Time Course\" ontologyTerm=\"term1\">");
		fs.increaseIndent();
		
		// term2? well I haven't defined it
		fs.indentedLine("<!-- term2 isn't defined, I'm not sure how to go about this -->");
		fs.indentedLine("<method name=\"Stochastic Simulation Algorithm\" ontologyTerm=\"term2\"/>");
		fs.indentedLine("<software name=\" BioPEPA Eclipse Plug-in\" " +
				// "version=\"COPASI 4.4 Build 26\" " +
				"URL=\"http://www.biopepa.org\"/>");
		
		fs.indentedLine("<result>");
		fs.increaseIndent();
		fs.indentedLine("<resultComponent id=\"component1\">");
		fs.increaseIndent();
		
		
		fs.indentedLine("<dimensionDescription>");
		fs.increaseIndent();
		
		fs.indentedLine("<compositeDescription name=\"Time\" " +
				"ontologyTerm=\"term3\" indexType=\"double\">");
		fs.increaseIndent();
		
		fs.indentedLine("<compositeDescription name=\"species\" indexType=\"string\">");
		fs.increaseIndent();
		
		fs.indentedLine("<tupleDescription>");
		fs.increaseIndent();
		
		fs.indentedLine("<atomicDescription name=\"Concentration\" "+
				"ontologyTerm=\"term4\" valueType=\"double\"/>");
		fs.indentedLine("<atomicDescription name=\"Particle Numbers\" " +
				"ontologyTerm=\"term5\" valueType=\"integer\"/>");
		
		fs.decreaseIndent();
		fs.indentedLine("</tupleDescription>");
		
		fs.decreaseIndent();
		fs.indentedLine("</compositeDescription>");
		fs.decreaseIndent();
		fs.indentedLine("</compositeDescription>");
		
		fs.decreaseIndent();
		fs.indentedLine("</dimensionDescription>");
		
		fs.indentedLine("<dimension>");
		fs.increaseIndent();
		
		String[] compNames     = results.getComponentNames();
		double [] timePoints   = results.getTimePoints();
		double [][] timeSeries = new double[compNames.length][];
		for (int nameIndex = 0; nameIndex < compNames.length; nameIndex++){
			timeSeries[nameIndex] = results.getTimeSeries(nameIndex);
		}
		
		for (int timeIndex = 0; timeIndex < timePoints.length; timeIndex++){
			fs.indentedLine("<compositeValue indexValue=\"" + timePoints[timeIndex] + "\">");
			fs.increaseIndent();
			for (int nameIndex = 0; nameIndex < timeSeries.length; nameIndex++){
				fs.indentedLine("<compositeValue indexValue = \"" +
						compNames[nameIndex] + "\">");
				fs.increaseIndent();
				
				fs.indentedString("<atomicValue>");
				fs.append(timeSeries[nameIndex][timeIndex] + "</atomicValue>");
				fs.endLine();
				
				fs.decreaseIndent();
				fs.indentedLine("</compositeValue>");
			}
			fs.decreaseIndent();
			fs.indentedLine("</compositeValue>");
		}
		
		fs.decreaseIndent();
		fs.indentedLine("</dimension>");
		
		fs.decreaseIndent();
		fs.indentedLine("</resultComponent>");
		
		fs.decreaseIndent();
		fs.indentedLine("</result>");
		
		fs.decreaseIndent();
		fs.indentedLine("</operation>");
		
		fs.decreaseIndent();
		fs.indentedLine("</operations>");
		
		fs.decreaseIndent();
		fs.indentedLine("</sbrml>");		
		
		fs.closeStringConsumer();
	}
	
}
