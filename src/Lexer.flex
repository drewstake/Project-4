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

%%

"\uFEFF" { /* skip BOM if present */ }

"num" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NUM;
}
"(?i)bool" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL;
}
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
"and" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.TERMOP;
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
{num} {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NUM_LIT;
}
{identifier} {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.IDENT;
}
{linecomment} {
    column += yytext().length();
    /* skip line comment */
}
{newline} {
    lineno++;
    column = 1;
}
{whitespace} {
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

\b {
    System.err.println("Sorry, backspace doesn't work");
}

[^] {
    System.err.println("Error: unexpected character '" + yytext() + "'");
    column += yytext().length();
    return -1;
}
