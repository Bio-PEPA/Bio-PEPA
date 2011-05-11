package uk.ac.ed.inf.biopepa.core.dom;

import java.util.List;

import uk.ac.ed.inf.biopepa.core.dom.InfixExpression.Operator;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration.Kind;

/*
 * We're going to start with a simple tracer which will
 * keep a count of how many times a particular reaction is
 * fired. This can be useful in seeing how much of a given
 * component is ever produced. Of course a particular tracer
 * for that is even better. Such a tracer would look at the
 * component and see which reactions produce and then produce
 * a similar component with no consuming reactions.
 */

public class AddReactionTracer {

	/*
	private Model astModel;
	private String reactionName;
	private String tracerName = "ReactionCounter";
	public AddReactionTracer (Model astModel, String reactionName){
		this.astModel = astModel;
		this.reactionName = reactionName;
	}
	*/
	
	public static void addReactionTracer (Model astModel, String reactionName,
			String tracerName){
		List<Statement> statements = astModel.statements();
		
		VariableDeclaration newComponentDec = 
			astModel.ast.newVariableDeclaration();
		newComponentDec.setKind(Kind.COMPONENT);
		
		Name name = astModel.ast.newName();
		name.setIdentifier(tracerName);
		newComponentDec.setName(name);
		
		InfixExpression compExpr = newComponentDec.ast.newInfixExpression();
		compExpr.setOperator(Operator.PRODUCT);
		Name rName = compExpr.ast.newName();
		rName.setIdentifier(reactionName);
		compExpr.setLeftHandSide(rName);
		compExpr.setRightHandSide(name);
		
		newComponentDec.setRightHandSide(compExpr);
		
		/*
		Prefix compPrefix = newComponentDec.ast.newPrefix();
        PostfixExpression actionType = compPrefix.ast.newPostfixExpression();
        // I think the operand is just the name, so we are defining it as
        // Tracer = action >> Tracer;
        // Which could be shortcutted as: Tracer = action >> ;
        // by the user, but not us.
        actionType.setOperand(name);
        actionType.setOperator(PostfixExpression.Operator.PRODUCT);
		compPrefix.setActionType(actionType);
		NumberLiteral stoichometry = compPrefix.ast.newNumberLiteral();
		stoichometry.setToken ("1");
		compPrefix.setStoichometry(stoichometry);
		
		Name rName = newComponentDec.ast.newName();
		rName.setIdentifier(reactionName);
		InfixExpression 
		
		newComponentDec.setRightHandSide(compPrefix);
		*/
		
		statements.add(newComponentDec);
		
		// We could set a variable and do a quick check that we did
		// indeed find the system equation.
		for (Statement s : statements){
			if (s instanceof ExpressionStatement){
				ExpressionStatement systemEq = (ExpressionStatement) s;
				
				Expression system = systemEq.getExpression ();
				Cooperation newSystem = astModel.ast.newCooperation();
				
				NameSet actionSet = newSystem.ast.newNameSet();
				Name newName = actionSet.ast.newName();
				newName.setIdentifier(Cooperation.WILDCARD);
				actionSet.names.add(newName);
				
				newSystem.setActionSet(actionSet);
				newSystem.setLeftHandSide(system);
				
				Component comp = newSystem.ast.newComponent();
				NumberLiteral level = comp.ast.newNumberLiteral();
				level.setToken("0");
				comp.setName(name);
				comp.setLevel(level);				
				newSystem.setRightHandSide(comp);
				
				systemEq.setExpression(newSystem);
				
				break;
			}
		}
	}
}
