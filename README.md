# 实现一个简单的 compiler







## 0X00 仓库介绍



本仓库主要记录自己学习大佬的教程一步一步实现一个 toy_compiler，从「前端」到「后端」，从「解释器」到「编译器」



大佬教程如下：



[http://craftinginterpreters.com](http://www.craftinginterpreters.com/)  





## 0X01 目录结构



+ `articles` 记录了我学习编译原理所写的博客



+ `code` 记录了最后实现的代码



+ `images` 记录了一些图片



其中编译器代码的入口在：



`code/MyLox/src/app/MyLox.java`



## 0X02 学习路线



从 2019.10.1 ~ 2019.10.28 中间大概花了 20 天的时间全部投入 「编译原理」当中：  



前七天刷完了：[编译原理 华保健老师](https://mooc.study.163.com/course/1000002001?tid=1000003000) 前八章。  



重点放在了「编译原理」的前端。  



之后把所有精力投入看大佬的教程：[http://craftinginterpreters.com](http://www.craftinginterpreters.com/)  



这是我为了学习编译原理所写的所有博客：



+ [编译原理——流程控制](https://www.jianshu.com/p/885e89319ce6)
+ [编译原理——函数！](https://www.jianshu.com/p/752781690393)
+ [编译原理——变量！](https://www.jianshu.com/p/ad806361cf0e)
+ [编译原理——让我们输出「I Love You」](https://www.jianshu.com/p/9e4cff9f4594)
+ [抽象语法树的执行](https://www.jianshu.com/p/e6e9f9d9c30e)
+ [如何将 token 流转换成抽象语法树（上）](https://www.jianshu.com/p/e420992c6078)
+ [编译原理——如何将字符流转换为 token 流](https://www.jianshu.com/p/047a0d4a9ad5)
+ [LR(1) 分析算法](https://www.jianshu.com/p/3cfb02203586)
+ [语法分析——LR(0) 分析算法](https://www.jianshu.com/p/d56a08249b24)
+ [语法分析——FOLLOW 集合](https://www.jianshu.com/p/9e0324383100)
+ [语法分析——LL(1) 分析算法](https://www.jianshu.com/p/b6240f6b347f)
+ [语法分析基础](https://www.jianshu.com/p/5f2c0c10a2df)
+ [DFA 的代码表示](https://www.jianshu.com/p/593be71e85db)[DFA 的最小化——详解 Hopcroft 算法](https://www.jianshu.com/p/4900b44ada62)
+ [NFA 与 DFA 的转换——详解子集构造算法](https://www.jianshu.com/p/6bfc32fb520a)
+ [详解正则表达式与 NFA 的转换](https://www.jianshu.com/p/190ce24c420f)



由于逃了太多课，没有精力再继续学下去了，先暂停一段时间



## 0X03 实现的功能



基本实现了一个「面向过程」语言的所有功能



```javascript
fun add(a, b, c) {
    return a+b+c;
}

fun b() {

    var a = 0;
    fun closure() {
        a = a + 1;
        print a;
    }

    return closure;
}

fun main() {
    if(1) {
        var fun1 = b();

        fun1();
        fun1();
    } else {
        print add(1, 2, 3);
    }
}

main();
```



输出：

```
1
2
```





+ 字符流到 token 流
+ 从 token 流到「抽象语法树」
+ 从「抽象语法树」到「执行抽象语法树」



实现了：



+ `局部变量` 与 `全局变量`
+ `流程控制 if else 与 while for 循环`
+ `函数的声明及调用`
+ `闭包`
+ ...



## 0X04 最后



扫个码我们做朋友吧！**顺便点个 star 呗！**



![](./images/wx.jpg)



