package uk.ac.ed.inf.biopepa.cl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Command {

	String command_name;
	
	public Command(String name){
		this.command_name = name;
		this.options = new LinkedList<CLOption> ();
	}
	
	private class CLOption {
		String name;
		int number_of_arguments;
		
		public CLOption (String n, int a){
			this.name = n;
			this.number_of_arguments = a;
		}
		
		public String getName(){
			return this.name;
		}
		public int getNumberOfArgs(){
			return this.number_of_arguments;
		}
		
		public boolean is_matching_flag(String f){
			return f.equals("-" + this.name) ||
			       f.equals("--" + this.name);
		}
	}
	
	private List<CLOption> options;
	public void add_option (CLOption o){
		this.options.add(o);
	}
	
	public void add_options(Collection<CLOption> opts){
		this.options.addAll(opts);
	}
	
	private boolean is_flag(String f){
		return f.startsWith("-");
	}
	
	public LinkedList<String> arguments;
	public LinkedList<String[]> flags;
	public LinkedList<String> errors;
	public boolean parseCommand(String [] clargs){
		// If there is no first argument equal to this command's name
		// then we simply return false as this is not our command.
		if (clargs.length < 1 || !this.command_name.equals(clargs[0])){
			return false;
		}
		// Otherwise we can parse the remainder of the flags and arguments
		int index = 1;
		arguments = new LinkedList<String>();
		flags = new LinkedList<String[]>();
		errors = new LinkedList<String>();
		while (index < clargs.length){
			if (is_flag(clargs[index])){
			  boolean recognised = false;
			  for (CLOption clopt : options){
				  // Should check that the arguments are not flags.
				  if (clopt.is_matching_flag(clargs[index]) &&
					       	index + clopt.getNumberOfArgs() < clargs.length){
					  String[] flag = new String[1 + clopt.getNumberOfArgs()];
					  flag[0] = clopt.getName();
					  index++;
					  for (int i = 1; i < clopt.getNumberOfArgs(); i++){
						  flag[i] = clargs[index];
						  index++;
					  }
					  flags.addLast(flag);
					  recognised = true;
					  break;
				  }
			  }
			  if (!recognised){
				  errors.addLast("Unrecognised flag: " + clargs[index]);
			  }
			} else {
				arguments.addLast(clargs[index]);
				index++;
			}
		}
		// Eventually if we get to here and we haven't reported an
		// error then we are good to go
		return true;
	}
	
	public boolean hasErrors(){
		return !this.errors.isEmpty();
	}
	
}
