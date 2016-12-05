package com.leelay.freshlayout.verticalre;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 支持垂直下拉刷新的Layout
 * Created by leelay on 2016/12/3.
 */

public class VRefreshLayout extends ViewGroup {

    private static final String TAG = "VRefreshLayout";

    public final static int STATUS_INIT = 0;//原始状态
    public final static int STATUS_DRAGGING = 1;//正在下拉
    public final static int STATUS_RELEASE_PREPARE = 2;//松手将要刷新
    public final static int STATUS_REFRESHING = 3;//正在刷新
    public final static int STATUS_RELEASE_CANCEL = 4;//松手取消
    public final static int STATUS_COMPLETE = 5;//刷新完成

    private int mStatus;

    private View mHeaderView;

    private View mContentView;

    private int mHeaderOrginTop;

    private int mMaxDragDistance = -1;

    private float DRAG_RATE = .5f;

    private int mHeaderCurrentTop;
    private int mHeaderLayoutIndex = -1;
    private boolean mIsInitMesure = true;
    private boolean mIsBeingDragged;
    private float mInitDownY;
    private float mInitMotionY;
    private boolean mIsRefreshing;

    private int mActivePointerId = -1;
    private float mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private Progress mProgress = new Progress();
    private int mRefreshDistance;
    private int mToStartDuration = 200;
    private int mToRetainDuration = 200;
    private int mAutoRefreshDuration = 800;
    private int mCompleteStickDuration = 400;
    private float ratioOfHeaderHeightToRefresh = 1.0f;
    private float ratioOfHeaderHeightToReach = 1.6f;

    public VRefreshLayout(Context context) {
        this(context, null);
    }

    public VRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefultHeaderView();
        setChildrenDrawingOrderEnabled(true);
    }

    public void setRatioOfHeaderHeightToRefresh(float ratio) {
        ratio = Math.max(ratio, 1.0f);
        this.ratioOfHeaderHeightToRefresh = ratio;
        this.ratioOfHeaderHeightToReach = Math.max(ratioOfHeaderHeightToRefresh, ratioOfHeaderHeightToReach);
    }

    public void setRatioOfHeaderHeightToReach(float ratio) {
        ratio = Math.max(Math.max(ratio, 1.0f), ratioOfHeaderHeightToRefresh);
        this.ratioOfHeaderHeightToReach = ratio;
    }
    public void setToStartDuration(int toStartDuration) {
        mToStartDuration = toStartDuration;
    }

    public void setToRetainDuration(int toRetainDuration) {
        mToRetainDuration = toRetainDuration;
    }

    public void setAutoRefreshDuration(int autoRefreshDuration) {
        mAutoRefreshDuration = autoRefreshDuration;
    }

    public void setCompleteStickDuration(int completeStickDuration) {
        mCompleteStickDuration = completeStickDuration;
    }


    private void setMaxDragDistance(int distance) {
        mMaxDragDistance = distance;
        mProgress.totalY = distance;
    }

    private void setRefreshDistance(int distance) {
        mRefreshDistance = distance;
        mProgress.refreshY = mRefreshDistance;
    }

    private void setDefultHeaderView() {
        DefultHeaderView defultHeaderView = new DefultHeaderView(getContext());
        defultHeaderView.setPadding(0, dp2px(10), 0, dp2px(10));
        defultHeaderView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, dp2px(64)));
        setHeaderView(defultHeaderView);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure: ");
        ensureContent();
        //measure contentView
        if (mContentView != null) {
            int contentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int contentHeight = getMeasuredHeight() - getPaddingTop() + getPaddingBottom();
            mContentView.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY));
        }

        //measure headerView
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            if (mIsInitMesure) {
                int measuredHeight = mHeaderView.getMeasuredHeight();
                mHeaderOrginTop = mHeaderCurrentTop = -measuredHeight;
                setMaxDragDistance((int) (measuredHeight * ratioOfHeaderHeightToReach));
                setRefreshDistance((int) (measuredHeight * ratioOfHeaderHeightToRefresh));
                mIsInitMesure = false;
            }
        }
        mHeaderLayoutIndex = -1;
        for (int i = 0; i < getChildCount(); i++) {
            if (mHeaderView == getChildAt(i)) {
                mHeaderLayoutIndex = i;
            }
        }
    }

    private void ensureContent() {
        if (mContentView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                if (childAt != mHeaderView) {
                    mContentView = childAt;
                    break;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (mContentView == null) {
            ensureContent();
        }

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        Log.e(TAG, "onLayout: mHeaderCurrentTop" + mHeaderCurrentTop);
        //layout headerView
        if (mHeaderView != null) {
            mHeaderView.layout(paddingLeft, mHeaderCurrentTop, paddingLeft + mHeaderView.getMeasuredWidth(), mHeaderCurrentTop + mHeaderView.getMeasuredHeight());
        }

        //layout contentView
        if (mContentView != null) {
            int distance = mHeaderView.getTop() - mHeaderOrginTop;
            int contentHeight = mContentView.getMeasuredHeight();
            int contentWidth = mContentView.getMeasuredWidth();
            int left = paddingLeft;
            int top = paddingTop;
            int right = left + contentWidth;
            int bottom = paddingTop + contentHeight;
            mContentView.layout(left, top + distance, right, bottom);
        }
        Log.e(TAG, "onLayout: ");
    }


    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mHeaderLayoutIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            return mHeaderLayoutIndex;
        } else if (i >= mHeaderLayoutIndex) {
            return i + 1;
        } else {
            return i;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureContent();
        int action = MotionEventCompat.getActionMasked(ev);
        if (!isEnabled() || canChildScrollUp() || mIsRefreshing) {
            return false;
        }
        int pointerIndex;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onInterceptTouchEvent: ACTION_DOWN");
                mIsBeingDragged = false;
                notifyStatus(STATUS_INIT);
                mActivePointerId = ev.getPointerId(0);
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onInterceptTouchEvent: ACTION_MOVE");
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                float evY = ev.getY(pointerIndex);
                checkDragging(evY);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                Log.e(TAG, "onInterceptTouchEvent: ACTION_POINTER_UP");
                checkOtherPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "onInterceptTouchEvent: ACTION_DOWN");
                mIsBeingDragged = false;
                mActivePointerId = -1;
                break;
        }


        return mIsBeingDragged;
    }

    private void checkDragging(float y) {
        float dy = y - mInitDownY;
        mInitMotionY = mInitDownY + mTouchSlop;
        if (dy > mTouchSlop && !mIsBeingDragged) {
            mIsBeingDragged = true;
        }
    }

    private void checkOtherPointerUp(MotionEvent ev) {
        int pointIndex = MotionEventCompat.getActionIndex(ev);
        int pointerId = ev.getPointerId(pointIndex);
        if (pointerId == mActivePointerId) {
            int newPointIndex = pointIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointIndex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || canChildScrollUp() || mIsRefreshing) {
            return false;
        }
        int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent: ACTION_DOWN");
                mIsBeingDragged = false;
                notifyStatus(STATUS_INIT);
                mActivePointerId = ev.getPointerId(0);
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onTouchEvent: ACTION_MOVE");
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                float evY = ev.getY(pointerIndex);
                checkDragging(evY);
                if (mIsBeingDragged) {
                    float dy = (evY - mInitMotionY) * DRAG_RATE;
                    if (dy > 0) {
                        actionMoving(dy);
                    }
                    notifyStatus(STATUS_DRAGGING);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.e(TAG, "onTouchEvent: ACTION_POINTER_DOWN");
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                Log.e(TAG, "onTouchEvent: ACTION_POINTER_UP");
                checkOtherPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent: ACTION_UP");
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                float upY = ev.getY(pointerIndex);
                if (mIsBeingDragged) {
                    float upDy = (upY - mInitMotionY) * DRAG_RATE;
                    actionUp(upDy);
                    mIsBeingDragged = false;
                }
                mActivePointerId = -1;

                break;
            case MotionEvent.ACTION_CANCEL:
                Log.e(TAG, "onTouchEvent: ACTION_CANCEL");
                return false;
        }
        return true;

    }


    private void actionUp(float dy) {
        Log.e(TAG, "actionUp: " + dy);
        if (dy < mRefreshDistance) {
            //cancel
            animOffsetToStartPos();
            mIsRefreshing = false;
            notifyStatus(STATUS_RELEASE_CANCEL);
        } else {
            animOffsetToRetainPos();
            mIsRefreshing = true;
            notifyStatus(STATUS_RELEASE_PREPARE);
        }
    }

    private void moveAnimation(int star, int end, int duration, Animator.AnimatorListener animatorListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(star, end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                moveTo(value);
            }
        });
        if (animatorListener != null) {
            valueAnimator.addListener(animatorListener);
        }
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    private void animOffsetToRetainPos() {
        final int from = mHeaderCurrentTop = mHeaderView.getTop();
        int to = getPaddingTop() + mRefreshDistance - mHeaderView.getMeasuredHeight();
        moveAnimation(from, to, mToRetainDuration, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                notifyRefreshListeners();
                notifyStatus(STATUS_REFRESHING);
            }
        });
    }

    private void animOffsetAutoRefresh() {
        mHeaderCurrentTop = mHeaderView.getTop();
        final int from = mHeaderCurrentTop;
        int to = mMaxDragDistance + mHeaderOrginTop;
        moveAnimation(from, to, mAutoRefreshDuration, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                notifyStatus(STATUS_RELEASE_PREPARE);
                animOffsetToRetainPos();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                notifyStatus(STATUS_DRAGGING);
            }
        });
    }

    private void animOffsetToStartPos() {
        final int from = mHeaderCurrentTop = mHeaderView.getTop();
        int to = mHeaderOrginTop;
        moveAnimation(from, to, mToStartDuration, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsRefreshing = false;
                notifyStatus(STATUS_INIT);
            }
        });
    }

    private void actionMoving(float y) {
        y = Math.min(y, mMaxDragDistance);
        if (y <= mMaxDragDistance) {
            int targetY = (int) (mHeaderOrginTop + y);
            moveTo(targetY);
        }
    }


    public void setHeaderView(View view) {
        if (view == null || view == mHeaderView) {
            return;
        }
        LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
        }
        removeView(mHeaderView);
        mHeaderView = view;
        mIsInitMesure = true;
        this.addView(mHeaderView);
        if (view instanceof UpdateHandler) {
            setUpdateHandler((UpdateHandler) view);
        }
    }

    public void setHeaderView(@LayoutRes int redId) {
        View view = LayoutInflater.from(getContext()).inflate(redId, this, false);
        setHeaderView(view);
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mContentView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mContentView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mContentView, -1) || mContentView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mContentView, -1);
        }
    }

    public void refreshComplete() {
        if (mIsRefreshing) {
            notifyStatus(STATUS_COMPLETE);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    animOffsetToStartPos();
                }
            }, mCompleteStickDuration);
        }
    }

    public void autoRefresh() {
        if (!mIsRefreshing) {
            mIsRefreshing = true;
            animOffsetAutoRefresh();
        }
    }

    private void moveTo(int y) {
        int dy = y - mHeaderCurrentTop;
        ViewCompat.offsetTopAndBottom(mHeaderView, dy);
        ViewCompat.offsetTopAndBottom(mContentView, dy);
        mHeaderCurrentTop = mHeaderView.getTop();
        mProgress.currentY = mHeaderCurrentTop - mHeaderOrginTop;
        notifyProgress();
    }

    private void notifyProgress() {
        if (mUpdateHandler != null) {
            mUpdateHandler.onProgressUpdate(this, mProgress, mStatus);
        }
    }

    private void notifyStatus(int status) {
        mStatus = status;
        if (mUpdateHandler != null) {
            mUpdateHandler.onProgressUpdate(this, mProgress, mStatus);
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private List<OnRefreshListener> mOnRefreshListeners;

    public void addOnRefreshListener(OnRefreshListener onRefreshListener) {
        if (mOnRefreshListeners == null) {
            mOnRefreshListeners = new ArrayList<>();
        }
        mOnRefreshListeners.add(onRefreshListener);
    }

    private void notifyRefreshListeners() {
        if (mOnRefreshListeners == null || mOnRefreshListeners.isEmpty()) {
            return;
        }
        for (OnRefreshListener onRefreshListener : mOnRefreshListeners) {
            onRefreshListener.onRefresh();
        }
    }

    private UpdateHandler mUpdateHandler;

    public void setUpdateHandler(UpdateHandler updateHandler) {
        mUpdateHandler = updateHandler;
    }


    public interface UpdateHandler {
        void onProgressUpdate(VRefreshLayout layout, Progress progress, int status);
    }

    public static class Progress {
        private int totalY;
        private int currentY;
        private int refreshY;

        public int getRefreshY() {
            return refreshY;
        }

        public int getTotalY() {
            return totalY;
        }

        public int getCurrentY() {
            return currentY;
        }
    }


}
