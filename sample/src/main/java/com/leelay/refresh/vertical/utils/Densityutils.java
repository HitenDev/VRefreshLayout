package com.leelay.refresh.vertical.utils;

import android.content.Context;

/**
 * Created by Lilei on 2016.
 */

public class Densityutils {

    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }
}
