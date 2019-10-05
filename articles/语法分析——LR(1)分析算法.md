# LR(1) 分析算法





> 前言：我把学习「编译原理」的整个重心放在了前端上，前八周的课我已经看完了。现在是补博客的时间，语法分析的算法很多，慢慢梳理吧，今天复习「LR(1) 分析算法」





## 0X00 本质不同



**LR(0) LR(1) 其实没有什么不同的地方，本质区别在于「分析表怎么画」**







## 0X01 一起画一个 LR(1) 分析表



分析表中的一个状态被叫做项目



![](https://upload-images.jianshu.io/upload_images/15548795-fc0d7900bad9d8dd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



这个被叫做项目集。下面举个实例画这个分析表：



![](https://upload-images.jianshu.io/upload_images/15548795-b5e2b17fddb70652.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

+ 首先得到我们需要的所有生成式：



```
0: S -> E$
1: E -> E + E
2: 	  -> E * E
3:    -> n
```





+ 给第 0 项目添加第一条移进规则



```
S -> ·E
```



然后有以下规则：



![](https://upload-images.jianshu.io/upload_images/15548795-1ce39791df8877e9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



首先解释什么是 FIRST_S 集合，简单来说 FIRST_S 集合就是 FIRST 集合，只不过 FIRST_S 可以是一串字符拼接就像图中 $\beta\alpha$。



然后解释 a 的值是什么，简单来说 a 是 FOLLOW( $Y\beta$ )，所以在这条规则中 a 是 \$，而 $\beta$ 为空



所以 b = FIRST( \$ ) = \$



+ 按上述操作可以求得 0 项目新的 LR(1) 分析表：



```
S -> ·E, $
E -> ·E + E, $
	-> ·E * E, $
	-> n, $
```



写到这里：关键的地方来了，由于 E 由被写进去了，所以还要将 E 又写进去，此时的 $\beta$ 是 +E，a 是 \$ 所以 FIRST(+E$ ) = +，最后合并在一起就变成

```
S -> ·E, $
E -> ·E + E, $, +, *
	-> ·E * E, $, +, *
	-> ·n, $, +, *
```



... 



## 0X02 LR(1) 分析算法的工具（暂时还没有研究）



大概以后会看这篇文章：



https://www.ibm.com/developerworks/cn/linux/sdk/lex/index.html



