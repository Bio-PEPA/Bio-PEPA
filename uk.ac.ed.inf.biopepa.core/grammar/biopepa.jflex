package uk.ac.ed.inf.biopepa.core.dom.internal;

import java_cup.runtime.*;
import java.io.Reader;
import java.util.HashMap;
import java.io.InputStreamReader;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;
import java_cup.runtime.Symbol;

%%

/* Activates Line, Character and Column Counting */
%line
%char
%column

%class BioPEPALexer
%public

/* Informs JFlex that we will be using a CUP generated parser, with Symbol file called BioPEPAToken */
%cupsym BioPEPAToken
%cup
%unicode

%{
	    
	private BioPEPASymbolFactory symbolFactory;
	
	final static HashMap<Integer, String> map = new HashMap<Integer, String>();
	
	static {
		map.put(BioPEPAToken.ACTIVATOR, "(+)");
		map.put(BioPEPAToken.INHIBITOR, "(-)");
		map.put(BioPEPAToken.GENERIC, "(.)");
		map.put(BioPEPAToken.COMMA, ",");
		map.put(BioPEPAToken.COLON, ":");
		map.put(BioPEPAToken.SEMI, ";");
		map.put(BioPEPAToken.EQUALS, "=");
		map.put(BioPEPAToken.LCOOP, "<");
		map.put(BioPEPAToken.RCOOP, ">");
		map.put(BioPEPAToken.MULT, "*");
		map.put(BioPEPAToken.MINUS, "-");
		map.put(BioPEPAToken.PLUS, "+");
		map.put(BioPEPAToken.DIVIDE, "/");
		map.put(BioPEPAToken.POWER, "^");
		map.put(BioPEPAToken.SIZE, "size");
		map.put(BioPEPAToken.LOCATION_DEF, "location");
		map.put(BioPEPAToken.SPECIES, "species");
		map.put(BioPEPAToken.FUNCTION, "kineticLawOf");
		map.put(BioPEPAToken.STEP, "step-size");
		map.put(BioPEPAToken.MAX_CONC, "upper");
		map.put(BioPEPAToken.MIN_CONC, "lower");
		map.put(BioPEPAToken.REACTANT, "<<");
		map.put(BioPEPAToken.PRODUCT, ">>");
		map.put(BioPEPAToken.LPAREN, "(");
		map.put(BioPEPAToken.RPAREN, ")");
		map.put(BioPEPAToken.LSQUARE, "[");
		map.put(BioPEPAToken.RSQUARE, "]");
		map.put(BioPEPAToken.AT, "@");
		map.put(BioPEPAToken.IN, "in");
		map.put(BioPEPAToken.TYPE, "type");
		map.put(BioPEPAToken.UMOVE, "->");
		map.put(BioPEPAToken.BMOVE, "<->");
		map.put(BioPEPAToken.COMPARTMENT, "compartment");
		map.put(BioPEPAToken.MEMBRANE, "membrane");
		map.put(BioPEPAToken.TIME, "time");
	}
	
	public BioPEPALexer(Reader reader, BioPEPASymbolFactory sf) {
		this(reader);
	  	symbolFactory = sf;
	}
	
	public BioPEPALexer(BioPEPASymbolFactory sf){
	 	this(new InputStreamReader(System.in));
	    symbolFactory = sf;
	}
	
	public Symbol symbol(String name, int code, Object lexem){
        return symbolFactory.newLocationAwareSymbol(name, code, lexem,
			yychar, yychar + yylength(), yyline, yycolumn);
   
    }
    
    public Symbol symbol(String name, int code){
        return symbol(name, code, null);
    }
    
    public Symbol symbol(int code) {
    	return symbol(null, code, null);
    }
    
    int getCurrentLineNumber(){
	    return yyline;
	}
	
	int getCurrentColumn(){
	    return yycolumn;
	}
	  
	int getCurrentChar() {
	  	return yychar;
	}
	

%} 

/* Macro Definitions */
LineTerminator = \r|\n|\r\n

%state COMMENT
%state CPPCOMMENT

%eofval{
    return symbol(BioPEPAToken.EOF);
%eofval}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment}

TraditionalComment   = "/*" [^*] ~"*/" | "/*" "*"+ "/"
/* Note no LineTerminator at the end here, since that would not allow a
 * line comment on the final line of the file
 */
EndOfLineComment     = "//" {InputCharacter}*
DocumentationComment = "/**" {CommentContent} "*"+ "/"
CommentContent       = ( [^*] | \*+ [^/*] )*

%% 

<YYINITIAL> {
   /* comments */
  {Comment}                      { /* ignore */ }
 
  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }
}

<YYINITIAL> "location" { return symbol(BioPEPAToken.LOCATION_DEF); }

<YYINITIAL> "size" { return symbol(BioPEPAToken.SIZE); }

<YYINITIAL> "species" { return symbol(BioPEPAToken.SPECIES); }

<YYINITIAL> "kineticLawOf" { return symbol(BioPEPAToken.FUNCTION); }

<YYINITIAL> "step-size" { return symbol(BioPEPAToken.STEP); }

<YYINITIAL> "upper" { return symbol(BioPEPAToken.MAX_CONC); }

<YYINITIAL> "lower" { return symbol(BioPEPAToken.MIN_CONC); }

<YYINITIAL> "<<" { return symbol(BioPEPAToken.REACTANT); }

<YYINITIAL> ">>" { return symbol(BioPEPAToken.PRODUCT); }

<YYINITIAL> "(" { return symbol(BioPEPAToken.LPAREN); }

<YYINITIAL> ")" { return symbol(BioPEPAToken.RPAREN); }

<YYINITIAL> "(+)" { return symbol(BioPEPAToken.ACTIVATOR); }

<YYINITIAL> "(-)" { return symbol(BioPEPAToken.INHIBITOR); }

<YYINITIAL> "(.)" { return symbol(BioPEPAToken.GENERIC); }

<YYINITIAL> "," { return symbol(BioPEPAToken.COMMA); }

<YYINITIAL> ":" { return symbol(BioPEPAToken.COLON); }

<YYINITIAL> ";" { return symbol(BioPEPAToken.SEMI); }

<YYINITIAL> "=" { return symbol(BioPEPAToken.EQUALS); }

<YYINITIAL> "<" { return symbol(BioPEPAToken.LCOOP); }

<YYINITIAL> ">" { return symbol(BioPEPAToken.RCOOP); }

<YYINITIAL> "*" { return symbol(BioPEPAToken.MULT); }

<YYINITIAL> "-" { return symbol(BioPEPAToken.MINUS); }

<YYINITIAL> "+" { return symbol(BioPEPAToken.PLUS); }

<YYINITIAL> "/" { return symbol(BioPEPAToken.DIVIDE); }

<YYINITIAL> "^" { return symbol(BioPEPAToken.POWER); }

<YYINITIAL> "[" { return symbol(BioPEPAToken.LSQUARE); }

<YYINITIAL> "]" { return symbol(BioPEPAToken.RSQUARE); }

<YYINITIAL> "@" {return symbol(BioPEPAToken.AT); }

<YYINITIAL> "in" {return symbol(BioPEPAToken.IN); }

<YYINITIAL> "->" {return symbol(BioPEPAToken.UMOVE); }

<YYINITIAL> "<->" {return symbol(BioPEPAToken.BMOVE); }

<YYINITIAL> "type" {return symbol(BioPEPAToken.TYPE); }

<YYINITIAL> "compartment" {return symbol(BioPEPAToken.COMPARTMENT); }

<YYINITIAL> "membrane" {return symbol(BioPEPAToken.MEMBRANE); }

<YYINITIAL> "time" {return symbol(BioPEPAToken.TIME); }

<YYINITIAL,COMMENT> {LineTerminator}  { }

<YYINITIAL> [:digit:]*("."[:digit:]+)? (("E"|"e")("+"|"-")[:digit:]+)? {
	return symbol(null, BioPEPAToken.NUMBER_LITERAL, yytext());
}

/* Essentially this is intended to capture that a name can be
   a sequece of letters, digits and colons. The restrictions being
   that they must start with a letter and cannot end with a colon.
   This also inforces that we cannot have two colons in a row but that
   is more ease of implementation than a design feature.
*/
<YYINITIAL> [:jletter:]([:jletterdigit:])*((":")+([:jletterdigit:])+)* {
        
	return symbol(null, BioPEPAToken.NAME,yytext());
}
. {
	return symbol("ERROR", BioPEPAToken.error,"Illegal character: <" + yytext() + ">");
}