/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import uk.ac.ed.inf.biopepa.ui.BioPEPAPlugin;

public class BioPEPAViewerConfiguration extends SourceViewerConfiguration {

	ColourManager colourManager;
	ITokenScanner scanner;

	public BioPEPAViewerConfiguration() {
		colourManager = BioPEPAPlugin.getDefault().getColourManager();
		scanner = new BioPEPAScanner();
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
				BioPEPAPartitionScanner.COMMENT};
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		DefaultDamagerRepairer dr;
		dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(
				new BioPEPAPartitionScanner(), new TextAttribute(colourManager
						.getColor(ColourManager.COMMENT)));
		reconciler.setDamager(ndr, BioPEPAPartitionScanner.COMMENT);
		reconciler.setRepairer(ndr, BioPEPAPartitionScanner.COMMENT);
		return reconciler;
	}
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant contentAssistant = new ContentAssistant();
		contentAssistant.setContentAssistProcessor(new BioPEPACompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		// System.err.println("Returning content assistance...");
		return null;
		// return contentAssistant;
	}

}
