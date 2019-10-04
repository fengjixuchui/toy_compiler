# 语法分析——LL(1) 分析算法



> 前言：LL(1) 分析算法，是一个经典的自顶向下的语法分析算法



## 0X00 解释为什么叫做 LL(1)

从左（L）向右读入程序，最左（L）推导，采用一个（1）前看符号。所以就叫做 LL(1)



LL(1) 是一个自顶向下的语法分析算法，算法的基本思想是：`表驱动的分析算法`



## 0X01 算法伪代码



```c
tokens[]; // all tokens
i=0;
stack = [S] // S是开始符号
while (stack != []) {
    // 这里给 t 赋值了
    if (stack[top] is a terminal t) {
        if (stack[top] == tokens[i++]) {
			pop();
        } else {
            error();
        }
    } else if (stack[top] is a nonterminal T) {
        // 这里给 T 赋值了
        pop(); 
        push(the correct table[N, T]);
    }
}
```



## 0X02 结合具体例子解释伪代码



假设有这么一个规则：



![](https://upload-images.jianshu.io/upload_images/15548795-de4d49361d2cfc8a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



由这一个规则可以得到一个表（怎么得来的，后面再详细说）：



![](https://upload-images.jianshu.io/upload_images/15548795-d5a299fd683d686c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



假设有这么一段输入：`gdw`



一开始的栈结构如下，

```c
|_S_|
```



S 不是终结符，将表中 (S, g) 对应的规则 0 推入栈中，此时栈为：

```
|_N1__|
|__V__|
|_N2__|
```



N1 不是终结符，将 (N1, g) 对应的规则 3 推入栈中，此时栈为：

```
|__g__|
|__V__|
|_N2__|
```



g 是终结符，此时 i = 0，token[i++] = g 所以不报错，此时栈为：



```
|__V__|
|_N2__|
```



V 是不是终结符，将 (V, d) 对应的规则 6 压入栈中，此时栈为：



```
|__d__|
|_N2__|
```



...



最后检验没有出错。



接下来我们说说：这个表是如何生成的



## 0X02 FIRST 表

![](https://upload-images.jianshu.io/upload_images/15548795-d5a299fd683d686c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



这个表叫做叫做 First 表，又叫做 LL(1) 分析表。由下面的这个关系转换得出



![](https://upload-images.jianshu.io/upload_images/15548795-de4d49361d2cfc8a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 0X03 如何生成 FIRST 表



伪代码很简单（有缺陷）：

```c
foreach (nonterminal N)
	FIRST(N) = {}
while(some set is changing)
	foreach (production p: N->β1 … βn)
		if (β1== a …)
			FIRST(N) ∪= {a}
		if (β1== M …)
			FIRST(N) ∪= FIRST(M)
```



简单来说，就是`并上字符以及并上非终结符的 FIRST 表`





## 0X04 上述代码的缺陷



假如我们有这么一个转换规则：



![](https://upload-images.jianshu.io/upload_images/15548795-c428f37e1333cc56.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





就会发现 (N,w) 有两条对应规则，所以最后我们得到的表是：



![](https://upload-images.jianshu.io/upload_images/15548795-ad608b0f476a1399.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





这样就又可能出现了「回溯」。





还有一种出现空集的情况：



![](https://upload-images.jianshu.io/upload_images/15548795-604894c19eb9ea05.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

X 可能为空，Y 可能为空，又出现了回溯的情况。





接下来我们将讨论一般情况下的 FIRST 表的生成



## 0X05 一般情况下的 FIRST 表的生成——解决空集问题





### 首先定义：可以为空的非终结符属于 NULLABLE 集合



NULLABLE 集合就是**所有可以为空的非终结符，有的直接可以空，有的可以再推导以后可以为空**。



用数学的方法表示一个非终结符是否为空：



![](https://upload-images.jianshu.io/upload_images/15548795-023826c0812141d3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 计算 NULLABLE 集合——伪代码



```c
NULLABLE = {};
while (NULLABLE is still changing)
	foreach (production p: X-> β)
    	if (β== NULL)
            NULLABLE ∪= {X}
		if (β == Y1 … Yn)
			if (Y1 in NULLABLE && … && Yn in NULLABLE)
				NULLABLE  ∪ = {X}
```





### 计算 NULLABLE 集合——举个例子



![](https://upload-images.jianshu.io/upload_images/15548795-6434ba392f88bb46.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





首先可以看出 Y in NULLABLE



对与 X 可以写出：



X -> Y，由于 Y in NULLABLE，所以 X in NULLABLE



对于 Z 可以写出



Z -> X Y Z，所以 Z 不是





## 0X06 一般情况下的 FIRST 表的生成——冲突的处理



我们来看下面这个例子，思考产生冲突的原因一：



假如有这么一条规则：



```c
0: E -> Eb
1: 	  -> a
```



就一定会产生冲突，比如第 0 条产生式就可以写成：aaaaab，第 1 条生成式就可以写成 a，所以遇到 a 的时候 0, 1 都可以指



一定会冲突！



解决的办法就是：`把左递归转换成右递归`可以把上述有错的生成式写成：



```c
0: E -> aE1
1: E1 -> aE1
2:       -> b
3:       -> 
```



另外一种消除冲突的方法是**提取左公因子**，比如有以下文法：



```c
0: E -> ab
1: 	  -> ac
```



我们可以写成：



```c
0: E -> aE1
1: E1 -> b
2:       -> c
```







## 0X07 一般情况下的 FIRST 表的生成——伪代码



至此，我们就可以写出一般情况下的 FIRST 表的生成伪代码：



```c
foreach (nonterminal N)
	FIRST(N) = {}
while(some set is changing)
	foreach (production p: N->β1 … βn)
		foreach (βi from β1 upto βn)
			if (βi== a …)
				FIRST(N) ∪= {a}
				break
			if (βi== M …)
				FIRST(N) ∪= FIRST(M)
				if (M is not in NULLABLE)
					break
```



其实理解起来就是这个：





![](https://upload-images.jianshu.io/upload_images/15548795-2e87dee61004cb7e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)









编译原理全部都是算法。。。写得我好累啊



