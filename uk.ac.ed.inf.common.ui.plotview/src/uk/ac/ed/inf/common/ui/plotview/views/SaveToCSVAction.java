/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotview.views;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.SaveAsDialog;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.Plotting;
import uk.ac.ed.inf.common.ui.plotview.PlotViewPlugin;
import uk.ac.ed.inf.common.ui.plotview.views.actions.PlotViewAction;

public class SaveToCSVAction extends PlotViewAction {
	
	private static final String EXTENSION = "csv";

	public SaveToCSVAction(PlotView view) {
		super(view);
		setText("Export to CSV");
		setToolTipText("Export chart to CSV file");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.inf.common.ui.plotview.views.PlotViewAction#doRun(org.eclipse.swt.widgets.TabItem[])
	 */
	@Override
	protected void doRun(IStructuredSelection selection) {
		IChart chart = (IChart) selection.getFirstElement();
		TabItem selectedItem = view.getTab(chart);
		Shell shell = selectedItem.getControl().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalName(selectedItem.getText() != null ? selectedItem
				.getText()
				+ "." + EXTENSION : "");
		dialog.open();
		IPath path = dialog.getResult();
		if (path == null)
			return; // save canceled
		
		IFile file = getFile(shell, path);
		
		
		final ContainerGenerator generator = new ContainerGenerator(file
				.getParent().getFullPath());
		try {

			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

				public void run(IProgressMonitor monitor) throws CoreException {
					generator.generateContainer(monitor);
				}
			}, new NullProgressMonitor());

		} catch (CoreException e) {
			ErrorDialog.openError(shell, "Error saving",
					"An error has occurred while exporting the graph.",
					PlotViewPlugin.wrapException(e.getMessage(), e));
		}
		try {
			ByteArrayInputStream is  = new ByteArrayInputStream(
					Plotting.getPlottingTools().convertToCSV(chart));
			if (file.exists()) {
				file.setContents(is, true, false, new NullProgressMonitor());
			} else {
				file.create(is, true, new NullProgressMonitor());
			}
			// should open the file
			// org.eclipse.ui.ide.IDE.openEditor(this.view.getPage(), file);

		}  catch (Exception e) {
			ErrorDialog.openError(shell, "Error converting",
					"An error has occurred while converting the resource",
					PlotViewPlugin.wrapException("Error converting chart", e));

		}
	}
	
	private IFile getFile(Shell shell, IPath path) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		String extension = file.getFileExtension();
		if (extension == null || (!extension.equalsIgnoreCase(EXTENSION))) {
			if(MessageDialog
					.openQuestion(
							shell,
							"Extension Required",
							"The file specified does not have the '"
									+ EXTENSION
									+ "' extension. Would you like to add the extension?")) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(file.getFullPath().addFileExtension(EXTENSION));
				
			}
			
		}
		return file;
	}
}
