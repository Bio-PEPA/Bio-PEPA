package uk.ac.ed.inf.biopepa.core.sba;

import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;

public class MidiaOutput {
	
	private final static String rtruestring = "TRUE";
	private final static String rfalsestring = "FALSE";
	
	private boolean transposed = false;
	public void setTransposed(boolean b){
		this.transposed = b;
	}
	
	private int granularity = 1;
	public void setGranularity (int g){
		this.granularity = g;
	}
	
	public String produceMidiaOutput (SBAModel sbaModel){
		LineStringBuilder lsb = new LineStringBuilder();
		
		lsb.appendLine("load(\"MIDIAbeta1.1.RData\")");
		lsb.appendLine("library(gRbase)");
		lsb.endLine();
		
		SBAReaction[] reactions = sbaModel.getReactions();
		ComponentNode[] components = sbaModel.getComponents();
		int numRows = this.transposed == false ? 
				      components.length: 
				      reactions.length;
		int numCols = this.transposed == false ?
				      reactions.length:
				      components.length;
		
		lsb.appendLine("mS <- matrix(0, " + 
				       numRows +
				       ", " +
				       numCols +
				       ")");
		lsb.appendLine("mR <- matrix(0, " + 
			       numRows +
			       ", " +
			       numCols +
			       ")");
		lsb.appendLine("mP <- matrix(0, " + 
			       numRows +
			       ", " +
			       numCols +
			       ")");
		lsb.appendLine("componentnames <- array(\"name\", " +
				       components.length + ")");
		lsb.appendLine("reactionnames <- array(\"cname\", " +
				       reactions.length + ")");
		
		// Set up the component names 
		for (int index = 0; index < components.length; index++){
			ComponentNode cnode = components[index];
			String compName = cnode.getName();
			lsb.appendLine("componentnames[" + (index + 1) + "] <- \"" + 
				       compName + "\"");
		}
		// And also set up the reaction names
		for (int cindex = 0; cindex < reactions.length; cindex++){
			SBAReaction reaction = reactions[cindex];
			String rName = reaction.getName();
			lsb.appendLine("reactionnames[" + (cindex + 1) + "] <- \"" + 
				       rName + "\"");
		}
		
		/* Now the column and row names of the matrices mS, mR and mP
		 * depend on whether we are transposing the matrix or not, if not
		 * then the row names are the component names and the column names
		 * are the reaction names, obviously if we are transposing then we
		 * have the opposite.
		 */
		if (!this.transposed){
			// Row names are the components
			lsb.appendLine("rownames(mS) <- componentnames");
			lsb.appendLine("rownames(mR) <- componentnames");
			lsb.appendLine("rownames(mP) <- componentnames");
			// column names are the reactions
			lsb.appendLine("colnames(mS) <- reactionnames");
			lsb.appendLine("colnames(mR) <- reactionnames");
			lsb.appendLine("colnames(mP) <- reactionnames");
		} else {
			// Row names are the reactions
			lsb.appendLine("rownames(mS) <- reactionnames");
			lsb.appendLine("rownames(mR) <- reactionnames");
			lsb.appendLine("rownames(mP) <- reactionnames");
			// column names are the components
			lsb.appendLine("colnames(mS) <- componentnames");
			lsb.appendLine("colnames(mR) <- componentnames");
			lsb.appendLine("colnames(mP) <- componentnames");
		}
		
		/*
		 * Now we go through and actually set up each of the
		 * three matrices.
		 */
		for (int row_i = 0; row_i < numRows; row_i++){
			for (int col_i = 0; col_i < numCols; col_i++){
				ComponentNode cnode = this.transposed == false ?
						              components[row_i] :
						              components[col_i];	
				String compName = cnode.getName();	       
				SBAReaction reaction = this.transposed == false ?
						               reactions[col_i] :
						               reactions[row_i];
				int effect = 
					AnalysisUtils.netGainForReaction(reaction, compName);
				boolean isReactantB = 
					AnalysisUtils.componentIsReactant(compName, reaction);
				String isReactant = isReactantB ? rtruestring : rfalsestring ;
				/*
				 * For MIDIA, catalysts are counted as both reactants and
				 * products, so here we need to add something as product if
				 * it is a reactant which is not effected.
				 */
				boolean isProductB =
					AnalysisUtils.componentIsProduct(compName, reaction) ||
					(effect == 0 && isReactantB);
				String isProduct = isProductB ? rtruestring : rfalsestring;
				lsb.appendLine("mS[" +
						       (row_i + 1) +
						       ", " +
						       (col_i + 1) +
						       "] <- " +
						       effect);
				lsb.appendLine("mR[" +
						       (row_i + 1) +
						       ", " +
						       (col_i + 1) +
						       "] <- " +
						       isReactant
						      );
				lsb.appendLine("mP[" +
							   (row_i + 1) +
							   ", " +
							   (col_i + 1) +
							   "] <- " +
							   isProduct
							  );
				
			}
		}
		
		lsb.appendLine("uG=matrix(nrow=0,ncol=0)");
		
		lsb.appendLine("kig <- KIGofmRmS(rbind(mR,mS))");
		lsb.appendLine("uG <- ugraph(kig)");
		lsb.appendLine("ugraph(uG)  # ensure user-supplied network is treated as undirected");
		lsb.appendLine("G_T <- MinimalTriang(uG)                # G_T is a minimal triangulation of the KIG or of the user-supplied undirected graph uG");
        lsb.appendLine("T_C <- GetMPD(G_T)                              # T_C is a Clique decomposition of G_T [rip(G_T) is implemented; T_C is returned by GetMPD in the correct format]");
        lsb.appendLine("Granularity=" + this.granularity); 
        lsb.appendLine("MaxIter=100"); 
        lsb.appendLine("T_MI <- CoarseGrainResidBound(T_C,Granularity)");
        lsb.appendLine("T_M <- SpeciesCopiedTree(Tree=T_MI,mS=mS,mR=mR,mP=mP,MAX_ITERATE=MaxIter,ForbidIOs=list())");
        lsb.appendLine("print(\"----- The module residuals -----\")");
        lsb.appendLine("print(T_M[[5]])");
        lsb.appendLine("print(\"----- Edge contents -----\")");
        lsb.appendLine("print(T_M[[3]])"); 
        lsb.appendLine("print(\"----- Parents -----\")");
        lsb.appendLine("print(T_M[[4]])");




		return lsb.toString();
	}
}
