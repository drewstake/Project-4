import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Parser {
    // --- Existing Tokens ---
    public static final int ENDMARKER = 0;
    public static final int LEXERROR   = 1; // Used by lexer on error

    public static final int NUM        = 10;
    public static final int BEGIN      = 11;
    public static final int END        = 12;
    public static final int LPAREN     = 13;
    public static final int RPAREN     = 14;
    public static final int SEMI       = 15;
    public static final int NUM_LIT    = 16;
    public static final int IDENT      = 17;
    public static final int LBRACKET   = 18;
    public static final int RBRACKET   = 19;
    public static final int BOOL_LIT   = 23;
    public static final int NEW        = 24;
    public static final int SIZE       = 25;
    public static final int BOOL       = 26;

    // --- New Tokens ---
    public static final int ASSIGN     = 27; // <-
    public static final int PLUS       = 28; // +
    public static final int MINUS      = 29; // -
    public static final int TIMES      = 30; // *
    public static final int DIVIDE     = 31; // /
    public static final int OR         = 32; // or
    public static final int AND        = 21; // and (reusing 21 explicitly)
    public static final int PRINT      = 33; // print
    public static final int RETURN     = 34; // return
    public static final int FLOAT_LIT  = 35; // 1.23

    // New token for dot ('.')
    public static final int DOT        = 36;

    // New token for "if"
    public static final int IF         = 37;

    // New token for equals operator "="
    public static final int EQ         = 38;
    
    // New token for comma (",")
    public static final int COMMA      = 39;

    // Define TERMOP to satisfy lexer references (for "and")
    public static final int TERMOP = AND;

    // Updated Token class now stores the actual lexeme and its starting column.
    public class Token {
        public int type;
        public ParserVal attr;
        public String lexeme; // actual lexeme
        public int col;       // starting column
        public Token(int type, ParserVal attr, String lexeme, int col) {
            this.type = type;
            this.attr = attr;
            this.lexeme = lexeme;
            this.col = col;
        }
        @Override
        public String toString() {
            return "Token [type=" + Parser.tokenToString(type) + "(" + type + "), lexeme=" + lexeme + ", col=" + col + "]";
        }
    }

    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    Compiler _compiler; // Assuming Compiler class exists
    public ParseTree.Program _parsetree;
    public String _errormsg;

    public Parser(java.io.Reader r, Compiler compiler) throws Exception {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null; // Initialize _token before first Advance
        Advance(); // Get the first token
    }

    public void Advance() throws Exception {
        // Temporarily redirect System.err to suppress dot errors from the lexer
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos));
        int token_type = _lexer.yylex();
        // Restore System.err
        System.setErr(originalErr);
        String text = _lexer.yytext();
        if (token_type == LEXERROR && text.equals(".")) {
            token_type = DOT;
        }
        // Create a new token storing the lexeme and current column.
        _token = new Token(token_type, new ParserVal(text), text, _lexer.tokenStart);
    }

    public String Match(int token_type) throws Exception {
        if (_token.type == LEXERROR) {
            throw new Exception("Lexical error on token \"" +
                (_token.lexeme != null ? _token.lexeme : "") +
                "\" at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
        int currentCol = _token.col;
        boolean match = (token_type == _token.type);
        String lexeme = _token.lexeme;
        if (!match) {
            String expectedStr = tokenToString(token_type);
            String foundStr = _token.lexeme != null ? _token.lexeme : tokenToString(_token.type);
            throw new Exception("\"" + expectedStr + "\" is expected instead of \"" +
                foundStr + "\" at " + _lexer.lineno + ":" + currentCol + ".");
        }
        if (_token.type != ENDMARKER)
            Advance();
        return lexeme;
    }

    public int yyparse() throws Exception {
        try {
            _parsetree = program();
            return 0;
        } catch (Exception e) {
            _errormsg = e.getMessage();
            return -1;
        }
    }

    // Production: program -> decl_list
    public ParseTree.Program program() throws Exception {
        switch (_token.type) {
            case NUM:
            case BOOL:
                List<ParseTree.FuncDecl> funcs = decl_list();
                Match(ENDMARKER);
                return new ParseTree.Program(funcs);
            default:
                throw new Exception("No matching production in program at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Production: decl_list -> fun_decl decl_list | ϵ
    public List<ParseTree.FuncDecl> decl_list() throws Exception {
        switch (_token.type) {
            case NUM:
            case BOOL:
                ParseTree.FuncDecl first = fun_decl();
                List<ParseTree.FuncDecl> rest = decl_list();
                rest.add(0, first);
                return rest;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
            default:
                throw new Exception("No matching production in decl_list' at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Production: fun_decl -> prim_type IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
    public ParseTree.FuncDecl fun_decl() throws Exception {
        ParseTree.TypeSpec type = prim_type();
        String id = Match(IDENT);
        Match(LPAREN);
        List<ParseTree.Param> p = params();
        Match(RPAREN);
        Match(BEGIN);
        List<ParseTree.LocalDecl> locals = local_decls();
        List<ParseTree.Stmt> stmts = stmt_list();
        Match(END);
        return new ParseTree.FuncDecl(id, type, p, locals, stmts);
    }

    // Production: prim_type -> NUM | BOOL
    public ParseTree.TypeSpec prim_type() throws Exception {
        switch (_token.type) {
            case NUM:
                Match(NUM);
                return new ParseTree.TypeSpec(new ParseTree.PrimTypeNum(), new ParseTree.TypeSpec_Value());
            case BOOL:
                Match(BOOL);
                return new ParseTree.TypeSpec(new ParseTree.PrimTypeBool(), new ParseTree.TypeSpec_Value());
            default:
                throw new Exception("No matching production in prim_type (expected 'num' or 'bool') at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Production: params -> param_list | ϵ
    public List<ParseTree.Param> params() throws Exception {
        List<ParseTree.Param> params = new ArrayList<>();
        if (_token.type == RPAREN)
            return params;
        params.add(param());
        while (_token.type == COMMA) {
            Match(COMMA);
            params.add(param());
        }
        return params;
    }
    
    // Production: param -> prim_type [ "[]" ] IDENT
    public ParseTree.Param param() throws Exception {
        // Check that the parameter type is either NUM or BOOL.
        if (_token.type != NUM && _token.type != BOOL) {
            if (_token.type == COMMA)
                throw new Exception("No matching production in param at " +
                    _lexer.lineno + ":" + _lexer.tokenStart + ".");
            else
                throw new Exception("No matching production in params at " +
                    _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
        ParseTree.TypeSpec type = prim_type();
        if (_token.type == LBRACKET) {
            Match(LBRACKET);
            Match(RBRACKET);
            type = new ParseTree.TypeSpec(type.type, new ParseTree.TypeSpec_Array());
        }
        String id = Match(IDENT);
        return new ParseTree.Param(id, type);
    }

    // Production: local_decls -> var_decl local_decls | ϵ
    public List<ParseTree.LocalDecl> local_decls() throws Exception {
        switch (_token.type) {
            case NUM:
            case BOOL:
                ParseTree.LocalDecl decl = var_decl();
                List<ParseTree.LocalDecl> rest = local_decls();
                rest.add(0, decl);
                return rest;
            // Added IF below so that a starting 'if' statement is recognized as part of stmt_list.
            case IDENT:
            case PRINT:
            case RETURN:
            case BEGIN:
            case END:
            case IF:
                return new ArrayList<ParseTree.LocalDecl>();
            default:
                throw new Exception("No matching production in local_decls (expected variable declaration, statement, or '}') at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Production: var_decl -> prim_type [ "[]" ] IDENT SEMI
    public ParseTree.LocalDecl var_decl() throws Exception {
        ParseTree.TypeSpec type = prim_type();
        if (_token.type == LBRACKET) {
            Match(LBRACKET);
            Match(RBRACKET);
            type = new ParseTree.TypeSpec(type.type, new ParseTree.TypeSpec_Array());
        }
        String id = Match(IDENT);
        Match(SEMI);
        return new ParseTree.LocalDecl(id, type);
    }

    // Production: stmt_list -> stmt stmt_list | ϵ
    public List<ParseTree.Stmt> stmt_list() throws Exception {
        switch (_token.type) {
            case IF:
            case IDENT:
            case PRINT:
            case RETURN:
            case BEGIN:
                ParseTree.Stmt s = stmt();
                List<ParseTree.Stmt> rest = stmt_list();
                rest.add(0, s);
                return rest;
            case END:
                return new ArrayList<ParseTree.Stmt>();
            default:
                throw new Exception("No matching production in stmt_list' at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Production: stmt -> if_stmt | while_stmt | compound_stmt | assignment_stmt | print_stmt | return_stmt
    public ParseTree.Stmt stmt() throws Exception {
        if (_token.type == IF) {
            return if_stmt();
        }
        if (_token.type == IDENT && _token.lexeme.equals("while")) {
            return while_stmt();
        }
        if (_token.type == BEGIN) {
            return compound_stmt();
        }
        switch (_token.type) {
            case IDENT:
                return assignment_stmt();
            case PRINT:
                return print_stmt();
            case RETURN:
                return return_stmt();
            default:
                throw new Exception("No matching production in stmt (expected if, while, compound, assignment, print, or return) at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // New production: while_stmt -> "while" LPAREN expr RPAREN stmt
    public ParseTree.Stmt while_stmt() throws Exception {
        String whileLex = Match(IDENT); // should be "while"
        if (!whileLex.equals("while")) {
            throw new Exception("\"while\" is expected instead of \"" + whileLex + "\" at " +
                    _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
        Match(LPAREN);
        ParseTree.Expr cond = expr();
        Match(RPAREN);
        ParseTree.Stmt body = stmt();
        return new ParseTree.StmtWhile(cond, body);
    }

    // New production: compound_stmt -> BEGIN local_decls stmt_list END
    public ParseTree.Stmt compound_stmt() throws Exception {
        Match(BEGIN);
        List<ParseTree.LocalDecl> locals = local_decls();
        List<ParseTree.Stmt> stmts = stmt_list();
        Match(END);
        return new ParseTree.StmtCompound(locals, stmts);
    }

    // Production: if_stmt -> IF LPAREN expr RPAREN stmt "else" stmt
    public ParseTree.Stmt if_stmt() throws Exception {
        String ifLex = Match(IF);
        Match(LPAREN);
        ParseTree.Expr cond = expr();
        Match(RPAREN);
        ParseTree.Stmt thenStmt = stmt();
        int elseCol = _token.col;
        String elseLex = Match(IDENT);
        if (!elseLex.equals("else")) {
            throw new Exception("\"else\" is expected instead of \"" + elseLex + "\" at " +
                    _lexer.lineno + ":" + elseCol + ".");
        }
        ParseTree.Stmt elseStmt = stmt();
        return new ParseTree.StmtIf(cond, thenStmt, elseStmt);
    }

    // Production: assignment_stmt -> IDENT ASSIGN expr SEMI
    public ParseTree.Stmt assignment_stmt() throws Exception {
        String id = Match(IDENT);
        Match(ASSIGN);
        ParseTree.Expr e = expr();
        Match(SEMI);
        return new ParseTree.StmtAssign(id, e);
    }

    // Production: print_stmt -> PRINT expr SEMI
    public ParseTree.Stmt print_stmt() throws Exception {
        Match(PRINT);
        ParseTree.Expr e = expr();
        Match(SEMI);
        return new ParseTree.StmtPrint(e);
    }

    // Production: return_stmt -> RETURN expr SEMI
    public ParseTree.Stmt return_stmt() throws Exception {
        Match(RETURN);
        ParseTree.Expr e = expr();
        Match(SEMI);
        return new ParseTree.StmtReturn(e);
    }

    // Production: expr -> term expr'
    public ParseTree.Expr expr() throws Exception {
        ParseTree.Term t = term();
        ParseTree.Expr_ eprime = exprPrime();
        return new ParseTree.Expr(t, eprime);
    }

    // Production: expr' -> PLUS term expr' | MINUS term expr' | OR term expr' | AND term expr' | EQ term expr' | ϵ
    public ParseTree.Expr_ exprPrime() throws Exception {
        String op = null;
        switch (_token.type) {
            case PLUS: op = Match(PLUS); break;
            case MINUS: op = Match(MINUS); break;
            case OR: op = Match(OR); break;
            case AND: op = Match(AND); break;
            case EQ: op = Match(EQ); break;
            default:
                return new ParseTree.Expr_();
        }
        ParseTree.Term t = term();
        ParseTree.Expr_ eprime = exprPrime();
        return new ParseTree.Expr_(op, t, eprime);
    }

    // Production: term -> factor term'
    public ParseTree.Term term() throws Exception {
        ParseTree.Factor f = factor();
        ParseTree.Term_ tprime = termPrime();
        return new ParseTree.Term(f, tprime);
    }

    // Production: term' -> TIMES factor term' | DIVIDE factor term' | ϵ
    public ParseTree.Term_ termPrime() throws Exception {
        String op = null;
        switch (_token.type) {
            case TIMES: op = Match(TIMES); break;
            case DIVIDE: op = Match(DIVIDE); break;
            default:
                return new ParseTree.Term_();
        }
        ParseTree.Factor f = factor();
        ParseTree.Term_ tprime = termPrime();
        return new ParseTree.Term_(op, f, tprime);
    }

    // Production: factor -> LPAREN expr RPAREN | IDENT factorIdentExt | NUM_LIT | BOOL_LIT | FLOAT_LIT | NEW prim_type LBRACKET expr RBRACKET
    public ParseTree.Factor factor() throws Exception {
        switch (_token.type) {
            case LPAREN:
                Match(LPAREN);
                ParseTree.Expr e = expr();
                Match(RPAREN);
                return new ParseTree.FactorParen(e);
            case IDENT:
                String id = Match(IDENT);
                ParseTree.Factor_ ext = factorIdentExt();
                return new ParseTree.FactorIdentExt(id, ext);
            case NUM_LIT:
                String numlit = Match(NUM_LIT);
                double vald = 0;
                try {
                    vald = Double.parseDouble(numlit);
                } catch (NumberFormatException nfe) {}
                return new ParseTree.FactorNumLit(vald);
            case BOOL_LIT:
                String boollit = Match(BOOL_LIT);
                boolean b = boollit.equals("true");
                return new ParseTree.FactorBoolLit(b);
            case FLOAT_LIT:
                String floatlit = Match(FLOAT_LIT);
                double valf = 0;
                try {
                    valf = Double.parseDouble(floatlit);
                } catch (NumberFormatException nfe) {}
                return new ParseTree.FactorNumLit(valf);
            case NEW:
                Match(NEW);
                if (_token.type == NUM) {
                    Match(NUM);
                    ParseTree.PrimType pt = new ParseTree.PrimTypeNum();
                    Match(LBRACKET);
                    ParseTree.Expr newExpr = expr();
                    Match(RBRACKET);
                    return new ParseTree.FactorNew(pt, newExpr);
                } else if (_token.type == BOOL) {
                    Match(BOOL);
                    ParseTree.PrimType pt = new ParseTree.PrimTypeBool();
                    Match(LBRACKET);
                    ParseTree.Expr newExpr = expr();
                    Match(RBRACKET);
                    return new ParseTree.FactorNew(pt, newExpr);
                } else {
                    throw new Exception("No matching production in new expression at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
                }
            default:
                throw new Exception("No matching production in factor at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // New method to handle optional factor extensions for identifiers.
    public ParseTree.Factor_ factorIdentExt() throws Exception {
        if (_token.type == DOT) {
            Match(DOT);
            String keyword;
            if (_token.type == IDENT)
                keyword = Match(IDENT);
            else if (_token.type == SIZE)
                keyword = Match(SIZE);
            else {
                throw new Exception("No matching production in factor extension at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
            }
            if (!keyword.equals("size")) {
                throw new Exception("No matching production in factor extension at " +
                        _lexer.lineno + ":" + _lexer.tokenStart + ".");
            }
            return new ParseTree.FactorIdent_DotSize();
        } else if (_token.type == LBRACKET) {
            Match(LBRACKET);
            ParseTree.Expr e = expr();
            Match(RBRACKET);
            return new ParseTree.FactorIdent_BrackExpr(e);
        } else if (_token.type == LPAREN) {
            Match(LPAREN);
            List<ParseTree.Arg> args = argList();
            Match(RPAREN);
            return new ParseTree.FactorIdent_ParenArgs(args);
        } else {
            return new ParseTree.FactorIdent_Eps();
        }
    }

    // New method to parse argument lists.
    // Grammar: args -> expr ( COMMA expr )* | ε
    public List<ParseTree.Arg> argList() throws Exception {
        List<ParseTree.Arg> args = new ArrayList<>();
        if (_token.type == RPAREN)
            return args;
        if (_token.type == COMMA)
            throw new Exception("No matching production in args at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
        args.add(new ParseTree.Arg(expr()));
        while (_token.type == COMMA) {
            Match(COMMA);
            if (_token.type == RPAREN)
                throw new Exception("No matching production in expr at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
            args.add(new ParseTree.Arg(expr()));
        }
        return args;
    }

    // Helper method: tokenToString
    private static String tokenToString(int token) {
        switch (token) {
            case ENDMARKER: return "end-of-file";
            case LEXERROR: return "lexical error";
            case NUM: return "num";
            case BOOL: return "bool";
            case BEGIN: return "{";
            case END: return "}";
            case LPAREN: return "(";
            case RPAREN: return ")";
            case SEMI: return ";";
            case LBRACKET: return "[";
            case RBRACKET: return "]";
            case NUM_LIT: return "number literal";
            case IDENT: return "identifier";
            case DOT: return ".";
            case AND: return "and";
            case BOOL_LIT: return "boolean literal";
            case NEW: return "new";
            case SIZE: return "size";
            case ASSIGN: return "<-";
            case PLUS: return "+";
            case MINUS: return "-";
            case TIMES: return "*";
            case DIVIDE: return "/";
            case OR: return "or";
            case PRINT: return "print";
            case RETURN: return "return";
            case FLOAT_LIT: return "float literal";
            case EQ: return "=";
            case IF: return "if";
            case COMMA: return ",";
            default: return "unknown token (" + token + ")";
        }
    }
}
