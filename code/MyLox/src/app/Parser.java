package app;

import static app.TokenType.BANG;
import static app.TokenType.BANG_EQUAL;
import static app.TokenType.EOF;
import static app.TokenType.EQUAL;
import static app.TokenType.EQUAL_EQUAL;
import static app.TokenType.FALSE;
import static app.TokenType.GREATER;
import static app.TokenType.GREATER_EQUAL;
import static app.TokenType.IDENTIFIER;
import static app.TokenType.LEFT_PAREN;
import static app.TokenType.LESS;
import static app.TokenType.LESS_EQUAL;
import static app.TokenType.MINUS;
import static app.TokenType.NIL;
import static app.TokenType.NUMBER;
import static app.TokenType.PLUS;
import static app.TokenType.PRINT;
import static app.TokenType.RIGHT_PAREN;
import static app.TokenType.SEMICOLON;
import static app.TokenType.SLASH;
import static app.TokenType.STAR;
import static app.TokenType.STRING;
import static app.TokenType.TRUE;
import static app.TokenType.VAR;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    private static class ParseError extends RuntimeException {
    }

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();

    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private ParseError error(Token token, String message) {
        MyLox.error(token, message);
        return new ParseError();
    }

    private Expr primary() {
        // primary → NUMBER | STRING | "false" | "true" | "nil"
        // | "(" expression ")" ;
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(STRING, NUMBER)) {
            Token token = previous();
            return new Expr.Literal(token.literal);
        }

        if (match(IDENTIFIER)) {
            Token token = previous();
            return new Expr.Variable(token);
        }

        if (match(LEFT_PAREN)) {
            // 可能是左括号
            Expr expr = expression();
            if (match(RIGHT_PAREN)) {
                return new Expr.Grouping(expr);
            } else {
                throw error(peek(), "Expect ')' after expression.");
            }
        } else {
            throw error(peek(), "Expect expression.");
        }

    }

    private Expr unary() {
        // unary → ( "!" | "-" ) unary
        // | primary ;
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr multiplication() {
        // multiplication → unary ( ( "/" | "*" ) unary )* ;
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        // addition → multiplication ( ( "-" | "+" ) multiplication )*;
        Expr expr = multiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;
    }

    private Expr comparison() {
        // comparison → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        // equality → comparison ( ( "!=" | "==" ) comparison )* ;
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr expression() {
        // expression → equality ;
        return equality();
    }

    private Stmt printStatement() {
        Expr expr = expression();
        // 检查分号 print "some thing";
        consume(SEMICOLON, "Expect ';' after value.");

        return new Stmt.Print(expr);
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) {
            return advance();
        }
        // 报错后并没有立刻停止
        throw error(peek(), message);
    }
    private Stmt expressionStatement() {
        Expr expr = expression();
        // 检查分号
        consume(SEMICOLON, "Expect ';' after value.");

        return new Stmt.Expression(expr);
    }
    private Stmt statement() {
        if(match(PRINT)) {
            return printStatement();
        }
        return expressionStatement();     
    }

    private Stmt varDeclaration() {
        // varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
        // 返回变量声明抽象语法树
        Token identifier = consume(IDENTIFIER, "after var must be an Identifier");
        Expr initializer = null;

        if(match(EQUAL)) {
            initializer = expression();
            consume(SEMICOLON, "Expect ';' after value.");
        }
        return new Stmt.Var(identifier, initializer);
    }
    private Stmt declaration() {
        if(match(VAR)) {
            return varDeclaration();
        } else {
            return statement();
        }
    }
    List<Stmt> parse() {                          
        List<Stmt> statements = new ArrayList<>();  
        while (!isAtEnd()) {                        
            statements.add(declaration());              
        }

        return statements;
    }

}