# 语法分析——LR(0) 分析算法





> 前言：语法分析算法挺多的，LR(0) 算法是一个经典的「自底向上」的语法分析算法





## 0X00 自底向上分析的基本思想

首先我们来看一下「自底向上分析」的具体例子，这里有一个生成式规则：



![](https://upload-images.jianshu.io/upload_images/15548795-c09bce088ddd3633.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



以及一个待「归约」的式子：`2 + 3 * 4`



![](https://upload-images.jianshu.io/upload_images/15548795-d55ed66513568431.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



从下往上看这是一个「最右推导」，所以这个算法的核心思想就是：



`最右推导的逆过程！`



## 0X01 加上「点记号」的算法分析

为了更加清晰的认识上述：自底向上的过程。我们加入了一个点记号

![](https://upload-images.jianshu.io/upload_images/15548795-c2028916335581ee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

红点就是我们的点记号，红点左边就是已经读入的，红点右边就是还没有读入的。因此我们可以把上述过程写成：



![](https://upload-images.jianshu.io/upload_images/15548795-4eaa1a644f2b23f4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





## 0X02 如何生成一个逆序的最右推导

说到底这个「逆序的最右推导」其实就两个步骤：



+ **移进**：将记号移入栈中

+ **归约**：将栈顶的 n 个符号（实际上是某产生式的右部）换成这个产生式的左部

  比如说有生成式 A -> B1, B2, ..., Bn 栈顶是 Bn, ..., B2, B1。那么「归约」成 A



所以接下来我们将讨论：`如何确定「移进」和「归约」的时机`



## 0X03 LR(0)分析算法——伪代码

毫不意外，我们有这样一张 LR(0) 分析表：

![](https://upload-images.jianshu.io/upload_images/15548795-6e564b73cd38286e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



具体这张表怎么得来的，在下面一节中会说（这里的 $ 是类似于 EOF 之类的东西。s2, g6, r2 代表的是 2, 6, 2）。





然后我们来看 LR(0) 分析算法的「伪代码」：



```c
stack = []
push ($) // $: end of file
push (1) // 1: initial state
while (true)
	token t = nextToken()
	state s = stack[top]
	if (ACTION[s, t] == "si")
		push (t); 
		push (i)
	else if (ACTION[s, t] == "rj")
		pop (the right hand of production "j: X -> B")
		state s = stack[top]
		push (X); 
		push(GOTO[s, X])
else error (…)
```





## 0X04 LR(0)分析算法——举例子

用一个例子，运行上述伪代码：



输入串：`x x y $`



开始时栈的结构：

```
|_1_|
|_$_|
```



+ t = x, s = 1

  ACTION[1, x] = s2, push(x) push(2)

  此时栈为： 

```
|_2_|
|_x_|
|_1_|
|_$_|
```



+ t = x, s = 2

  ACTION[2, x] = s3, push(x) push(3)

  此时栈为：

```
|_3_|
|_x_|
|_2_|
|_x_|
|_1_|
|_$_|
```



+ t = x, s = y

  ACTION[3, y] = s4, push(y) push(4)

  此时栈为：

```
|_4_|
|_y_|
|_3_|
|_x_|
|_2_|
|_x_|
|_1_|
|_$_|
```



+ t = $, s = 4

  ACTION[s, t] = r2。开始进行 2 的「归约」，pop(4, y)

  s = 3

  push(T)

  push(GOTO[s, X]) = push(GOTO[3, T]) = push(5)

  此时栈为：

```
|_5_|
|_T_|
|_3_|
|_x_|
|_2_|
|_x_|
|_1_|
|_$_|
```



+ t = $, s = 5

  ACTION[s, t] = r1。开始进行，1 的「归约」，pop(5, T, 3, x, 2, x)

  s = 1 

  push(S)

  push(GOTO[s, X]) = push(GOTO[1, S]) = push(6) 

  此时栈为：

```
|_6_|
|_S_|
|_1_|
|_$_|
```



+ t = $, s = 6

  最后到了接受态，无报错







## 0X05 LR(0) 分析表构造伪代码（暂时没有研究）



![](https://upload-images.jianshu.io/upload_images/15548795-9b4714bc191f05d7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





![](https://upload-images.jianshu.io/upload_images/15548795-c4f719376e75969e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 0X06 LR(0) 算法的改进——SLR 算法



这个算法，仅仅是在 LR(0) 算法的基础上，增加了归约的规则：



+ 仅对要「规约」时最后一个 token 的 follow 集进行归约





举个例子：



![](https://upload-images.jianshu.io/upload_images/15548795-6e564b73cd38286e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



原来我们的归约表是这样的，看第 4 行，如果 4 状态又来了一个 x，此时应该是报错，而不是归约。





所以我们只对 4 状态接受到 $（相当于 EOF）进行归约，其他的从表中删除：



![](https://upload-images.jianshu.io/upload_images/15548795-155d070f7de2d04b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



 





