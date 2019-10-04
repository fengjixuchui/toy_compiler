# 详解正则表达式与 NFA 的转换



> 前言：编译原理开坑！



## 0X00 什么是 NFA



NFA 是 Non-deterministic Finite state Automata  的缩写。所以先理解 NFA 之前我们先理解 DFA，也就是 deterministic Finite state Automata。



### 先理解 DFA



通俗的说，`DFA 就是一系列状态的合集`，关键词是**状态**！



我们先写一个关于灯泡的 DFA：





![](https://upload-images.jianshu.io/upload_images/15548795-3da18ddc33745f63.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





（两个圈是终态，我们把「灯泡关闭」的状态当做终态）





### 再理解 NFA



简单来说，NFA 就是存在着不确定状态转换的 DFA。



我们还拿灯泡的例子：



![](https://upload-images.jianshu.io/upload_images/15548795-9abdf42f4265c7b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





（灯泡打开的时候还有可能会坏掉）



## 0X01 正则表达式与 NFA 的转换



先列出三种基本正则表达式的 NFA 图：



+ AB

表示 A 与 B 的连接，NFA 图如下：



![](https://upload-images.jianshu.io/upload_images/15548795-905c279b8c8e1d83.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





+ A|B



![](https://upload-images.jianshu.io/upload_images/15548795-f1a6528b6140b86e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





+ A*



![](https://upload-images.jianshu.io/upload_images/15548795-0bcf3a8bdcf3bbb8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



## 0X02 完成复杂的正则表达式与 NFA 的转换





我们来画一个复杂的正则表达式与 NFA 的转换





+ a(b|c)*



1）首先把 a 看成 A，把 (b|c)* 看成 B就有：





![](https://upload-images.jianshu.io/upload_images/15548795-45e543301b3ef1f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



2）再拆解 (b|c)*





![](https://upload-images.jianshu.io/upload_images/15548795-2f6f233c8c23220f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



3）最后拆解 b|c





![](https://upload-images.jianshu.io/upload_images/15548795-855f964d484f2a5f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



好！我们继续学习编译原理！