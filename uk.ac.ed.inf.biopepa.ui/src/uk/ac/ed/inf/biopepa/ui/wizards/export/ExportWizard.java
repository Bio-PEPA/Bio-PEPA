/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.wizards.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.SaveAsDialog;

import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.interfaces.IResourceProvider;

public class ExportWizard extends Wizard implements IResourceProvider {
	
	BioPEPAModel model;
	
	ExportPage exportPage;
	
	public ExportWizard(BioPEPAModel model) {
		if(model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Export options for Bio-PEPA");
	}

	@Override
	public boolean performFinish() {
		Exporter exporter = exportPage.getExporter();
		// exporter.setModel(model.getSBAModel());
		exporter.setName(model.getUnderlyingResource().getName());
		SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
		IPath path = model.getUnderlyingResource().getFullPath();
		path = path.removeFileExtension().addFileExtension(exporter.getExportPrefix());
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		saveAsDialog.setOriginalFile(file);
		saveAsDialog.open();
		path = saveAsDialog.getResult();
		if(path == null)
			return false;
		path = path.removeFileExtension().addFileExtension(exporter.getExportPrefix());
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		String contents = null;
		try {
			contents = exporter.toString();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		InputStream source = new ByteArrayInputStream(contents.getBytes());
		try {
			if(file.exists()) {
				file.setContents(source, IResource.NONE, null);
			} else {
				file.create(source, IResource.NONE, null);
			}
			file.refreshLocal(0, null);
		} catch(CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void addPages() {
		exportPage = new ExportPage(model);
		addPage(exportPage);
	}

	public IResource getUnderlyingResource() {
		return model.getUnderlyingResource();
	}

}
