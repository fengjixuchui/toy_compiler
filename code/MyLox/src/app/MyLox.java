package app;

import static app.TokenType.AND;
import static app.TokenType.BANG;
import static app.TokenType.BANG_EQUAL;
import static app.TokenType.CLASS;
import static app.TokenType.COMMA;
import static app.TokenType.DOT;
import static app.TokenType.ELSE;
import static app.TokenType.EOF;
import static app.TokenType.EQUAL;
import static app.TokenType.EQUAL_EQUAL;
import static app.TokenType.FALSE;
import static app.TokenType.FOR;
import static app.TokenType.FUN;
import static app.TokenType.GREATER;
import static app.TokenType.GREATER_EQUAL;
import static app.TokenType.IDENTIFIER;
import static app.TokenType.IF;
import static app.TokenType.LEFT_BRACE;
import static app.TokenType.LEFT_PAREN;
import static app.TokenType.LESS;
import static app.TokenType.LESS_EQUAL;
import static app.TokenType.MINUS;
import static app.TokenType.NIL;
import static app.TokenType.NUMBER;
import static app.TokenType.OR;
import static app.TokenType.PLUS;
import static app.TokenType.PRINT;
import static app.TokenType.RETURN;
import static app.TokenType.RIGHT_BRACE;
import static app.TokenType.RIGHT_PAREN;
import static app.TokenType.SEMICOLON;
import static app.TokenType.SLASH;
import static app.TokenType.STAR;
import static app.TokenType.STRING;
import static app.TokenType.SUPER;
import static app.TokenType.THIS;
import static app.TokenType.TRUE;
import static app.TokenType.VAR;
import static app.TokenType.WHILE;
import static app.TokenType.假;
import static app.TokenType.函数;
import static app.TokenType.否则;
import static app.TokenType.声明;
import static app.TokenType.如果;
import static app.TokenType.并且;
import static app.TokenType.当;
import static app.TokenType.循环;
import static app.TokenType.或者;
import static app.TokenType.真;
import static app.TokenType.空;
import static app.TokenType.类;
import static app.TokenType.继承;
import static app.TokenType.输出;
import static app.TokenType.返回;
import static app.TokenType.这个;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        // 中文编程
        keywords.put("并且", 并且);
        keywords.put("类", 类);
        keywords.put("否则", 否则);
        keywords.put("假", 假);
        keywords.put("循环", 循环);
        keywords.put("函数", 函数);
        keywords.put("如果", 如果);
        keywords.put("空", 空);
        keywords.put("或者", 或者);
        keywords.put("输出", 输出);
        keywords.put("返回", 返回);
        keywords.put("继承", 继承);
        keywords.put("这个", 这个);
        keywords.put("真", 真);
        keywords.put("声明", 声明);
        keywords.put("当", 当);
    }

    Scanner(String source) {
        this.source = source;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        // token 有四个属性 type lexeme literal line
        // 只有 字符串 和 数字 有 literal，其他的暂时都没有
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);

        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current + 1) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        int idx = current + 1;
        if (idx >= source.length()) {
            return '\0';
        }
        return source.charAt(idx);
    }

    private void string() {
        // 拿到所有 "" 内的东西
        // "" 可能报错因为差一个 "，也就是说到文件末尾也没有匹配到下一个 "
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            // 消耗当前字符
            advance();
        }
        // 退出循环有两种情况
        // 在文件末尾
        if (isAtEnd()) {
            MyLox.error(line, "Unterminated string.");
            return;
        }

        // 消耗另外一个 "
        advance();

        String value = source.substring(start, current);
        addToken(STRING, value);
    }

    private boolean isDigit(char ch) {
        return ch <= '9' && ch >= '0';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void number() {
        // 匹配数字
        while (isDigit(peek())) {
            advance();
        }

        // 如果有小数
        if (peek() == '.') {
            if (isDigit(peekNext())) {
                // 下个字符是数字，消耗当前 .
                advance();
                while (isDigit(peek())) {
                    advance();
                }
            } else {
                MyLox.error(line, "Unexpected character '.'");
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        // 首先得到 identifier 的名字
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);

        // 检查这个 identifier 是不是关键字
        TokenType type = keywords.get(text);

        if (type == null) {
            type = IDENTIFIER;
        }
        addToken(type);
    }

    private void scanToken() {
        // 扫描 token
        char c = advance();
        switch (c) {
        // 首先扫描最基本的 Token, 不会和其他类型 token 开头相等的单字符
        case ' ':
        case '\r':
        case '\t':
            break;
        case '\n':
            line++;
            break;
        case '(':
            addToken(LEFT_PAREN);
            break;
        case ')':
            addToken(RIGHT_PAREN);
            break;
        case '{':
            addToken(LEFT_BRACE);
            break;
        case '}':
            addToken(RIGHT_BRACE);
            break;
        case ',':
            addToken(COMMA);
            break;
        case '.':
            addToken(DOT);
            break;
        case '-':
            addToken(MINUS);
            break;
        case '+':
            addToken(PLUS);
            break;
        case ';':
            addToken(SEMICOLON);
            break;
        case '*':
            addToken(STAR);
            break;
        // 双字符
        case '!':
            addToken(match('=') ? BANG_EQUAL : BANG);
            break;
        case '=':
            addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            break;
        case '<':
            addToken(match('=') ? LESS_EQUAL : LESS);
            break;
        case '>':
            addToken(match('=') ? GREATER_EQUAL : GREATER);
            break;
        // 特殊情况——注释
        case '/':
            // 如果下一个还是 /, 那么就是注释
            if (match('/')) {
                // 我们不需要注释的内容
                while (peek() != '\n' && !isAtEnd()) {
                    // 消耗注释的内容
                    advance();
                }
            } else {
                addToken(SLASH);
            }
            break;

        // 字符串
        case '"':
            string();
            break;

        default:
            if (isDigit(c)) {
                number();
            } else if (isAlpha(c)) {
                identifier();
            } else {
                MyLox.error(line, "Unexpected character.");
            }
            break;

        }

    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return this.tokens;
    }

}

/**
 * MyLox, see beforeWrite.md
 */
public class MyLox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        if (hadError) {
            System.exit(65);
        }
        if(hadRuntimeError) {
            System.exit(70);
        }
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // for(Token token : tokens) {
        //     System.out.println(token.toString());
        // }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // AstPrinter ast = new AstPrinter();
        // System.out.println(ast.printStatements(statements));


        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);

    }

    private static void runPrompt() throws IOException {
        // 读一行
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print("> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == EOF) {
            // 代码结束了
            report(token.line, " at end", message);
        } else {
            // 没有结束
            report(token.line, " at '" + token.lexeme + "' ", message);
        }
    }

    static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("usage MyLox <input_file>");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
}