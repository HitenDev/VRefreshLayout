package com.leelay.refresh.vertical.header;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leelay.freshlayout.verticalre.VRefreshLayout;
import com.leelay.refresh.vertical.R;
import com.leelay.refresh.vertical.utils.Densityutils;

/**
 * Created by Lilei on 2016.
 */

public class JDHeaderView extends RelativeLayout implements VRefreshLayout.UpdateHandler {

    private ImageView mPeopleIv, mPackageIv;
    private TextView mStatusTv;
    private AnimationDrawable mAnimationDrawable;

    public JDHeaderView(Context context) {
        this(context, null);
    }

    public JDHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.header_jd, this, true);
        mPeopleIv = (ImageView) findViewById(R.id.iv_people);
        mPackageIv = (ImageView) findViewById(R.id.iv_package);
        mStatusTv = (TextView) findViewById(R.id.tv_status);
    }

    @Override
    public void onProgressUpdate(VRefreshLayout layout, VRefreshLayout.Progress progress, int status) {
        switch (status) {
            case VRefreshLayout.STATUS_INIT:
                mPeopleIv.setScaleX(0.1f);
                mPeopleIv.setScaleY(0.1f);
                mPeopleIv.setTranslationX(0);
                mPackageIv.setScaleX(0.1f);
                mPackageIv.setScaleY(0.1f);
                mPeopleIv.setImageResource(R.drawable.jd_people01);
                mStatusTv.setText(R.string.pull_to_refresh);
                if (mAnimationDrawable != null) {
                    mAnimationDrawable.stop();
                    mAnimationDrawable = null;
                }

                break;
            case VRefreshLayout.STATUS_DRAGGING:
                float currentY = progress.getCurrentY();
                float refreshY = progress.getRefreshY();
                float percent = Math.min(1.0f, currentY / refreshY);
                mPackageIv.setScaleX(percent);
                mPackageIv.setScaleY(percent);
                mPeopleIv.setScaleX(percent);
                mPeopleIv.setScaleY(percent);
                mPeopleIv.setTranslationX(percent * Densityutils.dp2px(getContext(), 18f));
                mPackageIv.setVisibility(VISIBLE);
                if (percent >= 1.0f) {
                    mStatusTv.setText(R.string.release_to_refresh);
                } else {
                    mStatusTv.setText(R.string.pull_to_refresh);
                }
                break;
            case VRefreshLayout.STATUS_RELEASE_PREPARE:
            case VRefreshLayout.STATUS_REFRESHING:
                if (mAnimationDrawable == null) {
                    mStatusTv.setText(R.string.refreshing);
                    mPackageIv.setVisibility(INVISIBLE);
                    mPeopleIv.setImageResource(R.drawable.anim_jd_people);
                    mAnimationDrawable = (AnimationDrawable) mPeopleIv.getDrawable();
                    mAnimationDrawable.start();
                }
        }
    }
}
