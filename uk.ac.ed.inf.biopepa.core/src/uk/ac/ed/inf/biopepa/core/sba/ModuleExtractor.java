package uk.ac.ed.inf.biopepa.core.sba;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.CompartmentData;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;

/*
 * This class implements the extraction of a sub-model from
 * the larger model. We're working over the sba of the model.
 */
public class ModuleExtractor {
	
	private SBAModel sbaModel;
	private Map<String, Number> components;
	
	/*
	 * The idea is that we can allow multiple module extractions
	 * but that each reaction must only go into exactly one module.
	 */
	private Set<SBAReaction> seen_reactions;
	
	public ModuleExtractor (SBAModel sbaModel, 
			Map<String, Number> subModuleComps){
		this.sbaModel = sbaModel;
		this.components = subModuleComps;
		this.seen_reactions = new HashSet<SBAReaction>();
	}
	
	public void reset_seen_reactions(){
		this.seen_reactions.clear();
	}
	
	
	private class ProcessDef {
		private String name ;
		// A mapping from reaction names to the behaviour of this component
		// within that reaction.
		HashMap<String, SBAComponentBehaviour> rmap;
		public ProcessDef (String name){
			this.name = name;
			this.rmap = new HashMap<String, SBAComponentBehaviour>();
		}
		
		public String getName (){
			return this.name;
		}
		
		public void add_reaction_behaviour(String reaction, 
				SBAComponentBehaviour behaviour){
			rmap.put(reaction, behaviour);
		}
		
		public void pretty_print (StringConsumer sc) throws IOException{
			sc.append(this.name);
			sc.append(" = ");
			boolean first = true;
			for (Entry<String, SBAComponentBehaviour> entry : rmap.entrySet()){
				if (!first){ sc.append(" + "); } else { first = false; }
				String rName = entry.getKey();
				SBAComponentBehaviour sbaBehave = entry.getValue();
				String behaviour = sbaBehave.format(rName);
				sc.append(behaviour);
			}
			sc.appendLine(" ;");
		}
	}
	
	private class ReactionDef {
		private String name ;
		private CompiledExpression rate;
		
		public ReactionDef (String name, CompiledExpression rate){
			this.name = name;
			this.rate = rate.returnExpandedIfPresent();
		}
		
		public void pretty_print(StringConsumer sc) throws IOException{
			sc.append(this.name);
			sc.append(" = [");
			sc.append(rate.toString());
			sc.appendLine("];");
		}
	}
	
	private class LocationDef {
		private String name;
		private CompartmentData location;
		
		public LocationDef (String name, CompartmentData l){
			this.name = name;
			this.location = l;
		}
		
		public String getName(){
			return this.name;
		}
		
		public void pretty_print(StringConsumer sc) throws IOException {
			String parent = location.getParent();
			double volume = location.getVolume();
			double step_size = location.getStepSize();
			
			sc.append("location ");
			sc.append(this.name);
			
			if (parent != null && !parent.equals("")){
				sc.append(" in ");
				sc.append(parent);
			}
			
			sc.append(" : ");
			
			if (!Double.isNaN(volume)){
				sc.append("size = ");
				sc.append(Double.toString(volume));
				sc.append(", ");
			}
			
			if (!Double.isNaN(step_size)){
				sc.append("step-size = ");
				sc.append(Double.toString(step_size));
				sc.append(", ");
			}			
			/*
			 * Note we have type last here as this can always be output
			 * and hence we do not need to worry about the above possibly
			 * leaving a trailing comma separator.
			 */
			sc.append("type=");
			sc.append(this.location.getType().format());
			sc.append(" ;");
			
			/* small note, upper and lower properties are accepted by
			 * the parser, but seem otherwise unimplemented, in particular
			 * in CompartmentData.setProperty an exception will be raised.
			 * Hence we just ignore any possibility of them here.
			 */
		}
	}
	
	private class SourcelessModel {
		LinkedList<ProcessDef> processdefs;
		LinkedList<ReactionDef> reactiondefs;
		LinkedList<LocationDef> locationdefs;
		// A mapping from component names to initial populations
		HashMap<String, Number> initPops;
		
		public SourcelessModel(){
			this.processdefs = new LinkedList<ProcessDef>();
			this.reactiondefs = new LinkedList<ReactionDef>();
			this.locationdefs = new LinkedList<LocationDef>();
			this.initPops = new HashMap<String, Number>();
		}
		
		/*
		 * Return the process definition associated with the given component
		 * name. This may result in the addition of a new process definition
		 * if no appropriate process definition exists.
		 * Note that this is not the simple case of looking to see if the
		 * given name is the same, we must check if the given name is a
		 * located name, and if so check only for the non-located part.
		 */
		public ProcessDef get_process_definition(String comp){
			for (ProcessDef pd : this.processdefs){
				if (pd.getName().equals(comp)){
					return pd;
				}
			}
			// If we get this far then no current process definition is
			// associated with the given name.
			ProcessDef newpd = new ProcessDef(comp);
			this.processdefs.addLast(newpd);
			return newpd;
		}
		
		
		public void add_reaction_def(ReactionDef rd){
			this.reactiondefs.addLast(rd);
		}
		
		public void add_initial_population(String comp, Number pop){
			this.initPops.put(comp, pop);
		}
		
		public void add_location_definition(CompartmentData location){
			// Loop through the current location definitions and see
			// if we have already added a location with the given name.
			String name = location.getName();
			for (LocationDef ldef : this.locationdefs){
				if (ldef.getName().equals(name)){
					return ;
				}
			}
			// If there is no such location definition already then we
			// simply add a new one.
			LocationDef newld = new LocationDef(name, location);
			this.locationdefs.addLast(newld);
		}
		
		public void pretty_print (StringConsumer sc) throws IOException{
			
			sc.appendLine("// Location definitions");
			for (LocationDef ldef : this.locationdefs){
				ldef.pretty_print(sc);
			}
			
			sc.endLine();
			sc.appendLine("// Reaction rate definitions");
			for (ReactionDef rdef : reactiondefs){
				rdef.pretty_print(sc);
			}
			
			sc.endLine();
			sc.appendLine("// Process definitions");
			for (ProcessDef pdef : processdefs){
				pdef.pretty_print(sc);
			}
			
			sc.endLine();
			sc.appendLine("// System equation");
			boolean first = true;
			for (Entry<String, Number> entry : initPops.entrySet()){
				if (!first){ sc.appendLine("<*>"); } else { first = false; }
				
				sc.append(entry.getKey());
				sc.append("[");
				sc.append(entry.getValue().toString());
				sc.append("]");
				
			}
		}
	}
	
	public void extract (StringConsumer sc) 
				throws IOException, BioPEPAException{
		SourcelessModel smodel = new SourcelessModel();
		SBAReaction[] allReactions = sbaModel.getReactions();
		LinkedList<SBAReaction> reactions = new LinkedList<SBAReaction>();
		
		// Figure out the set of reactions we are going to keep.
		for (SBAReaction reaction : allReactions){
			// Basically the reaction can stay if nobody outside
			// of components affects the rate of the reaction.
			// So first obtain all the components which affect the
			// rate of the current reaction
			
			// If the resultant set is now empty we know that there
			// are no components which affect the rate of this reaction
			// that we do not intend to keep, hence we can keep the reaction.
			// Of course we also hope require that a reaction be in at most one
			// module, hence if we have already seen the reaction then we shouldn't
			// add it here.
			if (this.seen_reactions.contains(reaction)){
				continue;
			}
			
			Set<String> rateAffectors = 
				AnalysisUtils.reactionRateModifiers(reaction);
			// Remove from that set all those components which
			// are in the set of components we wish to keep
			for (String component : this.components.keySet()){
				rateAffectors.remove(component);
			}
			
			if (rateAffectors.isEmpty()){
				reactions.addLast(reaction);
			}
		}
		
		
		for (SBAReaction reaction : reactions){
			ReactionDef rd = new ReactionDef(reaction.getName(), 
					 						 reaction.getRate());
			smodel.add_reaction_def(rd);
			// We also add it to her self contained list of seen_reactions
			// in this way we can avoid exporting a reaction in two different
			// modules.
			this.seen_reactions.add(reaction);
		}
		
		
		for (Entry<String, Number> entry : this.components.entrySet()){
			String comp = entry.getKey();
			ComponentNode compNode = sbaModel.getNamedComponent(comp);
			
			// If there is no such node we should probably raise an exception
			if (compNode == null){
				String message = "Could not find component: " + comp;
				throw new BioPEPAException (message);
			}
			
			// To obtain the correct process definition we must give
			// the unlocalised name of the component which may be different
			// from the localised name. For example if the component name is
			// A@l then we want the process definition for just A.
			String unlocalised = compNode.getComponent();
			ProcessDef pd = smodel.get_process_definition(unlocalised);
						
			// Add stuff to the definition
			for (SBAReaction reaction : reactions){
				SBAComponentBehaviour cb = 
					AnalysisUtils.involvedBehaviour(comp, reaction);
				if (cb != null){
					String rname = reaction.getName();
					pd.add_reaction_behaviour(rname, cb);
				}
			}
			
			// In addition to adding the process definition we must add
			// the initial concentration.
			smodel.add_initial_population(comp, entry.getValue());
			// And also we must add the definition of the location
			// unless it has already been added or we are not dealing with
			// a localised name:
			CompartmentData location = compNode.getCompartment();
			if (location != null){
				smodel.add_location_definition(location);
			}
		}
		
		smodel.pretty_print(sc);
	}
}
