package com.leelay.refresh.vertical;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by leelay on 2016/12/6.
 */

public class SeekBarLayout extends LinearLayout {

    private AppCompatSeekBar mSeekBar;
    private TextView mTitle;
    private TextView mValue;

    private int gap;
    private boolean isF;

    public SeekBarLayout(Context context) {
        this(context, null);
    }

    public SeekBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_seekbar, this, true);
        initView();
    }

    public void setTitle(String title) {
        this.mTitle.setText(title);
    }

    public void setRange(int... range) {
        isF = false;
        doRange(range);
        notifyValue(mSeekBar.getProgress());
    }

    public void setProgress(int progress) {
        mSeekBar.setProgress(progress - gap);
    }

    public void setProgress(float progress) {
        if (isF)
            mSeekBar.setProgress((int) (progress * 10) - gap);
        else
            mSeekBar.setProgress((int) progress - gap);
    }

    public void setRange(float... range) {
        int[] rangeI = new int[range.length];
        for (int i = 0; i < range.length; i++) {
            rangeI[i] = (int) (range[i] * 10);
        }
        setRange(rangeI);
        isF = true;
        notifyValue(mSeekBar.getProgress());
    }

    private void doRange(int... range) {
        if (range.length < 2) {
            throw new RuntimeException("range length must > 2");
        }
        int[] numbers = range.clone();
        int start = numbers[0];
        int end = numbers[1];

        if (end <= start) {
            throw new RuntimeException("range end must > start");
        }
        gap = start;
        mSeekBar.setMax(end - start);
    }

    private void initView() {
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.seekbar);
        mTitle = (TextView) findViewById(R.id.title);
        mValue = (TextView) findViewById(R.id.value);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                notifyValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void notifyValue(int progress) {
        if (isF) {
            float p = (float) (progress + gap) / 10f;
            mValue.setText(String.valueOf(p));
        } else {
            mValue.setText(String.valueOf(progress + gap));
        }
    }

    public String getProcess() {
        return mValue.getText().toString();
    }


}
