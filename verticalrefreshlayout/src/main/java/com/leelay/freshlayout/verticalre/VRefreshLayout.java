package com.leelay.freshlayout.verticalre;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 支持垂直下拉刷新的Layout
 * Created by leelay on 2016/12/3.
 */

public class VRefreshLayout extends ViewGroup {

    private static final String TAG = "VRefreshLayout";
    private static final int DEFUTL_HEADER_HEIGHT = 56;
    private View mHeaderView;

    private View mContentView;

    private int mHeaderOrginTop;

    private float mMaxDragDistance = -1;

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

    public VRefreshLayout(Context context) {
        this(context, null);
    }

    public VRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefultHeaderView();
        setChildrenDrawingOrderEnabled(true);
        mMaxDragDistance = dp2px(98);
    }

    private void setDefultHeaderView() {
        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setBackgroundColor(Color.BLACK);
        TextView textView = new TextView(getContext());
        textView.setText("Header");
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(textView, layoutParams);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, dp2px(DEFUTL_HEADER_HEIGHT)));
        setHeaderView(relativeLayout);
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
                mHeaderOrginTop = mHeaderCurrentTop = -mHeaderView.getMeasuredHeight();
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

            default:

                break;
        }

        return true;

    }


    private void actionUp(float dy) {
        Log.e(TAG, "actionUp: " + dy);

        float mMinDistance = mMaxDragDistance;
        if (dy < mMinDistance) {
            //cancel
            animOffsetToStartPos();
            mIsRefreshing = false;
        } else {
            animOffsetToRetainPos();
            mIsRefreshing = true;
            //return
        }
    }

    private void animOffsetToRetainPos() {
        final int from = mHeaderCurrentTop = mHeaderView.getTop();
        int to = 0;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int dy = value - mHeaderCurrentTop;
                ViewCompat.offsetTopAndBottom(mHeaderView, dy);
                ViewCompat.offsetTopAndBottom(mContentView, dy);
                mContentView.setBottom(mContentView.getBottom() - dy);
                mHeaderCurrentTop = mHeaderView.getTop();

            }
        });
        valueAnimator.setDuration(200);
        valueAnimator.start();
    }


    private void animOffsetToStartPos() {
        final int from = mHeaderCurrentTop = mHeaderView.getTop();
        int to = mHeaderOrginTop;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int dy = value - mHeaderCurrentTop;
                ViewCompat.offsetTopAndBottom(mHeaderView, dy);
                ViewCompat.offsetTopAndBottom(mContentView, dy);
                mContentView.setBottom(mContentView.getBottom() - dy);
                mHeaderCurrentTop = mHeaderView.getTop();

            }
        });
        valueAnimator.setDuration(200);
        valueAnimator.start();
    }

    private void actionMoving(float dy) {
        if (dy <= mMaxDragDistance) {
            int targetY = (int) (mHeaderOrginTop + dy);
            int ddy = targetY - mHeaderCurrentTop;
            Log.e(TAG, "actionMoving: dy" + dy);
            Log.e(TAG, "actionMoving: targetY" + targetY);
            Log.e(TAG, "actionMoving: ddy" + ddy);
            ViewCompat.offsetTopAndBottom(mHeaderView, ddy);
            ViewCompat.offsetTopAndBottom(mContentView, ddy);
            mContentView.setBottom(mContentView.getBottom() - ddy);
            mHeaderCurrentTop = mHeaderView.getTop();
            Log.e(TAG, "actionMoving: mHeaderCurrentTop" + mHeaderCurrentTop);
        }
        int bottom = mContentView.getBottom();
        System.out.println(bottom);


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
        this.addView(mHeaderView);

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
}
