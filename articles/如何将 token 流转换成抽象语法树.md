# 如何将 token 流转换成抽象语法树（上）



> 前言：之前我们不是太艰难地将字符流转换成了 token 流，今天我们将尝试将 token 流转换成「抽象语法树」，本系列博客大部分内容来自 http://www.craftinginterpreters.com/，以下只是我的学习笔记。



![1+2*3](https://upload-images.jianshu.io/upload_images/15548795-52d22281e236ce9e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 0X00 基础理论



这次实现的抽象语法树只包括基础表达式，比如：



`1+2*3` 转换成如下的抽象语法树：



![1+2*3](https://upload-images.jianshu.io/upload_images/15548795-52d22281e236ce9e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





（非常建议和我一样在实现这个解释器的小伙伴，先去学习理论（华保健老师 编译原理前 8 章）再动手实现这个解释器）







### 基础理论——上下文无关文法



首先我们来感性地认识什么是「上下文无关文法」：



上下文无关文法没啥特别的意思，无非就是一种文法规则。比如我们定义了这样几条规则：



```
句子 -> 名词 动词 名词
名词 -> 羊
			| 草
			| 老虎
动词 -> 吃
			| 喝
```



这就是上下文无关文法。用这个文法我们可以构造出来：



+ 羊（名词）吃（动词）草（名词）

+ 老虎（名词）吃（动词）羊（名词）
+ 羊（名词）吃（动词）老虎（名词）

+ ...



接着我们用数学的手段描述一下上下文无关文法：



上下文无关文法是一个四元组：G(T, N, P, S)

- T 是终结符
- N 是非终结符
- P 是产生式规则
- S 是唯一的开始符号



套用上面的中文的例子：



T 是「羊 草 老虎 吃 喝」这样不能被替换的词

N 是「动词 名词」这样能够被替换的词

P 是「句子 -> 名词 动词 名词」这样描述替换的规则

S 是 一切的开始



### 基础理论——优先级和符号关联性



这样我们就能写一些有关本章的「上下文无关文法」：



```c
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





这是实现本章 token 流与抽象语法树转换的关键，其中 * 是可重复的意思。比如 1 * 2 * 3 * 4



完全理解之前，我们得先弄懂两个基本概念：「优先级」和「符号关联性」



+ **优先级**

这一点不用说，* 比 + 高。这里的符号优先级与 C 语言类似，且在上面那个「上下文无关文法」中，越在上面的符号，优先级越低也就是



`( "!=" | "==" ) < ( ">" | ">=" | "<" | "<=") <  ( "-" | "+" ) <  ( "/" | "*" ) <  ( "!" | "-" )`



+ **符号关联性**



除了 "!" "-" 这两个符号，其他符号都是左关联。



-1 !0 右关联



1 + 2 + 3 左关联



## 0X01 代码实现



接着我们要凭借着：



```c
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



实现抽象语法树了！注意，按照这个实现语法树，就会完成的符号的优先级了，而对于符号的关联性只需要在代码中具体实现就好了！



用的方法很简单，扫描 token，自顶向下建立抽象语法树





### 基本框架



先写出基本框架：从 parse() 解析 expression 开始：



```java
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    Expr parse() {
        return expression();
    }
}
```



### 根据文法写出替换



我们看到前两个替换：



```c
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
```



所以代码写成：



```java
    private Expr equality() {
        // equality → comparison ( ( "!=" | "==" ) comparison )* ;
        Expr expr = comparison();

        if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
            return expr;
        }

        return expr;
    }

    private Expr expression() {
        // expression → equality ;
        return equality();
    }
```



一路顺下来就可以写出最后的代码见：https://github.com/TensShinet/toy_compiler/blob/master/code/MyLox/src/app/Parser.java。



### 最后解释一下 Expr.Binary 这个是什么



由于现在是简单的表达式：`1+2*3` 这样的，所以表达式的种类并不多。



只有四种：



+ Unary 一元表达式。-1 
+ Binary 二元表达式 1 + 2
+ Group 组 (expression)
+ Literal 值 现在只有 数字 字符串 true false null













