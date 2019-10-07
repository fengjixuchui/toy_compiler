# 开始写 Scanner 之前

> 梳理一下我们到底要写什么



1. 解释器的基本框架
+ 命令行
+ 运行脚本
+ 报错

2. Lexeme type
```java
enum TokenType {                                   
  // Single-character tokens.                      
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, 

  // One or two character tokens.                  
  BANG, BANG_EQUAL,                                
  EQUAL, EQUAL_EQUAL,                              
  GREATER, GREATER_EQUAL,                          
  LESS, LESS_EQUAL,                                

  // Literals.                                     
  IDENTIFIER, STRING, NUMBER,                      

  // Keywords.                                     
  AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,  
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,    

  EOF                                              
}
```

3. token 类

```java
class Token {
    // type 是 TokenType 里的一种
    final TokenType type;
    // lexeme 是识别出来的东西
    final String lexeme;
    // 字面量是值
    final Object literal;
    // 行数
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
```

4. scanner 类

这个是本章教程的关键。

+ 基本 scanner 类

```java
class Scanner {
  private int start = 0;                               
  private int current = 0;                             
  private int line = 1;                                                private final String source;   
  private final List<Token> tokens = new ArrayList<>();          

  Scanner(String source) {
    this.source = source;
  }
  
  List<Token> scanTokens() {                        
    while (!isAtEnd()) {                            
      // We are at the beginning of the next lexeme.
      start = current;                              
      scanToken();                                  
    }

    tokens.add(new Token(EOF, "", null, line));     
    return tokens;                                  
  }                                                                     
}
```