# qianghongbao
微信抢红包插件APP

首先声明 只是本人学习需要研究了一下 我不负任何法律责任
暂时实现的功能 如果能接收到微信消息的notification是完全可以抢到红包的
原理很简单 就是有notification过来 看notification里面有没有"微信红包字样" 如果有 
用AccessibilityService模拟打开notification 然后点击聊天记录中最新的那个红包 然后拆红包

AccessibilityService这个类原本是为了方便一些不方便的人 然后。。被我们用来抢红包了 我也是醉了
在微信6.3.8测试通过 其他更新的版本就不知道了 毕竟微信有很多方法可以杜绝这样的抢红包行为

主要监听如下3个变化
typeNotificationStateChanged  最主要的有notification过来然后分析上面的文字
typeWindowStateChanged      因为微信主页的构造Fragment+FragmentActivity这样的形式做的 所以打开整个微信主页和聊天界面就只会触发1次
typeWindowContentChanged    内容改变就会收到通知 这个通知的频率很高 慎用 

代码书写的很详细了

也有参考人家的代码 大家互相学习 共同进步

有些逻辑还不是很严谨 不过今年过年回去 妈妈再也不用担心我漏掉红包了
