/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.editors;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.*;

public class BioPEPACompletionProcessor implements IContentAssistProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		System.err.println("Proposals obtained");
		List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
		String[] test = new String[] {"location 1", "location 2", "outside the known universe..."};
		CompletionProposal proposal;
		for(String s : test) {
			proposal = new CompletionProposal(s, offset, 0, s.length());
			proposals.add(proposal);
		}
		return proposals.toArray(new ICompletionProposal[] {});
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {'@'};
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}

}
