# 哔哩番剧

哔哩番剧正在迁移至Kotlin，Java实现项目地址：[BiliBangumiDownloader_Java](https://github.com/SGPublic/BiliBangumiDownloader_Java)

## 申明

这是一款开源的哔哩下载工具，本人不会为此项目申请计算机软件著作权、商标等其他任何形式的版权保护证明。

项目中会使用用户在b站上的个人信息，例如：用户昵称、追番列表、会员状态等，但本人不会利用此项目以任何形式窃取这些信息，例如：上传至第三方服务器等，所有信息仅以本地存放的形式保存在用户手机上。

源码仅供学习与交流，请勿用于非法用途。

2020.08.10

## 前言

这是一个业余开发者在初学阶段写出来的纯原生开发的项目，没有使用例如 MVVM 等任何框架，并且源码本身没有太多技术性，所以阅读源码时请压抑住顺着网线过来打我的冲动。

## 如何使用

1. 在b站中找到你想看的番，点击追番。
2. 打开哔哩番剧，登录之后即可看到你的追番列表。
3. 找到你追番列表中想缓存的番剧，点击打开详情界面，根据需要下载对应剧集即可。

## 常见问答

### Q：为什么必须登录才能使用？

答：b站部分番剧为大会员限定，因此我们必须验证您的会员权限。

### Q：我是 Android 9 以上的用户，为什么不能把番剧直接下载到b站缓存目录？

答：Android 9 及以上的系统中增加了软件对内部储存的读写限制，导致软件不能读写其他软件的私有目录，因此哔哩番剧会在这部分机型上默认把番剧下载到 **Download** 目录，建议您将b站缓存目录也设置为此目录，下载的内容依旧能正常识别。
