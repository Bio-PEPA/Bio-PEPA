/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.dom.internal.BindingResolver;

/**
 * Abstract Syntax Tree.
 * 
 * @author mtribast
 * 
 */
public class AST {

	public enum Literals {

		LOCATION_DEF("location"), SIZE("size"), SPECIES("species"), FUNCTION("kineticLawOf"), STEP("step-size"), MAX_CONCENTRATION(
				"upper"), MIN_CONCENTRATION("lower"), COMPARTEMENT_PROPERTY("V"), REACTANT(">>"), ACTIVATOR("(+)"), INHIBITOR(
				"(-)"), GENERIC_MODIFIER("(.)"), PRODUCT("<<"), PLUS("+"), MINUS("-"), DIVIDE("/"), TIMES("*"), POWER(
				"^"), EQUALS("="), LOCATION("@"), IN("in"), TYPE("type"), UMOVE("->"), BMOVE("<->"), COMPARTMENT(
				"compartment"), MEMBRANE("membrane"),
		// Following are functions not required in the jflex file
		LOGARITHM("log"), EXP("exp"), HEAVISIDE("H"), MASS_ACTION("fMA"), MICHAELIS_MENTEN("fMM"), COMPETITIVE_INHIBITION(
				"fCI"), FLOOR("floor"), CEILING("ceil"), TANH("tanh"), TIME("time");

		private String token;

		Literals(String token) {
			this.token = token;
		}

		public String getToken() {
			return token;
		}

		@Override
		public String toString() {
			return token;
		}
	}

	/**
	 * Initially, a do-nothing binding resolver
	 */
	private IBindingResolver bindingResolver;

	private AST() {
		bindingResolver = new IBindingResolver() {

			public IBinding resolveName(String identifier) {
				return null;
			}

		};
	}

	public static AST newAST() {
		return new AST();
	}

	public Component newComponent() {
		return new Component(this);
	}

	public NameSet newNameSet() {
		return new NameSet(this);
	}

	public Cooperation newCooperation() {
		return new Cooperation(this);
	}

	public ExpressionStatement newExpressionStatement() {
		return new ExpressionStatement(this);
	}

	public FunctionCall newFunctionCall() {
		return new FunctionCall(this);
	}

	public InfixExpression newInfixExpression() {
		return new InfixExpression(this);
	}

	public Model newModel() {
		Model newModel = new Model(this);
		this.bindingResolver = new BindingResolver(newModel);
		return newModel;
	}

	public Name newName() {
		return new Name(this);
	}

	public LocatedName newLocatedName() {
		return new LocatedName(this);
	}

	public Transport newTransport() {
		return new Transport(this);
	}

	public NumberLiteral newNumberLiteral() {
		return new NumberLiteral(this);
	}

	public PostfixExpression newPostfixExpression() {
		return new PostfixExpression(this);
	}

	public Prefix newPrefix() {
		return new Prefix(this);
	}

	public PropertyInitialiser newPropertyInitialiser() {
		return new PropertyInitialiser(this);
	}

	public PropertyLiteral newPropertyLiteral() {
		return new PropertyLiteral(this);
	}

	public VariableDeclaration newVariableDeclaration() {
		return new VariableDeclaration(this);
	}

	public SystemVariable newSystemVariable() {
		return new SystemVariable(this);
	}

	IBindingResolver getBindingResolver() {
		return bindingResolver;
	}

}
