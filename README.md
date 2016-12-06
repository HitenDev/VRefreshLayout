# VRefreshLayout
一个竖直方向的下拉刷新控件，支持自定义Header，可配置参数，最重要的特点是**刷新时头部置顶显示，且不影响列表滑动**；

## 为什么要做头部置顶显示
现在越来越多的App下拉刷新时是置顶显示的，比如手机京东和天猫商城，我们在下拉刷新时，头部的刷新视图是保持显示的，这样在滚动列表的时候，用户可以知道当前正在刷新状态，而传统的下拉刷新库，比如[Android-PullToRefresh](https://github.com/chrisbanes/Android-PullToRefresh)和[android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh)，是不支持的头部置顶显示的，用户往下翻看时，刷新状态就可能被隐藏；google的SwpieRefreshLayout是置顶层显示的，但是它的内容区域的不会跟着动的，且可定制性太差。


## 功能预览

头部刷新时列表可以正常响应sh事件，且头部不会隐藏

![](https://github.com/ileelay/VRefreshLayout/blob/master/pics/gif1.gif)

良好的兼容性，支持ListView/RecyclerView/ScrollView/TextViw/Image等绝大多数view

![](https://github.com/ileelay/VRefreshLayout/blob/master/pics/gif2.gif)

可以配置下拉高度，阻尼度以及各部分动画时长等参数

![](https://github.com/ileelay/VRefreshLayout/blob/master/pics/gif3.gif)


## 简单使用

- 在布局中，包裹一个将要刷新的View/ViewGroup
```java
 <com.leelay.freshlayout.verticalre.VRefreshLayout
        android:id="@+id/refresh_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        >

        <ListView
            android:id="@+id/listView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@android:color/white"
            />

    </com.leelay.freshlayout.verticalre.VRefreshLayout>
    
```
java代码中，监听和控制刷新状态
```
 mRefreshLayout = (VRefreshLayout) findViewById(R.id.refresh_layout);
 mRefreshLayout.addOnRefreshListener(new VRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshLayout.refreshComplete();
                        }
                    }, 2000);
                }
            });
 

```

这样就能在下拉时触发刷新时间，2000ms后刷新完成，隐藏刷新状态；

## 更多介绍

- setDragRate(float dragRate)  设置拖拽的阻尼比例，默认是0.5，一般设置在0-1之间比较合适;
- setRatioOfHeaderHeightToRefresh(float ratio)  设置允许刷新的高度和HeaderView高度的比例，默认为1.0;
- setRatioOfHeaderHeightToReach(float ratio) 设置能下拉的最大高度和HeaderView高度的比例，默认为1.6;
- setToStartDuration(int toStartDuration) 设置回到初始状态动画执行时间，单位为ms，默认是200ms;
- setToRetainDuration(int toRetainDuration) 设置手指离开屏幕后触发刷新时到刷新保持的位置需要的动画时长，单位为ms,默认为200ms;
- setAutoRefreshDuration(int autoRefreshDuration) 自动刷新需要的动画时长 ，默认为800ms；
- setCompleteStickDuration(int completeStickDuration) 刷新完成后状态保持的时长，默认为200ms;
- setHeaderView(View view) 设置一个自定义的HeaderView;
- setUpdateHandler(UpdateHandler updateHandler) 设置状态和进度更新处理，一般是Header实现；
- addOnRefreshListener(OnRefreshListener onRefreshListener) 添加一个刷新回调监听，支持add方式；
- refreshComplete() 调用次方法，可以接受刷新；

## 自定义HeaderView
自定义Header只需要集成一个ViewGroup/View，实现VRefreshLayout.UpdateHandler接口即可；
最基本的代码
```java
public class CustomHeaderView extends ViewGroup implements VRefreshLayout.UpdateHandler{
    public CustomHeaderView(Context context) {
        super(context);
    }

    public CustomHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        
    }

    @Override
    public void onProgressUpdate(VRefreshLayout layout, VRefreshLayout.Progress progress, int status) {

    }
}
```
UpdateHandler只有一个方法onProgressUpdate(VRefreshLayout layout, VRefreshLayout.Progress progress, int status)，重写它就能监听下拉进度和状态；
### VRefreshLayout.PProgress
Progress类一个简单的Entity，其中有三个属性，分别是
- totalY 可下拉的最大Y方向距离
- currentY 当前Y方向距离
- refreshY 触发刷新的距离这些
- refreshY 触发刷新的距离
这些距离的参考点是contentView的Top点;

### status状态
```
public final static int STATUS_INIT = 0;//原始状态
public final static int STATUS_DRAGGING = 1;//正在下拉
public final static int STATUS_RELEASE_PREPARE = 2;//松手将要刷新
public final static int STATUS_REFRESHING = 3;//正在刷新
public final static int STATUS_RELEASE_CANCEL = 4;//松手取消
public final static int STATUS_COMPLETE = 5;//刷新完成
    
```
状态解析：
- STATUS_INIT 原始状态，发生在MOVE事件之前和刷新完成headerView完全隐藏之后
- STATUS_DRAGGING 手指拖拽状态，只有在触发下拉后且手指没有完全离开屏幕，发生在这一时间段内；
- STATUS_RELEASE_PREPARE 拖拽的距离超过可触发刷新的距离，在手指放开到正在刷新之前的这一段时间内；
- STATUS_REFRESHING 正在刷新，调用refreshComplete()结束该状态；
- STATUS_COMPLETE 调用refreshComplete()方法开始，到headerView即将完全隐藏之后，这一点时间内
这些状态正好形成一个状态循环；


## 参考
- [android-Ultra-Pull-To-Refresh](https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh) -liaohuqiu
- [SwipeRefreshLayout](https://developer.android.com/reference/android/support/v4/widget/SwipeRefreshLayout.html) -googel
