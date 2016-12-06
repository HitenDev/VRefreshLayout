package com.leelay.refresh.vertical;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by Lilei on 2016.
 */

public class ScrollViewActivity extends BaseActivity {
    LinearLayout mLinearLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollview);
        mLinearLayout = (LinearLayout) findViewById(R.id.layout_content);
        addChilden();
    }

    private void addChilden() {
        String[] array = getResources().getStringArray(R.array.language_array);
        for (String a : array) {
            AppCompatTextView textView = new AppCompatTextView(this);
            textView.setBackgroundColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setText(a);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(56));
            layoutParams.bottomMargin = dp2px(1);
            mLinearLayout.addView(textView, layoutParams);
        }
    }
}
