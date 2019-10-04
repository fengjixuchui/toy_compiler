# DFA 的最小化——详解 Hopcroft 算法





> 前言：继续干 Hopcroft 算法，这都是一些简单的小算法





## 0X00 为什么要这么做



在之前的子集构造算法中，我们最终得到了这么一个图：





![](https://upload-images.jianshu.io/upload_images/15548795-0dc5d83a4a527923.png?imageMogr2/auto-orient/strip|imageView2/2/w/341/format/webp)





但是这个图还能化简：



![](https://upload-images.jianshu.io/upload_images/15548795-c8da24c3de625256.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



这个算法就是用来化简 DFA 图的



## 0X01 伪代码



```c
split(S)
    foreach (character c)
        if (c can split S)
            split S into T1, …, Tk


hopcroft ()
	split all nodes into N, A
	while (set is still changes)
		split(s)
```



## 0X02 详解伪代码



主函数是第二个函数 `hopcroft()`，首先我们先弄清楚 `split all nodes into N, A` 这行代码是什么意思（拿出我们上一次画出的图）：





![](https://upload-images.jianshu.io/upload_images/15548795-0dc5d83a4a527923.png?imageMogr2/auto-orient/strip|imageView2/2/w/341/format/webp)



这行代码的意思就是将图中所有的节点分成两部分，A（Accept）和 N（Non A），A 就是我们的终态，也叫作接手态



所以就把上述节点分成：

![](https://upload-images.jianshu.io/upload_images/15548795-1f8769b07106988b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

绿色是 N 区，红色是 A 区。



现在我们来看 `split(s)` 是什么意思（从名字我们可以看出来，这是一个分割）：



![](https://upload-images.jianshu.io/upload_images/15548795-b8e8dcde8b3b1ed0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



假设有三个集合：`红绿黄`。



红色集合可以被分割成：{q1, q2} {q0}



也就是说可以分割指的是：`同一个集合能够被分成指向不同集合的不同集合`（有点绕口）



再回到我们这个图片：

![](https://upload-images.jianshu.io/upload_images/15548795-1f8769b07106988b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



被分成两个集合以后，再也不能被分割了。所以结果就变成：



![](https://upload-images.jianshu.io/upload_images/15548795-c8da24c3de625256.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



### 0X03 再举个例子



![](https://upload-images.jianshu.io/upload_images/15548795-af1ff570a8c8da75.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



首先被分成 A，N



N：{q0, q1, q2, q4}

A：{q3, q5}



由于 A 不接受任何字符所以不能再分，由于字母 e 可以让 q0, q1 指向内部或者不指向，而使 q2, q4 指向另外一个集合，所以 N 被分割成了：{q0, q1} {q2, q4}





然后 {q2, q4} 不能再分，{q0, q1} 还可以分为 {q0, q1}。所以最后结果为：



{q0} {q1} {q2, q4} {q3, q5}



![](https://upload-images.jianshu.io/upload_images/15548795-5ebc611216658a95.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)