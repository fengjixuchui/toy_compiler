package booklox;

import static booklox.TokenType.BANG;
import static booklox.TokenType.BANG_EQUAL;
import static booklox.TokenType.EOF;
import static booklox.TokenType.EQUAL;
import static booklox.TokenType.EQUAL_EQUAL;
import static booklox.TokenType.FALSE;
import static booklox.TokenType.GREATER;
import static booklox.TokenType.GREATER_EQUAL;
import static booklox.TokenType.IDENTIFIER;
import static booklox.TokenType.LEFT_BRACE;
import static booklox.TokenType.LEFT_PAREN;
import static booklox.TokenType.LESS;
import static booklox.TokenType.LESS_EQUAL;
import static booklox.TokenType.MINUS;
import static booklox.TokenType.NIL;
import static booklox.TokenType.NUMBER;
import static booklox.TokenType.PLUS;
import static booklox.TokenType.PRINT;
import static booklox.TokenType.RIGHT_BRACE;
import static booklox.TokenType.RIGHT_PAREN;
import static booklox.TokenType.SEMICOLON;
import static booklox.TokenType.SLASH;
import static booklox.TokenType.STAR;
import static booklox.TokenType.STRING;
import static booklox.TokenType.TRUE;
import static booklox.TokenType.VAR;

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

    private Expr assignment() {
        // assignment → IDENTIFIER "=" assignment
        // | equality;
        // 左值可能需要计算我们先跳过计算左值
        Expr expr = equality();

        if (match(EQUAL)) {
            Token equal = previous();
            Expr value = assignment();
            // expr 通过递归，应该得到是一个 Variable，不应该是其他的别的东西
            // 否则报错
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equal, "Invalid assignment target.");

        }
        return expr;
    }

    private Expr expression() {
        // expression → assignment ;
        return assignment();
    }

    private Stmt printStatement() {
        Expr expr = expression();
        // 检查分号 print "some thing";
        consume(SEMICOLON, "Expect ';' after value.");

        return new Stmt.Print(expr);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
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

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        }
        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        return expressionStatement();
    }

    private Stmt varDeclaration() {
        // varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
        // 返回变量声明抽象语法树
        Token identifier = consume(IDENTIFIER, "after 'var' must be an Identifier");
        Expr initializer = null;

        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Var(identifier, initializer);
    }

    private void synchronize() {
        // 从当前语句中退出
        // 去生成其他语句的「抽象语法树」
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
            case CLASS:
            case FUN:
            case VAR:
            case FOR:
            case IF:
            case WHILE:
            case PRINT:
            case RETURN:
                return;
            }

            advance();
        }
    }

    private Stmt declaration() {
        // 在这里抓错误
        try {
            if (match(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError e) {
            // 进入 panic mode
            synchronize();
            return null;

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