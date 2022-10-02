package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTNode;

public class SimpleParser implements IParser {
    ILexer scanner;
    IToken t;

    public SimpleParser(ILexer scanner) {
        this.scanner = scanner;
        try {
            t = scanner.next();
        } catch (LexicalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ASTNode parse() throws PLPException {
        return null;
    }

    private boolean isKind(IToken t, IToken.Kind kind) {
        return t.getKind() == kind;
    }

    public IToken consume() throws LexicalException {
        t = scanner.next();
        return t;
    }

    public IToken match(IToken.Kind kind) throws LexicalException, SyntaxException {
        if (isKind(t, kind)) {
            return consume();
        } else {
            throw new SyntaxException("Syntax Error");
        }
    }

    private void program() throws LexicalException, SyntaxException {
        block();
        match(IToken.Kind.DOT);
        //return null;
    }

    private void block() throws LexicalException, SyntaxException {
        while (isKind(t, IToken.Kind.KW_CONST)) {
            match(IToken.Kind.KW_CONST);
            match(IToken.Kind.IDENT);
            match(IToken.Kind.EQ);
            const_val();
            while (isKind(t, IToken.Kind.COMMA)) {
                match(IToken.Kind.COMMA);
                match(IToken.Kind.IDENT);
                match(IToken.Kind.EQ);
            }
            match(IToken.Kind.SEMI);
        }

        while (isKind(t, IToken.Kind.KW_VAR)) {
            match(IToken.Kind.KW_VAR);
            match(IToken.Kind.IDENT);
            while (isKind(t, IToken.Kind.COMMA)) {
                match(IToken.Kind.COMMA);
                match(IToken.Kind.IDENT);
            }
            match(IToken.Kind.SEMI);
        }

        while (isKind(t, IToken.Kind.KW_PROCEDURE)) {
            match(IToken.Kind.KW_PROCEDURE);
            match(IToken.Kind.IDENT);
            match(IToken.Kind.SEMI);
            block();
            match(IToken.Kind.SEMI);
        }
        statement();
    }

    private void statement() throws LexicalException, SyntaxException {
        if (isKind(t, IToken.Kind.IDENT)) {
            match(IToken.Kind.IDENT);
            match(IToken.Kind.ASSIGN);
            expression();
        } else if (isKind(t, IToken.Kind.KW_CALL)) {
            match(IToken.Kind.KW_CALL);
            match(IToken.Kind.IDENT);
        } else if (isKind(t, IToken.Kind.QUESTION)) {
            match(IToken.Kind.QUESTION);
            match(IToken.Kind.IDENT);
        } else if (isKind(t, IToken.Kind.BANG)) {
            match(IToken.Kind.BANG);
            expression();
        } else if (isKind(t, IToken.Kind.KW_BEGIN)) {
            match(IToken.Kind.KW_BEGIN);
            statement();
            while (isKind(t, IToken.Kind.SEMI)) {
                match(IToken.Kind.SEMI);
                statement();
            }
            match(IToken.Kind.KW_END);
        } else if (isKind(t, IToken.Kind.KW_IF)) {
            match(IToken.Kind.KW_IF);
            expression();
            match(IToken.Kind.KW_THEN);
            statement();
        } else if (isKind(t, IToken.Kind.KW_WHILE)) {
            match(IToken.Kind.KW_WHILE);
            expression();
            match(IToken.Kind.KW_DO);
            statement();
        } else {
            return;
        }
    }

    private void expression() throws LexicalException, SyntaxException {
        additive_expression();
        while (isKind(t, IToken.Kind.LT) || isKind(t, IToken.Kind.GT) || isKind(t, IToken.Kind.EQ) || isKind(t, IToken.Kind.NEQ) || isKind(t, IToken.Kind.LE) || isKind(t, IToken.Kind.GE)) {
            if (isKind(t, IToken.Kind.LT)) {
                match(IToken.Kind.LT);
            } else if (isKind(t, IToken.Kind.GT)) {
                match(IToken.Kind.GT);
            } else if (isKind(t, IToken.Kind.EQ)) {
                match(IToken.Kind.EQ);
            } else if (isKind(t, IToken.Kind.NEQ)) {
                match(IToken.Kind.NEQ);
            } else if (isKind(t, IToken.Kind.LE)) {
                match(IToken.Kind.LE);
            } else if (isKind(t, IToken.Kind.GE)) {
                match(IToken.Kind.GE);
            }
            additive_expression();
        }
    }

    private void additive_expression() throws LexicalException, SyntaxException {
        multiplicative_expression();
        while (isKind(t, IToken.Kind.PLUS) || isKind(t, IToken.Kind.MINUS)) {
            if (isKind(t, IToken.Kind.PLUS)) {
                match(IToken.Kind.PLUS);
            } else if (isKind(t, IToken.Kind.MINUS)) {
                match(IToken.Kind.MINUS);
            }
            multiplicative_expression();
        }
    }

    private void multiplicative_expression() throws LexicalException, SyntaxException {
        primary_expression();
        while (isKind(t, IToken.Kind.TIMES) || isKind(t, IToken.Kind.DIV) || isKind(t, IToken.Kind.MOD)) {
            if (isKind(t, IToken.Kind.TIMES)) {
                match(IToken.Kind.TIMES);
            } else if (isKind(t, IToken.Kind.DIV)) {
                match(IToken.Kind.DIV);
            } else if (isKind(t, IToken.Kind.MOD)) {
                match(IToken.Kind.MOD);
            }
            primary_expression();
        }
    }

    private void primary_expression() throws LexicalException, SyntaxException {
        if (isKind(t, IToken.Kind.IDENT)) {
            match(IToken.Kind.IDENT);
        } else if (isKind(t, IToken.Kind.NUM_LIT) || isKind(t, IToken.Kind.STRING_LIT) || isKind(t, IToken.Kind.BOOLEAN_LIT)) {
            const_val();
        } else if (isKind(t, IToken.Kind.LPAREN)) {
            match(IToken.Kind.LPAREN);
            expression();
            match(IToken.Kind.RPAREN);
        } else {
            throw new SyntaxException("Error Found");
        }
    }

    private void const_val() throws LexicalException, SyntaxException {
        IToken firstToken = t;
        ASTNode an = null;
        if (isKind(t, IToken.Kind.NUM_LIT)) {
            match(IToken.Kind.NUM_LIT);
        } else if (isKind(t, IToken.Kind.STRING_LIT)){
            match(IToken.Kind.STRING_LIT);
        } else if (isKind(t, IToken.Kind.BOOLEAN_LIT)){
            match(IToken.Kind.BOOLEAN_LIT);
        }  else {
            throw new SyntaxException("Error Found");
        }
    }


}
