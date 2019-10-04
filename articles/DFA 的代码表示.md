# DFA 的代码表示





> 前言：词法分析的最后一步，接下来详解如何使用「转移表」匹配字符串





![](https://upload-images.jianshu.io/upload_images/15548795-a295a6dd510a2300.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 0X00 什么是转移表



DFA 在本质上是一个有向图，而有向图当然可以用一张表来表示：



![](https://upload-images.jianshu.io/upload_images/15548795-56a14e7a7ad828f0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



把这张图转换成一张表可以表示为：





![](https://upload-images.jianshu.io/upload_images/15548795-6e2cbe1583e68421.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



举个例子，来解释表中的含义：



`状态 0 遇到字符 a 时就会跳到状态 1`。



其中没有填的我们可以叫他 `ERROR_STATE`



## 0X01 驱动代码的伪代码



有了这张表以后，我们就可以写「驱动代码」



什么是驱动代码呢？简单来说，现在我们有源程序。现在源程序源源不断进来了，我们要源源不断的处理源程序。这就是驱动代码。





伪代码如下：



```c
nextToken()
	state = 0
	stack = []
	while (state!=ERROR)
		c = getChar()
		if (state is ACCEPT)
			clear(stack)
		push(state)
		state = table[state][c]


	while(state is not ACCEPT)
		state = pop();
		rollback();
```





## 0X02 举个例子解释驱动代码





假设我们有这么一个 DFA：



![](https://upload-images.jianshu.io/upload_images/15548795-d36193c5f7b82c98.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





有这么两段源程序：



`ifif`



和



`ifii`



会发生什么？脑补一下吧。。。



接下来，我们就要开始语义分析了！









