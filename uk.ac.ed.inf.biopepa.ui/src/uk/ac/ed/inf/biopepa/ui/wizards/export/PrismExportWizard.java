package uk.ac.ed.inf.biopepa.ui.wizards.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SaveAsDialog;

import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.core.sba.export.PrismExport;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.interfaces.IResourceProvider;

public class PrismExportWizard extends Wizard implements IResourceProvider {
	BioPEPAModel model;
	
	private ExportPage exportPage;
	
	public PrismExportWizard(BioPEPAModel model) {
		if(model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Export options for Bio-PEPA");
	}
	
	
	private class ExportPage extends WizardPage {
		
		private Text levelSizeText;
		private IPath cslPath;
		private Button setPropertiesFile;
		private Label cslFileLabel;
		private Button outputCslCheckButton;

		protected ExportPage(String pageName) {
			super(pageName);
			this.setTitle("Prism CTMC Export");
			this.setDescription("Translate the biopepa model into a Prism model");
			this.cslPath = null;
		}

		public IPath getCSLPath(){
			return this.cslPath;
		}
		public boolean getOutputCsl(){
			return outputCslCheckButton.getSelection();
		}

		public void createControl(Composite parent) {
			int textStyle = SWT.RIGHT | SWT.BORDER;
			int labelStyle = SWT.SINGLE | SWT.LEFT;
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			
			setControl(composite);

			/* Just a small label to say what to do */
			Label tmpLabel = new Label(composite, labelStyle);
			tmpLabel.setText("Please set the level size for this translation");
			tmpLabel.setLayoutData(createDefaultGridData());
			
			// Create an inner composite for the labels and text fields
			Composite labelsComposite = new Composite(composite, SWT.NONE);
			GridLayout labelsCompLayout = new GridLayout(2, false);
			labelsComposite.setLayout(labelsCompLayout);		
			
			Label levelsLabel = new Label (labelsComposite, labelStyle);
			levelsLabel.setText("Level size: (default 1)");
			levelSizeText = new Text(labelsComposite, textStyle);
			// levelSizeText.setSize(80, 1);
			levelSizeText.setLayoutData(newTextGridData());
			levelSizeText.setText("");
			levelSizeText.addModifyListener(modifyListener);
			
			setPropertiesFile = new Button (labelsComposite, SWT.PUSH);
			setPropertiesFile.setText("Set .csl");
			setPropertiesFile.setEnabled(true);
			class SetCSL implements SelectionListener {
				public void widgetSelected(SelectionEvent event) {
					setCSLFile();
				}

				public void widgetDefaultSelected(SelectionEvent event) {
				}
			}
			setPropertiesFile.addSelectionListener(new SetCSL());
			
			cslFileLabel = new Label (labelsComposite, labelStyle);
			cslFileLabel.setText("No file set");
			
			outputCslCheckButton = new Button (labelsComposite, SWT.CHECK);
			outputCslCheckButton.setText("Output csl properties file");
			outputCslCheckButton.setSelection(true);
			
			validate();

		}

		/*
		 * A simple method to create a grid data object for
		 * text object, since I think it is better to create a
		 * new one for each text object such that any changes are
		 * not then global.
		 */
		private GridData newTextGridData (){
			GridData textGridData = new GridData ();
			textGridData.widthHint = 80;
			textGridData.horizontalAlignment = GridData.FILL;
			textGridData.grabExcessHorizontalSpace = true;
			return textGridData;
		}
		
		private ModifyListener modifyListener = new ModifyListener (){

			public void modifyText(ModifyEvent arg0) {
				validate ();
			}
			
		};
		
		private void validate() {
			this.setPageComplete(false);
			this.setErrorMessage(null);
			String lText = levelSizeText.getText().trim();
			try{
				if (!lText.isEmpty()){
					Integer.parseInt(lText);
				}
			} catch (Exception e){
				this.setPageComplete(false);
				this.setErrorMessage("Cannot parse level size");
				return ;
			}
			
			this.setPageComplete(true);
		}

		public int getLevelSize(){
			String text = levelSizeText.getText().trim();
			if (text.isEmpty()){
				return 1;
			}
			try{
				return Integer.parseInt(text);
			} catch (Exception e) {
				return 1;
			}
		}
		
		private GridData createDefaultGridData() {
			/* ...with grabbing horizontal space */
			return new GridData(SWT.FILL, SWT.CENTER, true, false);
		}
		
		private boolean setCSLFile() {
			SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
			IPath path = model.getUnderlyingResource().getFullPath();
			path = path.removeFileExtension().addFileExtension("csl");
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			saveAsDialog.setOriginalFile(file);
			saveAsDialog.open();
			path = saveAsDialog.getResult();
			this.cslPath = path;
			String label = ".../" + path.lastSegment();
			if (label.length() > 30){
				label = label.substring(0,25) + "...";
			}
			this.setPropertiesFile.setText("Change:");
			this.cslFileLabel.setText(label);
			return true;
		}
	}
	
	public void addPages (){
		exportPage = new ExportPage("Export a Traviando Trace");
		addPage (exportPage);
	}
	
	@Override
	public boolean performFinish() {
		SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
		IPath path = model.getUnderlyingResource().getFullPath();
		path = path.removeFileExtension().addFileExtension("pm");
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		saveAsDialog.setOriginalFile(file);
		saveAsDialog.open();
		path = saveAsDialog.getResult();
		if(path == null)
			return false;
		path = path.removeFileExtension().addFileExtension("pm");
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		
		// Get the csl path from the export page
		// but if it returns null (ie. it's not been set)
		// then just make a default based on the .pm file
		IPath propsPath = exportPage.getCSLPath();
		if (propsPath == null){
			propsPath = path.removeFileExtension().addFileExtension("csl");
		}
		IFile propsFile = ResourcesPlugin.getWorkspace().getRoot().getFile(propsPath);
		
		LineStringBuilder prismLsb = new LineStringBuilder ();
		LineStringBuilder propsLsb = new LineStringBuilder ();
		try {
			int levelSize = exportPage.getLevelSize();
			PrismExport pexport = new PrismExport ();
			pexport.setLevelSize(levelSize);
			pexport.setModel(model.getCompiledModel());
			pexport.setModel(model.getSBAModel());
			pexport.export(prismLsb, propsLsb);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		byte[] prismBuf = prismLsb.toString().getBytes();
		InputStream prismSource = new ByteArrayInputStream(prismBuf);
		byte[] propsBuf = propsLsb.toString().getBytes();
		InputStream propsSource = new ByteArrayInputStream (propsBuf);
		try {
			// First the prism file
			if(file.exists()) {
				file.setContents(prismSource, IResource.NONE, null);
			} else {
				file.create(prismSource, IResource.NONE, null);
			}
			file.refreshLocal(0, null);
			if (exportPage.getOutputCsl()) {
				if (propsFile.exists()) {
					propsFile.setContents(propsSource, IResource.NONE, null);
				} else {
					propsFile.create(propsSource, IResource.NONE, null);
				}
				propsFile.refreshLocal(0, null);
			}
		} catch(CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public IResource getUnderlyingResource() {
		return model.getUnderlyingResource();
	}
}

