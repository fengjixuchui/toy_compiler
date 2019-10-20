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
          				| printStmt ;

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









