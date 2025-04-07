/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>
 * All rights reserved.
 *
 * License: BSD
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
%%
%class Lexer
%byaccj

%{
  public Parser parser;
  public int lineno;
  public int column;
  public int tokenStart;  // starting column of current token

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
    this.tokenStart = 1;
  }
%}

/* Define macros */
digit = [0-9]
letter = [a-zA-Z]
identifier = {letter}({letter}|{digit})*
linecomment = "//"[^\n]*
blockcomment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
newline = \r|\n|\r\n
whitespace = [ \t\f]+
float_lit = {digit}+\.{digit}+
minc_linecomment = "%%"[^\n]*
minc_blockcomment = "%*"([^*]|\*+[^%])*\*+%

%%

<YYINITIAL> {
  "\uFEFF" { /* skip BOM if present */ }

  /* MinC Comments */
  {minc_linecomment} {
    // Skip the minc line comment.
    column += yytext().length();
  }
  {minc_blockcomment} {
    // Process the minc block comment: update line and column counters.
    for (int i = 0; i < yytext().length(); i++) {
      if (yytext().charAt(i) == '\n') {
        lineno++;
        column = 1;
      } else {
        column++;
      }
    }
  }
  
  /* C-style Comments */
  {linecomment} {
    column += yytext().length();
  }
  {blockcomment} {
    for (int i = 0; i < yytext().length(); i++) {
      if (yytext().charAt(i) == '\n') {
        lineno++;
        column = 1;
      } else {
        column++;
      }
    }
  }
  {newline} {
    lineno++;
    column = 1;
  }
  {whitespace} {
    column += yytext().length();
  }

  /* Keywords */
  "num" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NUM;
  }
  "bool" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL;
  }
  "true" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL_LIT;
  }
  "false" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL_LIT;
  }
  "new" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NEW;
  }
  "size" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.SIZE;
  }
  "and" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.TERMOP; // Using TERMOP for 'and'
  }
  "or" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.OR;
  }
  "print" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.PRINT;
  }
  "return" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RETURN;
  }
  "if" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.IF;
  }
  /* New: equals operator */
  "=" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.EQ;
  }
  /* New: comma */
  "," {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.COMMA;
  }

  /* Literals */
  {digit}+ {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NUM_LIT;
  }
  {float_lit} {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.FLOAT_LIT;
  }

  /* Identifiers */
  {identifier} {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.IDENT;
  }

  /* Operators and Punctuation */
  "{" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BEGIN;
  }
  "}" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.END;
  }
  "(" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.LPAREN;
  }
  ")" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RPAREN;
  }
  ";" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.SEMI;
  }
  "[" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.LBRACKET;
  }
  "]" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RBRACKET;
  }
  "<-" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.ASSIGN;
  }
  "+" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.PLUS;
  }
  "-" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.MINUS;
  }
  "*" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.TIMES;
  }
  "/" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.DIVIDE;
  }

  /* Error Handling */
  \b {
    System.err.println("Sorry, backspace doesn't work");
  }
  [^] {
    System.err.println("Lexer Error: unexpected character '" + yytext() + "' at " + lineno + ":" + column);
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.LEXERROR;
  }
}
