package com.leelay.refresh.vertical;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.leelay.freshlayout.verticalre.VRefreshLayout;
import com.leelay.refresh.vertical.config.Config;

/**
 * Created by Lilei on 2016.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    protected VRefreshLayout mRefreshLayout;
    private Toolbar mToolBar;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setToolBar();
        mRefreshLayout = (VRefreshLayout) findViewById(R.id.refresh_layout);
        if (mRefreshLayout != null) {
            mRefreshLayout.setBackgroundColor(Color.DKGRAY);
            mRefreshLayout.setAutoRefreshDuration(400);
            mRefreshLayout.setRatioOfHeaderHeightToReach(1.5f);
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
        }
    }

    private void setToolBar() {
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(!(this instanceof ListViewActivity));
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        supportActionBar.setTitle(getClass().getSimpleName().replace("Activity", ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_auto:
                if (mRefreshLayout != null)
                    mRefreshLayout.autoRefresh();
                break;
            case R.id.menu_setting:
                SettingDialog.showDialog(this, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshConfig();
                    }
                });
        }
        return false;
    }


    private void refreshConfig() {
        if (mRefreshLayout != null) {
            mRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Config config = Config.getInstance();
                    mRefreshLayout.setDragRate(config.dragRate);
                    mRefreshLayout.setRatioOfHeaderHeightToReach(config.ratioOfHeaderHeightToReach);
                    mRefreshLayout.setAutoRefreshDuration(config.autoRefreshDuration);
                    mRefreshLayout.setToRetainDuration(config.toRetainDuration);
                    mRefreshLayout.setToStartDuration(config.toStartDuration);
                    mRefreshLayout.setCompleteStickDuration(config.completeStickDuration);
                }
            }, 200);
        }
    }

    protected int dp2px(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }
}
