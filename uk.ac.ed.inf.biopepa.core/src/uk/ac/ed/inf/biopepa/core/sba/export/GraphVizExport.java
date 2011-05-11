package uk.ac.ed.inf.biopepa.core.sba.export;

import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;

public class GraphVizExport implements Exporter {

	public String getShortName() {
		return "dot";
	}

	public String getLongName() {
		return "graphviz-dot";
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setModel(SBAModel model) throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	public void setModel(ModelCompiler compiledModel) throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	public Object requiredDataStructure() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setName(String modelName) {
		// TODO Auto-generated method stub

	}

	public Object toDataStructure() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getExportPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	public String canExport() {
		// TODO Auto-generated method stub
		return null;
	}

}
