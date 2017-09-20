// JFlex parser specification written by
// Rahmadi Trimananda
// for Sentinel system
// University of California, Irvine

// Technische Universitaet Muenchen 
// Fakultaet fuer Informatik 

import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

%%

%public
%class Lexer
%cup
%implements sym
%char
%line
%column

%{
    StringBuffer string = new StringBuffer();
    public Lexer(java.io.Reader in, ComplexSymbolFactory sf){
	this(in);
	symbolFactory = sf;
    }
    ComplexSymbolFactory symbolFactory;

  private Symbol symbol(String name, int sym) {
      return symbolFactory.newSymbol(name, sym, new Location(yyline+1,yycolumn+1,yychar), new Location(yyline+1,yycolumn+yylength(),yychar+yylength()));
  }
  
  private Symbol symbol(String name, int sym, Object val) {
      Location left = new Location(yyline+1,yycolumn+1,yychar);
      Location right= new Location(yyline+1,yycolumn+yylength(), yychar+yylength());
      return symbolFactory.newSymbol(name, sym, left, right,val);
  } 
  private Symbol symbol(String name, int sym, Object val,int buflength) {
      Location left = new Location(yyline+1,yycolumn+yylength()-buflength,yychar+yylength()-buflength);
      Location right= new Location(yyline+1,yycolumn+yylength(), yychar+yylength());
      return symbolFactory.newSymbol(name, sym, left, right,val);
  }       
  private void error(String message) {
    System.out.println("Error at line "+(yyline+1)+", column "+(yycolumn+1)+" : "+message);
  }
%} 

%eofval{
     return symbolFactory.newSymbol("EOF", EOF, new Location(yyline+1,yycolumn+1,yychar), new Location(yyline+1,yycolumn+1,yychar+1));
%eofval}


Ident = [a-zA-Z$_] [a-zA-Z0-9$_\[\]]*

new_line = \r|\n|\r\n;

white_space = {new_line} | [ \t\f]

%state STRING

%%

<YYINITIAL>{
/* keywords */
"int"             { return symbol("int",TYPE, "int" ); }
"short"           { return symbol("short",TYPE, "short" ); }
"byte"            { return symbol("byte",TYPE, "byte" ); }
"long"            { return symbol("long",TYPE, "long" ); }
"float"           { return symbol("float",TYPE, "float" ); }
"double"          { return symbol("double",TYPE, "double" ); }
"char"            { return symbol("char",TYPE, "char" ); }
"string"          { return symbol("string",TYPE, "String" ); }
"String"          { return symbol("String",TYPE, "String" ); }
"boolean"         { return symbol("boolean",TYPE, "boolean" ); }
"void"            { return symbol("void",TYPE, "void" ); }
"public"          { return symbol("public",PUBLIC); }
"interface"       { return symbol("interface",INTERFACE); }
"capability"      { return symbol("capability",CAPABILITY); }
"description"     { return symbol("description",DESCRIPTION); }
"method"          { return symbol("method",METHOD); }
"requires"        { return symbol("requires",REQUIRES); }
"with"            { return symbol("with",WITH); }
"as"              { return symbol("as",AS); }
"enum"            { return symbol("enum",ENUM); }
"struct"          { return symbol("struct",STRUCT); }

/* names */
{Ident}           { return symbol("Identifier",IDENT, yytext()); }

  
/* string literals */

/* char literal */

/* bool literal */

/* literals */



/* separators */
  \"              { string.setLength(0); yybegin(STRING); }
";"               { return symbol("semicolon",SEMICOLON); }
","               { return symbol("comma",COMMA); }
"("               { return symbol("(",LPAR); }
")"               { return symbol(")",RPAR); }
"<"               { return symbol("<",LANG); }
">"               { return symbol(">",RANG); }
"{"               { return symbol("{",BEGIN); }
"}"               { return symbol("}",END); }
"="               { return symbol("=",ASSIGN); }

{white_space}     { /* ignore */ }

}

<STRING> {
  \"                             { yybegin(YYINITIAL); 
      return symbol("StringConst",STRINGCONST,string.toString(),string.length()); }
  [^\n\r\"\\]+                   { string.append( yytext() ); }
  \\t                            { string.append('\t'); }
  \\n                            { string.append('\n'); }

  \\r                            { string.append('\r'); }
  \\\"                           { string.append('\"'); }
  \\                             { string.append('\\'); }
}


/* error fallback */
.|\n              {  /* throw new Error("Illegal character <"+ yytext()+">");*/
		    error("Illegal character <"+ yytext()+">");
                  }
