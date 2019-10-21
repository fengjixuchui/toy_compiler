# 编译原理——让我们输出「I Love You」





> 前言：更一更「编译原理」，这篇文章的主要目的是执行 print "I Love You!"



## 0X00 基本原理



在之前我们实现了一个「计算器」，能够生成表达式（1 + 2 * 3）的「抽象语法树」并执行：



![1+2*3](https://upload-images.jianshu.io/upload_images/15548795-52d22281e236ce9e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



现在我们要**兼容**生成  print "I Love You!" 的「抽象语法树」，并能够执行



所以之前的「语法树」的根节点（Expr）类型不能用了。



我们正式引入「程序（program）」概念，并写出程序的「上下文无关文法」



```python
program   → statement* EOF ;

statement → exprStmt
          				| printStmt

exprStmt  → expression ";" ;
printStmt → "print" expression ";" ;

# 之前的表达式的上下文无关文法
# 实现「兼容」的效果
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "false" | "true" | "nil"
               | "(" expression ")" ;
```



并引入一个数组：`statements`



这个 statements 记录所有的 statement，每个 statement 是一个「抽象语法树」



## 0X01 代码实现



我们先简述一下我们要干些什么：



+ 识别代码中的 statement

现在我们只会识别两种 statement。

```python
statement → exprStmt
          				| printStmt
```



exprStmt 就是像这样的 `1 + 1 + 2;` 式子。printStmt 就是像这样的 print "I Love You"



+ 将每一个 statement 转成一个抽象语法树（Parser）

+ 执行每一个 statement（Interpreter）



### 识别代码中的 statement 以及将 statement 转换成「抽象语法树」



在 parser 中我们得到的是一个 token 流，我们根据 `;` 分割 statement



由于 print 的右边就是一个「表达式」，所以我们能够直接兼容上一次的代码，主要代码如下：



```java
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
    List<Stmt> parse() {                          
        List<Stmt> statements = new ArrayList<>();  
        while (!isAtEnd()) {                        
            statements.add(statement());              
        }

        return statements;
    }
```





### 执行「抽象语法树」



执行 print 的「抽象语法树」也很简单。



我们从 statements 出发，执行每一个 statement。



而对于 print statement 来说，最后要输出的还是它的「右值」，而右值是一个表达式，可以与之前的代码兼容，所以直接计算右值，主要代码如下：



```java
@Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        return null;
    }
```





```java
// 执行语句
private Object execute(Stmt stmt) {
    return stmt.accept(this);
}
public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            MyLox.runtimeError(error);
        }
    }
```





最后结果：



![](https://upload-images.jianshu.io/upload_images/15548795-bafb95d823cd83fb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)







