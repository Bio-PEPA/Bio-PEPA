/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.Wizard;

import uk.ac.ed.inf.common.launching.ILaunchingConstants;
import uk.ac.ed.inf.common.ui.wizards.internal.FileLocationPage;
import uk.ac.ed.inf.common.ui.wizards.internal.IUpdatable;
import uk.ac.ed.inf.common.ui.wizards.internal.PassageTimePage;

/**
 * Abstract wizard for configuring an option map that sets up passage-time
 * analysis using ipc/hydra.
 * 
 * @author mtribast
 * 
 */
public abstract class PassageTimeAnalysisWizard extends Wizard {

	/* Page for file location */
	private static final String FILE_LOCATION_PAGE = "file_location_page";

	/* Page for passage time analysis */
	private static final String PASSAGE_TIME_PAGE = "passage_time_page";

	/* Map holding the options */
	private Map<String, String> fOptionMap = new HashMap<String, String>();

	private IFile fInputFile;

	private IActionFieldsProvider fProvider;

	/**
	 * Creates the wizard with the input file of passage time analysis.
	 * 
	 * @param inputFile
	 * @throws NullPointerException
	 *             if input file is null
	 */
	protected PassageTimeAnalysisWizard(IFile inputFile, IActionFieldsProvider provider) {
		super();
		if (inputFile == null)
			throw new NullPointerException();
		if (provider == null) {
			provider = IActionFieldsProvider.DO_NOTHING_PROVIDER;
		}
		this.fInputFile = inputFile;
		this.fProvider = provider;
	}
	
	protected PassageTimeAnalysisWizard(IFile inputFile) {
		this(inputFile, null);
	}

	public final void addPages() {
		this.addPage(new FileLocationPage(FILE_LOCATION_PAGE));
		this.addPage(new PassageTimePage(PASSAGE_TIME_PAGE,fProvider));
	}

	/**
	 * Returns the input file for this passage time analysis.
	 * 
	 * @return
	 */
	public IFile getInputFile() {
		return fInputFile;
	}

	/**
	 * Returns the option map used for this passage time analysis. If this
	 * method is called <b>before</b> the wizard is opened or after it is
	 * canceled, it returns the original option map. Otherwise, it returns the
	 * option map that the user has set.
	 * 
	 * @return
	 */
	public Map<String, String> getOptionMap() {
		return fOptionMap;
	}

	@Override
	public final boolean performFinish() {
		/* set input file */
		fOptionMap.put(ILaunchingConstants.SRMC_FILE_PATH, fInputFile
				.getLocation().toString());

		/* File location */
		IUpdatable updatable;
		updatable = (IUpdatable) getPage(FILE_LOCATION_PAGE);
		updatable.update();

		updatable = (IUpdatable) getPage(PASSAGE_TIME_PAGE);
		updatable.update();

		return doPerformFinish(fOptionMap);
	}

	/**
	 * Performs finish on this wizard with the given option map.
	 * 
	 * @param optionMap
	 * @return
	 */
	protected abstract boolean doPerformFinish(Map<String, String> optionMap);

}
