# NFA 与 DFA 的转换——详解子集构造算法



> NFA 与 DFA 的转换



a(b|c)* 的 NFA 图



![](https://upload-images.jianshu.io/upload_images/15548795-e015b2fcadc32560.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)





今天我就要把上面这个图转换成 DFA



![](https://upload-images.jianshu.io/upload_images/15548795-0dc5d83a4a527923.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)







## 0X00 为什么要这么做



一旦有不确定的状态转换就会涉及**「回溯」**



一旦涉及「回溯」



就代表无用功，就代表无用的迭代，因此我们要避免回溯。



### 0X01 伪代码



```c
q0 <- eps_closure (n0)
Q <- {q0}
workList <- q0
while (workList != [])
    remove q from workList
    foreach (character c)
        t <- eps_closure(delta (q, c))
        D[q, c] <- t
        if (t not in Q)
       		add t to Q and workList
```

<- 是赋值

## 0X03 详解伪代码 

+ 伪代码的第一步就是求 n0 的  eps_closure，可以得到 q0 = {1}

  

### 首先什么是 eps_closure 

![](https://upload-images.jianshu.io/upload_images/15548795-e015b2fcadc32560.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



先有个感性的认识，求 2 的 eps_closure



{2， 3， 5}



4 的 eps_closure：



{4, 5, 3}



根据上面两个例子：



可以很感性地认识到一个节点的 eps_closure 就是**一个节点不需要其他输入时，所能到达的所有节点**



而求 eps_closure 很容易用「深度优先搜索」来得到。



+ 来到代码第 8 行：计算闭包 q 中**增加一个字符以后的新的闭包**



比如 q0 从 worklist 中拿出以后，可以和 a 结合得到 n1。那么由 n1 计算出来的新闭包就是q1 {2, 3, 5}





+ 代码第 9 行：D[q, c] 记录下这个关系 



+ 如果这个闭包 t 是没出现过的就加入 Q 和 worklist 中



### 最后 

worklist 为 空



Q 为 {q0, q1, q2, q3}

q0 = {1}

q1 = {2, 3, 5}

q2 = {3, 4, 5}



D 是记录关系的表

D[q0, a] = q1

D[q1, b] = q2

D[q1, c] = q2

D[q2, b] = q2

D[q2, c] = q2



注：**我也是看的华老师的《编译原理》但是画 NFA，DFA 的方法有些不一样。** 











