/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.wizards.internal;

import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import uk.ac.ed.inf.common.launching.ILaunchingConstants;
import uk.ac.ed.inf.common.ui.wizards.PassageTimeAnalysisWizard;

public class FileLocationPage extends WizardPage implements IUpdatable {

	private Map<String, String> optionMap = null;

	private Text outputFolderWorkspaceLoc = null;

	public FileLocationPage(String pageName) {
		super(pageName);
		setTitle("Output Settings");
		setDescription("Select location for intermediate PEPA models.");
	}

	public void setWizard(IWizard wizard) {
		super.setWizard(wizard);
		/* change current option map */
		optionMap = ((PassageTimeAnalysisWizard) wizard).getOptionMap();

	}

	/* Updates the option map with the new key,values pairs */
	public void update() {
		if (!isControlCreated())
			return;
		optionMap.put(ILaunchingConstants.SRMC_OUTPUT_DIR, ResourcesPlugin
				.getWorkspace().getRoot().getFolder(
						new Path(outputFolderWorkspaceLoc.getText()))
				.getLocation().toOSString());
	}

	public void createControl(Composite parent) {

		Composite main = new Composite(parent, SWT.NONE);
		GridLayout selectionLayout = new GridLayout();
		selectionLayout.numColumns = 1;
		selectionLayout.makeColumnsEqualWidth = false;
		main.setLayout(selectionLayout);
		setControl(main);

		ModifyListener listener = new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updatePage();
			}

		};
		/*
		 * OTHER FILES LOCATION
		 */
		Group dataGroup = new Group(main, SWT.SHADOW_OUT);
		dataGroup.setText("Output Files");
		dataGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout dataLayout = new GridLayout(3, false);
		dataGroup.setLayout(dataLayout);

		/*
		 * OUTPUT LOCATION
		 */
		outputFolderWorkspaceLoc = new Text(dataGroup, SWT.BORDER);
		outputFolderWorkspaceLoc.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		outputFolderWorkspaceLoc.addModifyListener(listener);
		Button browseWorkspaceFolder = new Button(dataGroup, SWT.PUSH);
		browseWorkspaceFolder.setText("Workspace...");
		browseWorkspaceFolder.setLayoutData(new GridData());
		browseWorkspaceFolder.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				ContainerSelectionDialog d = new ContainerSelectionDialog(
						outputFolderWorkspaceLoc.getShell(), ResourcesPlugin
								.getWorkspace().getRoot(), false,
						"Select directory to save output files to");

				if (d.open() == Window.OK) {
					Object[] resultArray = d.getResult();
					if (resultArray.length > 0) {
						if (resultArray[0] instanceof Path) {
							String fileLoc = ((Path) resultArray[0]).toString();
							outputFolderWorkspaceLoc.setText(fileLoc);

						}
					}
				}
			}

		});

		initialiseComponents();

	}

	/*
	 * Called after they're created, initialise with the current optionMap.
	 */
	private void initialiseComponents() {
		String currentPath = (String) optionMap
				.get(ILaunchingConstants.SRMC_OUTPUT_DIR);
		if (currentPath == null)
			outputFolderWorkspaceLoc
					.setText(((PassageTimeAnalysisWizard) getWizard())
							.getInputFile().getParent().getFullPath().append(
									"tmp").toString());
		else {
			IContainer container = ResourcesPlugin.getWorkspace().getRoot()
					.getContainerForLocation(new Path(currentPath));
			if (container != null && container instanceof IFolder)
				outputFolderWorkspaceLoc.setText(container.getFullPath()
						.toString());
			else {
				setErrorMessage("Please select output folder");
				setPageComplete(false);
				return;
			}
		}
		updatePage();
	}

	private void updatePage() {
		String message = validate();
		setErrorMessage(message);
		if (message == null) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	/*
	 * Validate fields, OK is string is null, otherwise string contains an error
	 * message to be shown to user.
	 */
	private String validate() {
		setMessage(null);
		String t = outputFolderWorkspaceLoc.getText();
		if (t == null) {
			return "Please specify folder";
		}

		Path path = new Path(t);
		if (!path.isValidPath(t) || path.segmentCount() < 2) {
			return "Please specify folder";
		}
		IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(
				path);
		if (!folder.exists()) {
			setMessage("Folder does not exist and will be created",
					DialogPage.WARNING);
		} else {
			IResource[] members = null;
			try {
				members = folder.members(true);
			} catch (CoreException e) {
				return "Unable to retrieve this folder's state."
						+ "Please check that the folder exists and is empty.";
			}
			if (members.length > 0) {
				setMessage(
						"Folder is not empty. Folder members will be deleted.",
						DialogPage.WARNING);
			}
		}

		return null;
	}

}
