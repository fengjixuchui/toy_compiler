package app;

import static app.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
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

        return call();
    }

    private Expr call() {
        // 匹配 primary ( "(" arguments? ")" )* ;
        Expr expr = primary();

        // 有这样几种情况：
        // A() A()() A(a, b, c)()

        // 如果 interpreter 遇到了这个表达式
        // Call(Expr callee, Token mark, List<Expr> arguments)
        // 先执行 callee 然后把 arguments 定义到环境中

        // 所以我们得不断匹配 callee 部分
        // 如果下一个 token 还是 ( 就把之前得到 callee + 参数部分 变成下一个 callee

        Expr callee = expr;
        while (match(LEFT_PAREN)) {
            callee = getCallee(callee);
        }
        expr = callee;

        return expr;
    }

    private Expr getCallee(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (match(RIGHT_PAREN)) {
            // 无参数
            Token mark = previous();
            return new Expr.Call(callee, mark, arguments);
        }

        // 有参数
        do {
            if (arguments.size() >= 255) {
                error(peek(), "Cannot have more than 255 arguments.");
            }
            Expr arg = expression();
            arguments.add(arg);
        } while (match(COMMA));

        // 匹配最后一个 )
        consume(RIGHT_PAREN, "Expect a ')'.");
        Token mark = previous();
        return new Expr.Call(callee, mark, arguments);
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

    private Expr logic_and() {
        Expr left = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            return new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr logic_or() {
        Expr left = logic_and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = logic_and();
            return new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr assignment() {
        // expression → assignment ;
        // assignment → identifier "=" assignment
        // | logic_or ;
        // logic_or → logic_and ( "or" logic_and )* ;
        // logic_and → equality ( "and" equality )* ;
        // 左值可能需要计算我们先跳过计算左值
        Expr expr = logic_or();

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
        // expression → assignment;
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
        // expressionStatement 是除了特殊 statement 以外的 statement
        // 比如赋值
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

    private Stmt ifStatement() {
        // 生成 ifStatement 的抽象语法树
        // ifStmt → "if" "(" expression ")" statement ( "else" statement )? ;
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if (condition == null)
            condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt returnStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after return");

        return new Stmt.Return(expr);
    }
    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        }
        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        if (match(IF)) {
            return ifStatement();
        }
        if (match(WHILE)) {
            return whileStatement();
        }
        // 将 for 转换成 while
        if (match(FOR)) {
            return forStatement();
        }

        if (match(RETURN)) {
            return returnStatement();
        }
        return expressionStatement();
    }

    private Stmt varDeclaration() {
        // varDecl → "var" IDENTIFIER ( "=" expression )? ";" ;
        // 返回变量声明抽象语法树
        Token identifier = consume(IDENTIFIER, "After 'var' must be an Identifier");
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

    private Stmt funcDeclaration() {
        // Token name, List<Token> params, List<Stmt> body
        Token name = consume(IDENTIFIER, "Expect function name.");
        consume(LEFT_PAREN, "Expect '(' in function declaration");

        List<Token> params = new ArrayList<>();
        if (match(RIGHT_PAREN)) {
            // 没参数
            // 程序的 body，先消耗一个 {
            consume(LEFT_BRACE, "Expect parameter name.");
            List<Stmt> body = block();
            return new Stmt.Function(name, params, body);
        }
        // 有参数
        do {
            if (params.size() >= 255) {
                error(peek(), "Cannot have more than 255 parameters.");
            }
            Token para = consume(IDENTIFIER, "Expect parameter name.");
            params.add(para);
        } while (match(COMMA));

        consume(RIGHT_PAREN, "Expect ')'.");
        consume(LEFT_BRACE, "Expect '{'.");
        List<Stmt> body = block();
        return new Stmt.Function(name, params, body);
    }

    private Stmt declaration() {
        // 在这里抓错误
        try {
            if (match(VAR)) {
                return varDeclaration();
            }
            // 函数声明
            if (match(FUN)) {
                return funcDeclaration();
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