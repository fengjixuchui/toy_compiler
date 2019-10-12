# 编译原理——如何将字符流转换为 token 流



> 前言：好吧，开始对编译器下手了。。。本系列博客大部分内容来自 http://www.craftinginterpreters.com/，以下只是我的学习笔记。



![](https://upload-images.jianshu.io/upload_images/15548795-4d408f4a1e235704.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



## 0X00 对原理的讨论



将字符流转换成 token 流是「编译原理」最开始的部分，把这一块的代码叫做「词法分析器」



首先说什么叫做 token：

![](https://upload-images.jianshu.io/upload_images/15548795-4d408f4a1e235704.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**像这样的每一块都是一个 token**。



在我要实现的解释器中，每一个 token 有四个属性他们分别是：`type`、`lexeme`、`literal`、`line`



先说简单的三个：`type` 、`lexeme`和 `line`



`type` 是 token 的类型，token 的类型一共有：



```java
public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
    
    EOF
}
```



`lexeme` 用来保存这个 token 的字符串，比如 var 这个 token。它的 lexeme 就是 "var"



而 `line` 是 token 所在的行，用来报错。



最后说 `literal`



由于我们使用 Java 实现我们的脚本语言，所以对于有些 token 是必须把字符串解释成 Java 中的 Object。



比如："lox" 就要解释成 Java 中的 String。



最后回到本质上，词法分析器的本质任务就是将字符串，归类成一个又一个的 token。接下来我们来说说如何将这些字符串进行识别！



## 0X01 识别特殊符号 token



编程中少不了的就是这些「特殊符号」：+ - * / 之类的，首先将这些特殊符号分类成单字符，和双字符的：



+ 单字符的特殊符号识别



```java
switch (c) {
        case ' ':
            break;
        case '\r':
            break;
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
```



+ 双字符的特殊符号识别



由于有些双字符，的第一位是单字符的特殊符号，所以有一些特殊的操作：





```java
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
```



match 是为了匹配下一个字符



由于注释的第一个字符是 /，可能是除法也可能是注释的第一个 /。如果是除法，我们添加这个 token，如果是注释，我们把整行注释都消耗掉。

```java
 case '/':
            if (match('/')) {
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd())
                    advance();
            } else {
                addToken(SLASH);
            }
            break;
```





## 0X02 识别字符串 token

在要实现的解释器中，我们规定字符串是以 " 开头，以 " 结尾，所以一旦发现了 " 就要小心了。



+ 如果识别了一个 \n 那么行数要增加！
+ 到了文件或者命令末尾还不能识别到另一个 " 就要报错了！



```java
private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            BookLox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }
```



## 0X03 识别数字 token



识别数字一定要放在识别标识符的前面，因为标识符中也可能出现数字。



数字无非是 123、或者 123.123，但是数字也有可能写错：123.，也就是小数点后面必须有数字，最后的识别代码：



```java
 private void number() {
        while (isDigit(peek()))
            advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();
            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }
```



## 0X04 识别标识符 token 与 关键字 token



由于我们先识别的是数字，所以标识符第一个字符肯定不是数字。



所以一旦遇到了字母，我们就把他当成标识符的开头，不断获取新的字符，直到拿到整个标识符（数字+字母）



这里的字母除了 a~z 和 A~Z 还有一个字符 _，所以写成：



```java
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
```



```java
private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }
```





拿到整个标识符以后，我们还要注意，他可能是「关键字」，这是我们预设好的关键字：





```java
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
    }
```



最后看这个 Hash 表里面有没有这个字符串，如果有这个字符串，我们就把它识别出来。



最后详细代码：https://github.com/TensShinet/toy_compiler/blob/master/code/MyLox/src/app/MyLox.java







