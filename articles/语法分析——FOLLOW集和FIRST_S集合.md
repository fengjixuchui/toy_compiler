# 语法分析——FOLLOW 集合

> 前言：学习编译原理就是被各种算法虐



## 0X00 FOLLOW 集合介绍

首先感性地认识一下什么是 FOLLOW 集合



假如有这么一些生成式规则：



```
S ->Aa | Ac
A ->b  
```



可以写成：



```
      S                  S  
     /  \              /   \
    A    a          A     c
    |                  |
    b                 b   
```



所以 A 的 Follow 集合就是 {a, c}



所以说，求 Follow 集合的意思就是求`非终结符右边所有终结符的集合`



## 0X01 FOLLOW 集合伪代码

求 Follow 集合的伪代码如下：



```c
foreach (nonterminal N)
	FOLLOW(N) = {}
while(some set is changing)
	foreach (production p: N->β 1 … βn)
        temp = FOLLOW(N)
        foreach (βi from β n downto β 1) // 逆序！
            if (βi== a …)
                temp = {a}
            if (βi== M …)
                FOLLOW(M) ∪= temp
                if (M is not NULLABLE)
                    temp = FIRST(M)
            else temp ∪= FIRST(M)
```



这个伪代码有点难懂，我们来看一个具体的例子



## 0X02 FOLLOW 集合——举例子

![](https://upload-images.jianshu.io/upload_images/15548795-65f4c3a3674ab3bf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

拿这个例子举例，首先得到它的 FIRST 表和 NULLABLE 集合：

![](https://upload-images.jianshu.io/upload_images/15548795-e7440e062ef2b74e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



首先我们有这么一张空表：

![](https://upload-images.jianshu.io/upload_images/15548795-43d03d3131744cf2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



不是很好描述，最好去看：



https://www.bilibili.com/video/av32233569/?p=53





