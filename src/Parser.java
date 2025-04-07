import java.util.List;
import java.util.ArrayList;

public class Parser {
    public static final int ENDMARKER = 0;
    public static final int LEXERROR   = 1;

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
    public static final int TERMOP     = 21;
    public static final int BOOL_LIT   = 23;
    public static final int NEW        = 24;
    public static final int SIZE       = 25;
    public static final int BOOL       = 26;

    public class Token {
        public int type;
        public ParserVal attr;
        public Token(int type, ParserVal attr) {
            this.type = type;
            this.attr = attr;
        }
    }

    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    Compiler _compiler;
    public ParseTree.Program _parsetree;
    public String _errormsg;

    public Parser(java.io.Reader r, Compiler compiler) throws Exception {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null;
        Advance();
    }

    public void Advance() throws Exception {
        int token_type = _lexer.yylex();
        if(token_type == 0)
            _token = new Token(ENDMARKER, null);
        else if(token_type == -1)
            _token = new Token(LEXERROR, yylval);
        else
            _token = new Token(token_type, yylval);
    }

    public String Match(int token_type) throws Exception {
        boolean match = (token_type == _token.type);
        String lexeme = "";
        if(_token.attr != null)
            lexeme = (String)_token.attr.obj;
        if(!match) {
            String expectedStr = tokenToString(token_type);
            String foundStr = tokenToString(_token.type);
            throw new Exception("\"" + expectedStr + "\" is expected instead of \"" + foundStr + "\" at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
        if(_token.type != ENDMARKER)
            Advance();
        return lexeme;
    }

    public int yyparse() throws Exception {
        try {
            _parsetree = program();
            return 0;
        } catch(Exception e) {
            _errormsg = e.getMessage();
            return -1;
        }
    }

    // Production: program -> decl_list
    public ParseTree.Program program() throws Exception {
        switch(_token.type) {
            case NUM:
            case BOOL:
            case ENDMARKER:
                List<ParseTree.FuncDecl> funcs = decl_list();
                String v1 = Match(ENDMARKER);
                return new ParseTree.Program(funcs);
        }
        throw new Exception("No matching production in program at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: decl_list -> decl_list'
    public List<ParseTree.FuncDecl> decl_list() throws Exception {
        switch(_token.type) {
            case NUM:
            case BOOL:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("No matching production in decl_list at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: decl_list' -> fun_decl decl_list' | ϵ
    public List<ParseTree.FuncDecl> decl_list_() throws Exception {
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.FuncDecl v1 = fun_decl();
                List<ParseTree.FuncDecl> v2 = decl_list_();
                v2.add(0, v1);
                return v2;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
        }
        throw new Exception("No matching production in decl_list' at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: fun_decl -> prim_type IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
    public ParseTree.FuncDecl fun_decl() throws Exception {
        switch(_token.type) {
            case NUM:
            case BOOL:
                ParseTree.TypeSpec v01 = prim_type();
                String v02 = Match(IDENT);
                String v03 = Match(LPAREN);
                List<ParseTree.Param> v04 = params();
                String v05 = Match(RPAREN);
                String v06 = Match(BEGIN);
                List<ParseTree.LocalDecl> v07 = local_decls();
                List<ParseTree.Stmt> v08 = stmt_list();
                String v09 = Match(END);
                return new ParseTree.FuncDecl(v02, v01, v04, v07, v08);
        }
        throw new Exception("No matching production in fun_decl at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: prim_type -> NUM | BOOL
    public ParseTree.TypeSpec prim_type() throws Exception {
        switch(_token.type) {
            case NUM:
                String v1 = Match(NUM);
                return new ParseTree.TypeSpec(new ParseTree.PrimTypeNum(), new ParseTree.TypeSpec_Value());
            case BOOL:
                String v2 = Match(BOOL);
                return new ParseTree.TypeSpec(new ParseTree.PrimTypeBool(), new ParseTree.TypeSpec_Value());
        }
        throw new Exception("No matching production in prim_type at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: params -> ϵ (for simplicity)
    public List<ParseTree.Param> params() throws Exception {
        switch(_token.type) {
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("No matching production in params at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: local_decls -> ϵ (for simplicity)
    public List<ParseTree.LocalDecl> local_decls() throws Exception {
        switch(_token.type) {
            case END:
                return local_decls_();
        }
        throw new Exception("No matching production in local_decls at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }
    public List<ParseTree.LocalDecl> local_decls_() throws Exception {
        switch(_token.type) {
            case END:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("No matching production in local_decls' at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // Production: stmt_list -> ϵ (for simplicity)
    public List<ParseTree.Stmt> stmt_list() throws Exception {
        switch(_token.type) {
            case END:
                return stmt_list_();
        }
        throw new Exception("No matching production in stmt_list at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception {
        switch(_token.type) {
            case END:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("No matching production in stmt_list' at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
    }

    // --- Minimal productions for expressions ---

    // Production: expr -> term expr'
    public ParseTree.Expr expr() throws Exception {
        ParseTree.Term t = term();
        ParseTree.Expr_ eprime = exprPrime();
        return new ParseTree.Expr(t, eprime);
    }

    // Production: expr' -> TERMOP term expr' | ϵ
    public ParseTree.Expr_ exprPrime() throws Exception {
        switch(_token.type) {
            case TERMOP:
                String op = Match(TERMOP);
                ParseTree.Term t = term();
                ParseTree.Expr_ eprime = exprPrime();
                return new ParseTree.Expr_(op, t, eprime);
            default:
                return new ParseTree.Expr_(); // epsilon
        }
    }

    // Production: term -> factor term'
    public ParseTree.Term term() throws Exception {
        ParseTree.Factor f = factor();
        ParseTree.Term_ tprime = termPrime();
        return new ParseTree.Term(f, tprime);
    }

    // Production: term' -> TERMOP factor term' | ϵ
    public ParseTree.Term_ termPrime() throws Exception {
        switch(_token.type) {
            case TERMOP:
                String op = Match(TERMOP);
                ParseTree.Factor f = factor();
                ParseTree.Term_ tprime = termPrime();
                return new ParseTree.Term_(op, f, tprime);
            default:
                return new ParseTree.Term_(); // epsilon
        }
    }

    // Production: factor -> LPAREN expr RPAREN | IDENT factor' | NUM_LIT | BOOL_LIT | NEW prim_type LBRACKET expr RBRACKET
    public ParseTree.Factor factor() throws Exception {
        switch(_token.type) {
            case LPAREN:
                String lp = Match(LPAREN);
                ParseTree.Expr e = expr();
                String rp = Match(RPAREN);
                return new ParseTree.FactorParen(e);
            case IDENT:
                String id = Match(IDENT);
                // For simplicity, assume no further extension (epsilon for factor')
                return new ParseTree.FactorIdentExt(id, new ParseTree.FactorIdent_Eps());
            case NUM_LIT:
                String numlit = Match(NUM_LIT);
                double val = Double.parseDouble(numlit);
                return new ParseTree.FactorNumLit(val);
            case BOOL_LIT:
                String boollit = Match(BOOL_LIT);
                boolean b = boollit.equals("true");
                return new ParseTree.FactorBoolLit(b);
            case NEW:
                String nw = Match(NEW);
                ParseTree.PrimType pt = prim_typeNew();
                String lb = Match(LBRACKET);
                ParseTree.Expr e2 = expr();
                String rb = Match(RBRACKET);
                return new ParseTree.FactorNew(pt, e2);
            default:
                throw new Exception("No matching production in factor at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Minimal production for prim_type in "new" expressions.
    public ParseTree.PrimType prim_typeNew() throws Exception {
        switch(_token.type) {
            case NUM:
                String n = Match(NUM);
                return new ParseTree.PrimTypeNum();
            case BOOL:
                String b = Match(BOOL);
                return new ParseTree.PrimTypeBool();
            default:
                throw new Exception("No matching production in prim_typeNew at " + _lexer.lineno + ":" + _lexer.tokenStart + ".");
        }
    }

    // Helper method: tokenToString
    private String tokenToString(int token) {
        switch(token) {
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
            case TERMOP: return "term operator";
            case BOOL_LIT: return "boolean literal";
            case NEW: return "new";
            case SIZE: return "size";
            default: return "unknown token";
        }
    }
}
