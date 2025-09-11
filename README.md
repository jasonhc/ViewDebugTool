# V1版本 代码集成
主要接口类ViewDebugger, 在要集成的Activity中使用该类. 在onCreate()中创建ViewDebugger实例, 在layout xml中增加DebugToolView. onCreate()中加入下面的初始化代码:
```
        mDebuggerView = findViewById(R.id.ViewDebugger);
        mViewDebugger = new ViewDebugger(mDebuggerView);
```

在dispatchKeyEvent()开头加上如下代码:
```
        if (mViewDebugger.handleKey(event)) {
            return true;
        }
```
代码的作用是处理打开工具的秘密键和工具的按键处理.

## V2版本 新的集成方法
让要集成的Activity 继承BaseActivityDebugToolView

代码在project tv-deskplatform, branch view-debug-tool/view-debug-tool-dev

# 使用方法:
  依次按"9-5-2-7"启动该工具. <br>
  按方向键移动光标, 按1-6改变光标移动步长(数字越大移动步长越大), 按0让光标回到屏幕中心位置. <br>
  按确定键显示光标处的View, 第一次按确定显示子View, 再次按确定显示该View的父View; 已显示光标处的View后, 长按方向键可以显示Framework处理方向键时搜索到的下一个可获得焦点的View. <br>
  屏幕左上角显示光标当前位置和光标处像素点的颜色值, 按菜单键重新获取屏幕上所有像素点的颜色值.
